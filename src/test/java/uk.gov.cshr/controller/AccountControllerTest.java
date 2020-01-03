package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.utils.MockMVCFilterOverrider;
import uk.gov.cshr.utils.WithMockCustomUser;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {SpringSecurityTestConfig.class})
@AutoConfigureMockMvc
public class AccountControllerTest {

    private final Boolean ACTIVE = true;
    private final Boolean LOCKED = false;
    private final String DESCRIPTION = "User";
    private final String EMAIL = "email";
    private final String NAME = "User";
    private final String PASSWORD = "password";
    private final Set<Role> ROLES = new HashSet();
    private static final String UID = "uid";
    private final String USERNAME = "test";
    private final String[] roleID = {"1"};

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailUpdateService emailUpdateService;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private IdentityDetails identityDetails;


    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );
    }

    @Test
    @WithUserDetails(
            value = "uid",
            userDetailsServiceBeanName = "userDetailsService")
    @WithMockCustomUser
    public void shouldUpdateEmailIfCodeIsValid() throws Exception {

        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);
        when(authentication.getPrincipal()).thenReturn(identityDetails);
        when(identityDetails.getIdentity()).thenReturn(identity);
        when(emailUpdateService.verifyCode(any(Identity.class), anyString())).thenReturn(true);

        String expectedCode = "1234567891234567";

        mockMvc.perform(get("/account/email/verify/1234567891234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/emailUpdated"))
                .andDo(print());

        verify(emailUpdateService, times(1)).updateEmailAddress(eq(identity), eq(expectedCode));
    }


}
