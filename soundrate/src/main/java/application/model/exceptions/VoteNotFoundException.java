package application.model.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException
public class VoteNotFoundException extends RuntimeException {

    public VoteNotFoundException() {
        super();
    }

    public VoteNotFoundException(String message) {
        super(message);
    }

    public VoteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    protected VoteNotFoundException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public VoteNotFoundException(Throwable cause) {
        super(cause);
    }

}
