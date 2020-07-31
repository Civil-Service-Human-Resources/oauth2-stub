package uk.gov.cshr.exception;

public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String message, Throwable e) {
        super(message, e);
    }
}
