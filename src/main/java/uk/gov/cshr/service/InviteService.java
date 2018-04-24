package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Status;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Date;
import java.util.HashMap;

public class InviteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteService.class);
    private static final String EMAIL_PERMISSION = "email";
    private static final String ACTIVATION_URL_PERMISSION = "activationUrl";
    private static final long EXPIRATION_IN_MILLISECONDS = 86400000; // 24hrs = 86400000ms

    @Autowired
    private InviteRepository inviteRepository;

    @Value("${api.key}")
    private String api;

    @Value("${template.id}")
    private String templateId;

    private String baseUrl = "http://localhost:8081/signup/";

    @Autowired
    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    public InviteService() {
    }

    public boolean hasEmailBeenInvitedBefore(String email) {
        if (inviteRepository.findByForEmail(email) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void sendEmail(Invite invite) throws NotificationClientException {
        StringBuilder activationUrl = new StringBuilder();
        activationUrl.append(baseUrl);
        activationUrl.append(invite.getCode());

        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put(EMAIL_PERMISSION, invite.getForEmail());
        personalisation.put(ACTIVATION_URL_PERMISSION, activationUrl.toString());

        NotificationClient client = new NotificationClient(api);
        SendEmailResponse response = client.sendEmail(templateId, invite.getForEmail(), personalisation, "");

        LOGGER.info(response.getBody());
    }

    public void saveInvite(Invite invite) {
        inviteRepository.save(invite);
    }

    public void updateInviteByCode(String code, Status newStatus) {
        Invite invite = inviteRepository.findByCode(code);
        invite.setStatus(newStatus);
        inviteRepository.save(invite);
    }

    public boolean isCodeExpired(String code) {
        Invite invite = inviteRepository.findByCode(code);
        long diffInMs = new Date().getTime() - invite.getInvitedAt().getTime();

        if (diffInMs > EXPIRATION_IN_MILLISECONDS && invite.getStatus().equals(Status.PENDING)) {
            updateInviteByCode(code, Status.ACCEPTED);
            return true;
        } else {
            updateInviteByCode(code, Status.EXPIRED);
            return false;
        }
    }
}