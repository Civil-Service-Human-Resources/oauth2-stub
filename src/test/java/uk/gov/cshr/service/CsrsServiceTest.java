package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.OrganisationalUnitDto;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsrsServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RestTemplate restTemplate;

    private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String agencyTokensByDomainAndOrganisationFormat;
    private String organisationalUnitsFlatUrl;
    private CsrsService csrsService;

    @Before
    public void setUp() {
        agencyTokensFormat = "http://locahost:9002/agencyTokens?domain=%s&token=%s&code=%s";
        agencyTokensByDomainFormat = "http://locahost:9002/agencyTokens?domain=%s";
        agencyTokensByDomainAndOrganisationFormat = "http://locahost:9002/agencyTokens?domain=%s&code=%s";
        organisationalUnitsFlatUrl = "http://locahost:9002/organisationalUnits/flat";

        csrsService = new CsrsService(restTemplate,
                agencyTokensFormat, agencyTokensByDomainFormat, agencyTokensByDomainAndOrganisationFormat,
                organisationalUnitsFlatUrl);

        ReflectionTestUtils.setField(csrsService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(csrsService, "agencyTokensFormat", agencyTokensFormat);
        ReflectionTestUtils.setField(csrsService, "agencyTokensByDomainFormat", agencyTokensByDomainFormat);
        ReflectionTestUtils.setField(csrsService, "agencyTokensByDomainAndOrganisationFormat", agencyTokensByDomainAndOrganisationFormat);
        ReflectionTestUtils.setField(csrsService, "organisationalUnitsFlatUrl", organisationalUnitsFlatUrl);
    }

    @Test
    public void shouldReturnTrueIfDomainInAgency() {
        String domain = "example.com";

        Boolean expectedBoolean = true;

        when(restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), Boolean.class)).thenReturn(expectedBoolean);

        assertEquals(expectedBoolean, csrsService.isDomainInAgency(domain));
    }

    @Test
    public void shouldReturnFalseIfDomainInAgency() {
        String domain = "example.com";

        Boolean expectedBoolean = false;

        when(restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), Boolean.class)).thenReturn(expectedBoolean);

        assertEquals(expectedBoolean, csrsService.isDomainInAgency(domain));
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

    @Test
    public void givenDomainAndOrganisationWithNoAgencyToken_whenGetAgencyTokenForDomainAndOrganisation_thenShouldReturnEmpty() {
        // given
        String domain = "example.com";
        String code = "code";
        when(restTemplate.getForObject(String.format(agencyTokensByDomainAndOrganisationFormat, domain, code), AgencyToken.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // when
        Optional<AgencyToken> actual = csrsService.getAgencyTokenForDomainAndOrganisation(domain, code);

        // then
        assertFalse(actual.isPresent());
    }
}
