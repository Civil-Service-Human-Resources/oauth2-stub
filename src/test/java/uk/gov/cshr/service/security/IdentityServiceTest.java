package uk.gov.cshr.service.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.InviteService;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IdentityServiceTest {

    @InjectMocks
    private IdentityService identityService;

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private InviteService inviteService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void shouldLoadIdentityByEmailAddress() {

        final String emailAddress = "test@example.org";
        final String uid = "uid";
        final Identity identity = new Identity(uid, emailAddress, "password", true, false, emptySet());

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

        assertThat(identityService.inviteExistsByEmail("test@example.org"), equalTo(true));
    }

    @Test
    public void shouldReturnFalseWhenInvitingAnNonExistingUser() {
        assertThat(identityService.inviteExistsByEmail("test2@example.org"), equalTo(false));
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
}
