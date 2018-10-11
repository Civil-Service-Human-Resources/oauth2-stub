package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.AuthenticationDetails;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/management/invite")
public class InviteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteController.class);

    @Autowired
    private AuthenticationDetails authenticationDetails;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private RoleRepository roleRepository;


    @GetMapping
    public String invite(Model model) {
        LOGGER.info("{} on Invite screen", authenticationDetails.getCurrentUsername());

        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("invites", inviteRepository.findAll());

        return "inviteList";
    }

    @PostMapping
    public String invited(@RequestParam(value = "forEmail") String forEmail, @RequestParam(value = "roleId", required = false) ArrayList<String> roleId, RedirectAttributes redirectAttributes) throws NotificationClientException {
        LOGGER.info("{} inviting {} ", authenticationDetails.getCurrentUsername(), forEmail);

        if (inviteRepository.existsByForEmailAndStatus(forEmail, InviteStatus.PENDING)) {
            LOGGER.info("{} has already been invited", forEmail);
            redirectAttributes.addFlashAttribute("status", forEmail + " has already been invited");
            return "redirect:/management/invite";
        }

        if (identityService.existsByEmail(forEmail)) {
            LOGGER.info("{} is already a user", forEmail);
            redirectAttributes.addFlashAttribute("status", "User already exists with email address " + forEmail);
            return "redirect:/management/invite";
        }

        Set<Role> roleSet = new HashSet<>();

        if (roleId != null) {
            for (String id : roleId) {
                Optional<Role> optionalRole = roleRepository.findById(Long.parseLong(id));

                if (optionalRole.isPresent()) {
                    roleSet.add(optionalRole.get());
                } else {
                    LOGGER.info("{} found no role for id {}", authenticationDetails.getCurrentUsername(), id);
                    return "redirect:/management/invite";
                }
            }
        }

        inviteService.createNewInviteForEmailAndRoles(forEmail, roleSet, authenticationDetails.getCurrentIdentity());

        LOGGER.info("{} invited {}", authenticationDetails.getCurrentUsername(), forEmail);

        redirectAttributes.addFlashAttribute("status", "Invite sent to " + forEmail);
        return "redirect:/management/invite";
    }

}