package uk.gov.cshr.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.InviteService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class IdentityService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    private IdentityRepository identityRepository;

    private InviteService inviteService;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public IdentityService(IdentityRepository identityRepository, PasswordEncoder passwordEncoder) {
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setInviteService(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(username);
        if (identity == null) {
            throw new UsernameNotFoundException("No user found with email address " + username);
        }
        return new IdentityDetails(identity);
    }

    @ReadOnlyProperty
    public boolean inviteExistsByEmail(String email) {
        return identityRepository.existsByEmail(email);
    }

    public void createIdentityFromInviteCode(String code, String password) {
        Invite invite = inviteService.findByCode(code);

        Set<Role> newRoles = new HashSet<>(invite.getForRoles());
        Identity identity = new Identity(UUID.randomUUID().toString(), invite.getForEmail(), passwordEncoder.encode(password), true, false, newRoles);
        identityRepository.save(identity);

        LOGGER.info("New identity {} successfully created", identity.getEmail());
    }

    public void updatePassword(Identity identity, String password) {
        identity.setPassword(passwordEncoder.encode(password));
        identityRepository.save(identity);
    }
}
