package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.service.security.IdentityService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgencyTokenServiceTest {
    private static final String DOMAIN = "someone@kainos.com";

    @Mock
    private IdentityService identityService;

    @Mock
    private CsrsService csrsService;

    @InjectMocks
    private AgencyTokenService agencyTokenService;

    @Test
    public void givenAWhitelistedDomain_whenIsDomainWhiteListed_thenShouldReturnTrue() {
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(true);

        boolean actual = agencyTokenService.isDomainWhiteListed(DOMAIN);

        assertTrue(actual);
    }

    @Test
    public void givenANonWhitelistedDomain_whenIsDomainWhiteListed_thenShouldReturnFalse() {
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(false);

        boolean actual = agencyTokenService.isDomainWhiteListed(DOMAIN);

        assertFalse(actual);
    }

    @Test
    public void givenNonWhitelistedDomainWithAgencyTokenDomains_whenIsDomainAnAgencyTokenDomain_thenShouldReturnTrue() {
        AgencyToken [] agencyTokens = new AgencyToken[3];
        agencyTokens[0] = new AgencyToken();
        agencyTokens[1] = new AgencyToken();
        agencyTokens[2] = new AgencyToken();
        when(csrsService.getAgencyTokensForDomain(anyString())).thenReturn(agencyTokens);

        boolean actual = agencyTokenService.isDomainAnAgencyTokenDomain(DOMAIN);

        assertTrue(actual);
    }

    @Test
    public void givenNonWhitelistedDomainWithNoAgencyTokenDomains_whenIsDomainAnAgencyTokenDomain_thenShouldReturnFalse() {
        AgencyToken [] agencyTokens = new AgencyToken[0];
        when(csrsService.getAgencyTokensForDomain(anyString())).thenReturn(agencyTokens);

        boolean actual = agencyTokenService.isDomainAnAgencyTokenDomain(DOMAIN);

        assertFalse(actual);
    }

    @Test
    public void shouldReturnTrueIfDomainInAgency() {
        when(csrsService.isDomainInAgency(DOMAIN)).thenReturn(true);

        assertTrue(agencyTokenService.isDomainInAgencyToken(DOMAIN));
    }

    @Test
    public void shouldReturnFalseIfDomainInAgency() {
        when(csrsService.isDomainInAgency(DOMAIN)).thenReturn(false);

        assertFalse(agencyTokenService.isDomainInAgencyToken(DOMAIN));
    }
}
