package uk.gov.cshr.service;

import uk.gov.cshr.domain.AccessToken;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.domain.User;

public interface AccessTokenService {
    AccessToken generateAccessToken(User user, Client client);
    AccessToken findActiveAccessToken(String token);
}
