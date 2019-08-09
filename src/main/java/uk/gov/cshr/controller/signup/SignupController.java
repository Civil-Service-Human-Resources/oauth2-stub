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
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.service.notify.NotificationClientException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignupController.class);

    private final InviteService inviteService;

    private final IdentityService identityService;

    private final CsrsService csrsService;

    private final InviteRepository inviteRepository;

    private final SignupFormValidator signupFormValidator;

    private final String lpgUiUrl;

    @Value("${invite.whitelist.domains}")
    private String[] whitelistedDomains;

    public SignupController(InviteService inviteService,
                            IdentityService identityService,
                            CsrsService csrsService,
                            InviteRepository inviteRepository,
                            SignupFormValidator signupFormValidator,
                            @Value("${lpg.uiUrl}") String lpgUiUrl) {

        this.inviteService = inviteService;
        this.identityService = identityService;
        this.csrsService = csrsService;
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
    public String sendInvite(Model model,
                             @ModelAttribute @Valid RequestInviteForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) throws NotificationClientException {

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
                inviteService.sendSelfSignupInvite(form.getEmail()); // link in email must be for /signup/enterToken/{code} page
                return "inviteSent";
            } else {
                redirectAttributes.addFlashAttribute("status", "Your organisation is unable to use this service. Please contact your line manager.");
                return "redirect:/signup/request";
            }
        }
    }

    @GetMapping(path = "/enterToken/{code}")
    public String enterToken(Model model,
                             @PathVariable(value = "code") String code) {

        LOGGER.info("User accessing token-based sign up screen");

        if (!inviteRepository.existsByCode(code) || inviteService.isCodeExpired(code)) {
            return "login";
        }

        OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

        model.addAttribute("organisations", organisations);
        model.addAttribute("enterTokenForm", new EnterTokenForm());

        return "enterToken";
    }

    @PostMapping(path = "/enterToken/{code}")
    public String submitToken(Model model,
                              @PathVariable(value = "code") String code,
                              @ModelAttribute @Valid EnterTokenForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) throws NotificationClientException {

        LOGGER.info("User attempting token-based sign up");

        if (bindingResult.hasErrors()) {
            model.addAttribute("enterTokenForm", form);
            return "enterToken";
        }

        if (!inviteRepository.existsByCode(code) || inviteService.isCodeExpired(code)) {
            return "login";
        }

        final String emailAddress = inviteRepository.findByCode(code).getForEmail();

        final boolean organisationAndTokenAndDomainMatch = true; // replace with call to CSRS endpoint
        if (!organisationAndTokenAndDomainMatch) {
            redirectAttributes.addFlashAttribute("status", "Incorrect token for this organisation");
            return "redirect:/signup/enterToken/" + code;
        }

        LOGGER.info("User submitted Enter Token form with org = {}, token = {}, email = {}", form.getOrganisation(), form.getToken(), emailAddress);

        model.addAttribute("invite", inviteRepository.findByCode(code));
        redirectAttributes.addFlashAttribute("organisation", form.getOrganisation());
        redirectAttributes.addFlashAttribute("token", form.getToken());
        return "redirect:/signup/" + code;
    }

    @GetMapping("/{code}")
    public String signup(Model model,
                         @PathVariable(value = "code") String code,
                         @ModelAttribute("organisation") String organisation,
                         @ModelAttribute("token") String token) {

        LOGGER.info("User accessing sign up screen with code {}", code);
        LOGGER.info("User accessing sign up screen with org = {}, token = {}", organisation, token);

        if (!inviteRepository.existsByCode(code) || inviteService.isCodeExpired(code)) {
            return "login";
        }

        model.addAttribute("invite", inviteRepository.findByCode(code));
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/{code}")
    @Transactional
    public String signup(@PathVariable(value = "code") String code,
                         @ModelAttribute @Valid SignupForm form,
                         BindingResult bindingResult,
                         Model model) {

        LOGGER.info("User attempting sign up with code {}", code);
        LOGGER.info("User attempting sign up with org = {}, token = {}", form.getOrganisation(), form.getToken());

        if (bindingResult.hasErrors()) {
            model.addAttribute("invite", inviteRepository.findByCode(code));
            model.addAttribute("organisation", form.getOrganisation());
            model.addAttribute("token", form.getToken());
            model.addAttribute("signupForm", new SignupForm());
            return "signup";
        }

        if (!inviteRepository.existsByCode(code) || inviteService.isCodeExpired(code)) {
            return "login";
        }

        final String emailAddress = inviteRepository.findByCode(code).getForEmail();
        final boolean domainIsAssociatedWithAnAgencyToken = true; // replace with call to CSRS endpoint

        if (domainIsAssociatedWithAnAgencyToken) {
            final boolean organisationAndTokenAndDomainMatch = true; // replace with call to CSRS endpoint
            if (!organisationAndTokenAndDomainMatch) {
                return "redirect:/signup/enterToken/" + code;
            }
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
