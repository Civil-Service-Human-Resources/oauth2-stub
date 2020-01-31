package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.cshr.dto.DomainDTO;
import uk.gov.cshr.service.security.IdentityService;

@Slf4j
@Controller
@RequestMapping("/domain")
public class DomainController {

    @Autowired
    private IdentityService identityService;

    @GetMapping("/isWhitelisted")
    public ResponseEntity<DomainDTO> isDomainWhitelisted(@RequestParam String domain) {
        DomainDTO dto = new DomainDTO();
        boolean isWhiteListed = false;

        try {
            isWhiteListed = identityService.isWhitelistedDomain(domain);
        } catch (Exception e) {
            dto.setIsWhiteListed("false");
        }
        if(isWhiteListed) {
            dto.setIsWhiteListed("true");
        } else {
            dto.setIsWhiteListed("false");
        }

        return ResponseEntity.ok(dto);
    }

}
