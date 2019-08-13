package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.OrganisationalUnitDto;

import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsrsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String organisationalUnitsFlatUrl;
    private CsrsService csrsService;

    @Before
    public void setUp() {
        agencyTokensFormat = "http://locahost:9002/agencyTokens?domain=%s&token=%s&code=%s";
        agencyTokensByDomainFormat = "http://locahost:9002/agencyTokens?domain=%s";
        organisationalUnitsFlatUrl = "http://locahost:9002/organisationalUnits/flat";

        csrsService = new CsrsService(restTemplate, agencyTokensFormat, agencyTokensByDomainFormat, organisationalUnitsFlatUrl);
    }

    @Test
    public void shouldReturnAgencyTokenForDomainTokenOrganisation() {
        AgencyToken agencyToken = new AgencyToken();
        String domain = "example.com";
        String token = "token123";
        String code = "code";
        Optional<AgencyToken> optionalAgencyToken = Optional.of(agencyToken);

        when(restTemplate.getForObject(String.format(agencyTokensFormat, domain, token, code), AgencyToken.class)).thenReturn(agencyToken);

        assertEquals(optionalAgencyToken, csrsService.getAgencyTokenForDomainTokenOrganisation(domain, token, code));
    }

    @Test
    public void shouldReturnListOfAgencyTokensForDomain() {
        AgencyToken agencyToken1 = new AgencyToken();
        AgencyToken agencyToken2 = new AgencyToken();
        AgencyToken[] agencyTokenArray = new AgencyToken[] { agencyToken1, agencyToken2 };
        String domain = "example.com";

        when(restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), AgencyToken[].class)).thenReturn(agencyTokenArray);

        assertArrayEquals(agencyTokenArray, csrsService.getAgencyTokensForDomain(domain));
    }

    @Test
    public void shouldReturnListOfOrganisations() {
        OrganisationalUnitDto organisationalUnitDto1 = new OrganisationalUnitDto();
        OrganisationalUnitDto organisationalUnitDto2 = new OrganisationalUnitDto();
        OrganisationalUnitDto[] organisationalUnitDtoArray = new OrganisationalUnitDto[] { organisationalUnitDto1, organisationalUnitDto2 };

        when(restTemplate.getForObject(organisationalUnitsFlatUrl, OrganisationalUnitDto[].class)).thenReturn(organisationalUnitDtoArray);

        assertArrayEquals(organisationalUnitDtoArray, csrsService.getOrganisationalUnitsFormatted());
    }
}
