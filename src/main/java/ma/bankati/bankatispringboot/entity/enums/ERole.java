package ma.bankati.bankatispringboot.entity.enums;

/**
 * Énumération des rôles utilisateur
 */
public enum ERole {
    ADMIN("Administrateur"),
    USER("Client");

    private final String displayName;

    ERole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}