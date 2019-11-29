package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.service.security.IdentityService;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgencyTokenServiceTest {

    @Mock
    private IdentityService identityService;

    @Mock
    private CsrsService csrsService;

    @InjectMocks
    private AgencyTokenService classUnderTest;

    private static final String DOMAIN = "someone@kainos.com";

    @Test
    public void givenAWhitelistedDomain_whenIsDomainWhiteListed_thenShouldReturnTrue() {
        // given
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(true);

        // when
        boolean actual = classUnderTest.isDomainWhiteListed(DOMAIN);

        // then
        assertTrue(actual);
    }

    @Test
    public void givenANonWhitelistedDomain_whenIsDomainWhiteListed_thenShouldReturnFalse() {
        // given
        when(identityService.isWhitelistedDomain(anyString())).thenReturn(false);

        // when
        boolean actual = classUnderTest.isDomainWhiteListed(DOMAIN);

        // then
        assertFalse(actual);
    }

    @Test
    public void givenNonWhitelistedDomainWithAgencyTokenDomains_whenIsDomainAnAgencyTokenDomain_thenShouldReturnTrue() {
        // given
        AgencyToken [] agencyTokens = new AgencyToken[3];
        agencyTokens[0] = new AgencyToken();
        agencyTokens[1] = new AgencyToken();
        agencyTokens[2] = new AgencyToken();
        when(csrsService.getAgencyTokensForDomain(anyString())).thenReturn(agencyTokens);

        // when
        boolean actual = classUnderTest.isDomainAnAgencyTokenDomain(DOMAIN);

        // then
        assertTrue(actual);
    }

    @Test
    public void givenNonWhitelistedDomainWithNoAgencyTokenDomains_whenIsDomainAnAgencyTokenDomain_thenShouldReturnFalse() {
        // given
        AgencyToken [] agencyTokens = new AgencyToken[0];
        when(csrsService.getAgencyTokensForDomain(anyString())).thenReturn(agencyTokens);

        // when
        boolean actual = classUnderTest.isDomainAnAgencyTokenDomain(DOMAIN);

        // then
        assertFalse(actual);
    }

}
