package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.cshr.service.security.IdentityService;

@Slf4j
@Controller
@RequestMapping("/domain")
public class DomainController {

    @Autowired
    private IdentityService identityService;

    @GetMapping("/isWhitelisted")
    public ResponseEntity<Boolean> isDomainWhitelisted(@RequestParam String domain) {
        boolean isWhiteListed = false;

        try {
            isWhiteListed = identityService.isWhitelistedDomain(domain);
        } catch (Exception e) {
            return ResponseEntity.ok(isWhiteListed);
        }
        log.info("is domain " + domain + " whitelisted=" + isWhiteListed);
        return ResponseEntity.ok(isWhiteListed);
    }

}
