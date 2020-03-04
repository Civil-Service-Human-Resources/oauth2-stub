package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.service.security.IdentityService;

@Slf4j
@RestController
public class DomainController {

    @Autowired
    private IdentityService identityService;

    @GetMapping(value = "/domain/isWhitelisted/{domain}/")
    public ResponseEntity<String> isDomainWhitelisted(@PathVariable String domain) {
        try {
            if(identityService.isWhitelistedDomain(domain)) {
                return new ResponseEntity<String>("true", HttpStatus.OK);
            } else {
                return new ResponseEntity<String>("false", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<String>("false", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
