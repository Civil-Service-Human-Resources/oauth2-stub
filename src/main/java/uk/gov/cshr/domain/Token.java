package uk.gov.cshr.domain;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity
public class Token {

    public static String extractTokenKey(String value) {
        if (value == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        try {
            byte[] bytes = digest.digest(value.getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private byte[] token;

    @Column(unique = true)
    private String tokenId;

    private String authenticationId;

    private String userName;

    private String clientId;

    private TokenStatus status;

    @Lob
    private byte[] authentication;

    private String refreshToken;

    protected Token() {
    }

    public Token(String authenticationId, OAuth2AccessToken token, OAuth2Authentication authentication) {
        this.authenticationId = authenticationId;
        this.clientId = authentication.getOAuth2Request().getClientId();
        this.token = SerializationUtils.serialize(token);
        this.tokenId = extractTokenKey(token.getValue());
        this.status = TokenStatus.ACTIVE;

        setAuthentication(authentication);

        if (token.getRefreshToken() != null) {
            this.refreshToken = extractTokenKey(token.getRefreshToken().getValue());
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

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public void setAuthentication(OAuth2Authentication authentication) {
        this.authentication = SerializationUtils.serialize(authentication);
    }
}
