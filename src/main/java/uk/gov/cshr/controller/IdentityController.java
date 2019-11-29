package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.dto.IdentityDTO;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.AuthenticationDetails;
import uk.gov.cshr.service.security.IdentityDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/management/")
public class IdentityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityController.class);

    private static final String STATUS_ATTRIBUTE = "status";

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationDetails authenticationDetails;

    @RequestMapping(value = "/identity", method = RequestMethod.GET)
    public IdentityDTO getIdentityDetailsfromAccessToken(Authentication authentication) {
        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        return new IdentityDTO(identityDetails.getIdentity());
    }

    @GetMapping("/identities")
    public String identities(Model model) {
        LOGGER.info("Listing all identities");

        Iterable<Identity> identities = identityRepository.findAll();
        model.addAttribute("identities", identities);
        return "identityList";
    }

    @GetMapping("/identities/update/{uid}")
    public String identityUpdate(Model model,
                                 @PathVariable("uid") String uid) {

        LOGGER.info("{} editing identity for uid {}", authenticationDetails.getCurrentUsername(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        Iterable<Role> roles = roleRepository.findAll();

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            model.addAttribute("identity", identity);
            model.addAttribute("roles", roles);
            return "updateIdentity";
        }

        LOGGER.info("No identity found for uid {}", authenticationDetails.getCurrentUsername(), uid);
        return "redirect:/management/identities";
    }

    @PostMapping("/identities/update")
    public String identityUpdate(@RequestParam(value = "emailRecentlyUpdated", required = false) Boolean emailRecentlyUpdated, @RequestParam(value = "locked", required = false) Boolean locked, @RequestParam(value = "active", required = false) Boolean active, @RequestParam(value = "roleId", required = false) ArrayList<String> roleId, @RequestParam("uid") String uid) {

        // get identity to edit
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();

            Set<Role> roleSet = new HashSet<>();
            // create roles from id
            if (roleId != null) {
                for (String id : roleId) {
                    Optional<Role> optionalRole = roleRepository.findById(Long.parseLong(id));
                    if (optionalRole.isPresent()) {
                        // got role
                        roleSet.add(optionalRole.get());
                    } else {
                        LOGGER.info("{} found no role for id {}", authenticationDetails.getCurrentUsername(), id);
                        // do something here , probably go to error page
                        return "redirect:/management/identities";
                    }
                }
            }
            // afer this give roleset to identity
            identity.setRoles(roleSet);
            // and update  the active property
            if (active != null) {
                identity.setActive(active);
            } else {
                identity.setActive(false);
            }

            if(locked != null) {
                identity.setLocked(locked);
            } else {
                identity.setLocked(false);
            }

            if(emailRecentlyUpdated != null) {
                identity.setEmailRecentlyUpdated(emailRecentlyUpdated);
            } else {
                identity.setEmailRecentlyUpdated(false);
            }

            identityRepository.save(identity);

            LOGGER.info("{} updated new role {}", authenticationDetails.getCurrentUsername(), identity);
        } else {
            LOGGER.info("{} found no identity for uid {}", authenticationDetails.getCurrentUsername(), uid);
            // do something here , probably go to error page
        }

        return "redirect:/management/identities";
    }

    @PostMapping("/identities/update/{uid}/emailSuccessfullyUpdated")
    public ResponseEntity identityUpdate(@PathVariable("uid") String uid) {

        try {

            LOGGER.info("{} resetting email recently updated field to be false for identity with uid {}", authenticationDetails.getCurrentUsername(), uid);

            Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

            if (optionalIdentity.isPresent()) {
                Identity existingIdentity = optionalIdentity.get();
                existingIdentity.setEmailRecentlyUpdated(false);
                identityRepository.save(existingIdentity);
                return ResponseEntity.noContent().build();
            } else {
                LOGGER.info("No identity found for uid {}", uid);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/identities/{domain}/notValid")
    public String notAValidEmailDomain(@PathVariable("domain") String domain, RedirectAttributes redirectAttributes) {
        LOGGER.info("{} is an invalid domain", domain);
        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Your organisation is unable to use this service. Please contact your line manager.");
        return "redirect:/signup/request";
    }

}
