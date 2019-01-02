package uk.gov.cshr.service.security;

import org.junit.Test;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class LoginAttemptServiceTest {
    private Map<String, Integer> loginAttemptCache = new HashMap<>();
    private int maxLoginAttempts = 3;

    private IdentityService identityService = mock(IdentityService.class);

    private LoginAttemptService loginAttemptService =
            new LoginAttemptService(maxLoginAttempts, identityService, loginAttemptCache);

    @Test
    public void loginSucceedSetAttemptsToZero() {
        String email = "test@domain.com";

        loginAttemptCache.put(email, 3);

        loginAttemptService.loginSucceeded(email);

        assertEquals(new Integer(0), loginAttemptCache.get(email));
    }

    @Test
    public void loginFailedIncrementsFailedAttempts() {
        String email = "test@domain.com";

        loginAttemptCache.clear();

        when(identityService.existsByEmail(email)).thenReturn(true);

        loginAttemptService.loginFailed(email);

        assertEquals(new Integer(1), loginAttemptCache.get(email));

        verify(identityService, times(0)).lockIdentity(email);
    }

    @Test
    public void loginFailedDoesNotIncrementCacheIfIdentityDoesNotExist() {
        String email = "test@domain.com";

        loginAttemptCache.clear();

        when(identityService.existsByEmail(email)).thenReturn(false);

        loginAttemptService.loginFailed(email);

        assertNull(loginAttemptCache.get(email));
    }


    @Test
    public void loginFailedLocksIdentityWhenMaxAttemptsIsExceeded() {
        String email = "test@domain.com";

        loginAttemptCache.put(email, 3);

        when(identityService.existsByEmail(email)).thenReturn(true);

        try {
            loginAttemptService.loginFailed(email);
        } catch (AuthenticationException e) {
            assertEquals("User account is locked", e.getMessage());
        }

        verify(identityService).lockIdentity(email);
    }

}