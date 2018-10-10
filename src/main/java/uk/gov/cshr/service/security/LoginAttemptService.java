package uk.gov.cshr.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginAttemptService {

    @Value("${account.lockout.maxAttempt}")
    private int maxAttempt;

    private Map<String, Integer> attemptCache;

    @Autowired
    private IdentityRepository identityRepository;

    public LoginAttemptService() {
        attemptCache = new HashMap<>();
    }

    public void loginSucceeded(String email) {
        attemptCache.replace(email, 0);
    }

    public void loginFailed(String email) {
        if (identityExists(email)) {
            if (areAttemptsMoreThanAllowedLimit(email)) {
                lockIdentity(email);
            }
        }
    }

    private void lockIdentity(String email) {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(email);
        identity.setLocked(true);
        identityRepository.save(identity);
    }

    private void incrementAttempts(String email) {
        attemptCache.merge(email, 1, Integer::sum);
    }

    private boolean areAttemptsMoreThanAllowedLimit(String email) {
        incrementAttempts(email);
        return attemptCache.get(email) >= maxAttempt;
    }

    private boolean identityExists(String email) {
        return identityRepository.existsByEmail(email);
    }
}
