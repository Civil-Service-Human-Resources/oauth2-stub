package uk.gov.cshr.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Status;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.AuthenticationDetails;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;

@Controller
@RequestMapping("/signup")
public class SignupController {

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
    private IdentityRepository identityRepository;

    @GetMapping("/{code}")
    public String signup(Model model, @PathVariable(value = "code") String code) {
        LOGGER.info("{} on signup screen with code {}", authenticationDetails.getCurrentUsername(), code);

        if (inviteRepository.existsByCode(code)) {
            if (!inviteService.isCodeExpired(code)) {
                inviteService.updateInviteByCode(code, Status.ACCEPTED);
                model.addAttribute("invite", inviteRepository.findByCode(code));
                model.addAttribute("identity", new Identity());
            }
        }

        return "signup";
    }

    @PostMapping
    public String signup(Model model, @ModelAttribute("identity") Identity identity, @RequestParam("email") String email, @RequestParam("password") String password) {
        LOGGER.info("Signed up");
        identityService.createIdentityFromInvitedUser(inviteRepository.findByForEmail(email));
        LOGGER.info("Successful signup");

        return "signupSuccess";
    }
}
