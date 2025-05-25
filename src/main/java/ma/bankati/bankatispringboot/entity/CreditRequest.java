package ma.bankati.bankatispringboot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.bankati.bankatispringboot.entity.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Entité CreditRequest pour Spring Data JPA
 */
@Entity
@Table(name = "credit_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation avec l'utilisateur client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "1000.0", message = "Le montant minimum est de 1000 DH")
    private BigDecimal amount;

    @Column(name = "duration", nullable = false)
    @Min(value = 6, message = "La durée minimum est de 6 mois")
    @Max(value = 120, message = "La durée maximum est de 120 mois")
    private Integer duration;

    @Column(name = "description", nullable = false, length = 1000)
    @NotBlank(message = "La description est obligatoire")
    private String description;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CreditStatus status = CreditStatus.PENDING;

    @Column(name = "processed_date")
    private LocalDate processedDate;

    @Column(name = "admin_comment", length = 500)
    private String adminComment;

    // Audit fields
    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDate.now();
        }
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (status == null) {
            status = CreditStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();

        // Si le statut change de PENDING vers APPROVED/REJECTED
        if ((status == CreditStatus.APPROVED || status == CreditStatus.REJECTED)
                && processedDate == null) {
            processedDate = LocalDate.now();
        }
    }

    // Méthodes utilitaires
    public String getFormattedRequestDate() {
        if (requestDate == null) return "N/A";
        return requestDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedProcessedDate() {
        if (processedDate == null) return "N/A";
        return processedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedAmount() {
        return String.format("%.2f DH", amount);
    }

    public BigDecimal getMonthlyPayment() {
        if (amount == null || duration == null || duration == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(BigDecimal.valueOf(duration), 2, BigDecimal.ROUND_HALF_UP);
    }

    public String getStatusDisplayName() {
        return switch (status) {
            case PENDING -> "En attente";
            case APPROVED -> "Approuvée";
            case REJECTED -> "Refusée";
        };
    }

    public String getStatusCssClass() {
        return switch (status) {
            case PENDING -> "warning";
            case APPROVED -> "success";
            case REJECTED -> "danger";
        };
    }

    public boolean isPending() {
        return status == CreditStatus.PENDING;
    }

    public boolean isApproved() {
        return status == CreditStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == CreditStatus.REJECTED;
    }

    public boolean canBeDeleted() {
        return status == CreditStatus.PENDING;
    }

    public boolean canBeProcessed() {
        return status == CreditStatus.PENDING;
    }

    @Override
    public String toString() {
        return "Demande #" + id + " - " + getFormattedAmount() + " sur " + duration + " mois (" + getStatusDisplayName() + ")";
    }
}