package uk.gov.cshr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Collections;
import java.util.HashMap;

@Service
@Transactional
public class NotifyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteService.class);
    private static final String EMAIL_PERMISSION = "email";
    private static final String ACTIVATION_URL_PERMISSION = "activationUrl";

    private final NotificationClient notificationClient;

    public NotifyService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void notify(String email, String code, String templateId, String actionUrl) throws NotificationClientException {
        String activationUrl = String.format(actionUrl, code);

        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put(EMAIL_PERMISSION, email);
        personalisation.put(ACTIVATION_URL_PERMISSION, activationUrl);

        SendEmailResponse response = notificationClient.sendEmail(templateId, email, personalisation, "");

        LOGGER.info("Notify email sent to: {}", response.getBody());
    }


    public void notify(String email, String templateId) {
        try {
            SendEmailResponse response =
                    notificationClient.sendEmail(templateId, email, Collections.emptyMap(), null);
            LOGGER.info("Update password notification sent to: {}", response.getBody());
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }
}
