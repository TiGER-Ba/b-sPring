package ma.bankati.bankatispringboot.entity.enums;

/**
 * Énumération des statuts de demande de crédit
 */
public enum CreditStatus {
    PENDING("En attente", "warning"),
    APPROVED("Approuvée", "success"),
    REJECTED("Refusée", "danger");

    private final String displayName;
    private final String cssClass;

    CreditStatus(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }
}
