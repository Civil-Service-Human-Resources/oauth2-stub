package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.gov.cshr.config.ClientDetails;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.repository.ClientRepository;

public class ClientDetailsService implements UserDetailsService {
    @Autowired
    private ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
        Client client = clientRepository.findFirstByActiveTrueAndUidEquals(uid);
        if (client == null) {
            throw new UsernameNotFoundException(uid);
        }
        return new ClientDetails(client);
    }
}
