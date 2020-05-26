package uk.gov.cshr.controller.account.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.controller.form.EmailUpdatedRecentlyEnterTokenForm;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.NotEnoughSpaceAvailableException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.AgencyTokenCapacityService;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/account/email/verify/agency")
public class AgencyTokenVerificationController {

    private static final String EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM_TEMPLATE = "emailUpdatedRecentlyEnterTokenForm";
    private static final String ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE = "enterTokenSinceEmailUpdate";

    private static final String REDIRECT_ENTER_TOKEN = "redirect:/account/email/verify/agency/";
    private static final String REDIRECT_ACCOUNT_EMAIL_UPDATED = "redirect:/account/email/updated";
    private static final String EMAIL_ATTRIBUTE = "email";

    private final EmailUpdateService emailUpdateService;

    private final CsrsService csrsService;

    private final IdentityService identityService;

    private final AgencyTokenCapacityService agencyTokenCapacityService;

    public AgencyTokenVerificationController(
            EmailUpdateService emailUpdateService,
            CsrsService csrsService,
            AgencyTokenCapacityService agencyTokenCapacityService,
            IdentityService identityService) {
        this.emailUpdateService = emailUpdateService;
        this.csrsService = csrsService;
        this.agencyTokenCapacityService = agencyTokenCapacityService;
        this.identityService = identityService;
    }

    @GetMapping(path = "/{code}")
    public String enterToken(Model model, @PathVariable String code) {
        log.info("User accessing token-based email updated screen");

        if (model.containsAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM_TEMPLATE)) {
            addOrganisationsToModel(model);
            model.addAttribute("code", code);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE;
        } else {
            Map<String, Object> modelMap = model.asMap();
            String domain = identityService.getDomainFromEmailAddress(String.valueOf(modelMap.get("email")));

            addOrganisationsToModel(model);

            EmailUpdatedRecentlyEnterTokenForm form = new EmailUpdatedRecentlyEnterTokenForm();
            form.setDomain(domain);

            model.addAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM_TEMPLATE, form);
            model.addAttribute("code", code);

            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE;
        }
    }

    @PostMapping(path = "/{code}")
    public String checkToken(Model model,
                             @ModelAttribute @Valid EmailUpdatedRecentlyEnterTokenForm form,
                             BindingResult bindingResult,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        log.info("User verifying token after updating their email");

        IdentityDetails identityDetails = (IdentityDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (bindingResult.hasErrors()) {
            buildGenericErrorModel(model, form);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE;
        }

        try {
            log.info("Token validation with values: {}", form.toString());

            String domain = form.getDomain();
            String organisation = form.getOrganisation();
            String token = form.getToken();
            String code = form.getCode();

            AgencyToken agencyToken = csrsService.getAgencyTokenForDomainTokenOrganisation(domain, token, organisation)
                    .orElseThrow(ResourceNotFoundException::new);

            if (!agencyTokenCapacityService.hasSpaceAvailable(agencyToken)) {
                log.info("Agency token uid = {}, capacity = {}, has no spaces available. User {} unable to signup");
                redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.NO_SPACES_AVAILABLE_ERROR_MESSAGE);

                return REDIRECT_ENTER_TOKEN + code;
            }

            Identity identity = identityDetails.getIdentity();
            EmailUpdate emailUpdate = emailUpdateService.getEmailUpdate(identity, code);
            emailUpdateService.updateEmailAddress(request, identity, emailUpdate, agencyToken);

            log.info("Token validation complete, new agencyTokenUid: {}", identity.getAgencyTokenUid());

            redirectAttributes.addFlashAttribute(EMAIL_ATTRIBUTE, emailUpdate.getEmail());

            return REDIRECT_ACCOUNT_EMAIL_UPDATED;
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.CHANGE_EMAIL_ERROR_MESSAGE);

            return REDIRECT_ENTER_TOKEN + form.getCode();
        } catch (NotEnoughSpaceAvailableException e) {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.NO_SPACES_AVAILABLE_ERROR_MESSAGE);

            return REDIRECT_ENTER_TOKEN + form.getCode();
        } catch (Exception e) {
            return "redirect:/login";
        }
    }


    private void buildGenericErrorModel(Model model, EmailUpdatedRecentlyEnterTokenForm form) {
        model.addAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.ENTER_TOKEN_ERROR_MESSAGE);
        model.addAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM_TEMPLATE, form);
        addOrganisationsToModel(model);
    }

    private void addOrganisationsToModel(Model model) {
        model.addAttribute("organisations", csrsService.getOrganisationalUnitsFormatted());
    }
}
