package uk.gov.cshr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessTokenDTO {

    @JsonProperty("access_token")
    private String token;

    @JsonProperty("token_type")
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private Long expiresIn;

    public AccessTokenDTO(String token, Long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }
}
