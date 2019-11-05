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
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.BadRequestException;
import uk.gov.cshr.exception.NotEnoughSpaceAvailableException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.exception.UnableToAllocateAgencyTokenException;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.service.notify.NotificationClientException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignupController.class);
    private static final String STATUS_ATTRIBUTE = "status";

    private final InviteService inviteService;

    private final IdentityService identityService;

    private final CsrsService csrsService;

    private final InviteRepository inviteRepository;

    private final SignupFormValidator signupFormValidator;

    private final String lpgUiUrl;

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

        final String email = form.getEmail();

        if (inviteRepository.existsByForEmailAndStatus(email, InviteStatus.PENDING)) {
            LOGGER.info("{} has already been invited", email);
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, email + " has already been invited");
            return "redirect:/signup/request";
        }

        if (identityService.existsByEmail(email)) {
            LOGGER.info("{} is already a user", email);
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "User already exists with email address " + email);
            return "redirect:/signup/request";
        }

        final String domain = identityService.getDomainFromEmailAddress(email);

        if (identityService.isWhitelistedDomain(domain)) {
            inviteService.sendSelfSignupInvite(email, true);
            return "inviteSent";
        } else {
            AgencyToken[] agencyTokensForDomain = csrsService.getAgencyTokensForDomain(domain);

            if (agencyTokensForDomain.length > 0) {
                inviteService.sendSelfSignupInvite(email, false);
                return "inviteSent";
            } else {
                redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Your organisation is unable to use this service. Please contact your line manager.");
                return "redirect:/signup/request";
            }
        }
    }

    @GetMapping("/{code}")
    public String signup(Model model,
                         @PathVariable(value = "code") String code) {

        LOGGER.info("User accessing sign up screen with code {}", code);

        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);
            if (!invite.isAuthorisedInvite()) {
                return "redirect:/signup/enterToken/" + code;
            }

            model.addAttribute("invite", invite);
            model.addAttribute("signupForm", new SignupForm());

            return "signup";
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("/{code}")
    @Transactional
    public String signup(@PathVariable(value = "code") String code,
                         @ModelAttribute @Valid SignupForm form,
                         BindingResult bindingResult,
                         Model model) {

        LOGGER.info("User attempting sign up with code {}", code);

        if (bindingResult.hasErrors()) {
            model.addAttribute("invite", inviteRepository.findByCode(code));
            return "signup";
        }

        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);
            if (!invite.isAuthorisedInvite()) {
                return "redirect:/signup/enterToken/" + code;
            }

            identityService.createIdentityFromInviteCode(code, form.getPassword());
            inviteService.updateInviteByCode(code, InviteStatus.ACCEPTED);

            model.addAttribute("lpgUiUrl", lpgUiUrl);

            return "signupSuccess";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping(path = "/enterToken/{code}")
    public String enterToken(Model model,
                             @PathVariable(value = "code") String code) {

        LOGGER.info("User accessing token-based sign up screen");

        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);
            if (invite.isAuthorisedInvite()) {
                return "redirect:/signup/" + code;
            }

            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

            model.addAttribute("organisations", organisations);
            model.addAttribute("enterTokenForm", new EnterTokenForm());

            return "enterToken";
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping(path = "/enterToken/{code}")
    public String submitToken(Model model,
                              @PathVariable(value = "code") String code,
                              @ModelAttribute @Valid EnterTokenForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        LOGGER.info("User attempting token-based sign up");

        if (bindingResult.hasErrors()) {
            model.addAttribute("enterTokenForm", form);
            return "enterToken";
        }

        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);

            final String emailAddress = invite.getForEmail();
            final String domain = identityService.getDomainFromEmailAddress(emailAddress);

            try {
                csrsService.updateSpacesAvailable(domain, form.getToken(), code, false);
                LOGGER.info("User submitted Enter Token form with org = {}, token = {}, email = {}", form.getOrganisation(), form.getToken(), emailAddress);

                invite.setAuthorisedInvite(true);
                inviteRepository.save(invite);

                model.addAttribute("invite", invite);

                redirectAttributes.addFlashAttribute("organisation", form.getOrganisation());
                redirectAttributes.addFlashAttribute("token", form.getToken());

                return "redirect:/signup/" + code;

            } catch (ResourceNotFoundException e) {
                redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect token for this organisation");
                return "redirect:/signup/enterToken/" + code;
            } catch (NotEnoughSpaceAvailableException e) {
                redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Not enough spaces available on this token");
                return "redirect:/signup/enterToken/" + code;
            } catch (BadRequestException e) {
                return "redirect:/login";
            } catch (UnableToAllocateAgencyTokenException e) {
                return "redirect:/login";
            }
        }

        return "redirect:/login";
    }

    @PutMapping(path = "/updateToken/{code}")
    public String updateToken(Model model,
                              @PathVariable(value = "code") String code,
                              @ModelAttribute @Valid UpdateTokenForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        LOGGER.info("User attempting to update agency token");

        if (bindingResult.hasErrors()) {
            model.addAttribute("updateTokenForm", form);
            return "updateToken";
        }

        // TODO - CALL NEW METHOD
        // domain token org boolean
        try {
            csrsService.updateSpacesAvailable(code, form.getToken(), form.getOrganisation(), form.isRemoveUser());
        } catch (Exception e) {

        }

        // TODO - CHECK WHERE THIS SHOULD GO TO
        return "TODO";
    }


    @InitBinder
    public void setupValidation(WebDataBinder binder) {
        if (binder.getTarget() instanceof SignupForm) {
            binder.addValidators(signupFormValidator);
        }
    }
}
