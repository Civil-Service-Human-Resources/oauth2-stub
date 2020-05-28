package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.AgencyTokenCapacityUsedDto;
import uk.gov.cshr.repository.IdentityRepository;

@Slf4j
@Service
public class AgencyTokenCapacityService {

    private IdentityRepository identityRepository;

    public AgencyTokenCapacityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public boolean hasSpaceAvailable(AgencyToken agencyToken) {
        Long spacesUsed = identityRepository.countByAgencyTokenUid(agencyToken.getUid());

        log.debug("Agency token uid={}, capacity={}, spaces used={}", agencyToken.getUid(), agencyToken.getCapacity(), spacesUsed);

        return (agencyToken.getCapacity() - spacesUsed) > 0;
    }

    public AgencyTokenCapacityUsedDto getSpacesUsedByAgencyToken(String uid) {
        return new AgencyTokenCapacityUsedDto(identityRepository.countByAgencyTokenUid(uid));
    }

    public Long getCountOfAgencyByUid(String uid) {
        return identityRepository.countByAgencyTokenUid(uid);
    }

    public void deleteAgencyToken(String agencyTokenUid) {
        identityRepository.removeAgencyToken(agencyTokenUid);
    }
}
