package application.model.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException
public class ConflictingBacklogEntryException extends RuntimeException {

    public ConflictingBacklogEntryException() {
        super();
    }

    public ConflictingBacklogEntryException(String message) {
        super(message);
    }

    public ConflictingBacklogEntryException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ConflictingBacklogEntryException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConflictingBacklogEntryException(Throwable cause) {
        super(cause);
    }

}

