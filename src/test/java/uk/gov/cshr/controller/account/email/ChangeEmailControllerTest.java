package uk.gov.cshr.controller.account.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.AgencyTokenService;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;
import uk.gov.cshr.utils.CsrfRequestPostProcessor;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class ChangeEmailControllerTest {

    private static final String UPDATE_EMAIL_FORM_TEMPLATE = "updateEmailForm";
    private static final String UPDATE_EMAIL_VIEW_NAME_TEMPLATE = "account/updateEmail";

    private static final String EMAIL_PATH = "/account/email";
    private static final String VERIFY_EMAIL_PATH = "/account/email/verify/";
    private static final String VERIFY_EMAIL_AGENCY_PATH = "/account/verify/agency/";

    private static final String UID = "uid";
    private static final String VERIFY_CODE = "ZBnX9unEnnOcgMmCJ6rI3H2LUQFs2xsiMNj2Ejou";
    private static final String DOMAIN = "example.com";

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private EmailUpdateService emailUpdateService;

    @MockBean
    private AgencyTokenService agencyTokenService;

    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    private ChangeEmailController changeEmailController;

    @Before
    public void setup() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");

        changeEmailController = new ChangeEmailController(
                identityService,
                emailUpdateService,
                agencyTokenService,
                lpgUiUrl);
    }

    @Test
    public void givenARequestToChangeYourEmail_whenUpdateEmailForm_shouldDisplayForm() throws Exception {
        mockMvc.perform(
                get(EMAIL_PATH)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(UPDATE_EMAIL_FORM_TEMPLATE))
                .andExpect(view().name(UPDATE_EMAIL_VIEW_NAME_TEMPLATE))
                .andDo(print());
    }

    @Test
    public void givenAnEmptyForm_whenSendEmailVerification_shouldDisplayFieldValidationErrors() throws Exception {
        Authentication authentication = prepareAuthentication(UID);

        mockMvc.perform(post(EMAIL_PATH)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email", "")
                .param("confirm", "")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().errorCount(2))
                .andExpect(model().attributeHasFieldErrorCode(UPDATE_EMAIL_FORM_TEMPLATE, "email", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode(UPDATE_EMAIL_FORM_TEMPLATE, "confirm", "NotBlank"))
                .andExpect(model().attributeExists(UPDATE_EMAIL_FORM_TEMPLATE))
                .andExpect(view().name(UPDATE_EMAIL_VIEW_NAME_TEMPLATE));

        verifyZeroInteractions(identityService);
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenAnInvalidForm_whenSendEmailVerification_shouldDisplayFieldValidationErrors() throws Exception {
        Authentication authentication = prepareAuthentication(UID);

        mockMvc.perform(post(EMAIL_PATH)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email", "someone")
                .param("confirm", "someone")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeExists(UPDATE_EMAIL_FORM_TEMPLATE))
                .andExpect(view().name(UPDATE_EMAIL_VIEW_NAME_TEMPLATE));

        verifyZeroInteractions(identityService);
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenNonMatchingForm_whenSendEmailVerification_shouldDisplayFieldValidationErrors() throws Exception {
        Authentication authentication = prepareAuthentication(UID);

        mockMvc.perform(post(EMAIL_PATH)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email", "basic@domain.com")
                .param("confirm", "someone")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeExists(UPDATE_EMAIL_FORM_TEMPLATE))
                .andExpect(view().name(UPDATE_EMAIL_VIEW_NAME_TEMPLATE));

        verifyZeroInteractions(identityService);
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenAValidFormAndAnEmailThatAlreadyExists_whenSendEmailVerification_shouldDisplayEmailAlreadyExistsError() throws Exception {
        Authentication authentication = prepareAuthentication(UID);
        when(identityService.checkEmailExists(anyString())).thenReturn(true);

        mockMvc.perform(post(EMAIL_PATH)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email", "basic@domain.com")
                .param("confirm", "basic@domain.com")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?emailAlreadyTaken=true"));

        verify(identityService, times(1)).checkEmailExists(eq("basic@domain.com"));
        verify(identityService, never()).checkValidEmail(anyString());
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenAValidFormAndAnEmailThatIsNotValid_whenSendEmailVerification_shouldDisplayUnableToUseThisServiceError() throws Exception {
        Authentication authentication = prepareAuthentication(UID);
        when(identityService.checkEmailExists(anyString())).thenReturn(false);

        mockMvc.perform(post(EMAIL_PATH)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email", "basic@domain.com")
                .param("confirm", "basic@domain.com")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?notValidEmailDomain=true"));

        verify(identityService, times(1)).checkEmailExists(eq("basic@domain.com"));
        verify(identityService, times(1)).checkValidEmail(eq("basic@domain.com"));
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenAValidFormAndEmailDoesntAlreadyExistAndIsAValidEmail_whenSendEmailVerification_shouldDisplayEmailVerificationSentScreen() throws Exception {
        Authentication authentication = prepareAuthentication(UID);
        when(identityService.checkEmailExists(anyString())).thenReturn(false);
        when(identityService.checkValidEmail(anyString())).thenReturn(true);

        mockMvc.perform(post(EMAIL_PATH)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email", "basic@domain.com")
                .param("confirm", "basic@domain.com")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/emailVerificationSent"));

        verify(identityService, times(1)).checkEmailExists(eq("basic@domain.com"));
        verify(identityService, times(1)).checkValidEmail(eq("basic@domain.com"));
        verify(emailUpdateService, times(1)).saveEmailUpdateAndNotify(identityArgumentCaptor.capture(), eq("basic@domain.com"));

        Identity actualIdentityToUpdate = identityArgumentCaptor.getValue();
        assertThat(actualIdentityToUpdate.getEmail()).isEqualTo("basic@domain.com");
    }


    @Test
    public void shouldRedirectToEmailUpdateIfNewEmailIsWhitelistedButNotAgency() throws Exception {
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

        Authentication authentication = prepareAuthentication(UID);

        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();
        String expectedUIDToBeUpdated = identity.getUid();
        String expectedEmailToBeUpdated = identity.getEmail();

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setCode(VERIFY_CODE);
        emailUpdate.setEmail(identity.getEmail());

        when(emailUpdateService.existsByCode(VERIFY_CODE)).thenReturn(true);
        when(emailUpdateService.getEmailUpdateByCode(VERIFY_CODE)).thenReturn(emailUpdate);
        when(identityService.getDomainFromEmailAddress(identity.getEmail())).thenReturn(DOMAIN);

        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(true);
        when(agencyTokenService.isDomainInAgencyToken(DOMAIN)).thenReturn(false);

        doNothing().when(emailUpdateService).updateEmailAddress(eq(emailUpdate));

        mockMvc.perform(get(VERIFY_EMAIL_PATH + VERIFY_CODE)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email/updated"))
                .andDo(print());
    }

    @Test
    public void shouldRedirectToEmailUpdateIfNewEmailIsNotWhitelistedButIsAgency() throws Exception {
        Authentication authentication = prepareAuthentication(UID);

        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setCode(VERIFY_CODE);
        emailUpdate.setEmail(identity.getEmail());

        when(emailUpdateService.existsByCode(VERIFY_CODE)).thenReturn(true);
        when(emailUpdateService.getEmailUpdateByCode(VERIFY_CODE)).thenReturn(emailUpdate);
        when(identityService.getDomainFromEmailAddress(identity.getEmail())).thenReturn(DOMAIN);

        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(false);
        when(agencyTokenService.isDomainInAgencyToken(DOMAIN)).thenReturn(true);

        doNothing().when(emailUpdateService).updateEmailAddress(eq(emailUpdate));

        mockMvc.perform(get(VERIFY_EMAIL_PATH + VERIFY_CODE)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("email", identity.getEmail()))
                .andExpect(redirectedUrl(VERIFY_EMAIL_AGENCY_PATH + VERIFY_CODE))
                .andDo(print());
    }

    @Test
    public void shouldRedirectToErrorOccuredIfNewEmailIsNotWhitelistedAndNotAgency() throws Exception {
        Authentication authentication = prepareAuthentication(UID);

        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setCode(VERIFY_CODE);
        emailUpdate.setEmail(identity.getEmail());

        when(emailUpdateService.existsByCode(VERIFY_CODE)).thenReturn(true);
        when(emailUpdateService.getEmailUpdateByCode(VERIFY_CODE)).thenReturn(emailUpdate);
        when(identityService.getDomainFromEmailAddress(identity.getEmail())).thenReturn(DOMAIN);

        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(false);
        when(agencyTokenService.isDomainInAgencyToken(DOMAIN)).thenReturn(false);

        mockMvc.perform(get(VERIFY_EMAIL_PATH + VERIFY_CODE)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.CHANGE_EMAIL_ERROR_MESSAGE))
                .andExpect(redirectedUrl("/login"))
                .andDo(print());
    }


    @Test
    public void givenAInvalidCode_whenUpdateEmail_shouldRedirectToUpdateEmailPageWithAnInvalidCodeError() throws Exception {
        Authentication authentication = prepareAuthentication(UID);
        when(emailUpdateService.existsByCode(anyString())).thenReturn(false);

        mockMvc.perform(get(VERIFY_EMAIL_PATH + VERIFY_CODE)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?invalidCode=true"))
                .andDo(print());

        verify(emailUpdateService, never()).updateEmailAddress(any(EmailUpdate.class));
    }

    @Test
    public void givenAValidCodeAndNonExistentIdentity_whenUpdateEmail_shouldRedirectToUpdateEmailPageWithAnInvalidEmailError() throws Exception {
        Authentication authentication = prepareAuthentication(UID);

        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();
        String expectedUIDToBeUpdated = identity.getUid();
        String expectedEmailToBeUpdated = identity.getEmail();

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setCode(VERIFY_CODE);
        emailUpdate.setEmail(identity.getEmail());

        when(emailUpdateService.existsByCode(VERIFY_CODE)).thenReturn(true);
        when(emailUpdateService.getEmailUpdateByCode(VERIFY_CODE)).thenReturn(emailUpdate);
        when(identityService.getDomainFromEmailAddress(identity.getEmail())).thenReturn(DOMAIN);

        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(true);
        when(agencyTokenService.isDomainInAgencyToken(DOMAIN)).thenReturn(true);

        doThrow(new ResourceNotFoundException()).when(emailUpdateService).updateEmailAddress(any(EmailUpdate.class));

        mockMvc.perform(get(VERIFY_EMAIL_PATH + VERIFY_CODE)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?invalidEmail=true"))
                .andDo(print());

        verify(emailUpdateService, times(1)).updateEmailAddress(eq(emailUpdate));
    }

    @Test
    public void givenAValidCodeAndATechnicalErrorWhenUpdating_whenUpdateEmail_shouldRedirectToUpdateEmailPageWithAnErrorOccurredError() throws Exception {
        Authentication authentication = prepareAuthentication(UID);

        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();
        String expectedUIDToBeUpdated = identity.getUid();
        String expectedEmailToBeUpdated = identity.getEmail();

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setCode(VERIFY_CODE);
        emailUpdate.setEmail(identity.getEmail());

        when(emailUpdateService.existsByCode(VERIFY_CODE)).thenReturn(true);
        when(emailUpdateService.getEmailUpdateByCode(VERIFY_CODE)).thenReturn(emailUpdate);
        when(identityService.getDomainFromEmailAddress(identity.getEmail())).thenReturn(DOMAIN);

        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(true);
        when(agencyTokenService.isDomainInAgencyToken(DOMAIN)).thenReturn(true);

        doThrow(new RuntimeException()).when(emailUpdateService).updateEmailAddress(any(EmailUpdate.class));

        mockMvc.perform(get(VERIFY_EMAIL_PATH + VERIFY_CODE)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.CHANGE_EMAIL_ERROR_MESSAGE))
                .andExpect(redirectedUrl("/login"))
                .andDo(print());

        verify(emailUpdateService, times(1)).updateEmailAddress(eq(emailUpdate));
    }

    @Test
    public void givenASuccessfulUpdateOfEmailAddress_whenEmailUpdated_shouldRedirectToEmailUpdatedView() throws Exception {
        mockMvc.perform(get("/account/email/updated"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/emailUpdated"));
    }

    private Authentication prepareAuthentication(String userNameToAuthenticateWith) {
        IdentityDetails identityDetails = (IdentityDetails) userDetailsService.loadUserByUsername(userNameToAuthenticateWith);
        return new UsernamePasswordAuthenticationToken(identityDetails, identityDetails.getPassword(), identityDetails.getAuthorities());
    }
}
