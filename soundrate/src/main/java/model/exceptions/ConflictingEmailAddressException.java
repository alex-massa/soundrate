package model.exceptions;

public class ConflictingEmailAddressException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictingEmailAddressException() {
        super();
    }

    public ConflictingEmailAddressException(String message) {
        super(message);
    }

}
