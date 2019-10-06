package model.exceptions;

public class ConflictingUsernameException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictingUsernameException() {
        super();
    }

    public ConflictingUsernameException(String message) {
        super(message);
    }

}
