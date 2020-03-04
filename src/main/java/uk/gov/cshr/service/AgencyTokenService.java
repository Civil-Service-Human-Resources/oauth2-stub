package uk.gov.cshr.service;

import org.springframework.stereotype.Service;
import uk.gov.cshr.service.security.IdentityService;

@Service
public class AgencyTokenService {

    private IdentityService identityService;

    private CsrsService csrsService;

    public AgencyTokenService(IdentityService identityService, CsrsService csrsService) {
        this.identityService = identityService;
        this.csrsService = csrsService;
    }

    public boolean isDomainWhiteListed(String domain) {
        return identityService.isWhitelistedDomain(domain);
    }

    public boolean isDomainAnAgencyTokenDomain(String domain) {
       return numAgencyTokens(domain) > 0 ? true : false;
    }

    private int numAgencyTokens(String domain) {
        return csrsService.getAgencyTokensForDomain(domain).length;
    }
}
