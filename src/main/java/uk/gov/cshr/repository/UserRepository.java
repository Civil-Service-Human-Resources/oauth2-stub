package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.cshr.domain.User;

public interface UserRepository extends CrudRepository<User, Long> {
    User findFirstByActiveTrueAndEmailEquals(String email);
}
