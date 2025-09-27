package com.example.banking.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Response from /protocol/openid-connect/token */
@Data
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    // Optional fields (not required, add if you need them)
    @JsonProperty("refresh_expires_in")
    private Integer refreshExpiresIn;

    @JsonProperty("scope")
    private String scope;
}