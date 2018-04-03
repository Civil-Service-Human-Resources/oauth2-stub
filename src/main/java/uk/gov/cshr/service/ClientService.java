package uk.gov.cshr.service;

import uk.gov.cshr.domain.Client;

public interface ClientService {
    Client createNewClient(String password, boolean status);
}
