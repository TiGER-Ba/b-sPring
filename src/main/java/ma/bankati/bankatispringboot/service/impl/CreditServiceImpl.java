package ma.bankati.bankatispringboot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.entity.CreditRequest;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.CreditStatus;
import ma.bankati.bankatispringboot.repository.CreditRequestRepository;
import ma.bankati.bankatispringboot.service.CreditService;
import ma.bankati.bankatispringboot.service.MoneyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service de gestion des crédits
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditServiceImpl implements CreditService {

    private final CreditRequestRepository creditRequestRepository;
    private final MoneyService moneyService;

    @Value("${bankati.credit.min-amount:1000}")
    private BigDecimal minAmount;

    @Value("${bankati.credit.min-duration:6}")
    private Integer minDuration;

    @Value("${bankati.credit.max-duration:120}")
    private Integer maxDuration;

    @Override
    @Transactional(readOnly = true)
    public List<CreditRequest> findAll() {
        log.debug("Récupération de toutes les demandes de crédit");
        return creditRequestRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditRequest> findById(Long id) {
        log.debug("Recherche demande de crédit par ID: {}", id);
        return creditRequestRepository.findById(id);
    }

    @Override
    public CreditRequest save(CreditRequest creditRequest) {
        log.debug("Sauvegarde demande de crédit: {}", creditRequest.getId());

        validateCreditRequest(creditRequest);

        CreditRequest saved = creditRequestRepository.save(creditRequest);
        log.info("Demande de crédit sauvegardée: ID = {}, Montant = {}",
                saved.getId(), saved.getAmount());

        return saved;
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Suppression demande de crédit ID: {}", id);

        if (!canDeleteCreditRequest(id)) {
            throw new IllegalStateException("Cette demande ne peut pas être supprimée");
        }

        creditRequestRepository.deleteById(id);
        log.info("Demande de crédit supprimée: ID = {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditRequest> findByClient(User client) {
        log.debug("Recherche demandes pour le client: {}", client.getUsername());
        return creditRequestRepository.findByClientOrderByRequestDateDesc(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditRequest> findByClientId(Long clientId) {
        log.debug("Recherche demandes pour le client ID: {}", clientId);
        return creditRequestRepository.findByClient_IdOrderByRequestDateDesc(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditRequest> findByStatus(CreditStatus status) {
        log.debug("Recherche demandes par statut: {}", status);
        return creditRequestRepository.findByStatusOrderByRequestDateDesc(status);
    }

    @Override
    public CreditRequest createCreditRequest(CreditRequest creditRequest) {
        log.debug("Création nouvelle demande de crédit pour le client: {}",
                creditRequest.getClient().getUsername());

        // Définir les valeurs par défaut
        creditRequest.setStatus(CreditStatus.PENDING);
        creditRequest.setRequestDate(LocalDate.now());

        validateCreditRequest(creditRequest);

        CreditRequest created = creditRequestRepository.save(creditRequest);
        log.info("Nouvelle demande de crédit créée: ID = {}, Client = {}, Montant = {}",
                created.getId(), created.getClient().getUsername(), created.getAmount());

        return created;
    }

    @Override
    public void approveCreditRequest(Long creditId, String adminComment) {
        log.debug("Approbation demande de crédit ID: {}", creditId);

        CreditRequest creditRequest = creditRequestRepository.findById(creditId)
                .orElseThrow(() -> new IllegalArgumentException("Demande non trouvée"));

        if (!canProcessCreditRequest(creditId)) {
            throw new IllegalStateException("Cette demande ne peut pas être traitée");
        }

        creditRequest.setStatus(CreditStatus.APPROVED);
        creditRequest.setProcessedDate(LocalDate.now());
        creditRequest.setAdminComment(adminComment);

        creditRequestRepository.save(creditRequest);

        // Mettre à jour le solde du compte
        moneyService.updateBalance(creditRequest.getAmount());

        log.info("Demande de crédit approuvée: ID = {}, Montant = {}",
                creditId, creditRequest.getAmount());
    }

    @Override
    public void rejectCreditRequest(Long creditId, String adminComment) {
        log.debug("Rejet demande de crédit ID: {}", creditId);

        CreditRequest creditRequest = creditRequestRepository.findById(creditId)
                .orElseThrow(() -> new IllegalArgumentException("Demande non trouvée"));

        if (!canProcessCreditRequest(creditId)) {
            throw new IllegalStateException("Cette demande ne peut pas être traitée");
        }

        creditRequest.setStatus(CreditStatus.REJECTED);
        creditRequest.setProcessedDate(LocalDate.now());
        creditRequest.setAdminComment(adminComment);

        creditRequestRepository.save(creditRequest);

        log.info("Demande de crédit rejetée: ID = {}", creditId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(CreditStatus status) {
        return creditRequestRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalApprovedAmount() {
        return creditRequestRepository.getTotalApprovedAmount();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalApprovedAmountByClient(Long clientId) {
        return creditRequestRepository.getTotalApprovedAmountByClient(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditRequest> findByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Recherche demandes entre {} et {}", startDate, endDate);
        return creditRequestRepository.findByRequestDateBetweenOrderByRequestDateDesc(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditRequest> searchByDescription(String searchTerm) {
        log.debug("Recherche demandes par description: {}", searchTerm);
        return creditRequestRepository.searchByDescription(searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditRequest> findOldPendingRequests(int daysOld) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysOld);
        return creditRequestRepository.findOldPendingRequests(cutoffDate);
    }

    @Override
    public void validateCreditRequest(CreditRequest creditRequest) {
        if (creditRequest == null) {
            throw new IllegalArgumentException("La demande de crédit ne peut pas être null");
        }

        if (creditRequest.getClient() == null) {
            throw new IllegalArgumentException("Le client est obligatoire");
        }

        if (creditRequest.getAmount() == null || creditRequest.getAmount().compareTo(minAmount) < 0) {
            throw new IllegalArgumentException("Le montant minimum est de " + minAmount + " DH");
        }

        if (creditRequest.getDuration() == null ||
                creditRequest.getDuration() < minDuration ||
                creditRequest.getDuration() > maxDuration) {
            throw new IllegalArgumentException("La durée doit être entre " + minDuration + " et " + maxDuration + " mois");
        }

        if (creditRequest.getDescription() == null || creditRequest.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }

        if (creditRequest.getDescription().length() > 1000) {
            throw new IllegalArgumentException("La description ne peut pas dépasser 1000 caractères");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCreditRequest(Long creditId) {
        return creditRequestRepository.findById(creditId)
                .map(request -> request.getStatus() == CreditStatus.PENDING)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canProcessCreditRequest(Long creditId) {
        return creditRequestRepository.findById(creditId)
                .map(request -> request.getStatus() == CreditStatus.PENDING)
                .orElse(false);
    }
}