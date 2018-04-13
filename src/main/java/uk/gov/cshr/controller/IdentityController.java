package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.IdentityDTO;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.IdentityService;
import uk.gov.cshr.service.security.IdentityDetails;


@Controller
@RequestMapping("/management/")
public class IdentityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private IdentityService identityService;

    @Autowired
    private IdentityRepository identityRepository;

    @RequestMapping(value = "/identity", method = RequestMethod.GET)
    public IdentityDTO getIdentityDetailsfromAccessToken(Authentication authentication) {
        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        return new IdentityDTO(identityDetails.getIdentity());
    }

    @GetMapping("/identities")
    public String identities(Model model) {
        LOGGER.debug("Listing all roles");

        Iterable<Identity> identities = identityService.findAll();

        model.addAttribute("identities", identities);
        model.addAttribute("identity", new Identity());

        return "identityList";
    }
}
