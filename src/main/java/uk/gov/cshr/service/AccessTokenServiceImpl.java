package uk.gov.cshr.service;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.AccessToken;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.TokenStatus;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.AccessTokenRepository;

import java.time.LocalDateTime;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    @Value("${accessToken.expiryInMinutes}")
    private Long expiryInMinutes;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Override
    public AccessToken generateAccessToken(Identity identity, Client client) {
        LocalDateTime now = LocalDateTime.now();
        AccessToken accessToken = new AccessToken(
                RandomStringUtils.random(42, true, true),
                now,
                now.plusMinutes(expiryInMinutes),
                identity,
                client
        );
        return accessTokenRepository.save(accessToken);
    }

    @Override
    public AccessToken findActiveAccessToken(String token) {
        AccessToken accessToken = accessTokenRepository.findFirstByStatusEqualsAndTokenEquals(TokenStatus.active, token);
        if (accessToken == null) {
            throw new ResourceNotFoundException();
        }
        return accessToken;
    }
}
