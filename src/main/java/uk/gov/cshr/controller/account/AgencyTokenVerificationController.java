package uk.gov.cshr.controller.account;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.controller.form.VerifyTokenForm;
import uk.gov.cshr.domain.*;
import uk.gov.cshr.exception.NotEnoughSpaceAvailableException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.exception.VerificationCodeTypeNotFound;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/account/verify/agency")
public class AgencyTokenVerificationController {

    private static final String CODE_ATTRIBUTE = "code";
    private static final String VERIFY_TOKEN_FORM_TEMPLATE = "verifyTokenForm";
    private static final String VERIFY_TOKEN_TEMPLATE = "verifyToken";
    private static final String REDIRECT_VERIFY_TOKEN = "redirect:/account/verify/agency/";
    private static final String REDIRECT_REACTIVATED_SUCCESS = "redirect:/account/reactivate/updated";
    private static final String REDIRECT_ACCOUNT_EMAIL_UPDATED_SUCCESS = "redirect:/account/email/updated";

    private static final String EMAIL_ATTRIBUTE = "email";

    private final EmailUpdateService emailUpdateService;

    private final CsrsService csrsService;

    private final IdentityService identityService;

    private final AgencyTokenCapacityService agencyTokenCapacityService;

    private final ReactivationService reactivationService;

    private final VerificationCodeDeterminationService verificationCodeDeterminationService;

    public AgencyTokenVerificationController(
            EmailUpdateService emailUpdateService,
            CsrsService csrsService,
            AgencyTokenCapacityService agencyTokenCapacityService,
            IdentityService identityService,
            VerificationCodeDeterminationService verificationCodeDeterminationService,
            ReactivationService reactivationService) {
        this.emailUpdateService = emailUpdateService;
        this.csrsService = csrsService;
        this.agencyTokenCapacityService = agencyTokenCapacityService;
        this.identityService = identityService;
        this.verificationCodeDeterminationService = verificationCodeDeterminationService;
        this.reactivationService = reactivationService;
    }

    @GetMapping(path = "/{code}")
    public String enterToken(Model model, @PathVariable String code) {
        log.info("User accessing token-based verification screen");

        if (model.containsAttribute(VERIFY_TOKEN_FORM_TEMPLATE)) {
            addOrganisationsToModel(model);
            model.addAttribute(CODE_ATTRIBUTE, code);
            return VERIFY_TOKEN_TEMPLATE;
        } else {
            addOrganisationsToModel(model);

            VerifyTokenForm form = new VerifyTokenForm();

            model.addAttribute(VERIFY_TOKEN_FORM_TEMPLATE, form);
            model.addAttribute(CODE_ATTRIBUTE, code);

            return VERIFY_TOKEN_TEMPLATE;
        }
    }

    @PostMapping(path = "/{code}")
    public String checkToken(Model model,
                             @ModelAttribute @Valid VerifyTokenForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            buildGenericErrorModel(model, form);
            return VERIFY_TOKEN_TEMPLATE;
        }

        try {
            log.info("Token validation with values: {}", form.toString());

            String organisation = form.getOrganisation();
            String token = form.getToken();
            String code = form.getCode();

            VerificationCodeDetermination verificationCodeDetermination = verificationCodeDeterminationService.getCodeType(code);
            String domainFromEmailAddress = identityService.getDomainFromEmailAddress(verificationCodeDetermination.getEmail());

            AgencyToken agencyToken = csrsService.getAgencyTokenForDomainTokenOrganisation(domainFromEmailAddress, token, organisation)
                    .orElseThrow(ResourceNotFoundException::new);

            if (!agencyTokenCapacityService.hasSpaceAvailable(agencyToken)) {
                log.error("Agency token uid = {}, capacity = {}, has no spaces available. User {} unable to signup", agencyToken.getUid(), agencyToken.getCapacity(), verificationCodeDetermination.getEmail());
                redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.NO_SPACES_AVAILABLE_ERROR_MESSAGE);

                return REDIRECT_VERIFY_TOKEN + code;
            }
            VerificationCodeType verificationCodeType = verificationCodeDetermination.getVerificationCodeType();
            switch (verificationCodeType) {
                case EMAIL_UPDATE:
                    log.info("EMAIL_UPDATE agency verification for ", verificationCodeDetermination.toString());
                    EmailUpdate emailUpdate = emailUpdateService.getEmailUpdateByCode(code);
                    emailUpdateService.updateEmailAddress(emailUpdate, agencyToken);

                    redirectAttributes.addFlashAttribute(EMAIL_ATTRIBUTE, emailUpdate.getEmail());

                    return REDIRECT_ACCOUNT_EMAIL_UPDATED_SUCCESS;
                case REACTIVATION:
                    log.info("REACTIVATION agency verification for ", verificationCodeDetermination.toString());
                    Reactivation reactivation = reactivationService.getReactivationByCodeAndStatus(code, ReactivationStatus.PENDING);
                    reactivationService.reactivateIdentity(reactivation, agencyToken);

                    return REDIRECT_REACTIVATED_SUCCESS;
                default:
                    throw new VerificationCodeTypeNotFound(String.format("Invalid verification code type: %s", verificationCodeType));
            }
        } catch (ResourceNotFoundException e) {
            log.error("ResourceNotFoundException during agency verification for form: {}", form.toString());

            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.ENTER_TOKEN_ERROR_MESSAGE);
            return REDIRECT_VERIFY_TOKEN + form.getCode();
        } catch (NotEnoughSpaceAvailableException e) {
            log.error("NotEnoughSpaceAvailableException during agency verification for form: {}", form.toString());

            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.NO_SPACES_AVAILABLE_ERROR_MESSAGE);
            return REDIRECT_VERIFY_TOKEN + form.getCode();
        } catch (Exception e) {
            log.error("Exception during agency verification for form: {}", form.toString());

            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.VERIFY_AGENCY_TOKEN_ERROR_MESSAGE);
            return "redirect:/login";
        }
    }

    private void buildGenericErrorModel(Model model, VerifyTokenForm form) {
        model.addAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.ENTER_TOKEN_ERROR_MESSAGE);
        model.addAttribute(VERIFY_TOKEN_FORM_TEMPLATE, form);
        model.addAttribute(CODE_ATTRIBUTE, form.getCode());

        addOrganisationsToModel(model);
    }

    private void addOrganisationsToModel(Model model) {
        model.addAttribute("organisations", csrsService.getOrganisationalUnitsFormatted());
    }
}
