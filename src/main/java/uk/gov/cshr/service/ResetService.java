package uk.gov.cshr.service;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.service.notify.NotificationClientException;

import java.util.Date;

@Service
@Transactional
public class ResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetService.class);

    @Value("${govNotify.template.reset}")
    private String govNotifyResetTemplateId;

    @Value("${govNotify.template.resetSuccessful}")
    private String govNotifySuccessfulResetTemplateId;

    @Value("${reset.url}")
    private String resetUrlFormat;

    @Value("${reset.validityInSeconds}")
    private int validityInSeconds;

    private ResetRepository resetRepository;

    private NotifyService notifyService;

    @Autowired
    public ResetService(ResetRepository resetRepository, @Qualifier("notifyServiceImpl") NotifyService notifyService) {
        this.resetRepository = resetRepository;
        this.notifyService = notifyService;
    }

    public boolean isResetExpired(Reset reset) {
        long diffInMs = new Date().getTime() - reset.getRequestedAt().getTime();

        if (diffInMs > validityInSeconds * 1000 && reset.getResetStatus().equals(ResetStatus.PENDING)) {
            reset.setResetStatus(ResetStatus.EXPIRED);
            resetRepository.save(reset);
            return true;
        }

        return false;
    }

    public boolean isResetPending(Reset reset) {
        return reset.getResetStatus().equals(ResetStatus.PENDING);
    }

    public void notifyForResetRequest(String email) throws NotificationClientException {
        Reset reset = new Reset();
        reset.setEmail(email);
        reset.setRequestedAt(new Date());
        reset.setResetStatus(ResetStatus.PENDING);
        reset.setCode(RandomStringUtils.random(40, true, true));

        notifyService.notify(reset.getEmail(), reset.getCode(), govNotifyResetTemplateId, resetUrlFormat);

        resetRepository.save(reset);

        LOGGER.info("Reset request sent to {} ", email);
    }

    public void notifyOfSuccessfulReset(Reset reset) throws NotificationClientException {
        reset.setResetAt(new Date());
        reset.setResetStatus(ResetStatus.RESET);

        notifyService.notify(reset.getEmail(), reset.getCode(), govNotifySuccessfulResetTemplateId, resetUrlFormat);

        resetRepository.save(reset);

        LOGGER.info("Reset success sent to {} ", reset.getEmail());
    }
}
