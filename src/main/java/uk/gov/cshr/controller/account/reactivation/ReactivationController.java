package uk.gov.cshr.controller.account.reactivation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.AgencyTokenService;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;


@Slf4j
@Controller
@RequestMapping("/account/reactivate")
public class ReactivationController {

    private static final String ACCOUNT_REACTIVATED_TEMPLATE = "account/accountReactivated";

    private static final String REDIRECT_ACCOUNT_REACTIVATED = "redirect:/account/reactivate/updated";

    private static final String REDIRECT_ACCOUNT_REACTIVATE_AGENCY = "redirect:/account/verify/agency/";

    private static final String REDIRECT_LOGIN = "redirect:/login";

    private static final String LPG_UI_URL_ATTRIBUTE = "lpgUiUrl";

    private final ReactivationService reactivationService;

    private final IdentityService identityService;

    private final AgencyTokenService agencyTokenService;

    private final String lpgUiUrl;


    public ReactivationController(ReactivationService reactivationService,
                                  IdentityService identityService,
                                  AgencyTokenService agencyTokenService,
                                  @Value("${lpg.uiUrl}") String lpgUiUrl) {
        this.reactivationService = reactivationService;
        this.identityService = identityService;
        this.agencyTokenService = agencyTokenService;
        this.lpgUiUrl = lpgUiUrl;
    }

    @GetMapping("/{code}")
    public String reactivateAccount(
            @PathVariable(value = "code") String code,
            RedirectAttributes redirectAttributes) {
        try {
            Reactivation reactivation = reactivationService.getReactivationByCodeAndStatus(code, ReactivationStatus.PENDING);
            String domain = identityService.getDomainFromEmailAddress(reactivation.getEmail());

            log.debug("Reactivating account using Reactivation: {}", reactivation);

            if (isNotWhitelistedAndIsAgency(domain)) {
                log.info("Account reactivation is agency, not whitelisted and requires token validation for Reactivation: {}", reactivation);
                return REDIRECT_ACCOUNT_REACTIVATE_AGENCY + code;
            } else {
                log.info("Account reactivation is not agency and can reactivate without further validation for Reactivation: {}", reactivation);
                reactivationService.reactivateIdentity(reactivation);
                return REDIRECT_ACCOUNT_REACTIVATED;
            }
        } catch (ResourceNotFoundException e) {
            log.error("ResourceNotFoundException for code: {}, with status {}", ReactivationStatus.PENDING);
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.REACTIVATION_CODE_IS_NOT_VALID_ERROR_MESSAGE);
            return REDIRECT_LOGIN;
        } catch (Exception e) {
            log.error("Unexpected error for code: {}, with cause {}", code, e.getCause());
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.ACCOUNT_REACTIVATION_ERROR_MESSAGE);
            return REDIRECT_LOGIN;
        }
    }

    @GetMapping("/updated")
    public String emailUpdated(Model model) {
        log.info("Account reactivation complete");
        model.addAttribute(LPG_UI_URL_ATTRIBUTE, lpgUiUrl + "/login");

        return ACCOUNT_REACTIVATED_TEMPLATE;
    }

    private boolean isNotWhitelistedAndIsAgency(String newDomain) {
        return !identityService.isWhitelistedDomain(newDomain) && agencyTokenService.isDomainInAgencyToken(newDomain);
    }
}
