package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.cshr.domain.Identity;

public interface IdentityRepository extends CrudRepository<Identity, Long> {
    Identity findFirstByActiveTrueAndEmailEquals(String email);
}
