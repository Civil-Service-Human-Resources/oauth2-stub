package uk.gov.cshr.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.LoginAttemptService;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationEventListenerTest {

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthenticationEventListener authenticationEventListener;

    @Test
    public void shouldOnlySetSucceededForUsers() {
        AuthenticationSuccessEvent event = mock(AuthenticationSuccessEvent.class);

        Authentication authentication = mock(Authentication.class);

        IdentityDetails identityDetails = mock(IdentityDetails.class);
        Identity identity = mock(Identity.class);
        String email = "email-address";
        when(identity.getEmail()).thenReturn(email);

        when(event.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(identityDetails);
        when(identityDetails.getIdentity()).thenReturn(identity);

        authenticationEventListener.onApplicationEvent(event);

        verify(loginAttemptService).loginSucceeded(email);
    }

    @Test
    public void shouldNotSetSucceededForClients() {
        String client = "client-name";
        AuthenticationSuccessEvent event = mock(AuthenticationSuccessEvent.class);

        Authentication authentication = mock(Authentication.class);

        when(event.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(client);

        authenticationEventListener.onApplicationEvent(event);

        verifyZeroInteractions(loginAttemptService);
    }

}