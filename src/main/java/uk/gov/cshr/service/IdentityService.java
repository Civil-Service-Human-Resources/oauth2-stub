package uk.gov.cshr.service;

import uk.gov.cshr.domain.Identity;



import java.util.Optional;

public interface IdentityService {
    Identity createNewIdentity(String email, String password, boolean status);
    Identity findActiveIdentity(String email);
    Boolean isValidCredentials(Identity identity, String password);
    Optional<Identity> getIdentity(String uid);
    Iterable<Identity> findAll();
    Identity updateIdentity(Identity identity);
}
