package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.cshr.service.security.IdentityService;

@Service
public class AgencyTokenService {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private CsrsService csrsService;

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
