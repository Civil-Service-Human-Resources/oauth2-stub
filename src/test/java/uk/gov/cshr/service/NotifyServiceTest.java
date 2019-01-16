package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotifyServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotifyService notifyService;

    @Test
    public void shouldSendNotificationWithEmailAddressAndTemplateId() throws NotificationClientException {
        String email = "learner@domain.com";
        String templateId = "template-id";
        String body = "response-body";

        SendEmailResponse response = mock(SendEmailResponse.class);
        when(response.getBody()).thenReturn(body);

        when(notificationClient.sendEmail(templateId, email, Collections.emptyMap(), null)).thenReturn(response);

        notifyService.notify(email, templateId);

        verify(notificationClient).sendEmail(templateId, email, Collections.emptyMap(), null);
    }

    @Test
    public void shouldThrowNotificationException() throws NotificationClientException {
        String email = "learner@domain.com";
        String templateId = "template-id";

        NotificationClientException exception = mock(NotificationClientException.class);

        doThrow(exception).when(notificationClient).sendEmail(templateId, email, Collections.emptyMap(), null);

        try {
            notifyService.notify(email, templateId);
            fail("Expected NotificationException");
        } catch (NotificationException e) {
            assertEquals(exception, e.getCause());
        }
    }
}