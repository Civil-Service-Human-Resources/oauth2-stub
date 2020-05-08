package uk.gov.cshr.controller.emailUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.controller.form.EmailUpdatedRecentlyEnterTokenForm;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.NotEnoughSpaceAvailableException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.utils.ApplicationConstants;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/emailUpdated")
public class EmailUpdateController {

    private static final String EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM_TEMPLATE = "emailUpdatedRecentlyEnterTokenForm";

    private static final String ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE = "enterTokenSinceEmailUpdate";

    private final EmailUpdateService emailUpdateService;

    private final CsrsService csrsService;

    private final IdentityRepository identityRepository;

    private final String lpgUiUrl;

    public EmailUpdateController(
            EmailUpdateService emailUpdateService,
            CsrsService csrsService,
            IdentityRepository identityRepository,
            @Value("${lpg.uiUrl}") String lpgUiUrl) {
        this.emailUpdateService = emailUpdateService;
        this.csrsService = csrsService;
        this.identityRepository = identityRepository;
        this.lpgUiUrl = lpgUiUrl;
    }

    @GetMapping(path = "/enterToken")
    public String enterToken(Model model) {

        log.info("User accessing token-based email updated screen");

        if (model.containsAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM_TEMPLATE)) {
            addOrganisationsToModel(model);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE;
        } else {
            String domain = (String) model.asMap().get("domain");

            addOrganisationsToModel(model);
            EmailUpdatedRecentlyEnterTokenForm form = new EmailUpdatedRecentlyEnterTokenForm();
            form.setDomain(domain);
            model.addAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM_TEMPLATE, form);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE;
        }
    }

    @PostMapping(path = "/enterToken")
    public String checkToken(Model model,
                             @ModelAttribute @Valid EmailUpdatedRecentlyEnterTokenForm form,
                             BindingResult bindingResult,
                             HttpServletRequest request) {

        log.info("User attempting token-based sign up since updating their email");

        IdentityDetails identityDetails = (IdentityDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String domain = form.getDomain();

        if (bindingResult.hasErrors()) {
            buildGenericErrorModel(model, form);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE;
        }

        try {
            Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(identityDetails.getIdentity().getUid());
            if (optionalIdentity.isPresent()) {
                log.info("User checking Enter Token form with domain = {}, token = {}, org = {}", domain, form.getToken(), form.getOrganisation());
                emailUpdateService.processEmailUpdatedRecentlyRequestForAgencyTokenUser(domain, form.getToken(), form.getOrganisation(), optionalIdentity.get(), request);
                return "redirect:" + lpgUiUrl;
            } else {
                log.info("No identity found for uid {}", identityDetails.getIdentity().getUid());
                return "redirect:/login";
            }
        } catch (ResourceNotFoundException e) {
            buildGenericErrorModel(model, form);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE;
        } catch (NotEnoughSpaceAvailableException e) {
            model.addAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.NO_SPACES_AVAILABLE_ERROR_MESSAGE);
            model.addAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM_TEMPLATE, form);
            addOrganisationsToModel(model);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME_TEMPLATE;
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
