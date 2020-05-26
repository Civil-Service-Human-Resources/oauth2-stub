package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.EmailUpdateRepository;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class EmailUpdateService {

    private final EmailUpdateRepository emailUpdateRepository;
    private final EmailUpdateFactory emailUpdateFactory;
    private final NotifyService notifyService;
    private final IdentityService identityService;
    private final CsrsService csrsService;
    private final AgencyTokenService agencyTokenService;
    private final String updateEmailTemplateId;
    private final String inviteUrlFormat;

    public EmailUpdateService(EmailUpdateRepository emailUpdateRepository,
                              EmailUpdateFactory emailUpdateFactory,
                              @Qualifier("notifyServiceImpl") NotifyService notifyService,
                              IdentityService identityService,
                              CsrsService csrsService,
                              AgencyTokenService agencyTokenService,
                              @Value("${govNotify.template.emailUpdate}") String updateEmailTemplateId,
                              @Value("${emailUpdate.urlFormat}") String inviteUrlFormat) {
        this.emailUpdateRepository = emailUpdateRepository;
        this.emailUpdateFactory = emailUpdateFactory;
        this.notifyService = notifyService;
        this.identityService = identityService;
        this.csrsService = csrsService;
        this.agencyTokenService = agencyTokenService;
        this.updateEmailTemplateId = updateEmailTemplateId;
        this.inviteUrlFormat = inviteUrlFormat;
    }

    public void saveEmailUpdateAndNotify(Identity identity, String email) {
        EmailUpdate emailUpdate = emailUpdateFactory.create(identity, email);
        emailUpdateRepository.save(emailUpdate);

        String activationUrl = String.format(inviteUrlFormat, emailUpdate.getCode());
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("activationUrl", activationUrl);

        notifyService.notifyWithPersonalisation(email, updateEmailTemplateId, personalisation);

        emailUpdate.getCode();
    }

    public boolean verifyEmailUpdateExists(Identity identity, String code) {
        return emailUpdateRepository.existsByIdentityAndCode(identity, code);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEmailAddress(HttpServletRequest request, Identity identity, EmailUpdate emailUpdate) {
        updateEmailAddress(request, identity, emailUpdate, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEmailAddress(HttpServletRequest request, Identity identity, EmailUpdate emailUpdate, AgencyToken agencyToken) {
        String newEmail = emailUpdate.getEmail();

        log.debug("Updating email address for: oldEmail = {}, newEmail = {}", identity.getEmail(), newEmail);

        identityService.updateEmailAddress(identity, newEmail, agencyToken);

        identityService.updateSpringWithRecentlyEmailUpdatedFlag(request, true);

        emailUpdateRepository.delete(emailUpdate);

        log.debug("Email address {} has been updated to {} successfully", identity.getEmail(), newEmail);

        log.debug("Deleting emailUpdateObject: {}", emailUpdate.toString());
    }

    // TODO: 24/05/2020 DELETE
    @Transactional(rollbackFor = Exception.class)
    public void processEmailUpdatedRecentlyRequestForWhiteListedDomainUser(HttpServletRequest request, Identity identity) {
        // update identity in the db
        identityService.resetRecentlyUpdatedEmailFlagToFalse(identity);
        // update spring
        identityService.updateSpringWithRecentlyEmailUpdatedFlag(request, false);
    }

    // TODO: 24/05/2020 DELETE
    @Transactional(rollbackFor = Exception.class)
    public void processEmailUpdatedRecentlyRequestForAgencyTokenUser(String newDomain, String newToken,
                                                                     String newOrgCode, Identity identity, HttpServletRequest request) {
        csrsService.updateSpacesAvailable(newDomain, newToken, newOrgCode, false);
        // update identity in the db
        identityService.resetRecentlyUpdatedEmailFlagToFalse(identity);
        // update spring
        identityService.updateSpringWithRecentlyEmailUpdatedFlag(request, false);
    }

    public EmailUpdate getEmailUpdate(Identity identity, String code) {
        return emailUpdateRepository.findByIdentityAndCode(identity, code)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
