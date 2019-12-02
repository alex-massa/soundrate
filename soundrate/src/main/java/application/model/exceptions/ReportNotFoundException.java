package application.model.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException() {
        super();
    }

    public ReportNotFoundException(String message) {
        super(message);
    }

    public ReportNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ReportNotFoundException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ReportNotFoundException(Throwable cause) {
        super(cause);
    }

}