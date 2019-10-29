package uk.gov.cshr.service.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;

@Service
public class LoginAttemptService {

    private final int maxAttempt;
    private final IdentityService identityService;

    public LoginAttemptService(@Value("${account.lockout.maxAttempt}") int maxAttempt, IdentityService identityService) {
        this.maxAttempt = maxAttempt;
        this.identityService = identityService;
    }

    public void loginSucceeded(String email) {
        identityService.resetFailedLoginAttempts(email);
    }

    public void loginFailed(String email) {
        if (identityExists(email)) {
            incrementAttempts(email);
            if (areAttemptsMoreThanAllowedLimit(email)) {
                lockIdentity(email);
                throw new AuthenticationException("User account is locked") {
                };
            }
        }
    }

    private void lockIdentity(String email) {
        identityService.lockIdentity(email);
    }

    private void incrementAttempts(String email) {
        identityService.increaseFailedLoginAttempts(email);
    }

    private boolean areAttemptsMoreThanAllowedLimit(String email) {
        return identityService.getFailedLoginAttempts(email) >= maxAttempt;
    }

    private boolean identityExists(String email) {
        return identityService.existsByEmail(email);
    }
}
