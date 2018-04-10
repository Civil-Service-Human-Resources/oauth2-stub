package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Client;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {

    Client findFirstByActiveTrueAndUidEquals(String uid);
}
