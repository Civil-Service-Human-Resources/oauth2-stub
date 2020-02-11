package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.service.security.IdentityService;

@Slf4j
@RestController
public class DomainController {

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value = "/domain/isWhitelisted/{domain}")
    public ResponseEntity<String> isDomainWhitelisted(@PathVariable String domain) {
        String dto = "false";
        boolean isWhiteListed = false;

        try {
            isWhiteListed = identityService.isWhitelistedDomain(domain);
        } catch (Exception e) {
            return new ResponseEntity<String>(dto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(isWhiteListed) {
            return new ResponseEntity<String>("true", HttpStatus.OK);
        }

        return new ResponseEntity<String>(dto, HttpStatus.OK);
    }

}
