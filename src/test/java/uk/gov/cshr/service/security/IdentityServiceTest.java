package uk.gov.cshr.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import uk.gov.cshr.domain.*;
import uk.gov.cshr.exception.IdentityNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.TokenRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.NotifyService;
import uk.gov.cshr.utils.SpringUserUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    private final String updatePasswordEmailTemplateId = "template-id";
    private final String[] whitelistedDomains = new String[]{"whitelisted.gov.uk"};
    private final String orgCode = "AB";

    private static final String EMAIL = "test@example.com";
    private static final String CODE = "abc123";
    private static final String UID = "uid123";
    private static final Boolean ACTIVE = true;
    private static final Boolean LOCKED = false;
    private static final String PASSWORD = "password";
    private static final Set<Role> ROLES = new HashSet();

    private static Identity IDENTITY = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);

    private MockHttpServletRequest request;

    private IdentityService identityService;

    @Mock(name="identityRepository")
    private IdentityRepository identityRepository;

    @Mock
    private InviteService inviteService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenServices tokenServices;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private NotifyService notifyService;

    @Mock
    private CsrsService csrsService;

    @Mock
    private SpringUserUtils springUserUtils;

    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        identityService = new IdentityService(
                updatePasswordEmailTemplateId,
                identityRepository,
                passwordEncoder,
                tokenServices,
                tokenRepository,
                notifyService,
                csrsService,
                springUserUtils,
                whitelistedDomains
        );

        request = new MockHttpServletRequest();
    }

    @Test
    public void shouldLoadIdentityByEmailAddress() {

        final String emailAddress = "test@example.org";
        final String uid = "uid";
        final Identity identity = new Identity(uid, emailAddress, "password", true, false, emptySet(), Instant.now(), false, false);

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress))
                .thenReturn(identity);

        IdentityDetails identityDetails = (IdentityDetails) identityService.loadUserByUsername(emailAddress);

        assertThat(identityDetails, notNullValue());
        assertThat(identityDetails.getUsername(), equalTo(uid));
        assertThat(identityDetails.getIdentity(), equalTo(identity));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldThrowErrorWhenNoClientFound() {

        final String emailAddress = "test@example.org";

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress))
                .thenReturn(null);

        identityService.loadUserByUsername(emailAddress);
    }

    @Test
    public void shouldReturnTrueWhenInvitingAnExistingUser() {
        final String emailAddress = "test@example.org";

        when(identityRepository.existsByEmail(emailAddress))
                .thenReturn(true);

        assertThat(identityService.existsByEmail("test@example.org"), equalTo(true));
    }

    @Test
    public void shouldReturnFalseWhenInvitingAnNonExistingUser() {
        assertThat(identityService.existsByEmail("test2@example.org"), equalTo(false));
    }

    @Test
    public void createIdentityFromInviteCode() {
        final String code = "123abc";
        final String email = "test@example.com";
        Role role = new Role();
        role.setName("USER");

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        Invite invite = new Invite();
        invite.setCode(code);
        invite.setForEmail(email);
        invite.setForRoles(roles);

        when(inviteService.findByCode(code)).thenReturn(invite);

        when(passwordEncoder.encode("password")).thenReturn("password");

        identityService.setInviteService(inviteService);

        identityService.createIdentityFromInviteCode(code, "password");

        ArgumentCaptor<Identity> inviteArgumentCaptor = ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(inviteArgumentCaptor.capture());

        Identity identity = inviteArgumentCaptor.getValue();
        assertThat(identity.getRoles().contains(role), equalTo(true));
        assertThat(identity.getPassword(), equalTo("password"));
        assertThat(identity.getEmail(), equalTo("test@example.com"));
    }

    @Test
    public void lockIdentitySetsLockedToTrue() {
        String email = "test-email";
        Identity identity = mock(Identity.class);
        when(identityRepository.findFirstByActiveTrueAndEmailEquals(email)).thenReturn(identity);

        identityService.lockIdentity(email);

        InOrder inOrder = inOrder(identity, identityRepository);

        inOrder.verify(identity).setLocked(true);
        inOrder.verify(identityRepository).save(identity);
    }

    @Test
    public void shouldRevokeAccessTokensForUser() {
        String uid = "_uid";
        Identity identity = mock(Identity.class);
        when(identity.getUid()).thenReturn(uid);

        String accessToken1Value = "token1-value";
        OAuth2AccessToken accessToken1 = mock(OAuth2AccessToken.class);
        when(accessToken1.getValue()).thenReturn(accessToken1Value);
        Token token1 = mock(Token.class);
        when(token1.getToken()).thenReturn(accessToken1);

        String accessToken2Value = "token2-value";
        OAuth2AccessToken accessToken2 = mock(OAuth2AccessToken.class);
        when(accessToken2.getValue()).thenReturn(accessToken2Value);
        Token token2 = mock(Token.class);
        when(token2.getToken()).thenReturn(accessToken2);

        when(tokenRepository.findAllByUserName(uid)).thenReturn(Arrays.asList(token1, token2));

        identityService.revokeAccessTokens(identity);

        verify(tokenServices).revokeToken(accessToken1Value);
        verify(tokenServices).revokeToken(accessToken2Value);
    }

    @Test
    public void shouldUpdatePasswordAndRevokeTokens() {
        String password = "_password";
        String encodedPassword = "encoded-password";
        String email = "learner@domain.com";
        String uid = "_uid";
        Identity identity = mock(Identity.class);
        when(identity.getUid()).thenReturn(uid);
        when(identity.getEmail()).thenReturn(email);

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        String accessToken1Value = "token1-value";
        OAuth2AccessToken accessToken1 = mock(OAuth2AccessToken.class);
        when(accessToken1.getValue()).thenReturn(accessToken1Value);
        Token token1 = mock(Token.class);
        when(token1.getToken()).thenReturn(accessToken1);

        String accessToken2Value = "token2-value";
        OAuth2AccessToken accessToken2 = mock(OAuth2AccessToken.class);
        when(accessToken2.getValue()).thenReturn(accessToken2Value);
        Token token2 = mock(Token.class);
        when(token2.getToken()).thenReturn(accessToken2);

        when(tokenRepository.findAllByUserName(uid)).thenReturn(Arrays.asList(token1, token2));

        identityService.updatePasswordAndRevokeTokens(identity, password);

        InOrder inOrder = inOrder(identity, identityRepository);

        inOrder.verify(identity).setPassword(encodedPassword);
        inOrder.verify(identityRepository).save(identity);

        verify(tokenServices).revokeToken(accessToken1Value);
        verify(tokenServices).revokeToken(accessToken2Value);

        verify(notifyService).notify(email, updatePasswordEmailTemplateId);
    }

    @Test
    public void givenAValidIdentityWithAWhitelistedDomain_whenUpdateEmailAddress_shouldReturnSuccessfully(){
        // given
        Optional<Identity> optionalIdentity = Optional.of(IDENTITY);
        when(identityRepository.findById(anyLong())).thenReturn(optionalIdentity);
        when(identityRepository.save(identityArgumentCaptor.capture())).thenReturn(new Identity());

        Identity identityParam = new Identity();
        identityParam.setId(new Long(123l));

        // when
        identityService.updateEmailAddressAndEmailRecentlyUpdatedFlagToTrue(identityParam, "mynewemail@whitelisted.gov.uk");

        // then
        verify(identityRepository, times(1)).findById(anyLong());
        verify(identityRepository, times(1)).save(optionalIdentity.get());
        Identity actualSavedIdentity = identityArgumentCaptor.getValue();
        assertThat(actualSavedIdentity.isEmailRecentlyUpdated(), equalTo(true));
    }

    @Test
    public void givenAValidIdentity_resetRecentlyUpdatedEmailFlag_shouldReturnSuccessfully(){
        // given
        IDENTITY.setId(123L);
        Optional<Identity> optionalIdentity = Optional.of(IDENTITY);
        when(identityRepository.findById(anyLong())).thenReturn(optionalIdentity);
        when(identityRepository.save(identityArgumentCaptor.capture())).thenReturn(new Identity());

        // when
        identityService.resetRecentlyUpdatedEmailFlagToFalse(IDENTITY);

        // then
        verify(identityRepository, times(1)).save(optionalIdentity.get());
        Identity actualSavedIdentity = identityArgumentCaptor.getValue();
        assertThat(actualSavedIdentity.isEmailRecentlyUpdated(), equalTo(false));
    }

    @Test(expected = IdentityNotFoundException.class)
    public void givenAnNotFoundIdentity_resetRecentlyUpdatedEmailFlag_shouldThrowIdentityNotFoundException(){

        // when
        identityService.resetRecentlyUpdatedEmailFlagToFalse(new Identity());

        // then
        verify(identityRepository, never()).save(any(Identity.class));
    }

    @Test(expected = RuntimeException.class)
    public void givenAnInvalidIdentity_resetRecentlyUpdatedEmailFlag_shouldThrowException(){
        // given
        Optional<Identity> optionalIdentity = Optional.of(IDENTITY);

        // when
        identityService.resetRecentlyUpdatedEmailFlagToFalse(new Identity());

        // then
        verify(identityRepository, times(1)).save(optionalIdentity.get());
    }

    @Test
    public void givenAValidIdentity_getEmailRecentlyUpdatedFlag_shouldReturnSuccessfully(){
        // given
        IDENTITY.setId(123L);
        IDENTITY.setEmailRecentlyUpdated(true);
        Optional<Identity> optionalIdentity = Optional.of(IDENTITY);
        when(identityRepository.findById(anyLong())).thenReturn(optionalIdentity);

        // when
        boolean actual = identityService.getRecentlyUpdatedEmailFlag(IDENTITY);

        // then
        assertTrue(actual);
        verify(identityRepository, times(1)).findById(eq(IDENTITY.getId()));
    }

    @Test(expected = IdentityNotFoundException.class)
    public void givenAnInvalidIdentity_getEmailRecentlyUpdatedFlag_shouldThrowIdentityNotFoundException(){
        // given

        // when
        boolean actual = identityService.getRecentlyUpdatedEmailFlag(IDENTITY);

        // then
        assertFalse(actual);
        verify(identityRepository, never()).findById(anyLong());
    }

    private AgencyToken buildAgencyToken() {
        AgencyToken at = new AgencyToken();
        at.setToken("token123");
        at.setCapacity(100);
        at.setCapacityUsed(11);
        return at;
    }
}
