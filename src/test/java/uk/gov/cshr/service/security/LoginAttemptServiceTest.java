package uk.gov.cshr.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoginAttemptServiceTest {
    private int maxLoginAttempts = 3;

    private IdentityService identityService;

    @Mock
    private IdentityRepository identityRepository;

    private LoginAttemptService loginAttemptService;

    @Before
    public void setUp(){
        identityService = new IdentityService("", identityRepository, null, null, null, null);
        loginAttemptService = new LoginAttemptService(maxLoginAttempts, identityService);
    }

    @Test
    public void loginSucceedSetAttemptsToZero() {
        String email = "test@domain.com";
        Identity identity = new Identity();
        identity.setFailedLoginAttempts(3L);

        when(identityRepository.findFirstByEmailEquals(email)).thenReturn(identity);

        loginAttemptService.loginSucceeded(email);


        assertEquals(new Long(0), identityService.getFailedLoginAttempts(email));
    }

   @Test
    public void loginFailedIncrementsFailedAttempts() {
       String email = "test2@domain.com";
       Identity identity = new Identity();
       identity.setFailedLoginAttempts(0L);

       when(identityRepository.findFirstByEmailEquals(email)).thenReturn(identity);
       when(identityRepository.existsByEmail(email)).thenReturn(true);

       loginAttemptService.loginFailed(email);

       assertEquals(new Long(1), identityService.getFailedLoginAttempts(email));
    }


    @Test
    public void loginFailedLocksIdentityWhenMaxAttemptsIsExceeded() {
        String email = "test@domain.com";
        Identity identity = new Identity();
        identity.setFailedLoginAttempts(3L);

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(email)).thenReturn(identity);
        when(identityRepository.findFirstByEmailEquals(email)).thenReturn(identity);
        when(identityRepository.existsByEmail(email)).thenReturn(true);

        try {
            loginAttemptService.loginFailed(email);
        } catch (AuthenticationException e) {
            assertEquals("User account is locked", e.getMessage());
        }

        assertEquals(true, identity.isLocked());
    }

}