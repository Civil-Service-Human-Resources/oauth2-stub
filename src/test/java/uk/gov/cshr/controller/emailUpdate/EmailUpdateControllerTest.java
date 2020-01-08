package uk.gov.cshr.controller.emailUpdate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.controller.form.EmailUpdatedRecentlyEnterTokenForm;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.utils.CsrfRequestPostProcessor;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import javax.servlet.Filter;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmailUpdateControllerTest {

    private static final String ENTER_TOKEN_URL = "/emailUpdated/enterToken";

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailUpdateService emailUpdateService;

    @MockBean
    private CsrsService csrsService;

    @MockBean(name="identityRepository")
    private IdentityRepository identityRepository;

    private OrganisationalUnitDto[] organisations;

    @Before
    public void setup() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );

        // set up organisations list for all test scenarios
        organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();
        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);
    }

    @Test
    public void givenARequestToDisplayEnterTokenPage_whenEnterToken_thenShouldDisplayEnterTokenPageWithAllPossibleOrganisations() throws Exception {
        // only called with 2 flash attributes, from redirect controller.
        mockMvc.perform(
                get(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr("uid", "myuid")
                        .flashAttr("domain", "mydomain"))
                .andExpect(status().isOk())
                .andExpect(model().size(4))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", "mydomain"))
                .andExpect(model().attribute("uid", "myuid"))
                .andExpect(model().attributeExists("emailUpdatedRecentlyEnterTokenForm"))
                .andExpect(view().name("enterTokenSinceEmailUpdate"))
                .andDo(print());
    }

    @Test
    public void givenARequestToDisplayEnterTokenPageAndFormAlreadyExistsInModel_whenEnterToken_thenShouldDisplayEnterTokenPageWithAllPossibleOrganisationsAndTheExistingForm() throws Exception {
        // given
        EmailUpdatedRecentlyEnterTokenForm existingForm = new EmailUpdatedRecentlyEnterTokenForm();
        existingForm.setUid("myuid");
        existingForm.setDomain("mydomain");
        existingForm.setOrganisation("myorganisation");
        existingForm.setToken("mytoken");

        mockMvc.perform(
                get(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr("emailUpdatedRecentlyEnterTokenForm", existingForm)
                        .flashAttr("uid", "myuid")
                        .flashAttr("domain", "mydomain"))
                .andExpect(status().isOk())
                .andExpect(model().size(4))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", "mydomain"))
                .andExpect(model().attribute("uid", "myuid"))
                .andExpect(model().attributeExists("emailUpdatedRecentlyEnterTokenForm"))
                .andExpect(view().name("enterTokenSinceEmailUpdate"))
                .andDo(print());
    }

    @Test
    public void givenAValidTokenForm_whenCheckToken_thenShouldSubmitToken() throws Exception {
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);
        Identity identityFound = new Identity();
        when(identityRepository.findFirstByUid(eq("myuid"))).thenReturn(Optional.of(identityFound));
        doNothing().when(emailUpdateService).processEmailUpdatedRecentlyRequestForAgencyTokenUser(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","myorganisation")
                        .param("token","mytoken")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(lpgUiUrl));

        verify(emailUpdateService, times(1)).processEmailUpdatedRecentlyRequestForAgencyTokenUser(eq("mydomain"), eq("mytoken"), eq("myorganisation"), eq("myuid"));
    }

    @Test
    public void givenAInvalidTokenFormNoOrganisation_whenCheckToken_thenShouldRedisplayToEnterTokenPageWithOneErrorMessage() throws Exception {
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);
        Identity identityFound = new Identity();
        when(identityRepository.findFirstByUid(eq("myuid"))).thenReturn(Optional.of(identityFound));
        doNothing().when(emailUpdateService).processEmailUpdatedRecentlyRequestForAgencyTokenUser(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","")
                        .param("token","mytoken")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("emailUpdatedRecentlyEnterTokenForm"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("emailUpdatedRecentlyEnterTokenForm", "organisation", "Please confirm your new organisation"))
                .andExpect(view().name("enterTokenSinceEmailUpdate"));

        verify(emailUpdateService, never()).processEmailUpdatedRecentlyRequestForAgencyTokenUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void givenAInvalidTokenFormNoOrganisationAndNoToken_whenCheckToken_thenShouldRedisplayToEnterTokenPageWithTwoErrorMessages() throws Exception {
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);
        Identity identityFound = new Identity();
        when(identityRepository.findFirstByUid(eq("myuid"))).thenReturn(Optional.of(identityFound));
        doNothing().when(emailUpdateService).processEmailUpdatedRecentlyRequestForAgencyTokenUser(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","")
                        .param("token","")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("emailUpdatedRecentlyEnterTokenForm"))
                .andExpect(model().errorCount(2))
                .andExpect(model().attributeHasFieldErrorCode("emailUpdatedRecentlyEnterTokenForm", "organisation", "Please confirm your new organisation"))
                .andExpect(model().attributeHasFieldErrorCode("emailUpdatedRecentlyEnterTokenForm", "toke", "Please confirm your new token"))
                .andExpect(view().name("enterTokenSinceEmailUpdate"));

        verify(emailUpdateService, never()).processEmailUpdatedRecentlyRequestForAgencyTokenUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void givenAValidTokenFormAndNoIdentityFound_whenCheckToken_thenShouldRedirectToLoginPage() throws Exception {
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);
        when(identityRepository.findFirstByUid(eq("myuid"))).thenReturn(Optional.empty());

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","myorganisation")
                        .param("token","mytoken")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(emailUpdateService, never()).processEmailUpdatedRecentlyRequestForAgencyTokenUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void givenAValidTokenFormAndResourceNotFoundDuringEmailUpdate_whenCheckToken_thenShouldRedirectToEnterTokenPageWithErrorMessage() throws Exception {
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);
        Identity identityFound = new Identity();
        when(identityRepository.findFirstByUid(eq("myuid"))).thenReturn(Optional.of(identityFound));
        doThrow(new ResourceNotFoundException()).when(emailUpdateService).processEmailUpdatedRecentlyRequestForAgencyTokenUser(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(
                post(ENTER_TOKEN_URL)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation","myorganisation")
                        .param("token","mytoken")
                        .param("domain","mydomain")
                        .param("uid","myuid")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emailUpdated/enterToken"))
                .andExpect(flash().attribute("status", "Incorrect token for this organisation"))
                .andExpect(flash().attributeExists("emailUpdatedRecentlyEnterTokenForm"));

        verify(emailUpdateService, times(1)).processEmailUpdatedRecentlyRequestForAgencyTokenUser(eq("mydomain"), eq("mytoken"), eq("myorganisation"), eq("myuid"));
    }

}
