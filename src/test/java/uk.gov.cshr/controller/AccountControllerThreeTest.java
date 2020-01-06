package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.utils.WithMockCustomUser;

import javax.servlet.Filter;

import java.security.Principal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerThreeTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Autowired
    private UserDetailsService userDetailsService;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity(springSecurityFilterChain))
                .build();

        MockitoAnnotations.initMocks(this);
    }

    @WithUserDetails(
            value = "uid",
            userDetailsServiceBeanName = "userDetailsService")
    @WithMockCustomUser
    @Test
    public void shouldUpdateEmailIfCodeIsValid1() throws Exception {

        prepareSecurityContext("uid");

        mockMvc.perform(get("/account/email/verify/1234567891234567")
                .with(request1 -> {
                            request1.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
                            return request1;
                        }
                ))
                .andDo(print());


     /*   mockMvc.perform(get("/account/email/verify/1234567891234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/emailUpdated"))
                .andDo(print());
*/
        //verify(emailUpdateService, times(1)).updateEmailAddress(eq(identity), eq(expectedCode));
    }

    private void prepareSecurityContext(String userNameToAuthenticateWith) {
        IdentityDetails identityDetails = (IdentityDetails) userDetailsService.loadUserByUsername(userNameToAuthenticateWith);
        Authentication authToken = new UsernamePasswordAuthenticationToken (identityDetails, identityDetails.getPassword(), identityDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}