package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.domain.factory.InviteFactory;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.service.notify.NotificationClientException;

import java.util.Date;
import java.util.Set;

@Service
@Transactional
public class InviteService {
    private final String govNotifyInviteTemplateId;
    private final int validityInSeconds;
    private final String signupUrlFormat;
    private final NotifyService notifyService;
    private final InviteRepository inviteRepository;
    private final InviteFactory inviteFactory;

    public InviteService(
            @Value("${govNotify.template.invite}") String govNotifyInviteTemplateId,
            @Value("${invite.validityInSeconds}") int validityInSeconds,
            @Value("${invite.url}") String signupUrlFormat,
            NotifyService notifyService,
            InviteRepository inviteRepository,
            InviteFactory inviteFactory) {
        this.govNotifyInviteTemplateId = govNotifyInviteTemplateId;
        this.validityInSeconds = validityInSeconds;
        this.signupUrlFormat = signupUrlFormat;
        this.notifyService = notifyService;
        this.inviteRepository = inviteRepository;
        this.inviteFactory = inviteFactory;
    }

    @ReadOnlyProperty
    public Invite findByCode(String code) {
        return inviteRepository.findByCode(code);
    }

    public boolean isCodeExpired(String code) {
        Invite invite = inviteRepository.findByCode(code);
        long diffInMs = new Date().getTime() - invite.getInvitedAt().getTime();

        if (diffInMs > validityInSeconds * 1000 && invite.getStatus().equals(InviteStatus.PENDING)) {
            updateInviteByCode(code, InviteStatus.ACCEPTED);
            return true;
        }

        updateInviteByCode(code, InviteStatus.EXPIRED);
        return false;
    }

    public void updateInviteByCode(String code, InviteStatus newStatus) {
        Invite invite = inviteRepository.findByCode(code);
        invite.setStatus(newStatus);
        inviteRepository.save(invite);
    }

    public void createNewInviteForEmailAndRoles(String email, Set<Role> roleSet, Identity inviter)
            throws NotificationClientException {
        Invite invite = inviteFactory.create(email, roleSet, inviter);

        notifyService.notify(invite.getForEmail(), invite.getCode(), govNotifyInviteTemplateId, signupUrlFormat);

        inviteRepository.save(invite);
    }

    public void sendSelfSignupInvite(String email) throws NotificationClientException {
        Invite invite = inviteFactory.createSelfSignUpInvite(email);

        // temporary, delete later
        System.out.println(" * * * invite code = " + invite.getCode());

        // commented out temporarily as can't use gov notify from local env yet
        //notifyService.notify(invite.getForEmail(), invite.getCode(), govNotifyInviteTemplateId, signupUrlFormat);

        inviteRepository.save(invite);
    }
}