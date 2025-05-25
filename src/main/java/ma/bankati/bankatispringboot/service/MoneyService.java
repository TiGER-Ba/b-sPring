// ===== ma/bankati/bankatispringboot/service/MoneyService.java =====
package ma.bankati.bankatispringboot.service;

import ma.bankati.bankatispringboot.dto.MoneyDataDto;
import ma.bankati.bankatispringboot.entity.enums.Devise;

import java.math.BigDecimal;

/**
 * Interface du service de gestion des devises et du solde
 */
public interface MoneyService {

    /**
     * Récupère le solde actuel du compte
     */
    BigDecimal getCurrentBalance();

    /**
     * Met à jour le solde du compte
     */
    void updateBalance(BigDecimal amount);

    /**
     * Convertit le solde dans une devise donnée
     */
    MoneyDataDto convertBalance(Devise devise);

    /**
     * Convertit un montant dans une devise donnée
     */
    MoneyDataDto convertAmount(BigDecimal amount, Devise devise);

    /**
     * Récupère le taux de change d'une devise
     */
    Double getExchangeRate(Devise devise);

    /**
     * Formate un montant avec sa devise
     */
    String formatAmount(BigDecimal amount, Devise devise);

    /**
     * Récupère le solde converti dans la devise par défaut (DH)
     */
    MoneyDataDto getDefaultBalance();
}