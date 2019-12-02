package application.model.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException
public class ConflictingReportException extends RuntimeException {

    public ConflictingReportException() {
        super();
    }

    public ConflictingReportException(String message) {
        super(message);
    }

    public ConflictingReportException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ConflictingReportException
            (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConflictingReportException(Throwable cause) {
        super(cause);
    }

}