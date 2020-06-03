package uk.gov.cshr.controller.account.reactivation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.AgencyTokenService;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class ReactivationControllerTest {

    private static final String CODE = "abc123";
    private static final String EMAIL_ADDRESS = "test@example.com";
    private static final String DOMAIN = "example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReactivationService reactivationService;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private AgencyTokenService agencyTokenService;

    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");
    }

    @Test
    public void shouldRedirectIfAccountIsAgency() throws Exception {
        Reactivation reactivation = new Reactivation();
        reactivation.setEmail(EMAIL_ADDRESS);

        when(reactivationService.getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING)).thenReturn(reactivation);
        when(identityService.getDomainFromEmailAddress(EMAIL_ADDRESS)).thenReturn(DOMAIN);
        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(false);
        when(agencyTokenService.isDomainInAgencyToken(DOMAIN)).thenReturn(true);

        doNothing().when(reactivationService).reactivateIdentity(reactivation);

        mockMvc.perform(
                get("/account/reactivate/" + CODE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/verify/agency/" + CODE));
    }

    @Test
    public void shouldReactivateAccount() throws Exception {
        Reactivation reactivation = new Reactivation();
        reactivation.setEmail(EMAIL_ADDRESS);

        when(reactivationService.getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING)).thenReturn(reactivation);
        when(identityService.getDomainFromEmailAddress(EMAIL_ADDRESS)).thenReturn(DOMAIN);
        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(true);

        doNothing().when(reactivationService).reactivateIdentity(reactivation);

        mockMvc.perform(
                get("/account/reactivate/" + CODE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/reactivate/updated"));
    }

    @Test
    public void shouldRedirectToLoginIfReactivationNotFound() throws Exception {
        doThrow(new ResourceNotFoundException()).when(reactivationService).getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING);

        mockMvc.perform(
                get("/account/reactivate/" + CODE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(ApplicationConstants.STATUS_ATTRIBUTE, "Reactivation code is not valid"));
    }

    @Test
    public void shouldRedirectToLoginIfTechnicalExceptionOccurs() throws Exception {
        doThrow(new RuntimeException()).when(reactivationService).getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING);

        mockMvc.perform(
                get("/account/reactivate/" + CODE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(ApplicationConstants.STATUS_ATTRIBUTE, "There was an error processing account reactivation. Please try again later."));
    }

    @Test
    public void shouldGetAccountReactivatedTemplate() throws Exception {
        mockMvc.perform(get("/account/reactivate/updated"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/accountReactivated"));
    }
}
