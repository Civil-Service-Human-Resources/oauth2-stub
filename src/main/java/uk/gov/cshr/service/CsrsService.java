package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.dto.AgencyTokenDTO;
import uk.gov.cshr.dto.UpdateOrganisationDTO;
import uk.gov.cshr.exception.*;

import java.util.Optional;

@Slf4j
@Service
public class CsrsService {
    private RestTemplate restTemplate;
    private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String organisationalUnitsFlatUrl;
    private String updateSpacesAvailableUrl;
    private String updateOrganisationUrl;

    public CsrsService(RestTemplate restTemplate,
                       @Value("${registry.agencyTokensFormat}") String agencyTokensFormat,
                       @Value("${registry.agencyTokensByDomainFormat}") String agencyTokensByDomainFormat,
                       @Value("${registry.organisationalUnitsFlatUrl}") String organisationalUnitsFlatUrl,
                       @Value("${registry.updateSpacesAvailableUrl}") String updateSpacesAvailableUrl,
                       @Value("${registry.updateOrganisationUrl}") String updateOrganisationUrl) {
        this.restTemplate = restTemplate;
        this.agencyTokensFormat = agencyTokensFormat;
        this.agencyTokensByDomainFormat = agencyTokensByDomainFormat;
        this.organisationalUnitsFlatUrl = organisationalUnitsFlatUrl;
        this.updateSpacesAvailableUrl = updateSpacesAvailableUrl;
        this.updateOrganisationUrl = updateOrganisationUrl;
    }

    public AgencyToken[] getAgencyTokensForDomain(String domain) {
        try {
            return restTemplate.getForObject(String.format(agencyTokensByDomainFormat, domain), AgencyToken[].class);
        } catch (HttpClientErrorException e) {
            System.out.println(e);
            return new AgencyToken[]{};
        }
    }

    public Optional<AgencyToken> getAgencyTokenForDomainTokenOrganisation(String domain, String token, String organisation) {
        try {
            return Optional.of(restTemplate.getForObject(String.format(agencyTokensFormat, domain, token, organisation), AgencyToken.class));
        } catch (HttpClientErrorException e) {
            System.out.println(e);
            return Optional.empty();
        }
    }

    public void updateSpacesAvailable(String domain, String token, String organisation, boolean removeUser) {
        try {
             updateCsrs(domain, token, organisation, removeUser);
        } catch (HttpClientErrorException e) {
            log.warn("*****httpClientException");
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new ResourceNotFoundException();
            } else if (HttpStatus.CONFLICT.equals(e.getStatusCode())) {
                throw new NotEnoughSpaceAvailableException("Not enough spaces available for AgencyToken " + token);
            } else {
                throw new BadRequestException(e);
            }
        } catch (HttpServerErrorException e) {
            log.warn("*****httpServerException");
            throw new UnableToAllocateAgencyTokenException(String.format("Error: Unable to update AgencyToken %s ", token));
        } catch (Exception e) {
            log.warn("*****Exception");
            throw new UnableToAllocateAgencyTokenException(String.format("Unexpected Error: Unable to update AgencyToken %s ", token));
        }
    }

    public void updateOrganisation(String uid, String orgCode) {
        try {
            updateOrganisationForUser(uid, orgCode);
        } catch (HttpClientErrorException e) {
            log.warn("*****httpClientException");
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new ResourceNotFoundException();
            } else {
                throw new BadRequestException(e);
            }
        } catch (HttpServerErrorException e) {
            log.warn("*****httpServerException", e);
            throw new UnableToUpdateOrganisationException(String.format("Error: Unable to update organisation for uid %s ", uid));
        } catch (Exception e) {
            log.warn("*****Exception", e);
            throw new UnableToUpdateOrganisationException(String.format("Unexpected Error: Unable to update organisation for uid %s ", uid));
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

    private void updateOrganisationForUser(String uid, String orgCode) {
        UpdateOrganisationDTO requestDTO = new UpdateOrganisationDTO();
        requestDTO.setOrgCode(orgCode);
        restTemplate.put(updateOrganisationUrl, requestDTO, Void.class);
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
