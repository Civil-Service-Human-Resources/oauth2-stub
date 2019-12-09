package uk.gov.cshr.exception;

public class UnableToUpdateOrganisationException extends RuntimeException {
    public UnableToUpdateOrganisationException(String message) {
        super(message);
    }
}
