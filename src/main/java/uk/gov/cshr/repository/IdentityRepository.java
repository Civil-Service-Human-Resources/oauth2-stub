package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Identity;

@Repository
public interface IdentityRepository extends CrudRepository<Identity, Long> {

    Identity findFirstByActiveTrueAndEmailEquals(String email);
}
