package application.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ConflictingEmailAddressException extends Exception {

    private static final long serialVersionUID = 1L;

    public ConflictingEmailAddressException() {
        super();
    }

    public ConflictingEmailAddressException(String message) {
        super(message);
    }

}
