package uk.gov.cshr.exception;

public class FieldMatchException extends RuntimeException {
    public FieldMatchException(Throwable e) {
        super(e);
    }
}