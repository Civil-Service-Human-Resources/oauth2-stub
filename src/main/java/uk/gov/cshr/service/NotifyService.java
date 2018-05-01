package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;

@Service
@Transactional
public class NotifyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteService.class);
    private static final String EMAIL_PERMISSION = "email";
    private static final String ACTIVATION_URL_PERMISSION = "activationUrl";

    @Value("${govNotify.key}")
    private String govNotifyKey;

    public void notify(String email, String code, String templateId, String actionUrl) throws NotificationClientException {
        String activationUrl = String.format(actionUrl, code);

        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put(EMAIL_PERMISSION, email);
        personalisation.put(ACTIVATION_URL_PERMISSION, activationUrl);

        NotificationClient client = new NotificationClient(govNotifyKey);
        SendEmailResponse response = client.sendEmail(templateId, email, personalisation, "");

        LOGGER.info("Invite email sent: {}", response.getBody());
    }

}
