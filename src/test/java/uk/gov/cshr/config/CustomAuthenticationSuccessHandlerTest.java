package uk.gov.cshr.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.service.security.IdentityDetails;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringSecurityTestConfig.class})
public class CustomAuthenticationSuccessHandlerTest {

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Value("${lpg.changeOrgUrl}")
    private String lpgChangeOrgUrl;

    @Autowired
    private AuthenticationSuccessHandler classUnderTest;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockBean
    private RedirectStrategy redirectStrategy;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @WithUserDetails(
            value = "uid",
            userDetailsServiceBeanName = "userDetailsService")
    public void givenNormalUser_whenOnAuthenticationSuccess_thenRedirectsToLPGUIHomePage() throws IOException, ServletException {
        prepareSecurityContext("uid");
        classUnderTest.onAuthenticationSuccess(request, response, SecurityContextHolder.getContext().getAuthentication());
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(lpgUiUrl));
    }

    @Test
    @WithUserDetails(
            value = "specialuid",
            userDetailsServiceBeanName = "userDetailsService")
    public void givenSpecialUser_whenOnAuthenticationSuccess_thenRedirectsToLPGUIUpdateOrgPage() throws IOException, ServletException {
        prepareSecurityContext("specialuid");
        classUnderTest.onAuthenticationSuccess(request, response, SecurityContextHolder.getContext().getAuthentication());
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(lpgChangeOrgUrl));
    }

    private void prepareSecurityContext(String userNameToAuthenticateWith) {
        IdentityDetails identityDetails = (IdentityDetails) userDetailsService.loadUserByUsername (userNameToAuthenticateWith);
        Authentication authToken = new UsernamePasswordAuthenticationToken (identityDetails, identityDetails.getPassword(), identityDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
