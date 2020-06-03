package uk.gov.cshr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.EmailUpdate;

import java.util.Optional;

@Repository
public interface EmailUpdateRepository extends JpaRepository<EmailUpdate, Long> {

    Optional<EmailUpdate> findByCode(String code);

    boolean existsByCode(String code);
}
