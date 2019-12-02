package application.model.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException
public class ConflictingUsernameException extends RuntimeException {

    public ConflictingUsernameException() {
        super();
    }

    public ConflictingUsernameException(String message) {
        super(message);
    }

    public ConflictingUsernameException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ConflictingUsernameException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConflictingUsernameException(Throwable cause) {
        super(cause);
    }

}
