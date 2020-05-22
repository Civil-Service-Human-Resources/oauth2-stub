package uk.gov.cshr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.EmailUpdate;
import uk.gov.cshr.domain.Identity;

import java.util.Optional;

@Repository
public interface EmailUpdateRepository extends JpaRepository<EmailUpdate, Long> {

    Optional<EmailUpdate> findByIdentityAndCode(Identity identity, String code);

    Optional<EmailUpdate> findByCode(String code);

    boolean existsByIdentityAndCode(Identity identity, String code);
}