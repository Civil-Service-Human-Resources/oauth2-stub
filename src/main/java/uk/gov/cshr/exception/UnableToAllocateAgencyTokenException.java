package uk.gov.cshr.exception;

public class UnableToAllocateAgencyTokenException extends RuntimeException {
    public UnableToAllocateAgencyTokenException(String message) {
        super(message);
    }

    public UnableToAllocateAgencyTokenException(String message, Throwable e) {
        super(message, e);
    }
}
