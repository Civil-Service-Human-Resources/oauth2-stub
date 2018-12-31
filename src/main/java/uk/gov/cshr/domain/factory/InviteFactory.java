package uk.gov.cshr.domain.factory;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Component;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class InviteFactory {
    private static final String LEARNER_ROLE_NAME = "LEARNER";
    private final RoleRepository roleRepository;

    public InviteFactory(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Invite create(String email, Set<Role> roleSet, Identity inviter) {
        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setForRoles(roleSet);
        invite.setInviter(inviter);
        invite.setInvitedAt(new Date());
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(RandomStringUtils.random(40, true, true));

        return invite;
    }

    public Invite createSelfSignUpInvite(String email) {
        Role role = roleRepository.findFirstByNameEquals(LEARNER_ROLE_NAME);

        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setForRoles(new HashSet<>(Collections.singletonList(role)));
        invite.setInvitedAt(new Date());
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(RandomStringUtils.random(40, true, true));

        return invite;
    }
}
