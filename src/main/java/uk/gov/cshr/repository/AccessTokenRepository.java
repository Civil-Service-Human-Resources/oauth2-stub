package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.cshr.domain.AccessToken;
import uk.gov.cshr.domain.TokenStatus;

public interface AccessTokenRepository extends CrudRepository<AccessToken, Long> {
    AccessToken findFirstByStatusEqualsAndTokenEquals(TokenStatus status, String token);
}
