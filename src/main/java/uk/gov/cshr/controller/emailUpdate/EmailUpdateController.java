package uk.gov.cshr.controller.emailUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.controller.form.EmailUpdatedRecentlyEnterTokenForm;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.EmailUpdateService;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/emailUpdated")
public class EmailUpdateController {

    private static final String EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM = "emailUpdatedRecentlyEnterTokenForm";

    private static final String ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME = "enterTokenSinceEmailUpdate";

    private static final String STATUS_ATTRIBUTE = "status";

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

        if(model.containsAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM)) {
            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();
            model.addAttribute("organisations", organisations);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME;
        } else {
            String domain = (String) model.asMap().get("domain");
            String uid = (String) model.asMap().get("uid");

            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

            model.addAttribute("organisations", organisations);
            EmailUpdatedRecentlyEnterTokenForm form = new EmailUpdatedRecentlyEnterTokenForm();
            form.setDomain(domain);
            form.setUid(uid);
            model.addAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM, form);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME;
        }

    }

    @PostMapping(path = "/enterToken")
    public String checkToken(Model model,
                              @ModelAttribute @Valid EmailUpdatedRecentlyEnterTokenForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        log.info("User attempting token-based sign up since updating their email");

        String domain = form.getDomain();
        String uid = form.getUid();

        if (bindingResult.hasErrors()) {
            model.addAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM, form);
            return ENTER_TOKEN_SINCE_EMAIL_UPDATE_VIEW_NAME;
        }

        try {
            Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
            if(optionalIdentity.isPresent()) {
                log.info("User checking Enter Token form with domain = {}, token = {}, org = {}", domain, form.getToken(), form.getOrganisation());
                emailUpdateService.processEmailUpdatedRecentlyRequestForAgencyTokenUser(domain, form.getToken(), form.getOrganisation(), uid);
                return "redirect:" + lpgUiUrl;
            } else {
                log.info("No identity found for uid {}", uid);
                return "redirect:/login";
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect token for this organisation");
            redirectAttributes.addFlashAttribute(EMAIL_UPDATED_RECENTLY_ENTER_TOKEN_FORM, form);
            return "redirect:/emailUpdated/enterToken";
        } catch (Exception e) {
            return "redirect:/login";
        }

    }

}
