package uk.gov.cshr.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.cshr.exception.IdentityNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.TokenRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.NotifyService;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class IdentityService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    private final String updatePasswordEmailTemplateId;

    private final IdentityRepository identityRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenServices tokenServices;
    private final TokenRepository tokenRepository;
    private final NotifyService notifyService;
    private InviteService inviteService;
    private String[] whitelistedDomains;

    public IdentityService(@Value("${govNotify.template.passwordUpdate}") String updatePasswordEmailTemplateId,
                           IdentityRepository identityRepository,
                           PasswordEncoder passwordEncoder,
                           TokenServices tokenServices,
                           @Qualifier("tokenRepository") TokenRepository tokenRepository,
                           NotifyService notifyService,
                           @Value("${invite.whitelist.domains}") String[] whitelistedDomains) {
        this.updatePasswordEmailTemplateId = updatePasswordEmailTemplateId;
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenServices = tokenServices;
        this.tokenRepository = tokenRepository;
        this.notifyService = notifyService;
        this.whitelistedDomains = whitelistedDomains;
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
    public boolean existsByEmail(String email) {
        return identityRepository.existsByEmail(email);
    }

    public void createIdentityFromInviteCode(String code, String password) {
        Invite invite = inviteService.findByCode(code);

        Set<Role> newRoles = new HashSet<>(invite.getForRoles());
        Identity identity = new Identity(UUID.randomUUID().toString(), invite.getForEmail(), passwordEncoder.encode(password), true, false, newRoles, Instant.now(), false);
        identityRepository.save(identity);

        LOGGER.info("New identity {} successfully created", identity.getEmail());
    }

    public void updatePassword(Identity identity, String password) {
        identity.setActive(true);
        identity.setDeletionNotificationSent(false);
        identity.setPassword(passwordEncoder.encode(password));
        identityRepository.save(identity);
    }

    public void lockIdentity(String email) {
        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(email);
        identity.setLocked(true);
        identityRepository.save(identity);
    }

    public boolean checkPassword(String username, String password) {
        UserDetails userDetails = loadUserByUsername(username);
        return passwordEncoder.matches(password, userDetails.getPassword());
    }

    public void updatePasswordAndRevokeTokens(Identity identity, String password) {
        identity.setPassword(passwordEncoder.encode(password));
        identityRepository.save(identity);
        revokeAccessTokens(identity);
        notifyService.notify(identity.getEmail(), updatePasswordEmailTemplateId );
    }

    public void revokeAccessTokens(Identity identity) {
        tokenRepository.findAllByUserName(identity.getUid())
                .forEach(token -> tokenServices.revokeToken(token.getToken().getValue()));
    }

    public boolean checkEmailExists(String email) {
        return identityRepository.existsByEmail(email);
    }


    public Identity setLastLoggedIn(Instant datetime, Identity identity) {
        identity.setLastLoggedIn(datetime);
        return identityRepository.save(identity);
    }

    public void updateEmailAddress(Identity identity, String email) {
        Identity savedIdentity = identityRepository.findById(identity.getId())
                .orElseThrow(() -> new IdentityNotFoundException("No such identity: " + identity.getId()));

        savedIdentity.setEmail(email);

        identityRepository.save(savedIdentity);
    }

    public boolean isWhitelistedDomain(String domain) {
        return Arrays.asList(whitelistedDomains).contains(domain);
    }

    public String getDomainFromEmailAddress(String emailAddress) {
        return emailAddress.substring(emailAddress.indexOf('@') + 1);
    }
}
