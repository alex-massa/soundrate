package application.exceptions;

public class ConflictingReviewException extends RuntimeException {

    public ConflictingReviewException() {
        super();
    }

    public ConflictingReviewException(String message) {
        super(message);
    }

    public ConflictingReviewException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ConflictingReviewException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConflictingReviewException(Throwable cause) {
        super(cause);
    }

}
