package ma.bankati.bankatispringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.bankati.bankatispringboot.entity.enums.Devise;

import java.math.BigDecimal;

/**
 * DTO pour les données monétaires
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoneyDataDto {

    private BigDecimal amount;
    private Devise devise;
    private String formattedAmount;

    @Override
    public String toString() {
        return formattedAmount != null ? formattedAmount :
                String.format("%.2f %s", amount, devise.getSymbol());
    }
}