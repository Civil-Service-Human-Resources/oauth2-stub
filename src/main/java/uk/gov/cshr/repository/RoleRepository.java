package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

    Role findFirstByNameEquals(String name);
}
