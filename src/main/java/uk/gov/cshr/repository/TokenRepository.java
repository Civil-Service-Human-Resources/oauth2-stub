package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Token;

import java.util.Collection;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {

    Token findByTokenId(String tokenId);

    Token findByAuthenticationId(String authenticationId);

    Collection<Token> findByClientIdAndUserName(String clientId, String userName);

    Collection<Token> findByClientId(String clientId);
}
