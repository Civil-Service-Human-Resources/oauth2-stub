package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Value;
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

    @Transactional
    public void updateEmailAddress(Identity identity, String code) {
        EmailUpdate emailUpdate = emailUpdateRepository.findByIdentityAndCode(identity, code)
                .orElseThrow(() -> new InvalidCodeException(String.format("Code %s does not exist for identity %s", code, identity)));

        String oldDomain = identityService.getDomainFromEmailAddress(identity.getEmail());
        boolean isWhitelistedPersonBeforeEmailChange = identityService.isWhitelistedDomain(oldDomain);

        if(!isWhitelistedPersonBeforeEmailChange) {
            // work out the token
            String oldOrg = csrsService.getOrgCode(identity.getUid());
            Optional<AgencyToken> agencyToken = csrsService.getAgencyTokenForDomainAndOrganisation(oldDomain, oldOrg);
            if (agencyToken.isPresent()) {
                AgencyToken at = agencyToken.get();
                csrsService.updateSpacesAvailable(oldDomain, at.getToken(), oldOrg, true);
            } else {
                throw new ResourceNotFoundException();
            }
        }
        identityService.updateEmailAddress(identity, emailUpdate.getEmail());
        emailUpdateRepository.delete(emailUpdate);

    }

    @Transactional
    public void updateOrganisationAndResetFlag(String newOrgCode, String uid){
        csrsService.updateOrganisation(uid, newOrgCode);
        identityService.resetRecentlyUpdatedEmailFlag(uid);
    }

    @Transactional
    public void updateOrganisationUpdateAgencyTokenSpacesAndResetFlag(String newDomain, String newToken,
                                                                         String newOrgCode, String uid){
        csrsService.updateOrganisation(uid, newOrgCode);
        identityService.resetRecentlyUpdatedEmailFlag(uid);
        csrsService.updateSpacesAvailable(newDomain, newToken, newOrgCode, false);
    }
}
