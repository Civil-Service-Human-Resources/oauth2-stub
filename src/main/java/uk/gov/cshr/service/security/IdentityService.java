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
import uk.gov.cshr.utils.SpringUserUtils;

import javax.servlet.http.HttpServletRequest;
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
    private final SpringUserUtils springUserUtils;
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
                           SpringUserUtils springUserUtils,
                           @Value("${invite.whitelist.domains}") String[] whitelistedDomains, AgencyTokenCapacityService agencyTokenCapacityService) {
        this.updatePasswordEmailTemplateId = updatePasswordEmailTemplateId;
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenServices = tokenServices;
        this.tokenRepository = tokenRepository;
        this.notifyService = notifyService;
        this.csrsService = csrsService;
        this.springUserUtils = springUserUtils;
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

    public void updateEmailAddressAndEmailRecentlyUpdatedFlagToTrue(Identity identity, String email) {
        Identity savedIdentity = identityRepository.findById(identity.getId())
                .orElseThrow(() -> new IdentityNotFoundException("No such identity: " + identity.getId()));

        savedIdentity.setEmail(email);
        savedIdentity.setEmailRecentlyUpdated(true);
        Identity updatedIdentity = identityRepository.save(savedIdentity);
        log.info("identity has been updated to have a recently updated email flag of: " + updatedIdentity.isEmailRecentlyUpdated());
    }

    public void resetRecentlyUpdatedEmailFlagToFalse(Identity identity) {
        Identity savedIdentity = identityRepository.findById(identity.getId())
                .orElseThrow(() -> new IdentityNotFoundException("No such identity: " + identity.getId()));
        savedIdentity.setEmailRecentlyUpdated(false);
        Identity updatedIdentity = identityRepository.save(savedIdentity);
        log.info("identity has been updated to have a recently updated email flag of: " + updatedIdentity.isEmailRecentlyUpdated());
    }

    public boolean getRecentlyUpdatedEmailFlag(Identity identity) {
        Identity savedIdentity = identityRepository.findById(identity.getId())
                .orElseThrow(() -> new IdentityNotFoundException("No such identity: " + identity.getId()));
        log.info("found identity, email recently updated flag is: " + savedIdentity.isEmailRecentlyUpdated());
        return savedIdentity.isEmailRecentlyUpdated();
    }

    public boolean isWhitelistedDomain(String domain) {
        return Arrays.asList(whitelistedDomains).contains(domain);
    }

    public String getDomainFromEmailAddress(String emailAddress) {
        return emailAddress.substring(emailAddress.indexOf('@') + 1);
    }

    public void updateSpringWithRecentlyEmailUpdatedFlag(HttpServletRequest request, boolean emailUpdatedFlag) {
        // update spring authentication and spring session
        Identity identityFromSpringAuth = springUserUtils.getIdentityFromSpringAuthentication();
        identityFromSpringAuth.setEmailRecentlyUpdated(emailUpdatedFlag);
        springUserUtils.updateSpringAuthenticationAndSpringSessionWithUpdatedIdentity(request, identityFromSpringAuth);
    }

    public boolean checkValidEmail(String email) {
        final String domain = getDomainFromEmailAddress(email);

        if (isWhitelistedDomain(domain)) {
            return true;
        } else {
            AgencyToken[] agencyTokensForDomain = csrsService.getAgencyTokensForDomain(domain);

            if (agencyTokensForDomain.length > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean requestHasTokenData(TokenRequest tokenRequest) {
        return hasData(tokenRequest.getDomain())
                && hasData(tokenRequest.getToken())
                && hasData(tokenRequest.getOrg());
    }

    private boolean hasData(String s) {
        return s != null && s.length() > 0;
    }
}
