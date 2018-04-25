package uk.gov.cshr.controller;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.domain.Status;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.AuthenticationDetails;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.service.notify.NotificationClientException;

import java.util.*;

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

        model.addAttribute("invite", new Invite());
        model.addAttribute("roles", roleRepository.findAll());

        return "inviteList";
    }

    @PostMapping
    public String invited(@ModelAttribute("identity") Invite invite, @RequestParam(value = "roleId", required = false) ArrayList<String> roleId, RedirectAttributes redirectAttributes) throws NotificationClientException {
        LOGGER.info("{} inviting {} ", authenticationDetails.getCurrentUsername(), invite.getForEmail());

        if (inviteRepository.existsByForEmailAndStatus(invite.getForEmail(), Status.PENDING)) {
            LOGGER.info("{} has already been invited", invite.getForEmail());
            redirectAttributes.addFlashAttribute("status", invite.getForEmail() + " has already been invited");
            return "redirect:/management/invite";
        }

        if (identityService.inviteExistsByEmail(invite.getForEmail())) {
            LOGGER.info("{} is already a user", invite.getForEmail());
            redirectAttributes.addFlashAttribute("status", "User already exists with email address " + invite.getForEmail());
            return "redirect:/management/invite";
        }

        Set<Role> roleSet = new HashSet();

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

        invite.setForRoles(roleSet);
        invite.setInviter(authenticationDetails.getCurrentIdentity());
        invite.setInvitedAt(new Date());
        invite.setStatus(Status.PENDING);

        invite.setCode(RandomStringUtils.random(40, true, true));

        inviteService.sendEmail(invite);
        inviteService.saveInvite(invite);

        LOGGER.info("{} invited {}", authenticationDetails.getCurrentUsername(), invite.getForEmail());

        redirectAttributes.addFlashAttribute("status", "Invite sent to " + invite.getForEmail());
        return "redirect:/management/invite";
    }
}