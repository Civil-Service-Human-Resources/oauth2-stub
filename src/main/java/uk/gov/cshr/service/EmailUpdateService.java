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
    private final String updateEmailTemplateId;
    private final String inviteUrlFormat;

    public EmailUpdateService(EmailUpdateRepository emailUpdateRepository,
                              EmailUpdateFactory emailUpdateFactory,
                              @Qualifier("notifyServiceImpl") NotifyService notifyService,
                              IdentityService identityService,
                              @Value("${govNotify.template.emailUpdate}") String updateEmailTemplateId,
                              @Value("${emailUpdate.urlFormat}") String inviteUrlFormat) {
        this.emailUpdateRepository = emailUpdateRepository;
        this.emailUpdateFactory = emailUpdateFactory;
        this.notifyService = notifyService;
        this.identityService = identityService;
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

    public boolean existsByCode(String code) {
        return emailUpdateRepository.existsByCode(code);
    }

    public EmailUpdate getEmailUpdateByCode(String code) {
        return emailUpdateRepository.findByCode(code)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEmailAddress(EmailUpdate emailUpdate) {
        updateEmailAddress(emailUpdate, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEmailAddress(EmailUpdate emailUpdate, AgencyToken agencyToken) {
        Identity emailUpdateIdentity = emailUpdate.getIdentity();
        Identity existingIdentity = identityService.getIdentityByEmail(emailUpdateIdentity.getEmail());

        String newEmail = emailUpdate.getEmail();

        log.debug("Updating email address for: oldEmail = {}, newEmail = {}", existingIdentity.getEmail(), newEmail);

        identityService.updateEmailAddress(existingIdentity, newEmail, agencyToken);

        emailUpdateRepository.delete(emailUpdate);

        log.debug("Email address {} has been updated to {} successfully", existingIdentity.getEmail(), newEmail);

        log.debug("Deleting emailUpdateObject: {}", emailUpdate);
    }
}
