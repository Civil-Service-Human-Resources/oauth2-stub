package uk.gov.cshr.config;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.LoginAttemptService;

@Component
public class AuthenticationEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final LoginAttemptService loginAttemptService;

    public AuthenticationEventListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    public void onApplicationEvent(AuthenticationSuccessEvent e) {
        Object principal = e.getAuthentication().getPrincipal();

        if (principal instanceof IdentityDetails) {
            IdentityDetails identityDetails = (IdentityDetails) principal;
            loginAttemptService.loginSucceeded(identityDetails.getIdentity().getEmail());
        }
    }
}