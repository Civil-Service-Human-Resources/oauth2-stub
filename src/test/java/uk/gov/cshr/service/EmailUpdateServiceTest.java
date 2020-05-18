package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.exception.IdentityNotFoundException;
import uk.gov.cshr.exception.InvalidCodeException;
import uk.gov.cshr.repository.EmailUpdateRepository;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@RunWith(SpringRunner.class)
public class EmailUpdateServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String CODE = "abc123";
    private static final String UID = "uid123";
    private static final Boolean ACTIVE = true;
    private static final Boolean LOCKED = false;
    private static final String PASSWORD = "password";
    private static final Set<Role> ROLES = new HashSet();

    private static Identity IDENTITY = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);

    private MockHttpServletRequest request;

    @MockBean
    private EmailUpdateRepository emailUpdateRepository;

    @MockBean
    private EmailUpdateFactory emailUpdateFactory;

    @MockBean
    private NotifyService notifyService;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private CsrsService csrsService;

    @Value("${govNotify.template.emailUpdate}")
    private String updateEmailTemplateId;

    @Value("${emailUpdate.urlFormat}")
    private String inviteUrlFormat;

    @Captor
    private ArgumentCaptor<EmailUpdate> emailUpdateArgumentCaptor;

    @Autowired
    private EmailUpdateService classUnderTest;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    public void givenAValidCodeForIdentity_whenVerifyCode_thenReturnsTrue() {
        // given
        when(emailUpdateRepository.findByIdentityAndCode(any(Identity.class), anyString())).thenReturn(Optional.of(new EmailUpdate()));

        // when
        boolean actual = classUnderTest.verifyCode(IDENTITY, "co");

        // then
        assertTrue(actual);
        verify(emailUpdateRepository, times(1)).findByIdentityAndCode(eq(IDENTITY), eq("co"));
    }

    @Test
    public void givenAInvalidCodeForIdentity_whenVerifyCode_thenReturnsFalse() {
        // given
        when(emailUpdateRepository.findByIdentityAndCode(any(Identity.class), anyString())).thenReturn(Optional.empty());

        // when
        boolean actual = classUnderTest.verifyCode(IDENTITY, "co");

        // then
        assertFalse(actual);
        verify(emailUpdateRepository, times(1)).findByIdentityAndCode(eq(IDENTITY), eq("co"));
    }

    @Test
    public void givenAValidIdentity_whenUpdateEmailAddress_shouldReturnSuccessfully(){
        // given
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setId(100l);
        emailUpdate.setEmail("myemail@mynewemail.com");
        Optional<EmailUpdate> optionalEmailUpdate = Optional.of(emailUpdate);
        when(emailUpdateRepository.findByIdentityAndCode(any(Identity.class), anyString())).thenReturn(optionalEmailUpdate);

        doNothing().when(identityService).updateEmailAddressAndEmailRecentlyUpdatedFlagToTrue(eq(IDENTITY), eq(emailUpdate.getEmail()));
        doNothing().when(emailUpdateRepository).delete(any(EmailUpdate.class));

        // when
        classUnderTest.updateEmailAddress(request, IDENTITY, "CO");

        // then
        verify(emailUpdateRepository, times(1)).findByIdentityAndCode(eq(IDENTITY), eq("CO"));
        verify(identityService, times(1)).updateEmailAddressAndEmailRecentlyUpdatedFlagToTrue(eq(IDENTITY), eq(emailUpdate.getEmail()));
        verify(identityService, times(1)).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), eq(true));
        verify(emailUpdateRepository, times(1)).delete(emailUpdateArgumentCaptor.capture());

        EmailUpdate actualDeletedEmailUpdate = emailUpdateArgumentCaptor.getValue();
        assertThat(actualDeletedEmailUpdate.getId(), equalTo(100l));
    }

    @Test(expected = InvalidCodeException.class)
    public void givenANonExistentEmailUpdate_whenUpdateEmailAddress_shouldThrowInvalidCodeException(){
        // given
        Optional<EmailUpdate> optionalEmailUpdate = Optional.empty();
        when(emailUpdateRepository.findByIdentityAndCode(any(Identity.class), anyString())).thenReturn(optionalEmailUpdate);

        // when
        classUnderTest.updateEmailAddress(request, IDENTITY, "CO");

        // then
        verify(emailUpdateRepository, times(1)).findByIdentityAndCode(eq(IDENTITY), eq("CO"));
        verify(identityService, never()).updateEmailAddressAndEmailRecentlyUpdatedFlagToTrue(any(Identity.class), anyString());
        verify(identityService, never()).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), anyBoolean());
        verify(emailUpdateRepository, never()).delete(any(EmailUpdate.class));
    }

    /*
        Normally, transactions in tests are flagged for rollback when they start. However, if the method has a @Commit annotation, they start flagged for commit instead:
     */
    @Commit
    @Test
    public void givenAValidUIDAndAWhitelistedUser_whenProcessEmailUpdatedRecentlyRequestForWhiteListedDomainUser_shouldReturnSuccessfullyAndCommitTransaction(){
        // given
        doNothing().when(identityService).resetRecentlyUpdatedEmailFlagToFalse(any(Identity.class));

        // when
        classUnderTest.processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(request, IDENTITY);

        // then
        verify(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        verify(identityService, times(1)).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), eq(false));
        assertTrue(TestTransaction.isActive());
        assertFalse(TestTransaction.isFlaggedForRollback());
    }

    @Test (expected = RuntimeException.class)
    public void givenAValidUIDAndAWhitelistedUserAndTechnicalErrorOccurs_whenProcessEmailUpdatedRecentlyRequestForWhiteListedDomainUser_shouldThrowExceptionAndRollbackTransaction(){
        // given
        doThrow(new RuntimeException()).when(identityService).resetRecentlyUpdatedEmailFlagToFalse(any(Identity.class));

        // when
        classUnderTest.processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(request, IDENTITY);

        // then
        verify(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        verify(identityService, never()).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), anyBoolean());
        assertTrue(TestTransaction.isActive());
        assertTrue(TestTransaction.isFlaggedForRollback());
    }

    @Test (expected = IdentityNotFoundException.class)
    public void givenAValidUIDAndAWhitelistedUserAndIdentityNotFoundErrorOccurs_whenProcessEmailUpdatedRecentlyRequestForWhiteListedDomainUser_shouldThrowIdentityNotFoundExceptionAndRollbackTransaction(){
        // given
        doThrow(new IdentityNotFoundException("abc")).when(identityService).resetRecentlyUpdatedEmailFlagToFalse(any(Identity.class));

        // when
        classUnderTest.processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(request, IDENTITY);

        // then
        verify(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        verify(identityService, never()).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), anyBoolean());
        assertTrue(TestTransaction.isActive());
        assertTrue(TestTransaction.isFlaggedForRollback());
    }

    /*
        Normally, transactions in tests are flagged for rollback when they start. However, if the method has a @Commit annotation, they start flagged for commit instead:
     */
    @Commit
    @Test
    public void givenAValidUIDAndAnAgencyTokenUser_whenprocessEmailUpdatedRecentlyRequestForAgencyTokenUser_shouldReturnSuccessfullyAndCommitTransaction(){
        // given
        doNothing().when(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        doNothing().when(csrsService).updateSpacesAvailable(anyString(), anyString(), anyString(), anyBoolean());

        // when
        classUnderTest.processEmailUpdatedRecentlyRequestForAgencyTokenUser("mynewdomain", "mynewtoken", "myneworgcode", IDENTITY, request);

        // then

        verify(csrsService).updateSpacesAvailable(eq("mynewdomain"), eq("mynewtoken"), eq("myneworgcode"), eq(false));
        verify(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        verify(identityService, times(1)).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), eq(false));
        assertTrue(TestTransaction.isActive());
        assertFalse(TestTransaction.isFlaggedForRollback());
    }

    @Test (expected = RuntimeException.class)
    public void givenAValidUIDAndAnAgencyTokenAndTechnicalErrorOccurs_processEmailUpdatedRecentlyRequestForAgencyTokenUser_shouldThrowExceptionAndRollbackTransaction(){
        // given
        doThrow(new RuntimeException()).when(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));

        // when
        classUnderTest.processEmailUpdatedRecentlyRequestForAgencyTokenUser("mynewdomain", "mynewtoken", "myneworgcode", IDENTITY, request);

        // then
        verify(csrsService).updateSpacesAvailable(eq("mynewdomain"), eq("mynewtoken"), eq("myneworgcode"), eq(false));
        verify(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        verify(identityService, never()).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), anyBoolean());
        assertTrue(TestTransaction.isActive());
        assertTrue(TestTransaction.isFlaggedForRollback());
    }

    @Test (expected = IdentityNotFoundException.class)
    public void givenAValidUIDAndAnAgencyTokenUserAndIdentityNotFoundErrorOccurs_processEmailUpdatedRecentlyRequestForAgencyTokenUser_shouldThrowIdentityNotFoundExceptionAndRollbackTransaction(){
        // given
        doThrow(new IdentityNotFoundException("abc")).when(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));

        // when
        classUnderTest.processEmailUpdatedRecentlyRequestForAgencyTokenUser("mynewdomain", "mynewtoken", "myneworgcode", IDENTITY, request);

        // then
        verify(csrsService, never()).updateSpacesAvailable(anyString(), anyString(), anyString(), anyBoolean());
        verify(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        verify(identityService, never()).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), anyBoolean());
        assertTrue(TestTransaction.isActive());
        assertTrue(TestTransaction.isFlaggedForRollback());
    }

    @Test (expected = RuntimeException.class)
    public void givenAValidUIDAndAnAgencyTokenUserAndTechnicalErrorOccurs_processEmailUpdatedRecentlyRequestForAgencyTokenUser_shouldThrowExceptionAndRollbackTransaction(){
        // given
        doNothing().when(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        doThrow(new RuntimeException()).when(csrsService).updateSpacesAvailable(anyString(), anyString(), anyString(), anyBoolean());

        // when
        classUnderTest.processEmailUpdatedRecentlyRequestForAgencyTokenUser("mynewdomain", "mynewtoken", "myneworgcode", IDENTITY, request);

        // then
        verify(csrsService).updateSpacesAvailable(eq("mynewdomain"), eq("mynewtoken"), eq("myneworgcode"), eq(false));
        verify(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        verify(identityService, never()).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), anyBoolean());
        assertTrue(TestTransaction.isActive());
        assertTrue(TestTransaction.isFlaggedForRollback());
    }

    private AgencyToken buildAgencyToken() {
        AgencyToken at = new AgencyToken();
        return at;
    }
}
