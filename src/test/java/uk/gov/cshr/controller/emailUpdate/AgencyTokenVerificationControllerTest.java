package uk.gov.cshr.controller.emailUpdate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.controller.account.AgencyTokenVerificationController;
import uk.gov.cshr.controller.form.VerifyTokenForm;
import uk.gov.cshr.domain.*;
import uk.gov.cshr.exception.NotEnoughSpaceAvailableException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.CsrfRequestPostProcessor;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class AgencyTokenVerificationControllerTest {

    private static final String ACCOUNT_REACTIVATE_UPDATED = "/account/reactivate/updated";
    private static final String VERIFY_TOKEN_FORM = "verifyTokenForm";
    private static final String VERIFY_TOKEN_TEMPLATE = "verifyToken";
    private static final String VERIFY_TOKEN_URL = "/account/verify/agency/";
    private static final String REDIRECT_EMAIL_UPDATED = "/account/email/updated";
    private static final String LOGIN_URL = "/login";
    private static final String CODE = "7haQOIeV5n0CYk7yrfEmxzxHQtbuV5PPPN8BgCTM";
    private static final String DOMAIN = "example.com";
    private static final String TOKEN = "DOI1KFJD5D";
    private static final String IDENTITY_UID = "a9cc9b0c-d257-4fa6-a760-950c09143e37";
    private static final String ORGANISATION = "co";
    private static final String AGENCY_TOKEN_UID = "675fd21d-03f9-4922-a2ff-c19186270b04";
    private static final String EMAIL = "test@example.com";
    private static final String ERROR_TEXT = "There was a problem with this agency token, please try again later";
    private static final String NO_SPACE_AVAIL_TEXT = "No spaces available for this token. Please contact your line manager";
    private static final String INCORRECT_ORG_TOKEN_TEXT = "Incorrect organisation or token";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailUpdateService emailUpdateService;

    @MockBean
    private AgencyTokenCapacityService agencyTokenCapacityService;

    @MockBean
    private VerificationCodeDeterminationService verificationCodeDeterminationService;

    @MockBean
    private ReactivationService reactivationService;

    @MockBean
    private CsrsService csrsService;

    @MockBean
    private IdentityService identityService;

    private AgencyTokenVerificationController agencyTokenVerificationController;

    private OrganisationalUnitDto[] organisations;

    @Before
    public void setup() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");

        agencyTokenVerificationController = new AgencyTokenVerificationController(
                emailUpdateService,
                csrsService,
                agencyTokenCapacityService,
                identityService,
                verificationCodeDeterminationService,
                reactivationService);

        organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();
        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);

        Authentication authentication = mock(Authentication.class);

        Identity identity = new Identity(IDENTITY_UID, null, null, true, false, null, null, false, true);
        IdentityDetails identityDetails = new IdentityDetails(identity);
        when(authentication.getPrincipal()).thenReturn(identityDetails);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void givenARequestToDisplayVerifyTokenPage_whenVerifyToken_thenShouldDisplayVerifyTokenPageWithAllPossibleOrganisations() throws Exception {
        mockMvc.perform(
                get(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr("uid", IDENTITY_UID)
                        .flashAttr("email", EMAIL)
                        .flashAttr("domain", DOMAIN))
                .andExpect(status().isOk())
                .andExpect(model().size(6))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("uid", IDENTITY_UID))
                .andExpect(model().attribute("code", CODE))
                .andExpect(model().attributeExists(VERIFY_TOKEN_FORM))
                .andExpect(view().name(VERIFY_TOKEN_TEMPLATE));
    }

    @Test
    public void givenARequestToDisplayVerifyTokenPageAndFormAlreadyExistsInModel_whenVerifyToken_thenShouldDisplayVerifyTokenPageWithAllPossibleOrganisationsAndTheExistingForm() throws Exception {
        VerifyTokenForm existingForm = new VerifyTokenForm();
        existingForm.setUid(IDENTITY_UID);
        existingForm.setOrganisation(ORGANISATION);
        existingForm.setToken(TOKEN);

        mockMvc.perform(
                get(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr(VERIFY_TOKEN_FORM, existingForm)
                        .flashAttr("uid", IDENTITY_UID)
                        .flashAttr("domain", DOMAIN))
                .andExpect(status().isOk())
                .andExpect(model().size(5))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("uid", IDENTITY_UID))
                .andExpect(model().attribute("code", CODE))
                .andExpect(model().attributeExists(VERIFY_TOKEN_FORM))
                .andExpect(view().name(VERIFY_TOKEN_TEMPLATE));
    }

    @Test
    public void shouldUpdateEmail() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        when(identityService.getDomainFromEmailAddress(EMAIL)).thenReturn(DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(true);

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(EMAIL);
        Identity identity = new Identity();
        identity.setEmail(EMAIL);

        VerificationCodeDetermination verificationCodeDetermination = new VerificationCodeDetermination(EMAIL, VerificationCodeType.EMAIL_UPDATE);
        when(verificationCodeDeterminationService.getCodeType(CODE)).thenReturn(verificationCodeDetermination);
        when(emailUpdateService.getEmailUpdateByCode(CODE)).thenReturn(emailUpdate);
        doNothing().when(emailUpdateService).updateEmailAddress(emailUpdate, agencyToken);

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation", ORGANISATION)
                        .param("token", TOKEN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(REDIRECT_EMAIL_UPDATED));
    }

    @Test
    public void shouldUpdateReactivation() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        when(identityService.getDomainFromEmailAddress(EMAIL)).thenReturn(DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(true);

        Reactivation reactivation = new Reactivation();
        reactivation.setEmail(EMAIL);
        Identity identity = new Identity();
        identity.setEmail(EMAIL);

        VerificationCodeDetermination verificationCodeDetermination = new VerificationCodeDetermination(EMAIL, VerificationCodeType.REACTIVATION);
        when(verificationCodeDeterminationService.getCodeType(CODE)).thenReturn(verificationCodeDetermination);
        when(reactivationService.getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING)).thenReturn(reactivation);
        doNothing().when(reactivationService).reactivateIdentity(reactivation);

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation", ORGANISATION)
                        .param("token", TOKEN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ACCOUNT_REACTIVATE_UPDATED));
    }

    @Test
    public void givenAValidTokenForm_whenCheckTokenAndNoSpacesAvailable_thenShouldRedirect() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        when(identityService.getDomainFromEmailAddress(EMAIL)).thenReturn(DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(false);

        EmailUpdate emailUpdate = new EmailUpdate();
        VerificationCodeDetermination verificationCodeDetermination = new VerificationCodeDetermination(EMAIL, VerificationCodeType.EMAIL_UPDATE);
        when(verificationCodeDeterminationService.getCodeType(CODE)).thenReturn(verificationCodeDetermination);
        when(emailUpdateService.getEmailUpdateByCode(CODE)).thenReturn(emailUpdate);

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation", ORGANISATION)
                        .param("token", TOKEN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("status", NO_SPACE_AVAIL_TEXT))
                .andExpect(redirectedUrl(VERIFY_TOKEN_URL + CODE));
    }

    @Test
    public void shouldRedirectToVerifyTokenIfResourceNotFound() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        when(identityService.getDomainFromEmailAddress(EMAIL)).thenReturn(DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(true);

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(EMAIL);

        VerificationCodeDetermination verificationCodeDetermination = new VerificationCodeDetermination(EMAIL, VerificationCodeType.EMAIL_UPDATE);
        when(verificationCodeDeterminationService.getCodeType(CODE)).thenReturn(verificationCodeDetermination);
        when(emailUpdateService.getEmailUpdateByCode(CODE)).thenReturn(emailUpdate);
        when(emailUpdateService.getEmailUpdateByCode(CODE)).thenReturn(emailUpdate);

        doThrow(new ResourceNotFoundException()).when(emailUpdateService).updateEmailAddress(any(EmailUpdate.class), any(AgencyToken.class));

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation", ORGANISATION)
                        .param("token", TOKEN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("status", INCORRECT_ORG_TOKEN_TEXT))
                .andExpect(redirectedUrl(VERIFY_TOKEN_URL + CODE));
    }

    @Test
    public void shouldRedirectToVerifyTokenIfNoSpaceAvailable() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        VerificationCodeDetermination verificationCodeDetermination = new VerificationCodeDetermination(EMAIL, VerificationCodeType.EMAIL_UPDATE);
        when(identityService.getDomainFromEmailAddress(EMAIL)).thenReturn(DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(false);
        when(verificationCodeDeterminationService.getCodeType(CODE)).thenReturn(verificationCodeDetermination);
        when(emailUpdateService.getEmailUpdateByCode(CODE)).thenThrow(new NotEnoughSpaceAvailableException("No space available"));

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation", ORGANISATION)
                        .param("token", TOKEN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("status", NO_SPACE_AVAIL_TEXT))
                .andExpect(redirectedUrl(VERIFY_TOKEN_URL + CODE));
    }

    @Test
    public void shouldRedirectToLoginIfExceptionOccurs() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        when(identityService.getDomainFromEmailAddress(EMAIL)).thenReturn(DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(true);

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(EMAIL);

        VerificationCodeDetermination verificationCodeDetermination = new VerificationCodeDetermination(EMAIL, VerificationCodeType.EMAIL_UPDATE);
        when(verificationCodeDeterminationService.getCodeType(CODE)).thenReturn(verificationCodeDetermination);
        when(emailUpdateService.getEmailUpdateByCode(CODE)).thenReturn(emailUpdate);
        doThrow(new NullPointerException()).when(emailUpdateService).updateEmailAddress(any(EmailUpdate.class), any(AgencyToken.class));

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("organisation", ORGANISATION)
                        .param("token", TOKEN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("status", ERROR_TEXT))
                .andExpect(redirectedUrl(LOGIN_URL));
    }

    @Test
    public void shouldBuildGenericErrorModelIfErrorOccurs() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(AGENCY_TOKEN_UID);

        VerificationCodeDetermination verificationCodeDetermination = new VerificationCodeDetermination(EMAIL, VerificationCodeType.EMAIL_UPDATE);
        when(identityService.getDomainFromEmailAddress(EMAIL)).thenReturn(DOMAIN);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(DOMAIN, TOKEN, ORGANISATION)).thenReturn(Optional.of(agencyToken));
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(false);
        when(verificationCodeDeterminationService.getCodeType(CODE)).thenReturn(verificationCodeDetermination);
        when(emailUpdateService.getEmailUpdateByCode(CODE)).thenThrow(new NotEnoughSpaceAvailableException("No space available"));

        mockMvc.perform(
                post(VERIFY_TOKEN_URL + CODE)
                        .with(CsrfRequestPostProcessor.csrf())
                        .param("token", TOKEN)
                        .param("uid", IDENTITY_UID)
        )
                .andExpect(flash().attribute("status", INCORRECT_ORG_TOKEN_TEXT))
                .andExpect(model().attributeExists("verifyTokenForm"))
                .andExpect(model().attribute("code", CODE))
                .andExpect(view().name(VERIFY_TOKEN_TEMPLATE));
    }
}
