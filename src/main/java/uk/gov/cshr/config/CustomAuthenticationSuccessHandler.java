package uk.gov.cshr.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.AgencyTokenService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    @Value("${emailUpdate.invalidDomainUrl}")
    private String invalidDomainUrl;

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Autowired
    private RedirectStrategy redirectStrategy;

    @Autowired
    private AgencyTokenService agencyTokenService;

    @Autowired
    private IdentityService identityService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws IOException {
        handle(request, response, authentication);
        clearAuthenticationAttributes(request);
    }

    protected void handle(HttpServletRequest request,
                          HttpServletResponse response, Authentication authentication)
            throws IOException {

        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            log.debug(
                    "Response has already been committed. Unable to redirect to "
                            + targetUrl);
            return;
        }

        log.debug("Redirecting to " + targetUrl);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(Authentication authentication) {
        String targetURL = lpgUiUrl;
        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();
        if(identity.isEmailRecentlyUpdated()) {
            log.debug("users email has recently been updated");
            targetURL = workoutIfUserShouldUseAnAgencyToken(identity);
        }

        return targetURL;
    }

    private String workoutIfUserShouldUseAnAgencyToken(Identity identity) {
        String domain = identityService.getDomainFromEmailAddress(identity.getEmail());
        String uid = identity.getUid();
        if (agencyTokenService.isDomainWhiteListed(domain)) {
            return "/redirectToChangeOrgPage/" + domain + "/" + uid;
        } else {
            if(agencyTokenService.isDomainAnAgencyTokenDomain(domain)) {
                return "/redirectToEnterTokenPage/" + domain + "/" + uid;
            } else {
                return invalidDomainUrl;
            }
        }
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

}
