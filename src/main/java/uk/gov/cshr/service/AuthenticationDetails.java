package uk.gov.cshr.service;

import uk.gov.cshr.service.security.IdentityDetails;

public interface AuthenticationDetails {
    IdentityDetails getCurrentIdentity();

    String getCurrentUsername();
}
