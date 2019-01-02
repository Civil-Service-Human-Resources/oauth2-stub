package uk.gov.cshr.controller.signup;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.controller.InviteController;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.service.notify.NotificationClientException;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignupController.class);

    private final InviteService inviteService;

    private final IdentityService identityService;

    private final InviteRepository inviteRepository;

    private final SignupFormValidator signupFormValidator;

    private final String lpgUiUrl;

    public SignupController(InviteService inviteService,
                            IdentityService identityService,
                            InviteRepository inviteRepository,
                            SignupFormValidator signupFormValidator,
                            @Value("${lpg.uiUrl}") String lpgUiUrl) {

        this.inviteService = inviteService;
        this.identityService = identityService;
        this.inviteRepository = inviteRepository;
        this.signupFormValidator = signupFormValidator;
        this.lpgUiUrl = lpgUiUrl;
    }

    @GetMapping(path = "/request")
    public String requestInvite(Model model) {
        model.addAttribute("requestInviteForm", new RequestInviteForm());
        return "requestInvite";
    }

    @PostMapping(path = "/request")
    public String sendInvite(Model model, @ModelAttribute @Valid RequestInviteForm form, BindingResult bindingResult) throws NotificationClientException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestInviteForm", form);
            return "requestInvite";
        }

        inviteService.sendSelfSignupInvite(form.getEmail());

        return "inviteSent";
    }


    @GetMapping("/{code}")
    public String signup(Model model, @PathVariable(value = "code") String code) {
        LOGGER.info("User accessing sign up screen with code {}", code);

        if (inviteRepository.existsByCode(code)) {
            if (!inviteService.isCodeExpired(code)) {
                model.addAttribute("invite", inviteRepository.findByCode(code));
                model.addAttribute("signupForm", new SignupForm());
                return "signup";
            }
        }
        return "login";
    }

    @PostMapping("/{code}")
    @Transactional
    public String signup(@PathVariable(value = "code") String code, @ModelAttribute @Valid SignupForm form,
                         BindingResult bindingResult, Model model) {
        LOGGER.info("User attempting sign up with code {}", code);

        if (bindingResult.hasErrors()) {
            model.addAttribute("invite", inviteRepository.findByCode(code));
            return "signup";
        }

        identityService.createIdentityFromInviteCode(code, form.getPassword());
        inviteService.updateInviteByCode(code, InviteStatus.ACCEPTED);

        model.addAttribute("lpgUiUrl", lpgUiUrl);

        return "signupSuccess";
    }

    @InitBinder
    public void setupValidation(WebDataBinder binder) {
        if (binder.getTarget() instanceof SignupForm) {
            binder.addValidators(signupFormValidator);
        }
    }
}
