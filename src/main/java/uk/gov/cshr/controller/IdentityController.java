package uk.gov.cshr.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.dto.IdentityDTO;
import uk.gov.cshr.service.security.IdentityDetails;

@RestController
public class IdentityController {

    @RequestMapping(value = "/identity", method = RequestMethod.GET)
    public IdentityDTO getIdentityDetailsfromAccessToken(Authentication authentication) {
        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        return new IdentityDTO(identityDetails.getIdentity());
    }
}
