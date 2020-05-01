package uk.gov.cshr.controller;

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
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.CsrfRequestPostProcessor;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {

    private static final String NORMAL_TEST_USER_UID = "uid";

    private static final String UPDATE_EMAIL_FORM_TEMPLATE = "updateEmailForm";

    private static final String UPDATE_EMAIL_VIEW_NAME_TEMPLATE = "account/updateEmail";

    private static final String EMAIL_URL = "/account/email";

    private static final String VERIFY_EMAIL_URL = "/account/email/verify/1234567891234567";

    private static final String EXPECTED_CODE = "1234567891234567";

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

    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    private AccountController classUnderTest;

    @Before
    public void setup() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );

        classUnderTest = new AccountController(
                identityService,
                emailUpdateService,
                lpgUiUrl);
    }

    @Test
    public void givenARequestToChangeYourEmail_whenUpdateEmailForm_shouldDisplayForm() throws Exception {
        mockMvc.perform(
                get(EMAIL_URL)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(UPDATE_EMAIL_FORM_TEMPLATE))
                .andExpect(view().name(UPDATE_EMAIL_VIEW_NAME_TEMPLATE))
                .andDo(print());
    }

    @Test
    public void givenAnEmptyForm_whenSendEmailVerification_shouldDisplayFieldValidationErrors() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);

        // when
        mockMvc.perform(post(EMAIL_URL)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email","")
                .param("confirm","")
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

        // then
        // error is displayed on form and no service logic is executed
        verifyZeroInteractions(identityService);
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenAnInvalidForm_whenSendEmailVerification_shouldDisplayFieldValidationErrors() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);

        // when
        mockMvc.perform(post(EMAIL_URL)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email","someone")
                .param("confirm","someone")
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

        // then
        // error is displayed on form and no service logic is executed
        verifyZeroInteractions(identityService);
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenNonMatchingForm_whenSendEmailVerification_shouldDisplayFieldValidationErrors() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);

        // when
        mockMvc.perform(post(EMAIL_URL)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email","basic@domain.com")
                .param("confirm","someone")
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

        // then
        // error is displayed on form and no service logic is executed
        verifyZeroInteractions(identityService);
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenAValidFormAndAnEmailThatAlreadyExists_whenSendEmailVerification_shouldDisplayEmailAlreadyExistsError() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);
        when(identityService.checkEmailExists(anyString())).thenReturn(true);

        // when
        mockMvc.perform(post(EMAIL_URL)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email","basic@domain.com")
                .param("confirm","basic@domain.com")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?emailAlreadyTaken=true"));

        // then
        // server side error is displayed on form
        verify(identityService, times(1)).checkEmailExists(eq("basic@domain.com"));
        verify(identityService, never()).checkValidEmail(anyString());
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenAValidFormAndAnEmailThatIsNotValid_whenSendEmailVerification_shouldDisplayUnableToUseThisServiceError() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);
        when(identityService.checkEmailExists(anyString())).thenReturn(false);
        when(identityService.checkValidEmail(anyString())).thenReturn(false);

        // when
        mockMvc.perform(post(EMAIL_URL)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email","basic@domain.com")
                .param("confirm","basic@domain.com")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?notValidEmailDomain=true"));

        // then
        // server side error is displayed on form
        verify(identityService, times(1)).checkEmailExists(eq("basic@domain.com"));
        verify(identityService, times(1)).checkValidEmail(eq("basic@domain.com"));
        verifyZeroInteractions(emailUpdateService);
    }

    @Test
    public void givenAValidFormAndEmailDoesntAlreadyExistAndIsAValidEmail_whenSendEmailVerification_shouldDisplayEmailVerificationSentScreen() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);
        when(identityService.checkEmailExists(anyString())).thenReturn(false);
        when(identityService.checkValidEmail(anyString())).thenReturn(true);

        // when
        mockMvc.perform(post(EMAIL_URL)
                .with(CsrfRequestPostProcessor.csrf())
                .param("email","basic@domain.com")
                .param("confirm","basic@domain.com")
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/emailVerificationSent"));

        // then
        verify(identityService, times(1)).checkEmailExists(eq("basic@domain.com"));
        verify(identityService, times(1)).checkValidEmail(eq("basic@domain.com"));
        verify(emailUpdateService, times(1)).saveEmailUpdateAndNotify(identityArgumentCaptor.capture(), eq("basic@domain.com"));
        // check correct identity was processed
        Identity actualIdentityToUpdate = identityArgumentCaptor.getValue();
        assertThat(actualIdentityToUpdate.getEmail()).isEqualTo("basic@domain.com");
    }


    @Test
    public void givenAValidCode_whenUpdateEmail_shouldUpdateEmailAddressAndRedirectToEmailUpdatedPage() throws Exception {
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
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);
        when(emailUpdateService.verifyCode(any(Identity.class), anyString())).thenReturn(true);
        // expected data to be sent for updating
        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();
        String expectedUIDToBeUpdated = identity.getUid();
        String expectedEmailToBeUpdated = identity.getEmail();

        // when
        mockMvc.perform(get(VERIFY_EMAIL_URL)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/emailUpdated"))
                .andDo(print());

        // then
        verify(emailUpdateService, times(1)).updateEmailAddress(identityArgumentCaptor.capture(), eq(EXPECTED_CODE));
        Identity actualIdentityToUpdate = identityArgumentCaptor.getValue();
        assertThat(actualIdentityToUpdate.getUid()).isEqualTo(expectedUIDToBeUpdated);
        assertThat(actualIdentityToUpdate.getEmail()).isEqualTo(expectedEmailToBeUpdated);
    }

    @Test
    public void givenAInvalidCode_whenUpdateEmail_shouldRedirectToUpdateEmailPageWithAnInvalidCodeError() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);
        when(emailUpdateService.verifyCode(any(Identity.class), anyString())).thenReturn(false);

        // when
        mockMvc.perform(get(VERIFY_EMAIL_URL)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?invalidCode=true"))
                .andDo(print());

        // then
        verify(emailUpdateService, never()).updateEmailAddress(any(Identity.class), anyString());
    }

    @Test
    public void givenAValidCodeAndNonExistentIdentity_whenUpdateEmail_shouldRedirectToUpdateEmailPageWithAnInvalidEmailError() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);
        when(emailUpdateService.verifyCode(any(Identity.class), anyString())).thenReturn(true);
        doThrow(new ResourceNotFoundException()).when(emailUpdateService).updateEmailAddress(any(Identity.class), anyString());
        // expected data to be sent for updating
        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();
        String expectedUIDToBeUpdated = identity.getUid();
        String expectedEmailToBeUpdated = identity.getEmail();

        // when
        mockMvc.perform(get(VERIFY_EMAIL_URL)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?invalidEmail=true"))
                .andDo(print());

        // then
        verify(emailUpdateService, times(1)).updateEmailAddress(identityArgumentCaptor.capture(), eq(EXPECTED_CODE));
        Identity actualIdentityToUpdate = identityArgumentCaptor.getValue();
        assertThat(actualIdentityToUpdate.getUid()).isEqualTo(expectedUIDToBeUpdated);
        assertThat(actualIdentityToUpdate.getEmail()).isEqualTo(expectedEmailToBeUpdated);
    }

    @Test
    public void givenAValidCodeAndATechnicalErrorWhenUpdating_whenUpdateEmail_shouldRedirectToUpdateEmailPageWithAnErrorOccurredError() throws Exception {
        // given
        Authentication authentication = prepareAuthentication(NORMAL_TEST_USER_UID);
        when(emailUpdateService.verifyCode(any(Identity.class), anyString())).thenReturn(true);
        doThrow(new RuntimeException()).when(emailUpdateService).updateEmailAddress(any(Identity.class), anyString());
        // expected data to be sent for updating
        IdentityDetails identityDetails = (IdentityDetails) authentication.getPrincipal();
        Identity identity = identityDetails.getIdentity();
        String expectedUIDToBeUpdated = identity.getUid();
        String expectedEmailToBeUpdated = identity.getEmail();

        // when
        mockMvc.perform(get(VERIFY_EMAIL_URL)
                .with(request1 -> {
                            request1.setUserPrincipal(authentication);
                            return request1;
                        }
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/email?errorOccurred=true"))
                .andDo(print());

        // then
        verify(emailUpdateService, times(1)).updateEmailAddress(identityArgumentCaptor.capture(), eq(EXPECTED_CODE));
        Identity actualIdentityToUpdate = identityArgumentCaptor.getValue();
        assertThat(actualIdentityToUpdate.getUid()).isEqualTo(expectedUIDToBeUpdated);
        assertThat(actualIdentityToUpdate.getEmail()).isEqualTo(expectedEmailToBeUpdated);
    }

    @Test
    public void givenASuccessfulUpdateOfEmailAddress_whenEmailUpdated_shouldRedirectToEmailUpdatedView() throws Exception {
        // when
        mockMvc.perform(get("/account/emailUpdated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(lpgUiUrl+ "/sign-out"));
    }

    private Authentication prepareAuthentication(String userNameToAuthenticateWith) {
        IdentityDetails identityDetails = (IdentityDetails) userDetailsService.loadUserByUsername(userNameToAuthenticateWith);
        return new UsernamePasswordAuthenticationToken(identityDetails, identityDetails.getPassword(), identityDetails.getAuthorities());
    }

}