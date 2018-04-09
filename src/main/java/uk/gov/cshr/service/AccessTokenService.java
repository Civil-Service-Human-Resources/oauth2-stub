package uk.gov.cshr.service;

import uk.gov.cshr.domain.AccessToken;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.domain.Identity;

public interface AccessTokenService {
    AccessToken generateAccessToken(Identity identity, Client client);
    AccessToken findActiveAccessToken(String token);
}
