package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Status;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;

@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignupController.class);

    @Autowired
    private InviteService inviteService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private InviteRepository inviteRepository;

    @GetMapping("/{code}")
    public String signup(Model model, @PathVariable(value = "code") String code) {
        LOGGER.info("User accessing sign up screen with code {}", code);

        if (inviteRepository.existsByCode(code)) {
            if (!inviteService.isCodeExpired(code)) {
                model.addAttribute("invite", inviteRepository.findByCode(code));
                return "signup";
            }
        }
        return "login";
    }

    @PostMapping("/{code}")
    public String signup(@PathVariable(value = "code") String code, @RequestParam("password") String password) {
        LOGGER.info("User attempting sign up with code {}", code);

        identityService.createIdentityFromInviteCode(code, password);

        inviteService.updateInviteByCode(code, Status.ACCEPTED);

        return "signupSuccess";
    }
}
