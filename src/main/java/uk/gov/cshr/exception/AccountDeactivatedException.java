package uk.gov.cshr.exception;

public class AccountDeactivatedException extends RuntimeException {

    public AccountDeactivatedException(String message) {
        super(message);
    }
}
