package application.exceptions;

public class BacklogEntryNotFoundException extends RuntimeException {

    public BacklogEntryNotFoundException() {
        super();
    }

    public BacklogEntryNotFoundException(String message) {
        super(message);
    }

    public BacklogEntryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    protected BacklogEntryNotFoundException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BacklogEntryNotFoundException(Throwable cause) {
        super(cause);
    }

}
