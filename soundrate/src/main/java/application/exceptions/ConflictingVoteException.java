package application.exceptions;

public class ConflictingVoteException extends RuntimeException {

    public ConflictingVoteException() {
        super();
    }

    public ConflictingVoteException(String message) {
        super(message);
    }

    public ConflictingVoteException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ConflictingVoteException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConflictingVoteException(Throwable cause) {
        super(cause);
    }

}
