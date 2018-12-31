package uk.gov.cshr.domain.factory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.repository.RoleRepository;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class InviteFactoryTest {
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private InviteFactory inviteFactory;

    @Test
    public void shouldReturnInvite() {
        String email = "user@domain.org";

        Identity inviter = new Identity();

        Role role1 = new Role("role1", "description");
        Role role2 = new Role("role2", "description");

        Set<Role> roleSet = new HashSet<>(Arrays.asList(role1, role2));

        Invite invite = inviteFactory.create(email, roleSet, inviter);

        assertEquals(email, invite.getForEmail());
        assertEquals(roleSet, invite.getForRoles());
        assertEquals(inviter, invite.getInviter());
        assertEquals(new Date().toString(), invite.getInvitedAt().toString());
        assertNotNull(invite.getCode());
        assertEquals(40, invite.getCode().length());
    }

    @Test
    public void shouldReturnSelfSignUpInvite() {
        String email = "user@domain.org";
        Role role = new Role();

        when(roleRepository.findFirstByNameEquals("LEARNER")).thenReturn(role);

        Invite invite = inviteFactory.createSelfSignUpInvite(email);

        assertEquals(email, invite.getForEmail());
        assertEquals(new HashSet<>(Collections.singletonList(role)), invite.getForRoles());
        assertNull(invite.getInviter());
        assertEquals(new Date().toString(), invite.getInvitedAt().toString());
        assertNotNull(invite.getCode());
        assertEquals(40, invite.getCode().length());
    }
}