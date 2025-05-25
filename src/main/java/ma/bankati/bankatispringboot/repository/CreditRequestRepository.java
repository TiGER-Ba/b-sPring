package ma.bankati.bankatispringboot.repository;

import ma.bankati.bankatispringboot.entity.CreditRequest;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.CreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour l'entité CreditRequest
 */
@Repository
public interface CreditRequestRepository extends JpaRepository<CreditRequest, Long> {

    /**
     * Trouve toutes les demandes d'un client
     */
    List<CreditRequest> findByClientOrderByRequestDateDesc(User client);

    /**
     * Trouve toutes les demandes d'un client par ID
     */
    List<CreditRequest> findByClient_IdOrderByRequestDateDesc(Long clientId);

    /**
     * Trouve toutes les demandes par statut
     */
    List<CreditRequest> findByStatusOrderByRequestDateDesc(CreditStatus status);

    /**
     * Trouve toutes les demandes en attente
     */
    List<CreditRequest> findByStatusOrderByRequestDateAsc(CreditStatus status);

    /**
     * Compte les demandes par statut
     */
    long countByStatus(CreditStatus status);

    /**
     * Compte les demandes d'un client par statut
     */
    long countByClientAndStatus(User client, CreditStatus status);

    /**
     * Trouve les demandes créées entre deux dates
     */
    List<CreditRequest> findByRequestDateBetweenOrderByRequestDateDesc(
            LocalDate startDate, LocalDate endDate
    );

    /**
     * Trouve les demandes avec un montant supérieur à une valeur
     */
    List<CreditRequest> findByAmountGreaterThanEqualOrderByAmountDesc(BigDecimal amount);

    /**
     * Trouve les demandes d'un client avec un statut spécifique
     */
    List<CreditRequest> findByClient_IdAndStatusOrderByRequestDateDesc(
            Long clientId, CreditStatus status
    );

    /**
     * Statistiques : Montant total des crédits approuvés
     */
    @Query("SELECT COALESCE(SUM(cr.amount), 0) FROM CreditRequest cr WHERE cr.status = 'APPROVED'")
    BigDecimal getTotalApprovedAmount();

    /**
     * Statistiques : Montant total des crédits d'un client
     */
    @Query("SELECT COALESCE(SUM(cr.amount), 0) FROM CreditRequest cr WHERE cr.client.id = :clientId AND cr.status = 'APPROVED'")
    BigDecimal getTotalApprovedAmountByClient(@Param("clientId") Long clientId);

    /**
     * Trouve les dernières demandes (limite)
     */
    @Query("SELECT cr FROM CreditRequest cr ORDER BY cr.requestDate DESC")
    List<CreditRequest> findLatestRequests(org.springframework.data.domain.Pageable pageable);

    /**
     * Recherche dans les descriptions de demandes
     */
    @Query("SELECT cr FROM CreditRequest cr WHERE LOWER(cr.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<CreditRequest> searchByDescription(@Param("search") String search);

    /**
     * Trouve les demandes nécessitant une attention (anciennes demandes en attente)
     */
    @Query("SELECT cr FROM CreditRequest cr WHERE cr.status = 'PENDING' AND cr.requestDate < :date")
    List<CreditRequest> findOldPendingRequests(@Param("date") LocalDate date);
}