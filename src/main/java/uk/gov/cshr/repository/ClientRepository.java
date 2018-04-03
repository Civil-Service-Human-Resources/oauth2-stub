package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.cshr.domain.Client;

public interface ClientRepository extends CrudRepository<Client, Long> {

    Client findFirstByActiveTrueAndUidEquals(String uid);
}
