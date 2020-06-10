package uk.gov.cshr.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.repository.ClientRepository;

@Service
public class LocalClientDetailsService implements ClientDetailsService {

    private ClientRepository clientRepository;

    private Integer accessTokenValiditySeconds;

    @Autowired
    public LocalClientDetailsService(ClientRepository clientRepository, @Value("${accessToken.validityInSeconds}") Integer accessTokenValiditySeconds) {
        this.clientRepository = clientRepository;
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    @Override
    @Cacheable(cacheNames = "loadClientByClientIdCache", key = "#clientId")
    public org.springframework.security.oauth2.provider.ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = clientRepository.findFirstByActiveTrueAndUidEquals(clientId);

        if (client == null) {
            throw new ClientRegistrationException("No client found with ID " + clientId);
        }
        return new ClientDetails(client, accessTokenValiditySeconds);
    }
}
