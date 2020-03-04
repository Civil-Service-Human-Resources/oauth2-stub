package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.InvalidCodeException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.EmailUpdateRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class EmailUpdateService {

    private final EmailUpdateRepository emailUpdateRepository;
    private final EmailUpdateFactory emailUpdateFactory;
    private final NotifyService notifyService;
    private final IdentityService identityService;
    private final CsrsService csrsService;
    private final String updateEmailTemplateId;
    private final String inviteUrlFormat;

    public EmailUpdateService(EmailUpdateRepository emailUpdateRepository,
                              EmailUpdateFactory emailUpdateFactory,
                              @Qualifier("notifyServiceImpl") NotifyService notifyService,
                              IdentityService identityService,
                              CsrsService csrsService,
                              @Value("${govNotify.template.emailUpdate}") String updateEmailTemplateId,
                              @Value("${emailUpdate.urlFormat}") String inviteUrlFormat) {
        this.emailUpdateRepository = emailUpdateRepository;
        this.emailUpdateFactory = emailUpdateFactory;
        this.notifyService = notifyService;
        this.identityService = identityService;
        this.csrsService = csrsService;
        this.updateEmailTemplateId = updateEmailTemplateId;
        this.inviteUrlFormat = inviteUrlFormat;
    }

    public String saveEmailUpdateAndNotify(Identity identity, String email) {
        EmailUpdate emailUpdate = emailUpdateFactory.create(identity, email);
        emailUpdateRepository.save(emailUpdate);

        String activationUrl = String.format(inviteUrlFormat, emailUpdate.getCode());
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("activationUrl", activationUrl);

        notifyService.notifyWithPersonalisation(email, updateEmailTemplateId, personalisation);

        return emailUpdate.getCode();
    }

    public boolean verifyCode(Identity identity, String code) {
        return emailUpdateRepository.findByIdentityAndCode(identity, code).isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEmailAddress(Identity identity, String code) {
        EmailUpdate emailUpdate = emailUpdateRepository.findByIdentityAndCode(identity, code)
                .orElseThrow(() -> new InvalidCodeException(String.format("Code %s does not exist for identity %s", code, identity)));

        String oldDomain = identityService.getDomainFromEmailAddress(identity.getEmail());
        boolean isWhitelistedPersonBeforeEmailChange = identityService.isWhitelistedDomain(oldDomain);

        // remove from old agency token quota
        if (!isWhitelistedPersonBeforeEmailChange) {
            // work out the token
            log.info("user used to be a token person, work out which token and remove them from that quota");
            String oldOrg = csrsService.getOrgCode(identity.getUid());
            if (oldOrg != null) {
                log.info("old org code found is:" + oldOrg);
            }

            AgencyToken agencyToken = csrsService.getAgencyTokenForDomainAndOrganisation(oldDomain, oldOrg)
                    .orElseThrow(() -> new ResourceNotFoundException());
            csrsService.updateSpacesAvailable(oldDomain, agencyToken.getToken(), oldOrg, true);

        } else {
            log.info("user used to be a whitelisted person, no agency token to update");
        }
        log.info("updating email address on users identity");
        identityService.updateEmailAddressAndEmailRecentlyUpdatedFlagToTrue(identity, emailUpdate.getEmail());
        log.info("deleting the email update config for this user");
        emailUpdateRepository.delete(emailUpdate);
        log.info("all ok");
    }

    @Transactional(rollbackFor = Exception.class)
    public void processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(Identity identity) {
        identityService.resetRecentlyUpdatedEmailFlagToFalse(identity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void processEmailUpdatedRecentlyRequestForAgencyTokenUser(String newDomain, String newToken,
                                                                     String newOrgCode, Identity identity) {
        csrsService.updateSpacesAvailable(newDomain, newToken, newOrgCode, false);
        identityService.resetRecentlyUpdatedEmailFlagToFalse(identity);
    }

}
