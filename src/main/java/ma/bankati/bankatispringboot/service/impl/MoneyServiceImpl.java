package ma.bankati.bankatispringboot.service.impl;

import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.dto.MoneyDataDto;
import ma.bankati.bankatispringboot.entity.enums.Devise;
import ma.bankati.bankatispringboot.service.MoneyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Implémentation du service de gestion des devises
 */
@Service
@Slf4j
public class MoneyServiceImpl implements MoneyService {

    private static final String BALANCE_FILE = "compte.txt";
    private static final BigDecimal DEFAULT_BALANCE = BigDecimal.valueOf(500.0);

    @Value("${bankati.currency.rates.usd:1.08}")
    private Double usdRate;

    @Value("${bankati.currency.rates.eur:1.0}")
    private Double eurRate;

    @Value("${bankati.currency.rates.gbp:0.84}")
    private Double gbpRate;

    private Path balanceFilePath;

    public MoneyServiceImpl() {
        initializeBalanceFile();
    }

    private void initializeBalanceFile() {
        try {
            // Créer le répertoire data s'il n'existe pas
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            balanceFilePath = dataDir.resolve(BALANCE_FILE);

            // Créer le fichier avec un solde par défaut s'il n'existe pas
            if (!Files.exists(balanceFilePath)) {
                List<String> initialContent = List.of(
                        "Solde du compte",
                        DEFAULT_BALANCE.toString()
                );
                Files.write(balanceFilePath, initialContent, StandardOpenOption.CREATE);
                log.info("Fichier de solde créé avec le solde par défaut: {}", DEFAULT_BALANCE);
            }

            log.debug("Chemin du fichier de solde: {}", balanceFilePath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Erreur lors de l'initialisation du fichier de solde", e);
            throw new RuntimeException("Impossible d'initialiser le fichier de solde", e);
        }
    }

    @Override
    public BigDecimal getCurrentBalance() {
        try {
            List<String> lines = Files.readAllLines(balanceFilePath);
            if (lines.size() > 1) {
                String balanceString = lines.get(1).trim();
                BigDecimal balance = new BigDecimal(balanceString);
                log.debug("Solde actuel lu: {}", balance);
                return balance;
            }

            log.warn("Fichier de solde incomplet, retour du solde par défaut");
            return DEFAULT_BALANCE;

        } catch (IOException e) {
            log.error("Erreur lors de la lecture du solde", e);
            return DEFAULT_BALANCE;
        } catch (NumberFormatException e) {
            log.error("Format de solde invalide dans le fichier", e);
            return DEFAULT_BALANCE;
        }
    }

    @Override
    public void updateBalance(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Le montant ne peut pas être null");
        }

        try {
            BigDecimal currentBalance = getCurrentBalance();
            BigDecimal newBalance = currentBalance.add(amount);

            List<String> lines = List.of(
                    "Solde du compte",
                    newBalance.toString()
            );

            Files.write(balanceFilePath, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Solde mis à jour: {} -> {} (ajout de {})",
                    currentBalance, newBalance, amount);

        } catch (IOException e) {
            log.error("Erreur lors de la mise à jour du solde", e);
            throw new RuntimeException("Impossible de mettre à jour le solde", e);
        }
    }

    @Override
    public MoneyDataDto convertBalance(Devise devise) {
        BigDecimal balance = getCurrentBalance();
        return convertAmount(balance, devise);
    }

    @Override
    public MoneyDataDto convertAmount(BigDecimal amount, Devise devise) {
        if (amount == null || devise == null) {
            throw new IllegalArgumentException("Le montant et la devise ne peuvent pas être null");
        }

        Double rate = getExchangeRate(devise);
        BigDecimal convertedAmount = amount.multiply(BigDecimal.valueOf(rate))
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("Conversion: {} DH = {} {} (taux: {})", amount, convertedAmount, devise.getSymbol(), rate);

        return MoneyDataDto.builder()
                .amount(convertedAmount)
                .devise(devise)
                .formattedAmount(formatAmount(convertedAmount, devise))
                .build();
    }

    @Override
    public Double getExchangeRate(Devise devise) {
        return switch (devise) {
            case DH -> 10.0;  // Base: 1 unité = 10 DH (comme dans votre ancien code)
            case USD -> usdRate;
            case EUR -> eurRate;
            case GBP -> gbpRate;
        };
    }

    @Override
    public String formatAmount(BigDecimal amount, Devise devise) {
        if (amount == null || devise == null) {
            return "0.00";
        }

        return String.format("%.2f %s", amount, devise.getSymbol());
    }

    @Override
    public MoneyDataDto getDefaultBalance() {
        return convertBalance(Devise.DH);
    }
}
