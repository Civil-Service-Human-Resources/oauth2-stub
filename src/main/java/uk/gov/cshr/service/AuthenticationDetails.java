package uk.gov.cshr.service;

import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;

public interface AuthenticationDetails {
    IdentityDetails getCurrentIdentityDetails();

    Identity getCurrentIdentity();

    String getCurrentUsername();
}
