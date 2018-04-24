package uk.gov.cshr.service.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.InviteRepository;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IdentityServiceTest {

    @InjectMocks
    private IdentityService identityService;

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private InviteRepository inviteRepository;

    @Test
    public void shouldLoadIdentityByEmailAddress() {

        final String emailAddress = "test@example.org";
        final Identity identity = new Identity("uid", emailAddress, "password", true, emptySet());

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress))
                .thenReturn(identity);

        IdentityDetails identityDetails = (IdentityDetails) identityService.loadUserByUsername(emailAddress);

        assertThat(identityDetails, notNullValue());
        assertThat(identityDetails.getUsername(), equalTo(emailAddress));
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
        final Identity identity = new Identity("uid", emailAddress, "password", true, emptySet());

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress))
                .thenReturn(identity);

        assertThat(identityService.isInvitedAnExistingUser("test@example.org"), equalTo(true));
    }

    @Test
    public void shouldReturnFalseWhenInvitingAnNonExistingUser() {
        assertThat(identityService.isInvitedAnExistingUser("test2@example.org"), equalTo(false));
    }

//    @Test
//    public void createIdentityFromInviteCode() {
//        final String code = "123abc";
//        final String email = "test@example.com";
//        Invite invite = new Invite();
//        invite.setCode(code);
//        invite.setForEmail(email);
//
//        when(inviteRepository.findByCode(code)).thenReturn(invite);
//
//        identityService.createIdentityFromInviteCode(code, "password");
//
//        ArgumentCaptor<Invite> inviteArgumentCaptor = ArgumentCaptor.forClass(Invite.class);
//
//        verify(inviteRepository).save(inviteArgumentCaptor.capture());
//
//        invite = inviteArgumentCaptor.getValue();
//        MatcherAssert.assertThat(invite.getCode(), equalTo(code));
//        MatcherAssert.assertThat(invite.getForEmail(), equalTo(email));
//    }
}
