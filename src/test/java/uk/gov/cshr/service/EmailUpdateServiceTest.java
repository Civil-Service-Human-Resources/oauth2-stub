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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.EmailUpdateRepository;
import uk.gov.cshr.service.security.IdentityService;

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

    private MockHttpServletRequest request;

    @MockBean
    private EmailUpdateRepository emailUpdateRepository;

    @MockBean
    private IdentityService identityService;

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
        when(emailUpdateRepository.existsByCode(anyString())).thenReturn(true);

        boolean actual = emailUpdateService.existsByCode("co");

        assertTrue(actual);
        verify(emailUpdateRepository, times(1)).existsByCode(eq("co"));
    }

    @Test
    public void givenAInvalidCodeForIdentity_whenVerifyCode_thenReturnsFalse() {
        when(emailUpdateRepository.existsByCode(anyString())).thenReturn(false);

        boolean actual = emailUpdateService.existsByCode("co");

        assertFalse(actual);
        verify(emailUpdateRepository, times(1)).existsByCode(eq("co"));
    }

    @Test
    public void givenAValidIdentity_whenNewDomainWhitelistedAndNotAgency_shouldReturnSuccessfully() throws Exception {
        Identity identity = new Identity();
        identity.setEmail(EMAIL);

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setId(100l);
        emailUpdate.setEmail(NEW_EMAIL_ADDRESS);
        emailUpdate.setIdentity(identity);

        when(identityService.getIdentityByEmail(EMAIL)).thenReturn(identity);

        doNothing().when(identityService).updateEmailAddress(eq(IDENTITY), eq(emailUpdate.getEmail()), isNull());
        doNothing().when(emailUpdateRepository).delete(any(EmailUpdate.class));

        emailUpdateService.updateEmailAddress(emailUpdate);

        verify(identityService, times(1)).updateEmailAddress(identityArgumentCaptor.capture(), eq(emailUpdate.getEmail()), isNull());
        verify(emailUpdateRepository, times(1)).delete(emailUpdateArgumentCaptor.capture());

        EmailUpdate actualDeletedEmailUpdate = emailUpdateArgumentCaptor.getValue();
        assertThat(actualDeletedEmailUpdate.getId(), equalTo(100l));

        Identity identityArgumentCaptorValue = identityArgumentCaptor.getValue();
        assertThat(identityArgumentCaptorValue.getEmail(), equalTo(EMAIL));
    }

    @Test
    public void givenAValidIdentity_whenNewDomainIsAgency_shouldReturnSuccessfully() throws Exception {
        Identity identity = new Identity();
        identity.setEmail(EMAIL);

        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setId(100l);
        emailUpdate.setEmail(NEW_EMAIL_ADDRESS);
        emailUpdate.setIdentity(identity);

        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(UID);

        when(identityService.getIdentityByEmail(EMAIL)).thenReturn(identity);

        doNothing().when(identityService).updateEmailAddress(eq(IDENTITY), eq(emailUpdate.getEmail()), eq(agencyToken));
        doNothing().when(emailUpdateRepository).delete(any(EmailUpdate.class));

        emailUpdateService.updateEmailAddress(emailUpdate, agencyToken);

        verify(identityService, times(1)).updateEmailAddress(identityArgumentCaptor.capture(), eq(emailUpdate.getEmail()), eq(agencyToken));
        verify(emailUpdateRepository, times(1)).delete(emailUpdateArgumentCaptor.capture());

        EmailUpdate actualDeletedEmailUpdate = emailUpdateArgumentCaptor.getValue();
        assertThat(actualDeletedEmailUpdate.getId(), equalTo(100l));

        Identity identityArgumentCaptorValue = identityArgumentCaptor.getValue();
        assertThat(identityArgumentCaptorValue.getEmail(), equalTo(EMAIL));
    }

    @Test
    public void shouldGetEmailUpdate() {
        EmailUpdate emailUpdate = new EmailUpdate();

        when(emailUpdateRepository.findByCode(anyString())).thenReturn(Optional.of(emailUpdate));

        assertEquals(emailUpdateService.getEmailUpdateByCode(CODE), emailUpdate);
    }
}
