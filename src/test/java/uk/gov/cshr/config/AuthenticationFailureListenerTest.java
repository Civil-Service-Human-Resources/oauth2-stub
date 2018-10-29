package uk.gov.cshr.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import uk.gov.cshr.service.security.LoginAttemptService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationFailureListenerTest {

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthenticationFailureListener listener;

    @Test
    public void shouldRecordFailedLoginAttempt() {
        AuthenticationFailureBadCredentialsEvent event = mock(AuthenticationFailureBadCredentialsEvent.class);
        String email = "email-address";
        Authentication authentication = mock(Authentication.class);
        when(event.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(email);

        listener.onApplicationEvent(event);

        verify(loginAttemptService).loginFailed(email);

    }
}