package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.dto.IdentityDTO;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.TokenServices;

@RestController
public class AuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    private TokenServices tokenServices;

    @Autowired
    public AuthenticationController(TokenServices tokenServices) {
        this.tokenServices = tokenServices;
    }

    @GetMapping("/oauth/revoke")
    public ResponseEntity<Void> revokeAccessToken(Authentication authentication) {
        LOGGER.debug("Revoking access token");

        String accessToken = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        LOGGER.trace("Access token value: {}", accessToken);

        if (tokenServices.revokeToken(accessToken)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/oauth/resolve")
    public IdentityDTO resolveIdentity(Authentication authentication) {
        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        return new IdentityDTO(identityDetails.getIdentity());
    }
}