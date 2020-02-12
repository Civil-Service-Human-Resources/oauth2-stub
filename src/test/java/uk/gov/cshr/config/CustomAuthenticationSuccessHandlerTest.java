package uk.gov.cshr.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.AgencyTokenService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringSecurityTestConfig.class})
public class CustomAuthenticationSuccessHandlerTest {

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Value("${emailUpdate.invalidDomainUrl}")
    private String invalidDomainUrl;

    @Autowired
    private AuthenticationSuccessHandler classUnderTest;

    // Spring bean from SpringSecurityTestConfig, not real one.
    @Autowired
    private UserDetailsService userDetailsService;

    @MockBean
    private RedirectStrategy redirectStrategy;

    @MockBean
    private AgencyTokenService agencyTokenService;

    @MockBean
    private IdentityService identityService;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private HttpSession session;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        when(identityService.getDomainFromEmailAddress(eq("basic@domain.com"))).thenReturn("domain.com");
        when(identityService.getDomainFromEmailAddress(eq("special@domain.com"))).thenReturn("domain.com");
        session = new MockHttpSession(null, "test-session-id");
        request.setSession(session);
    }

    @After
    public void atEndOfEachTest() {
        // ensure that this is always cleared.
        String webAttributeAuthException = (String) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        assertThat(webAttributeAuthException).isNull();
    }

    @Test
    @WithUserDetails(
            value = "uid",
            userDetailsServiceBeanName = "userDetailsService")
    public void givenNormalUser_whenOnAuthenticationSuccess_thenRedirectsToLPGUIHomePage() throws IOException, ServletException {
        // given
        prepareSecurityContext("uid");
        when(identityService.getRecentlyUpdatedEmailFlag(any(Identity.class))).thenReturn(false);

        // when
        classUnderTest.onAuthenticationSuccess(request, response, SecurityContextHolder.getContext().getAuthentication());

        // then
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(lpgUiUrl));
    }

    @Test
    @WithUserDetails(
            value = "specialuid",
            userDetailsServiceBeanName = "userDetailsService")
    public void givenSpecialUserThatHasRecentlyChangedTheirEmailAndIsWhitelisted_whenOnAuthenticationSuccess_thenRedirectsToLPGUIHomePage() throws IOException, ServletException {
        // *****as the profile checker in the UI will automatically redirect them to an org page.
        // i.e. the redirect happens within the UI application, not here in the Java.

        // given
        prepareSecurityContext("specialuid");
        when(identityService.getRecentlyUpdatedEmailFlag(any(Identity.class))).thenReturn(true);
        when(agencyTokenService.isDomainWhiteListed(anyString())).thenReturn(true);

        // when
        classUnderTest.onAuthenticationSuccess(request, response, SecurityContextHolder.getContext().getAuthentication());

        // then
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(lpgUiUrl));
    }

    @Test
    @WithUserDetails(
            value = "specialuid",
            userDetailsServiceBeanName = "userDetailsService")
    public void givenSpecialUserThatHasRecentlyChangedTheirEmailAndIsAnAgencyTokenPerson_whenOnAuthenticationSuccess_thenRedirectsToControllerThatStoresTheUIDAndDomainAndThenPerformsTheRedirectToEnterTokenPage() throws IOException, ServletException {
        // given
        prepareSecurityContext("specialuid");
        when(identityService.getRecentlyUpdatedEmailFlag(any(Identity.class))).thenReturn(true);
        when(agencyTokenService.isDomainWhiteListed(anyString())).thenReturn(false);
        when(agencyTokenService.isDomainAnAgencyTokenDomain(anyString())).thenReturn(true);

        // when
        classUnderTest.onAuthenticationSuccess(request, response, SecurityContextHolder.getContext().getAuthentication());

        // then
        String expectedEnterTokenUrl = "/redirectToEnterTokenPage/domain.com/specialuid";
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), contains(expectedEnterTokenUrl));
    }

    @Test
    @WithUserDetails(
            value = "specialuid",
            userDetailsServiceBeanName = "userDetailsService")
    public void givenSpecialUserThatHasRecentlyChangedTheirEmailAndIsAnInvalidDomain_whenOnAuthenticationSuccess_thenRedirectsToControllerThatPerformsTheRedirectToToSignOnPageWithErrorInvalidOrgMessage() throws IOException, ServletException {
        // given
        prepareSecurityContext("specialuid");
        when(identityService.getRecentlyUpdatedEmailFlag(any(Identity.class))).thenReturn(true);
        when(agencyTokenService.isDomainWhiteListed(anyString())).thenReturn(false);
        when(agencyTokenService.isDomainAnAgencyTokenDomain(anyString())).thenReturn(false);

        // when
        classUnderTest.onAuthenticationSuccess(request, response, SecurityContextHolder.getContext().getAuthentication());

        // then
        String expectedInvalidOrgUrl = String.format(invalidDomainUrl, "domain.com", "specialuid");
        verify(redirectStrategy, times(1)).sendRedirect(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(expectedInvalidOrgUrl));
    }

    private void prepareSecurityContext(String userNameToAuthenticateWith) {
        /*
         * Get the test user given from the userdetailsservice, in this case from Spring Security Test Config.
         * Create the users associated authentication object using the IdentityDetails provided.
         * See the Spring Security context to ensure Spring has this Authentication object.
         * (The same as the real code).
         * The tests have to ensure the same object types otherwise we get class cast exceptions and so on.
         */
        IdentityDetails identityDetails = (IdentityDetails) userDetailsService.loadUserByUsername(userNameToAuthenticateWith);
        Authentication authToken = new UsernamePasswordAuthenticationToken (identityDetails, identityDetails.getPassword(), identityDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
