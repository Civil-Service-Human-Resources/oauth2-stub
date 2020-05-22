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
    private static final String NEW_EMAIL_ADDRESS = "new@newexample.com";
    private static final String CODE = "abc123";
    private static final String UID = "uid123";
    private static final Boolean ACTIVE = true;
    private static final Boolean LOCKED = false;
    private static final String PASSWORD = "password";
    private static final Set<Role> ROLES = new HashSet();
    private static final String NEW_DOMAIN = "newexample.com";
    private static final String AGENCY_TOKEN_UID = "UID";
    private static Identity IDENTITY = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);
    private static Identity AGENCY_IDENTITY = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false, AGENCY_TOKEN_UID);

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
    private AgencyTokenService agencyTokenService;

    @MockBean
    private CsrsService csrsService;

    @Value("${govNotify.template.emailUpdate}")
    private String updateEmailTemplateId;

    @Value("${emailUpdate.urlFormat}")
    private String inviteUrlFormat;

    @Captor
    private ArgumentCaptor<EmailUpdate> emailUpdateArgumentCaptor;

    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    @Autowired
    private EmailUpdateService emailUpdateService;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    public void givenAValidCodeForIdentity_whenVerifyCode_thenReturnsTrue() {
        // given
        when(emailUpdateRepository.existsByIdentityAndCode(any(Identity.class), anyString())).thenReturn(true);

        // when
        boolean actual = emailUpdateService.verifyEmailUpdateExists(IDENTITY, "co");

        // then
        assertTrue(actual);
        verify(emailUpdateRepository, times(1)).existsByIdentityAndCode(eq(IDENTITY), eq("co"));
    }

    @Test
    public void givenAInvalidCodeForIdentity_whenVerifyCode_thenReturnsFalse() {
        // given
        when(emailUpdateRepository.existsByIdentityAndCode(any(Identity.class), anyString())).thenReturn(false);

        // when
        boolean actual = emailUpdateService.verifyEmailUpdateExists(IDENTITY, "co");

        // then
        assertFalse(actual);
        verify(emailUpdateRepository, times(1)).existsByIdentityAndCode(eq(IDENTITY), eq("co"));
    }

    @Test
    public void givenAValidIdentity_whenNewDomainWhitelistedAndNotAgency_shouldReturnSuccessfully() throws Exception {
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setId(100l);
        emailUpdate.setEmail(NEW_EMAIL_ADDRESS);

        when(identityService.getDomainFromEmailAddress(NEW_EMAIL_ADDRESS)).thenReturn(NEW_DOMAIN);

        doNothing().when(identityService).updateEmailAddressWhereNewEmailIsNotAgency(eq(IDENTITY), eq(emailUpdate.getEmail()));
        doNothing().when(identityService).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), eq(true));
        doNothing().when(emailUpdateRepository).delete(any(EmailUpdate.class));

        emailUpdateService.updateEmailAddress(request, IDENTITY, emailUpdate);

        verify(identityService, times(1)).updateEmailAddressWhereNewEmailIsNotAgency(identityArgumentCaptor.capture(), eq(emailUpdate.getEmail()));
        verify(identityService, times(1)).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), eq(true));
        verify(emailUpdateRepository, times(1)).delete(emailUpdateArgumentCaptor.capture());

        EmailUpdate actualDeletedEmailUpdate = emailUpdateArgumentCaptor.getValue();
        assertThat(actualDeletedEmailUpdate.getId(), equalTo(100l));

        Identity identity = identityArgumentCaptor.getValue();
        assertThat(identity.getUid(), equalTo(UID));
        assertThat(identity.getAgencyTokenUid(), equalTo(null));
    }

    // TODO: 21/05/2020 Test throwing the exception  
    /*
        Normally, transactions in tests are flagged for rollback when they start. However, if the method has a @Commit annotation, they start flagged for commit instead:
     */
    @Commit
    @Test
    public void givenAValidUIDAndAWhitelistedUser_whenProcessEmailUpdatedRecentlyRequestForWhiteListedDomainUser_shouldReturnSuccessfullyAndCommitTransaction(){
        // given
        doNothing().when(identityService).resetRecentlyUpdatedEmailFlagToFalse(any(Identity.class));

        // when
        emailUpdateService.processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(request, IDENTITY);

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
        emailUpdateService.processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(request, IDENTITY);

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
        emailUpdateService.processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(request, IDENTITY);

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
        emailUpdateService.processEmailUpdatedRecentlyRequestForAgencyTokenUser("mynewdomain", "mynewtoken", "myneworgcode", IDENTITY, request);

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
        emailUpdateService.processEmailUpdatedRecentlyRequestForAgencyTokenUser("mynewdomain", "mynewtoken", "myneworgcode", IDENTITY, request);

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
        emailUpdateService.processEmailUpdatedRecentlyRequestForAgencyTokenUser("mynewdomain", "mynewtoken", "myneworgcode", IDENTITY, request);

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
        emailUpdateService.processEmailUpdatedRecentlyRequestForAgencyTokenUser("mynewdomain", "mynewtoken", "myneworgcode", IDENTITY, request);

        // then
        verify(csrsService).updateSpacesAvailable(eq("mynewdomain"), eq("mynewtoken"), eq("myneworgcode"), eq(false));
        verify(identityService).resetRecentlyUpdatedEmailFlagToFalse(eq(IDENTITY));
        verify(identityService, never()).updateSpringWithRecentlyEmailUpdatedFlag(any(HttpServletRequest.class), anyBoolean());
        assertTrue(TestTransaction.isActive());
        assertTrue(TestTransaction.isFlaggedForRollback());
    }

    @Test
    public void shouldGetEmailUpdate() {
        EmailUpdate emailUpdate = new EmailUpdate();

        when(emailUpdateRepository.findByIdentityAndCode(any(Identity.class), anyString())).thenReturn(Optional.of(emailUpdate));

        assertEquals(emailUpdateService.getEmailUpdate(IDENTITY, CODE), emailUpdate);
    }

    private AgencyToken buildAgencyToken() {
        AgencyToken at = new AgencyToken();
        return at;
    }
}
