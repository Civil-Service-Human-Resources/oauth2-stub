package uk.gov.cshr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
   // private String agencyTokensFormat;
    private String agencyTokensByDomainFormat;
    private String agencyTokensByDomainAndOrganisationFormat;
    private String organisationalUnitsFlatUrl;
    private String updateSpacesAvailableUrl;
    private String getOrganisationUrl;
    private String updateOrganisationUrl;

    public CsrsService(RestTemplate restTemplate,
                      // @Value("${registry.agencyTokensFormat}") String agencyTokensFormat,
                       @Value("${registry.agencyTokensByDomainFormat}") String agencyTokensByDomainFormat,
                       @Value("${registry.agencyTokensByDomainAndOrganisationFormat}") String agencyTokensByDomainAndOrganisationFormat,
                       @Value("${registry.organisationalUnitsFlatUrl}") String organisationalUnitsFlatUrl,
                       @Value("${registry.updateSpacesAvailableUrl}") String updateSpacesAvailableUrl,
                       @Value("${registry.getOrganisationUrl}") String getOrganisationUrl,
                       @Value("${registry.updateOrganisationUrl}") String updateOrganisationUrl) {
        this.restTemplate = restTemplate;
       // this.agencyTokensFormat = agencyTokensFormat;
        this.agencyTokensByDomainFormat = agencyTokensByDomainFormat;
        this.agencyTokensByDomainAndOrganisationFormat = agencyTokensByDomainAndOrganisationFormat;
        this.organisationalUnitsFlatUrl = organisationalUnitsFlatUrl;
        this.updateSpacesAvailableUrl = updateSpacesAvailableUrl;
        this.getOrganisationUrl = getOrganisationUrl;
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
            return Optional.of(restTemplate.getForObject(String.format("", domain, token, organisation), AgencyToken.class));
        } catch (HttpClientErrorException e) {
            System.out.println(e);
            return Optional.empty();
        }
    }

    public Optional<AgencyToken> getAgencyTokenForDomainAndOrganisation(String domain, String organisation) {
        try {
            return Optional.of(restTemplate.getForObject(String.format(agencyTokensByDomainAndOrganisationFormat, domain, organisation), AgencyToken.class));
        } catch (HttpClientErrorException e) {
            System.out.println(e);
            return Optional.empty();
        }
    }

    public boolean checkTokenExists(String domain, String token, String organisation, boolean removeUser) {
            // check there is a valid token for this
            Optional<AgencyToken> agencyToken = getAgencyTokenForDomainTokenOrganisation(domain, token, organisation);

            if(agencyToken.isPresent()) {
                return true;
            } else {
                return false;
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

    public void getOrganisationCodeForCivilServant(String uid) throws Exception {
        try {
            getOrgCode(uid);
        } catch (HttpClientErrorException e) {
            log.warn("*****httpClientException");
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new ResourceNotFoundException();
            } else {
                throw new BadRequestException(e);
            }
        } catch (HttpServerErrorException e) {
            log.warn("*****httpServerException");
            throw new Exception(String.format("Error: Unable to get org code %s ", uid), e);
        } catch (Exception e) {
            log.warn("*****Exception");
            throw new Exception(String.format("Unexpected Error: Unable to get org code %s ", uid), e);
        }
    }

    public String getOrgCode(String uid) {
        String url = String.format(getOrganisationUrl, uid);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        UpdateOrganisationDTO requestDTO = new UpdateOrganisationDTO();
        requestDTO.setOrganisation(orgCode);
        requestDTO.setUid(uid);

        HttpEntity<UpdateOrganisationDTO> entity = new HttpEntity<UpdateOrganisationDTO>(requestDTO ,headers);
        restTemplate.put(updateOrganisationUrl, entity);
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
