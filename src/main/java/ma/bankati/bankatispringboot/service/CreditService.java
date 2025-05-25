package ma.bankati.bankatispringboot.service;

import ma.bankati.bankatispringboot.entity.CreditRequest;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.CreditStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface du service de gestion des crédits
 */
public interface CreditService {

    /**
     * Trouve toutes les demandes de crédit
     */
    List<CreditRequest> findAll();

    /**
     * Trouve une demande par son ID
     */
    Optional<CreditRequest> findById(Long id);

    /**
     * Sauvegarde une demande de crédit
     */
    CreditRequest save(CreditRequest creditRequest);

    /**
     * Supprime une demande par son ID
     */
    void deleteById(Long id);

    /**
     * Trouve toutes les demandes d'un client
     */
    List<CreditRequest> findByClient(User client);

    /**
     * Trouve toutes les demandes d'un client par ID
     */
    List<CreditRequest> findByClientId(Long clientId);

    /**
     * Trouve toutes les demandes par statut
     */
    List<CreditRequest> findByStatus(CreditStatus status);

    /**
     * Crée une nouvelle demande de crédit
     */
    CreditRequest createCreditRequest(CreditRequest creditRequest);

    /**
     * Approuve une demande de crédit
     */
    void approveCreditRequest(Long creditId, String adminComment);

    /**
     * Rejette une demande de crédit
     */
    void rejectCreditRequest(Long creditId, String adminComment);

    /**
     * Compte les demandes par statut
     */
    long countByStatus(CreditStatus status);

    /**
     * Statistiques : montant total des crédits approuvés
     */
    BigDecimal getTotalApprovedAmount();

    /**
     * Statistiques : montant total des crédits d'un client
     */
    BigDecimal getTotalApprovedAmountByClient(Long clientId);

    /**
     * Trouve les demandes créées entre deux dates
     */
    List<CreditRequest> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Recherche dans les descriptions
     */
    List<CreditRequest> searchByDescription(String searchTerm);

    /**
     * Trouve les demandes nécessitant une attention
     */
    List<CreditRequest> findOldPendingRequests(int daysOld);

    /**
     * Valide une demande de crédit
     */
    void validateCreditRequest(CreditRequest creditRequest);

    /**
     * Vérifie si une demande peut être supprimée
     */
    boolean canDeleteCreditRequest(Long creditId);

    /**
     * Vérifie si une demande peut être traitée
     */
    boolean canProcessCreditRequest(Long creditId);
}