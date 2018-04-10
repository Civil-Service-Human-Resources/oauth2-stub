package uk.gov.cshr.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.dto.IdentityDTO;

@RestController
public class IdentityController {


    @RequestMapping(value = "/identity", method = RequestMethod.POST)
    public IdentityDTO getIdentityDetailsfromAccessToken(@RequestParam("access_token") String accessToken) {
        return null;
    }
}
