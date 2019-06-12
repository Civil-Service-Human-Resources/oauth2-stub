package uk.gov.cshr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;

import java.util.Optional;

public interface EmailUpdateRepository extends JpaRepository<EmailUpdate, Long> {

    Optional<EmailUpdate> findByIdentityAndCode(Identity identity, String code);
}