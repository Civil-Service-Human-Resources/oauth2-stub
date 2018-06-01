package uk.gov.cshr.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.repository.ClientRepository;

@Service
public class LocalClientDetailsService implements ClientDetailsService {

    private ClientRepository clientRepository;

    @Autowired
    public LocalClientDetailsService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public org.springframework.security.oauth2.provider.ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = clientRepository.findFirstByActiveTrueAndUidEquals(clientId);
        if (client == null) {
            throw new ClientRegistrationException("No client found with ID " + clientId);
        }
        return new ClientDetails(client);
    }
}
