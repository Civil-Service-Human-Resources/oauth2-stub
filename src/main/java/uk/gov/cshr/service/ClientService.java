package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.repository.ClientRepository;

import java.util.UUID;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Client createNewClient(String password, boolean status) {
        return clientRepository.save(new Client(UUID.randomUUID().toString(), passwordEncoder.encode(password), status));
    }
}
