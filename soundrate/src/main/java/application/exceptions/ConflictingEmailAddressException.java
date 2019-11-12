package application.exceptions;

public class ConflictingEmailAddressException extends RuntimeException {

    public ConflictingEmailAddressException() {
        super();
    }

    public ConflictingEmailAddressException(String message) {
        super(message);
    }

    public ConflictingEmailAddressException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ConflictingEmailAddressException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConflictingEmailAddressException(Throwable cause) {
        super(cause);
    }

}
