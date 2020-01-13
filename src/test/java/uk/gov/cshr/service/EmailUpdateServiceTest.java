package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.exception.InvalidCodeException;
import uk.gov.cshr.repository.EmailUpdateRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailUpdateServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String CODE = "abc123";
    private static final String UID = "uid123";
    private static final Boolean ACTIVE = true;
    private static final Boolean LOCKED = false;
    private static final String PASSWORD = "password";
    private static final Set<Role> ROLES = new HashSet();

    private static Identity IDENTITY = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);

    @Mock
    private EmailUpdateRepository emailUpdateRepository;

    @Mock
    private EmailUpdateFactory emailUpdateFactory;

    @Mock
    private NotifyService notifyService;

    @Mock
    private IdentityService identityService;

    @Mock
    private CsrsService csrsService;

    @Captor
    private ArgumentCaptor<EmailUpdate> emailUpdateArgumentCaptor;

    private String updateEmailTemplateId;
    private String inviteUrlFormat;

    private EmailUpdateService classUnderTest;

    @Before
    public void setUp() {
        updateEmailTemplateId = "12345";
        inviteUrlFormat = "http://localhost:8080/account/email/verify/%s?redirect=true";

        classUnderTest = new EmailUpdateService(emailUpdateRepository, emailUpdateFactory, notifyService, identityService, csrsService, updateEmailTemplateId, inviteUrlFormat);
    }

    @Test
    public void givenAValidIdentityAndAValidCodeAndAWhitelistedDomain_whenUpdateEmailAddress_shouldReturnSuccessfully(){
        // given
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setId(100l);
        emailUpdate.setEmail("myemail@mynewemail.com");
        Optional<EmailUpdate> optionalEmailUpdate = Optional.of(emailUpdate);
        when(emailUpdateRepository.findByIdentityAndCode(any(Identity.class), anyString())).thenReturn(optionalEmailUpdate);
        when(identityService.getDomainFromEmailAddress(anyString())).thenReturn("myoldomain.com");
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(true);
        doNothing().when(identityService).updateEmailAddress(eq(IDENTITY), eq(emailUpdate.getEmail()));
        doNothing().when(emailUpdateRepository).delete(any(EmailUpdate.class));

        // when
        classUnderTest.updateEmailAddress(IDENTITY, "CO");

        // then
        verify(emailUpdateRepository, times(1)).findByIdentityAndCode(eq(IDENTITY), eq("CO"));
        verify(identityService, times(1)).getDomainFromEmailAddress(eq(IDENTITY.getEmail()));
        verify(identityService, times(1)).isWhitelistedDomain(eq("myoldomain.com"));
        verify(identityService, times(1)).updateEmailAddress(eq(IDENTITY), eq(emailUpdate.getEmail()));
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
        classUnderTest.updateEmailAddress(IDENTITY, "CO");

        // then
        verify(emailUpdateRepository, times(1)).findByIdentityAndCode(eq(IDENTITY), eq("CO"));
        verify(identityService, never()).getDomainFromEmailAddress(anyString());
        verify(identityService, never()).isWhitelistedDomain(anyString());
        verify(identityService, never()).updateEmailAddress(any(Identity.class), anyString());
        verify(emailUpdateRepository, never()).delete(any(EmailUpdate.class));
    }

    /*@Test
    public void givenAValidIdentityAndAValidCodeAndANonAgencyTokenPerson_whenUpdateEmailAddress_shouldReturnSuccessfully(){
        // given
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setId(100l);
        emailUpdate.setEmail("myemail@mynewemail.com");
        Optional<EmailUpdate> optionalEmailUpdate = Optional.of(emailUpdate);
        when(emailUpdateRepository.findByIdentityAndCode(any(Identity.class), anyString())).thenReturn(optionalEmailUpdate);
        when(identityService.getDomainFromEmailAddress(anyString())).thenReturn("myoldomain.com");
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(false);
        doNothing().when(identityService).updateEmailAddress(eq(IDENTITY), eq(emailUpdate.getEmail()));
        doNothing().when(emailUpdateRepository).delete(any(EmailUpdate.class));

        // when
        classUnderTest.updateEmailAddress(IDENTITY, "CO");

        // then
        verify(emailUpdateRepository, times(1)).findByIdentityAndCode(eq(IDENTITY), eq("CO"));
        verify(identityService, times(1)).getDomainFromEmailAddress(eq(IDENTITY.getEmail()));
        verify(identityService, times(1)).isWhitelistedDomain(eq("myoldomain.com"));
        verify(identityService, times(1)).updateEmailAddress(eq(IDENTITY), eq(emailUpdate.getEmail()));
        verify(emailUpdateRepository, times(1)).delete(emailUpdateArgumentCaptor.capture());

        EmailUpdate actualDeletedEmailUpdate = emailUpdateArgumentCaptor.getValue();
        assertThat(actualDeletedEmailUpdate.getId(), equalTo(100l));
    }
*/
}
