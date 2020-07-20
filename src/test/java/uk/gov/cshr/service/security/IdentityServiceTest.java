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
import uk.gov.cshr.exception.AccountDeactivatedException;
import uk.gov.cshr.exception.IdentityNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.TokenRepository;
import uk.gov.cshr.service.AgencyTokenCapacityService;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.NotifyService;

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

    private static final String EMAIL = "test@example.com";
    private static final String CODE = "abc123";
    private static final String UID = "uid123";
    private static final Boolean ACTIVE = true;
    private static final Boolean LOCKED = false;
    private static final String PASSWORD = "password";
    private static final Set<Role> ROLES = new HashSet();
    private static Identity IDENTITY = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);
    private final String updatePasswordEmailTemplateId = "template-id";
    private final String[] whitelistedDomains = new String[]{"whitelisted.gov.uk", "example.com"};
    private final String orgCode = "AB";
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
    private AgencyTokenCapacityService agencyTokenCapacityService;


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
                whitelistedDomains,
                agencyTokenCapacityService
        );

        request = new MockHttpServletRequest();
    }

    @Test
    public void shouldLoadIdentityByEmailAddress() {

        final String emailAddress = "test@example.org";
        final String uid = "uid";
        final Identity identity = new Identity(uid, emailAddress, "password", true, false, emptySet(), Instant.now(), false, false);

        when(identityRepository.findFirstByEmailEquals(emailAddress))
                .thenReturn(identity);

        IdentityDetails identityDetails = (IdentityDetails) identityService.loadUserByUsername(emailAddress);

        assertThat(identityDetails, notNullValue());
        assertThat(identityDetails.getUsername(), equalTo(uid));
        assertThat(identityDetails.getIdentity(), equalTo(identity));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldThrowErrorWhenNoClientFound() {

        final String emailAddress = "test@example.org";

        when(identityRepository.findFirstByEmailEquals(emailAddress))
                .thenReturn(null);

        identityService.loadUserByUsername(emailAddress);
    }

    @Test(expected = AccountDeactivatedException.class)
    public void shouldThrowErrorWhenUserDeactivated() {

        final String emailAddress = "test@example.org";
        Identity identity = new Identity();
        identity.setActive(false);

        when(identityRepository.findFirstByEmailEquals(emailAddress))
                .thenReturn(identity);

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
    public void createIdentityFromInviteCodeWithoutAgencyButIsWhitelisted() {
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

        TokenRequest tokenRequest = new TokenRequest();

        when(inviteService.findByCode(code)).thenReturn(invite);

        when(passwordEncoder.encode("password")).thenReturn("password");

        identityService.setInviteService(inviteService);

        identityService.createIdentityFromInviteCode(code, "password", tokenRequest);

        ArgumentCaptor<Identity> inviteArgumentCaptor = ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(inviteArgumentCaptor.capture());

        Identity identity = inviteArgumentCaptor.getValue();
        assertThat(identity.getRoles().contains(role), equalTo(true));
        assertThat(identity.getPassword(), equalTo("password"));
        assertThat(identity.getEmail(), equalTo("test@example.com"));
        assertThat(identity.getAgencyTokenUid(), equalTo(null));
    }

    @Test
    public void createIdentityFromInviteCodeWithAgency() {
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

        TokenRequest tokenRequest = new TokenRequest();
        String tokenDomain = "example.com";
        String tokenCode = "co";
        String tokenToken = "token123";
        tokenRequest.setDomain(tokenDomain);
        tokenRequest.setOrg(tokenCode);
        tokenRequest.setToken(tokenToken);

        String uid = "UID";
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(uid);

        when(inviteService.findByCode(code)).thenReturn(invite);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(tokenDomain, tokenToken, tokenCode)).thenReturn(Optional.of(agencyToken));
        when(passwordEncoder.encode("password")).thenReturn("password");
        when(agencyTokenCapacityService.hasSpaceAvailable(agencyToken)).thenReturn(true);
        identityService.setInviteService(inviteService);

        identityService.createIdentityFromInviteCode(code, "password", tokenRequest);

        ArgumentCaptor<Identity> inviteArgumentCaptor = ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(inviteArgumentCaptor.capture());

        Identity identity = inviteArgumentCaptor.getValue();
        assertThat(identity.getRoles().contains(role), equalTo(true));
        assertThat(identity.getPassword(), equalTo("password"));
        assertThat(identity.getEmail(), equalTo("test@example.com"));
        assertThat(identity.getAgencyTokenUid(), equalTo(uid));
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

    @Test(expected = IdentityNotFoundException.class)
    public void identityRepositoryShouldThrowExceptionIfNotFound() {
        when(identityRepository.findById(anyLong())).thenReturn(Optional.empty());

        Identity identityParam = new Identity();
        identityParam.setId(new Long(123l));

        identityService.updateEmailAddress(identityParam, "mynewemail@whitelisted.gov.uk", null);
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
        identityService.updateEmailAddress(identityParam, "mynewemail@whitelisted.gov.uk", null);

        // then
        verify(identityRepository, times(1)).findById(anyLong());
        verify(identityRepository, times(1)).save(optionalIdentity.get());
        Identity actualSavedIdentity = identityArgumentCaptor.getValue();
        assertThat(actualSavedIdentity.getAgencyTokenUid(), equalTo(null));
    }

    @Test
    public void givenAValidIdentityWithAnAgencyDomain_whenUpdateEmailAddress_shouldReturnSuccessfully() {
        // given
        Optional<Identity> optionalIdentity = Optional.of(IDENTITY);
        when(identityRepository.findById(anyLong())).thenReturn(optionalIdentity);
        when(identityRepository.save(identityArgumentCaptor.capture())).thenReturn(new Identity());

        Identity identityParam = new Identity();
        identityParam.setId(new Long(123l));

        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(UID);

        // when
        identityService.updateEmailAddress(identityParam, "mynewemail@whitelisted.gov.uk", agencyToken);

        // then
        verify(identityRepository, times(1)).findById(anyLong());
        verify(identityRepository, times(1)).save(optionalIdentity.get());
        Identity actualSavedIdentity = identityArgumentCaptor.getValue();
        assertThat(actualSavedIdentity.getAgencyTokenUid(), equalTo(UID));
    }

    @Test
    public void givenAValidWhitelistedEmail_whenCheckValidEmail_shouldReturnTrue(){
        // given
        // whitelisted.gov.uk which is whitelisted

        // when
        boolean actual = identityService.checkValidEmail("someone@whitelisted.gov.uk");

        // then
        assertTrue(actual);
        verifyZeroInteractions(csrsService);
    }

    @Test
    public void givenAValidAgencyTokenEmail_whenCheckValidEmail_shouldReturnTrue(){
        String email = "someone@badger.gov.uk";
        String domain = "badger.gov.uk";

        when(csrsService.isDomainInAgency(domain)).thenReturn(true);

        boolean actual = identityService.checkValidEmail(email);

        // then
        assertTrue(actual);
        verify(csrsService, times(1)).isDomainInAgency(eq("badger.gov.uk"));
    }

    @Test
    public void givenANonValidAgencyTokenEmail_whenCheckValidEmail_shouldReturnFalse(){
        String email = "someone@foo.com";
        String domain = "foo.com";

        when(csrsService.isDomainInAgency(domain)).thenReturn(false);

        boolean actual = identityService.checkValidEmail(email);

        assertFalse(actual);
        verify(csrsService, times(1)).isDomainInAgency(eq("foo.com"));
    }

    @Test
    public void shouldReactivateIdentity() {
        Identity identity = new Identity();
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(UID);

        identityService.reactivateIdentity(identity, agencyToken);

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Identity identityCaptor = identityArgumentCaptor.getValue();

        assertEquals(identityCaptor.isActive(), true);
        assertEquals(identityCaptor.getAgencyTokenUid(), UID);
    }

    @Test
    public void shouldGetIdentityByEmailAndActiveFalse() {
        Identity identity = mock(Identity.class);
        when(identity.getUid()).thenReturn(UID);
        when(identity.isActive()).thenReturn(false);

        when(identityRepository.findFirstByActiveFalseAndEmailEquals(EMAIL)).thenReturn(Optional.of(identity));

        Identity actualIdentity = identityService.getIdentityByEmailAndActiveFalse(EMAIL);

        assertEquals(UID, actualIdentity.getUid());
        assertEquals(false, actualIdentity.isActive());
    }

    @Test(expected = IdentityNotFoundException.class)
    public void shouldThrowExceptionIfIdentityNotFound() {
        doThrow(new IdentityNotFoundException("Identity not found")).when(identityRepository).findFirstByActiveFalseAndEmailEquals(EMAIL);

        identityService.getIdentityByEmailAndActiveFalse(EMAIL);
    }
}
