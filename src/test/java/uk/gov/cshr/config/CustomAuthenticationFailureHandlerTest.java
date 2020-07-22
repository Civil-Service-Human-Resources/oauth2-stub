package uk.gov.cshr.config;

import org.junit.Test;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class CustomAuthenticationFailureHandlerTest {


    private CustomAuthenticationFailureHandler authenticationFailureHandler = new CustomAuthenticationFailureHandler();

    @Test
    public void shouldSetErrorToLockedOnAccountLock() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        when(exception.getMessage()).thenReturn("User account is locked");

        authenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("/login?error=locked");
    }

    @Test
    public void shouldSetErrorToFailedOnFailedLogin() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        when(exception.getMessage()).thenReturn("Some other error");

        authenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("/login?error=failed");
    }

    @Test
    public void shouldSetErrorToFailedOnAccountBlocked() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        when(exception.getMessage()).thenReturn("User account is blocked");

        authenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("/login?error=blocked");
    }

    @Test
    public void shouldSetErrorToDeactivatedOnAccountDeactivated() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        when(exception.getMessage()).thenReturn("User account is deactivated");

        authenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("/login?error=deactivated");
    }
}