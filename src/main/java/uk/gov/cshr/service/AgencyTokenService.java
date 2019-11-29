package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.service.security.IdentityService;

@Service
public class AgencyTokenService {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private CsrsService csrsService;

    public boolean isDomainWhiteListed(String domain) {
        if (identityService.isWhitelistedDomain(domain)) {
            return true;
        }
        return false;
    }

    public boolean isDomainAnAgencyTokenDomain(String domain) {
        AgencyToken[] agencyTokensForDomain = csrsService.getAgencyTokensForDomain(domain);
        if (agencyTokensForDomain.length > 0) {
            return true;
        }
        return false;
    }
}
