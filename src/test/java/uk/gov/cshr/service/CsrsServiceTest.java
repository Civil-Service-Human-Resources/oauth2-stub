package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.OrganisationalUnitDto;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.ok;

@RunWith(MockitoJUnitRunner.class)
public class CsrsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String agencyTokensByDomainAndOrganisationFormat;
    private String organisationalUnitsFlatUrl;
    private String updateSpacesAvailableUrl;
    private String getOrganisationUrl;
    private CsrsService csrsService;

    @Before
    public void setUp() {
        agencyTokensFormat = "http://locahost:9002/agencyTokens?domain=%s&token=%s&code=%s";
        agencyTokensByDomainFormat = "http://locahost:9002/agencyTokens?domain=%s";
        agencyTokensByDomainAndOrganisationFormat = "http://locahost:9002/agencyTokens?domain=%s&code=%s";
        organisationalUnitsFlatUrl = "http://locahost:9002/organisationalUnits/flat";
        updateSpacesAvailableUrl = "http://locahost:9002/agencyTokens";
        getOrganisationUrl = "http://locahost:9002/civilServants/org?uid=%s";

        csrsService = new CsrsService(restTemplate,
                agencyTokensFormat, agencyTokensByDomainFormat, agencyTokensByDomainAndOrganisationFormat,
                organisationalUnitsFlatUrl, updateSpacesAvailableUrl, getOrganisationUrl);

        ReflectionTestUtils.setField(csrsService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(csrsService, "agencyTokensFormat", agencyTokensFormat);
        ReflectionTestUtils.setField(csrsService, "agencyTokensByDomainFormat", agencyTokensByDomainFormat);
        ReflectionTestUtils.setField(csrsService, "agencyTokensByDomainAndOrganisationFormat", agencyTokensByDomainAndOrganisationFormat);
        ReflectionTestUtils.setField(csrsService, "organisationalUnitsFlatUrl", organisationalUnitsFlatUrl);
        ReflectionTestUtils.setField(csrsService, "updateSpacesAvailableUrl", updateSpacesAvailableUrl);
        ReflectionTestUtils.setField(csrsService, "getOrganisationUrl", getOrganisationUrl);
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
    public void givenAgencyTokenDomainAndOrganisation_whenGetAgencyTokenForDomainAndOrganisation_thenShouldReturnAgencyToken() {
        // given
        AgencyToken agencyToken = buildAgencyToken();
        String domain = "example.com";
        String code = "code";
        when(restTemplate.getForObject(String.format(agencyTokensByDomainAndOrganisationFormat, domain, code), AgencyToken.class)).thenReturn(agencyToken);

        // when
        Optional<AgencyToken> actual = csrsService.getAgencyTokenForDomainAndOrganisation(domain, code);

        // then
        assertThat(actual.get().getToken(), equalTo(agencyToken.getToken()));
        assertThat(actual.get().getCapacity(), equalTo(100));
        assertThat(actual.get().getCapacityUsed(), equalTo(11));
    }

    @Test
    public void givenDomainAndOrganisationWithNoAgencyToken_whenGetAgencyTokenForDomainAndOrganisation_thenShouldReturnEmpty() {
        // given
        AgencyToken agencyToken = buildAgencyToken();
        String domain = "example.com";
        String code = "code";
        when(restTemplate.getForObject(String.format(agencyTokensByDomainAndOrganisationFormat, domain, code), AgencyToken.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // when
        Optional<AgencyToken> actual = csrsService.getAgencyTokenForDomainAndOrganisation(domain, code);

        // then
        assertFalse(actual.isPresent());
    }

    @Test
    public void givenAValidUID_whenGetOrgCode_thenShouldReturnSuccessfully() {
        // given
        String uid = "myuid";
        when(restTemplate.getForEntity(String.format(getOrganisationUrl, uid), String.class)).thenReturn(new ResponseEntity<String>("co", HttpStatus.OK));

        // when
        String actual = csrsService.getOrgCode(uid);

        // then
        assertThat(actual, equalTo("co"));
    }

    @Test
    public void givenAInvalidUID_whenGetOrgCode_thenShouldReturnEmptyString() {
        // given
        String uid = "myuid";
        when(restTemplate.getForEntity(String.format(getOrganisationUrl, uid), String.class)).thenThrow(new RuntimeException());

        // when
        String actual = csrsService.getOrgCode(uid);

        // then
        assertThat(actual, equalTo(""));
    }

    private AgencyToken buildAgencyToken() {
        AgencyToken at = new AgencyToken();
        at.setToken("token123");
        at.setCapacity(100);
        at.setCapacityUsed(11);
        return at;
    }

}
