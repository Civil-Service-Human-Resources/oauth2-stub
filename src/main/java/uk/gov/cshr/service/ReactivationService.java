package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.ReactivationRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.util.Date;

@Slf4j
@Service
public class ReactivationService {

    private ReactivationRepository reactivationRepository;
    private IdentityService identityService;

    public ReactivationService(ReactivationRepository reactivationRepository,
                               IdentityService identityService) {
        this.reactivationRepository = reactivationRepository;
        this.identityService = identityService;
    }

    public Reactivation getReactivationByCodeAndStatus(String code, ReactivationStatus reactivationStatus) {
        return reactivationRepository
                .findFirstByCodeAndReactivationStatusEquals(code, reactivationStatus)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public boolean existsByCodeAndStatus(String code, ReactivationStatus reactivationStatus) {
        return reactivationRepository
                .existsByCodeAndReactivationStatusEquals(code, reactivationStatus);
    }

    public void reactivateIdentity(Reactivation reactivation) {
        reactivateIdentity(reactivation, null);
    }

    public void reactivateIdentity(Reactivation reactivation, AgencyToken agencyToken) {
        Identity identity = identityService.getIdentityByEmail(reactivation.getEmail());
        identityService.reactivateIdentity(identity, agencyToken);

        reactivation.setReactivationStatus(ReactivationStatus.REACTIVATED);
        reactivation.setReactivatedAt(new Date());

        log.debug("Identity reactivated for Reactivation: {}", reactivation.toString());
        reactivationRepository.save(reactivation);
    }
}
