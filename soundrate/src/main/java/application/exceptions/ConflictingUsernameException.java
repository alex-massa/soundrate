package application.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ConflictingUsernameException extends Exception {

    private static final long serialVersionUID = 1L;

    public ConflictingUsernameException() {
        super();
    }

    public ConflictingUsernameException(String message) {
        super(message);
    }

}
