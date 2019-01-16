package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Token;
import uk.gov.cshr.domain.TokenStatus;

import java.util.Collection;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {

    Token findByTokenIdAndStatus(String tokenId, TokenStatus status);

    Token findByAuthenticationIdAndStatus(String authenticationId, TokenStatus status);

    Collection<Token> findByClientIdAndUserName(String clientId, String userName);

    Collection<Token> findByClientId(String clientId);

    Collection<Token> findAllByUserName(String userName);
}
