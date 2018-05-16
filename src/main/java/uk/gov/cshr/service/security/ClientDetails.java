package uk.gov.cshr.service.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.gov.cshr.domain.Client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class ClientDetails implements org.springframework.security.oauth2.provider.ClientDetails {

    private static final Set<GrantedAuthority> AUTHORITIES = new HashSet<GrantedAuthority>() {{
        add(new SimpleGrantedAuthority("CLIENT"));
    }};

    private static final Set<String> GRANT_TYPES = new HashSet<String>() {{
        add("implicit");
        add("password");
        add("authorization_code");
        add("client_credentials");
    }};

    private static final Set<String> SCOPE = new HashSet<String>() {{
        add("read");
        add("write");
    }};

    private Client client;

    public ClientDetails(Client client) {
        this.client = client;
    }

    @Override
    public String getClientId() {
        return client.getUid();
    }

    @Override
    public Set<String> getResourceIds() {
        return null;
    }

    @Override
    public boolean isSecretRequired() {
        return false;
    }

    @Override
    public String getClientSecret() {
        return client.getPassword();
    }

    @Override
    public boolean isScoped() {
        return false;
    }

    @Override
    public Set<String> getScope() {
        return SCOPE;
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        return GRANT_TYPES;
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        String redirectUri = client.getRedirectUri();
        if (redirectUri != null) {
            return singleton(redirectUri);
        }
        return null;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return AUTHORITIES;
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return null;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return null;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        return true;
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return null;
    }
}
