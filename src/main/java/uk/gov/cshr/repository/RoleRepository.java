package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.cshr.domain.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findFirstByNameEquals(String name);
}
