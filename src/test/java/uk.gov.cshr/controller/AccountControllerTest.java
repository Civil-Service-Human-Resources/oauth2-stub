package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.WithMockCustomUser;

import javax.servlet.Filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockBean
    private EmailUpdateService emailUpdateService;

    @MockBean
    private IdentityService identityService;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity(springSecurityFilterChain))
                .build();

        MockitoAnnotations.initMocks(this);
    }

   /* @WithUserDetails(
            value = "uid",
            userDetailsServiceBeanName = "userDetailsService")*/
    @Test
    public void shouldUpdateEmailIfCodeIsValid() throws Exception {
        /*
         *  SpringSecurityTestConfig sets up 2 users, uid and specialuid.  See @Import(SpringSecurityTestConfig.class)
         *
         *  1. Find the user uid, get the associated IdentityDetails (prepareAuthentication method)
         *  2. Create an Authentication object using this. (prepareAuthentication method)
         *  3. This Authentication object has to be in the request, as its a parameter of the controller method.
         *      Therefore set this Authentication to be used in the requests UserPrincipal, (the currently logged in user).
         *
         *  Note:  This now means that the Authentication object passed into the controller method is not null.
         *  Note:  If you just simply set the Spring Security Context then,
         *         this results in the Authentication object being passed into the controller method as null.
         *         i.e. Authentication must be set in the request......
         */

        // given
        Authentication authentication = prepareAuthentication("uid");
        when(emailUpdateService.verifyCode(any(Identity.class), anyString())).thenReturn(true);
        String expectedCode = "1234567891234567";

        // when
        mockMvc.perform(get("/account/email/verify/1234567891234567")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/emailUpdated"))
                .andDo(print());

        // then
        verify(emailUpdateService, times(1)).updateEmailAddress(any(Identity.class), eq(expectedCode));
    }

    private Authentication prepareAuthentication(String userNameToAuthenticateWith) {
        IdentityDetails identityDetails = (IdentityDetails) userDetailsService.loadUserByUsername(userNameToAuthenticateWith);
        return new UsernamePasswordAuthenticationToken(identityDetails, identityDetails.getPassword(), identityDetails.getAuthorities());
    }

}