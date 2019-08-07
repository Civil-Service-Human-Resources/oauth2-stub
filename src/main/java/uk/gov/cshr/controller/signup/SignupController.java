package uk.gov.cshr.controller.signup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.service.notify.NotificationClientException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Arrays;

@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignupController.class);

    private final InviteService inviteService;

    private final IdentityService identityService;

    private final InviteRepository inviteRepository;

    private final SignupFormValidator signupFormValidator;

    private final String lpgUiUrl;

    @Value("${invite.whitelist.domains}")
    private String[] whitelistedDomains;

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
    public String sendInvite(Model model, @ModelAttribute @Valid RequestInviteForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws NotificationClientException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestInviteForm", form);
            return "requestInvite";
        }

        if (inviteRepository.existsByForEmailAndStatus(form.getEmail(), InviteStatus.PENDING)) {
            LOGGER.info("{} has already been invited", form.getEmail());
            redirectAttributes.addFlashAttribute("status", form.getEmail() + " has already been invited");
            return "redirect:/signup/request";
        }

        if (identityService.existsByEmail(form.getEmail())) {
            LOGGER.info("{} is already a user", form.getEmail());
            redirectAttributes.addFlashAttribute("status", "User already exists with email address " + form.getEmail());
            return "redirect:/signup/request";
        }

        final String domain = form.getEmail().substring(form.getEmail().indexOf('@') + 1);
        final boolean domainIsWhitelisted = Arrays.asList(whitelistedDomains).contains(domain);

        if (domainIsWhitelisted) {
            inviteService.sendSelfSignupInvite(form.getEmail());
            return "inviteSent";
        } else {
            final boolean domainIsAssociatedWithAnAgencyToken = true; // replace with call to CSRS endpoint

            if (domainIsAssociatedWithAnAgencyToken) {
                redirectAttributes.addFlashAttribute("emailAddress", form.getEmail());
                return "redirect:/signup/enterToken";
            } else {
                redirectAttributes.addFlashAttribute("status", "Your organisation is unable to use this service. Please contact your line manager.");
                return "redirect:/signup/request";
            }
        }
    }

    @GetMapping(path = "/enterToken")
    public String enterToken(Model model, @ModelAttribute("emailAddress") String emailAddress) {
        LOGGER.info("User accessing token-based sign up screen");

        System.out.println("* * * Flash attributes = " + emailAddress);

        final boolean domainIsAssociatedWithAnAgencyToken = true; // replace with call to CSRS endpoint
        if (emailAddress.equals("") || !domainIsAssociatedWithAnAgencyToken) {
            return "redirect:/signup/request";
        }

        String[] organisations = { "Cabinet Office", "Department of Health & Social Care" }; // replace with CSRS call
        model.addAttribute("organisations", organisations);
        model.addAttribute("enterTokenForm", new EnterTokenForm());

        return "enterToken";
    }

    @PostMapping(path = "/enterToken")
    public String submitToken(Model model, @ModelAttribute @Valid EnterTokenForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        LOGGER.info("User attempting token-based sign up");

        if (bindingResult.hasErrors()) {
            model.addAttribute("enterTokenForm", form);
            return "enterToken";
        }

        final boolean organisationAndTokenMatch = true;   // replace with call to CSRS endpoint
        final boolean emailDomainIsAssociatedWithToken = true; // replace with call to CSRS endpoint

        if (!(organisationAndTokenMatch && emailDomainIsAssociatedWithToken)) {
            redirectAttributes.addFlashAttribute("status", "Incorrect token for this organisation");
            return "redirect:/signup/enterToken";
        }

        //inviteService.sendSelfSignupInvite(form.getEmail);
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
