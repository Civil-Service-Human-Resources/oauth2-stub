package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.Application;
import uk.gov.cshr.config.ClientDetails;
import uk.gov.cshr.domain.AccessToken;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.AccessTokenDTO;
import uk.gov.cshr.service.AccessTokenService;
import uk.gov.cshr.service.IdentityService;

@RestController
@RequestMapping("/oauth2")
public class PasswordGrantController {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public AccessTokenDTO handlePasswordGrant(
            Authentication authentication,
            @RequestParam("grant_type") String grantType,
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        if (grantType.isEmpty() || !grantType.equals("password")) {
            log.info("Invalid grant option supplied");
            throw new BadCredentialsException("Invalid grant type");
        }

        Identity foundIdentity = identityService.findActiveIdentity(username);

        if (foundIdentity == null) {
            log.info("Couldn't find identity ${username}");
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!identityService.isValidCredentials(foundIdentity, password)) {
            log.info("Invalid credentials");
            throw new BadCredentialsException("Invalid credentials");
        }

        Client client = ((ClientDetails) authentication.getPrincipal()).getClient();
        log.info("Got valid client");

        AccessToken accessToken = accessTokenService.generateAccessToken(foundIdentity, client);

        log.info("Issues a valid token", accessToken);
        return new AccessTokenDTO(
                accessToken.getToken(),
                accessToken.getExpiresInMinutes()
        );
    }
}
