package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.dto.AgencyTokenDTO;
import uk.gov.cshr.exception.BadRequestException;
import uk.gov.cshr.exception.NotEnoughSpaceAvailableException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.exception.UnableToAllocateAgencyTokenException;

import java.util.Optional;

@Slf4j
@Service
public class CsrsService {
    private RestTemplate restTemplate;
    private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String agencyTokensByDomainAndOrganisationFormat;
    private String organisationalUnitsFlatUrl;
    private String updateSpacesAvailableUrl;
    private String getOrganisationUrl;

    public CsrsService(@Autowired RestTemplate restTemplate,
                       @Value("${registry.agencyTokensFormat}") String agencyTokensFormat,
                       @Value("${registry.agencyTokensByDomainFormat}") String agencyTokensByDomainFormat,
                       @Value("${registry.agencyTokensByDomainAndOrganisationFormat}") String agencyTokensByDomainAndOrganisationFormat,
                       @Value("${registry.organisationalUnitsFlatUrl}") String organisationalUnitsFlatUrl,
                       @Value("${registry.updateSpacesAvailableUrl}") String updateSpacesAvailableUrl,
                       @Value("${registry.getOrganisationUrl}") String getOrganisationUrl) {
        this.restTemplate = restTemplate;
        this.agencyTokensFormat = agencyTokensFormat;
        this.agencyTokensByDomainFormat = agencyTokensByDomainFormat;
        this.agencyTokensByDomainAndOrganisationFormat = agencyTokensByDomainAndOrganisationFormat;
        this.organisationalUnitsFlatUrl = organisationalUnitsFlatUrl;
        this.updateSpacesAvailableUrl = updateSpacesAvailableUrl;
        this.getOrganisationUrl = getOrganisationUrl;
    }

    public Boolean isDomainInAgency(String domain) {
        try {
            return restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), Boolean.class);
        } catch (HttpClientErrorException e) {
            log.error("An error occurred checking if domain in agency", e);
            return false;
        }
    }

    public AgencyToken[] getAgencyTokensForDomain(String domain) {
        try {
            return restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), AgencyToken[].class);
        } catch (HttpClientErrorException e) {
            return new AgencyToken[]{};
        }
    }

    public Optional<AgencyToken> getAgencyTokenForDomainTokenOrganisation(String domain, String token, String organisation) {
        try {
            return Optional.of(restTemplate.getForObject(String.format(agencyTokensFormat, domain, token, organisation), AgencyToken.class));
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    public Optional<AgencyToken> getAgencyTokenForDomainAndOrganisation(String domain, String organisation) {
        try {
            return Optional.of(restTemplate.getForObject(String.format(agencyTokensByDomainAndOrganisationFormat, domain, organisation), AgencyToken.class));
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    /*
    By default all RuntimeExceptions rollback transaction whereas checked exceptions don't.
     */
    public void updateSpacesAvailable(String domain, String token, String organisation, boolean removeUser) {
        try {
             updateCsrs(domain, token, organisation, removeUser);
        } catch (HttpClientErrorException e) {
            log.warn("*****httpClientException");
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new ResourceNotFoundException();
            } else if (HttpStatus.CONFLICT.equals(e.getStatusCode())) {
                log.warn(String.format("Not enough spaces available on agency token: domain %s, token %s, organisation %s", domain, token, organisation));
                throw new NotEnoughSpaceAvailableException("Not enough spaces available for AgencyToken " + token);
            } else {
                BadRequestException badRequest = new BadRequestException(e);
                log.error("A client error occurred calling update agency token", badRequest);
                throw badRequest;
            }
        } catch (HttpServerErrorException e) {
            UnableToAllocateAgencyTokenException exception = new UnableToAllocateAgencyTokenException(String.format("Error: Unable to update AgencyToken %s ", token), e);
            log.error("An error occurred allocating agency token", exception);
            throw exception;
        } catch (Exception e) {
            UnableToAllocateAgencyTokenException exception = new UnableToAllocateAgencyTokenException(String.format("Unexpected Error: Unable to update AgencyToken %s ", token), e);
            log.error("An unexpected error occurred allocating agency token", exception);
            throw exception;
        }
    }

    public OrganisationalUnitDto[] getOrganisationalUnitsFormatted() {
        OrganisationalUnitDto[] organisationalUnitDtos;
        try {
            organisationalUnitDtos = restTemplate.getForObject(organisationalUnitsFlatUrl, OrganisationalUnitDto[].class);
        } catch (HttpClientErrorException e) {
            organisationalUnitDtos = new OrganisationalUnitDto[0];
        }
        return organisationalUnitDtos;
    }

    private void updateCsrs(String domain, String token, String organisation, boolean removeUser) {
        AgencyTokenDTO requestDTO = buildAgencyTokenDTO(domain, token, organisation, removeUser);
        restTemplate.put(updateSpacesAvailableUrl, requestDTO);
    }

    private AgencyTokenDTO buildAgencyTokenDTO(String domain, String token, String organisation, boolean removeUser) {
        AgencyTokenDTO agencyTokenDTO = new AgencyTokenDTO();
        agencyTokenDTO.setDomain(domain);
        agencyTokenDTO.setToken(token);
        agencyTokenDTO.setCode(organisation);
        agencyTokenDTO.setRemoveUser(removeUser);
        return agencyTokenDTO;
    }
}
