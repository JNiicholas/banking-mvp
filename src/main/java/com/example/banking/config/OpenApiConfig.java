package com.example.banking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String authUrl  = "http://localhost:8081/realms/BankingApp/protocol/openid-connect/auth";
        String tokenUrl = "http://localhost:8081/realms/BankingApp/protocol/openid-connect/token";

        SecurityScheme oauth2Scheme = new SecurityScheme()
                .name("keycloak")
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(
                        new OAuthFlow()
                                .authorizationUrl(authUrl)
                                .tokenUrl(tokenUrl)
                                .scopes(new Scopes()
                                                .addString("openid", "OpenID scope")
                                                .addString("profile", "User profile")
                                                .addString("email", "Email")
                                )
                ));

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("keycloak", oauth2Scheme))
                .addSecurityItem(new SecurityRequirement().addList("keycloak", List.of("openid")));
    }
}