package uk.gov.cshr.service.security;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenServices implements AuthorizationServerTokenServices, ConsumerTokenServices {

    private org.springframework.security.oauth2.provider.token.TokenStore tokenStore;

    private Integer accessTokenValiditySeconds;

    @Autowired
    public TokenServices(org.springframework.security.oauth2.provider.token.TokenStore tokenStore,
                         @Value("${accessToken.validityInSeconds}") Integer accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.tokenStore = tokenStore;
    }

    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {

        OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);

        if (existingAccessToken != null) {
            if (existingAccessToken.isExpired()) {
                tokenStore.removeAccessToken(existingAccessToken);
            } else {
                // Re-store the access token in case the authentication has changed
                tokenStore.storeAccessToken(existingAccessToken, authentication);
                return existingAccessToken;
            }
        }

        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(RandomStringUtils.random(42, true, true));
        token.setExpiration(new Date(System.currentTimeMillis() + (accessTokenValiditySeconds * 1000L)));
        token.setScope(authentication.getOAuth2Request().getScope());

        tokenStore.storeAccessToken(token, authentication);

        return token;
    }

    @Override
    public OAuth2AccessToken refreshAccessToken(String refreshToken, TokenRequest tokenRequest) throws AuthenticationException {
        return null;
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        return tokenStore.getAccessToken(authentication);
    }

    @Override
    public boolean revokeToken(String tokenValue) {
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
        if (accessToken == null) {
            return false;
        }
        tokenStore.removeAccessToken(accessToken);
        return true;
    }
}
