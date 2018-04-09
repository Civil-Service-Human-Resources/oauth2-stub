package uk.gov.cshr.service;

import uk.gov.cshr.domain.Identity;

public interface IdentityService {
    Identity createNewIdentity(String email, String password, boolean status);
    Identity findActiveIdentity(String email);
    Boolean isValidCredentials(Identity identity, String password);
}
