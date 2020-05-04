package uk.gov.cshr.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {SpringSecurityTestConfig.class})
@RunWith(SpringRunner.class)
public class SpringUserUtilsTest {

    // Spring bean from SpringSecurityTestConfig, not real one.
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SpringUserUtils classUnderTest;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    public void givenAnAuthenticatedUser_whenGetIdentityFromSpringAuthentication_thenShouldReturnUserDetailsFromCurrentSpringAuthentication() {
        // given
        prepareSecurityContext("uid");

        // when
        Identity actual = classUnderTest.getIdentityFromSpringAuthentication();

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getEmail()).isEqualTo("basic@domain.com");
    }

    @Test(expected = UsernameNotFoundException.class)
    public void givenNoAuthenticatedUser_whenGetIdentityFromSpringAuthentication_thenShouldThrowUserNotFoundException() {
        // given
        prepareSecurityContext("nobody");

        // when
        Identity actual = classUnderTest.getIdentityFromSpringAuthentication();

        // then
        assertThat(actual).isNull();
    }

    @Test
    public void givenAValidIdentityUser_whenUpdateSpringAuthenticationAndSpringSessionWithUpdatedIdentity_thenShouldSpringAuthenticationAndSpringSessionShouldBeUpdatedWithTheNewValue() {
        // given
        prepareSecurityContext("specialuid");
        Identity existingIdentity = classUnderTest.getIdentityFromSpringAuthentication();
        // identity with uid of "specialuid" has recently updated email flag set to true at the moment.
        assertThat(existingIdentity.isEmailRecentlyUpdated()).isTrue();
        // update flag to be false
        existingIdentity.setEmailRecentlyUpdated(false);

        // when
        classUnderTest.updateSpringAuthenticationAndSpringSessionWithUpdatedIdentity(request, existingIdentity);

        // then
        Authentication actualAuthentication = SecurityContextHolder.getContext().getAuthentication();
        IdentityDetails actualIdentityDetails = (IdentityDetails) actualAuthentication.getPrincipal();
        Identity actualIdentity = actualIdentityDetails.getIdentity();

        // check there is an identity
        assertThat(actualIdentity).isNotNull();

        // check that the identity that the spring authentication is using has recently updated email flag set to false now.
        assertThat(actualIdentity.isEmailRecentlyUpdated()).isFalse();

        // check that the identity that the spring session is using has recently reactivated flag set to false now.
        SecurityContext actualSpringSessionContext = (SecurityContext) request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        Authentication actualAuthenticationFromSpringSession = actualSpringSessionContext.getAuthentication();
        IdentityDetails actualIdentityDetailsFromSpringSession = (IdentityDetails) actualAuthenticationFromSpringSession.getPrincipal();
        Identity actualIdentityFromSpringSession = actualIdentityDetailsFromSpringSession.getIdentity();
        assertThat(actualIdentityFromSpringSession.isEmailRecentlyUpdated()).isFalse();
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
        Authentication authToken = new UsernamePasswordAuthenticationToken(identityDetails, identityDetails.getPassword(), identityDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}