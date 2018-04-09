package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.IdentityDTO;
import uk.gov.cshr.service.AccessTokenService;

@RestController
public class IdentityController {

    @Autowired
    private AccessTokenService accessTokenService;

    @RequestMapping(value = "/identity", method = RequestMethod.POST)
    public IdentityDTO getIdentityDetailsfromAccessToken(@RequestParam("access_token") String accessToken) {
        Identity identity = accessTokenService.findActiveAccessToken(accessToken).getIdentity();
        return new IdentityDTO(identity.getEmail(), identity.getUid(), identity.getRoles());
    }
}
