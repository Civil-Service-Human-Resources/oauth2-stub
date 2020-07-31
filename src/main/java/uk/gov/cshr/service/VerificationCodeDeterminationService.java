package uk.gov.cshr.service;

import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.*;
import uk.gov.cshr.exception.VerificationCodeTypeNotFound;

@Service
public class VerificationCodeDeterminationService {

    private final EmailUpdateService emailUpdateService;

    private final ReactivationService reactivationService;


    public VerificationCodeDeterminationService(EmailUpdateService emailUpdateService,
                                                ReactivationService reactivationService) {
        this.emailUpdateService = emailUpdateService;
        this.reactivationService = reactivationService;
    }

    public VerificationCodeDetermination getCodeType(String code) {
        if (emailUpdateService.existsByCode(code)) {
            EmailUpdate emailUpdate = emailUpdateService.getEmailUpdateByCode(code);

            return new VerificationCodeDetermination(emailUpdate.getEmail(), VerificationCodeType.EMAIL_UPDATE);
        } else if (reactivationService.existsByCodeAndStatus(code, ReactivationStatus.PENDING)) {
            Reactivation reactivation = reactivationService.getReactivationByCodeAndStatus(code, ReactivationStatus.PENDING);

            return new VerificationCodeDetermination(reactivation.getEmail(), VerificationCodeType.REACTIVATION);
        } else {
            throw new VerificationCodeTypeNotFound(String.format("Verification code type not found for code: %s", code));
        }
    }
}
