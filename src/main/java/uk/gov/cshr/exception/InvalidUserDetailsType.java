package uk.gov.cshr.exception;

public class InvalidUserDetailsType extends RuntimeException {
    public InvalidUserDetailsType(String msg) {
        super(msg);
    }
}
