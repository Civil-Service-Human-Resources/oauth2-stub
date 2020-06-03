package uk.gov.cshr.service.security;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.cshr.domain.*;
import uk.gov.cshr.exception.IdentityNotFoundException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.exception.UnableToAllocateAgencyTokenException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.TokenRepository;
import uk.gov.cshr.service.AgencyTokenCapacityService;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.NotifyService;

import java.time.Instant;
import java.util.*;

@Slf4j
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
    private final CsrsService csrsService;
    private InviteService inviteService;
    private String[] whitelistedDomains;
    private AgencyTokenCapacityService agencyTokenCapacityService;

    public IdentityService(@Value("${govNotify.template.passwordUpdate}") String updatePasswordEmailTemplateId,
                           IdentityRepository identityRepository,
                           PasswordEncoder passwordEncoder,
                           TokenServices tokenServices,
                           @Qualifier("tokenRepository") TokenRepository tokenRepository,
                           @Qualifier("notifyServiceImpl") NotifyService notifyService,
                           CsrsService csrsService,
                           @Value("${invite.whitelist.domains}") String[] whitelistedDomains, AgencyTokenCapacityService agencyTokenCapacityService) {
        this.updatePasswordEmailTemplateId = updatePasswordEmailTemplateId;
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenServices = tokenServices;
        this.tokenRepository = tokenRepository;
        this.notifyService = notifyService;
        this.csrsService = csrsService;
        this.whitelistedDomains = whitelistedDomains;
        this.agencyTokenCapacityService = agencyTokenCapacityService;
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

    @Transactional(noRollbackFor = UnableToAllocateAgencyTokenException.class)
    public void createIdentityFromInviteCode(String code, String password, TokenRequest tokenRequest) {
        Invite invite = inviteService.findByCode(code);

        Set<Role> newRoles = new HashSet<>(invite.getForRoles());

        String agencyTokenUid = null;
        if (requestHasTokenData(tokenRequest)) {
            Optional<AgencyToken> agencyTokenForDomainTokenOrganisation = csrsService.getAgencyTokenForDomainTokenOrganisation(tokenRequest.getDomain(), tokenRequest.getToken(), tokenRequest.getOrg());

            agencyTokenUid = agencyTokenForDomainTokenOrganisation
                    .map(agencyToken -> {
                        if (agencyTokenCapacityService.hasSpaceAvailable(agencyToken)) {
                            return agencyToken.getUid();
                        } else {
                            throw new UnableToAllocateAgencyTokenException("Agency token uid " + agencyToken.getUid() + " has no spaces available. Identity not created");
                        }
                    })
                    .orElseThrow(ResourceNotFoundException::new);

            log.info("Identity request has agency uid = {}", agencyTokenUid);
        }

        Identity identity = new Identity(UUID.randomUUID().toString(),
                invite.getForEmail(),
                passwordEncoder.encode(password),
                true,
                false,
                newRoles,
                Instant.now(),
                false,
                false,
                agencyTokenUid);

        identityRepository.save(identity);

        LOGGER.debug("New identity email = {} successfully created", identity.getEmail());
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

    public void reactivateIdentity(Identity identity, AgencyToken agencyToken) {
        identity.setActive(true);

        if (agencyToken != null && agencyToken.getUid() != null) {
            identity.setAgencyTokenUid(agencyToken.getUid());
        }
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

    public void updateEmailAddress(Identity identity, String email, AgencyToken newAgencyToken) {
        Identity savedIdentity = identityRepository.findById(identity.getId())
                .orElseThrow(() -> new IdentityNotFoundException("No such identity: " + identity.getId()));

        if (newAgencyToken != null && newAgencyToken.getUid() != null) {
            log.debug("Updating agency token for user: oldAgencyToken = {}, newAgencyToken = {}", identity.getAgencyTokenUid(), newAgencyToken.getUid());
            savedIdentity.setAgencyTokenUid(newAgencyToken.getUid());
        } else {
            log.debug("Setting existing agency token UID to null");
            savedIdentity.setAgencyTokenUid(null);
        }

        savedIdentity.setEmail(email);
        identityRepository.save(savedIdentity);
    }

    public boolean isWhitelistedDomain(String domain) {
        return Arrays.asList(whitelistedDomains).contains(domain);
    }

    public String getDomainFromEmailAddress(String emailAddress) {
        return emailAddress.substring(emailAddress.indexOf('@') + 1);
    }

    public boolean checkValidEmail(String email) {
        final String domain = getDomainFromEmailAddress(email);

        return (isWhitelistedDomain(domain) || csrsService.isDomainInAgency(domain));
    }

    private boolean requestHasTokenData(TokenRequest tokenRequest) {
        return hasData(tokenRequest.getDomain())
                && hasData(tokenRequest.getToken())
                && hasData(tokenRequest.getOrg());
    }

    private boolean hasData(String s) {
        return s != null && s.length() > 0;
    }

    public Identity getIdentityByEmail(String email) {
        return identityRepository.findFirstByEmailEquals(email);
    }
}
