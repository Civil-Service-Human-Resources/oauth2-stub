package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Slf4j
@Profile("runMocks")
@Service
public class NotifyServiceMockImpl implements NotifyService {

    @Override
    public void notify(String email, String code, String templateId, String actionUrl) throws NotificationClientException {
        log.info("calling mock notify with actionUrl");
    }

    @Override
    public void notify(String email, String templateId) {
        log.info("calling mock notify");
    }

    @Override
    public void notifyWithPersonalisation(String email, String templateId, Map<String, String> personalisation) {
        log.info("calling mock notify with personalisation");
    }
}
