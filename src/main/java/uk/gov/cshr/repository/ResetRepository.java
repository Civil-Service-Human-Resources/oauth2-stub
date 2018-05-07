package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Reset;

@Repository
public interface ResetRepository extends CrudRepository<Reset, Long> {

    boolean existsByCode(String code);

    Reset findByCode(String code);

}
