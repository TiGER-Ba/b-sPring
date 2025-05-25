package ma.bankati.bankatispringboot.exception;

/**
 * Exception personnalisée pour l'application Bankati
 */
public class BankatiException extends RuntimeException {

    public BankatiException(String message) {
        super(message);
    }

    public BankatiException(String message, Throwable cause) {
        super(message, cause);
    }

    public BankatiException(Throwable cause) {
        super(cause);
    }
}