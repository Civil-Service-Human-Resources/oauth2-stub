package uk.gov.cshr.controller.signup;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final String STATUS_ATTRIBUTE = "status";
    private static final String ORGANISATION_FLASH_ATTRIBUTE = "organisation";
    private static final String TOKEN_FLASH_ATTRIBUTE = "token";

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
            log.info("{} has already been invited", email);
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, email + " has already been invited");
            return "redirect:/signup/request";
        }

        if (identityService.existsByEmail(email)) {
            log.info("{} is already a user", email);
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

        log.info("User accessing sign up screen with code {}", code);

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
                         Model model,
                         RedirectAttributes redirectAttributes) {

        log.info("User attempting sign up with code {}", code);

        if (bindingResult.hasErrors()) {
            model.addAttribute("invite", inviteRepository.findByCode(code));
            return "signup";
        }

        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);
            if (!invite.isAuthorisedInvite()) {
                return "redirect:/signup/enterToken/" + code;
            }

            if(isRequestFromEnterTokenPage(form)) {
                // token quota to be updated at setting the password screen i.e. here
                // which requires the token and the organisation
                String organisationFromPreviousRequest = form.getOrganisation();
                String tokenFromPreviousRequest = form.getToken();

                    try {
                        log.info("User submitted signup password form with org = {}, token = {}, domain = {}", form.getOrganisation(), form.getToken(), form.getOrganisation());
                        log.info("Updating agency token quota");
                        csrsService.updateSpacesAvailable(form.getOrganisation(), form.getToken(), code, false);
                    } catch (ResourceNotFoundException e) {
                        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect token for this organisation");
                        return "redirect:/signup/enterToken/" + code;
                    } catch (NotEnoughSpaceAvailableException e) {
                        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Not enough spaces available on this token");
                        return "redirect:/signup/enterToken/" + code;
                    } catch (BadRequestException e) {
                        log.error("An error updating agency token quota has occurred", e);
                        return "redirect:/login";
                    } catch (UnableToAllocateAgencyTokenException e) {
                        log.error("An unexpected error updating agency token quota has occurred", e);
                        return "redirect:/login";
                    }

            }


            // for everyone
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

        log.info("User accessing token-based sign up screen");

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
    public String checkToken(Model model,
                              @PathVariable(value = "code") String code,
                              @ModelAttribute @Valid EnterTokenForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        log.info("User attempting token-based sign up");

        if (bindingResult.hasErrors()) {
            model.addAttribute("enterTokenForm", form);
            return "enterToken";
        }

        if (inviteService.isInviteValid(code)) {
            Invite invite = inviteRepository.findByCode(code);

            final String emailAddress = invite.getForEmail();
            final String domain = identityService.getDomainFromEmailAddress(emailAddress);

            return csrsService.getAgencyTokenForDomainTokenOrganisation(domain, form.getToken(), form.getOrganisation())
                    .map(agencyToken -> {
                        log.info("User submitted Enter Token form with org = {}, token = {}, email = {}", form.getOrganisation(), form.getToken(), emailAddress);

                        invite.setAuthorisedInvite(true);
                        inviteRepository.save(invite);

                        model.addAttribute("invite", invite);

                        // token quota to be updated at setting the password screen which requires the token and the organisation
                        redirectAttributes.addFlashAttribute(ORGANISATION_FLASH_ATTRIBUTE, form.getOrganisation());
                        redirectAttributes.addFlashAttribute(TOKEN_FLASH_ATTRIBUTE, form.getToken());

                        return "redirect:/signup/" + code;
                    }).orElseGet(() -> {
                        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect token for this organisation");
                        return "redirect:/signup/enterToken/" + code;
                    });
        } else {
            return "redirect:/login";
        }
    }

    @InitBinder
    public void setupValidation(WebDataBinder binder) {
        if (binder.getTarget() instanceof SignupForm) {
            binder.addValidators(signupFormValidator);
        }
    }

    private boolean isRequestFromEnterTokenPage(SignupForm signupForm) {
        if(signupForm.getOrganisation() == null && signupForm.getToken() == null) {
            return false;
        }
        return true;
    }

}
