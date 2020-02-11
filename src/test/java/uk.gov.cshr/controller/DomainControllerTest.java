package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.Filter;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DomainControllerTest {

    private static final String EMAIL = "test@example.org";
    private static final String PASSWORD = "password123";
    private static final String UID = "abc123";

    @MockBean
    private IdentityService identityService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    private Authentication authentication;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()).addFilter(springSecurityFilterChain)
                .build();
        MockitoAnnotations.initMocks(this);

        prepareAuthentication();
        authentication = SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    public void shouldReturnTrueIfWhitelistedTrue() throws Exception {
        // given
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(true);

        // when
        mockMvc.perform(
                get("/domain/isWhitelisted/domain.com/")
                        .with(request1 -> {
                                    request1.setUserPrincipal(authentication);
                                    return request1;
                                }
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // then
        verify(identityService, times(1)).isWhitelistedDomain(eq("domain.com"));
    }

    @Test
    public void shouldReturnFalseIfWhitelistedFalse() throws Exception {
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(false);

        mockMvc.perform(
                get("/domain/isWhitelisted/domain.com/")
                        .with(request1 -> {
                                    request1.setUserPrincipal(authentication);
                                    return request1;
                                }
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(identityService, times(1)).isWhitelistedDomain(eq("domain.com"));
    }

    @Test
    public void shouldReturnFalseIfExceptionThrown() throws Exception {
        when(identityService.isWhitelistedDomain(anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(
                get("/domain/isWhitelisted/domain.com/")
                        .with(request1 -> {
                                    request1.setUserPrincipal(authentication);
                                    return request1;
                                }
                        ))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("false"));

        verify(identityService, times(1)).isWhitelistedDomain(eq("domain.com"));
    }

    private void prepareAuthentication() {
        Identity identity = createIdentity();
        IdentityDetails identityDetails = new IdentityDetails(identity);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(identityDetails, identityDetails.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private Identity createIdentity() {
        return new Identity(UID, EMAIL, PASSWORD, true, false, null, Instant.now(), false, false);
    }

}
