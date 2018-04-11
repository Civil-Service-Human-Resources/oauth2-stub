package uk.gov.cshr.domain;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.persistence.*;

@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Lob
    private byte[] token;

    private String tokenId;

    private String authenticationId;

    private String userName;

    private String clientId;

    @Lob
    private byte[] authentication;

    private String refreshToken;

    protected Token() {
    }

    public Token(String authenticationId, OAuth2AccessToken token, OAuth2Authentication authentication) {
        this.authenticationId = authenticationId;
        this.authentication = SerializationUtils.serialize(authentication);
        this.clientId = authentication.getOAuth2Request().getClientId();
        this.token = SerializationUtils.serialize(token);
        this.tokenId = token.getValue(); // TODO extractTokenKey?

        if (token.getRefreshToken() != null) {
            this.refreshToken = token.getRefreshToken().getValue();
        }
        if (!authentication.isClientOnly()) {
            userName = authentication.getName();
        }
    }

    public OAuth2Authentication getAuthentication() {
        return SerializationUtils.deserialize(authentication);
    }

    public OAuth2AccessToken getToken() {
        return SerializationUtils.deserialize(token);
    }
}
