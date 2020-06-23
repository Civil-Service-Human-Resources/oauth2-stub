package uk.gov.cshr.exception;

public class VerificationCodeTypeNotFound extends RuntimeException {
    public VerificationCodeTypeNotFound(String message) {
        super(message);
    }
}
