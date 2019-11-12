package application.exceptions;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException() {
        super();
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }

    public ReviewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ReviewNotFoundException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ReviewNotFoundException(Throwable cause) {
        super(cause);
    }

}
