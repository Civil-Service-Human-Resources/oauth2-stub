package uk.gov.cshr.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.InviteRepository;

import java.util.UUID;

@Service
public class IdentityService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(username);
        if (identity == null) {
            throw new UsernameNotFoundException("No user found with email address " + username);
        }
        return new IdentityDetails(identity);
    }

    public boolean isInvitedAnExistingUser(String email) {
        if (identityRepository.findFirstByActiveTrueAndEmailEquals(email) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void createIdentityFromInviteCode(String code, String password) {
        Invite invite = inviteRepository.findByCode(code);
        Identity identity = new Identity(UUID.randomUUID().toString(), invite.getForEmail(), passwordEncoder.encode(password), true, null);
        identityRepository.save(identity);
        LOGGER.info("New identity {} successfully created", identity.getEmail());
    }
}
