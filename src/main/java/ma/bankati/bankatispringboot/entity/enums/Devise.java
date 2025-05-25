package ma.bankati.bankatispringboot.entity.enums;

/**
 * Énumération des devises supportées
 */
public enum Devise {
    DH("Dirham", "DH", 1.0),
    USD("Dollar américain", "$", 1.08),
    EUR("Euro", "€", 1.0),
    GBP("Livre sterling", "£", 0.84);

    private final String name;
    private final String symbol;
    private final Double exchangeRate;

    Devise(String name, String symbol, Double exchangeRate) {
        this.name = name;
        this.symbol = symbol;
        this.exchangeRate = exchangeRate;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getExchangeRate() {
        return exchangeRate;
    }

    @Override
    public String toString() {
        return symbol;
    }
}