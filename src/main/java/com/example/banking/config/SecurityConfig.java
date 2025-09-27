package com.example.banking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.*;

import java.util.List;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuerUri;

    @Value("${app.security.expected-audience}")
    String expectedAudience;

    @Value("${app.security.client-roles.clients:}")
    String clientRoleClients; // comma-separated list of client IDs to read roles from

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-ui/oauth2-redirect.html"
    };

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(keycloakJwtAuthConverter())
                                .decoder(jwtDecoderWithAudience())
                        )
                );
        return http.build();
    }

    /** Map Keycloak roles to ROLE_* authorities */
    private JwtAuthenticationConverter keycloakJwtAuthConverter() {
        var gac = new JwtGrantedAuthoritiesConverter();

        // 1) Realm roles (realm_access.roles) -> ROLE_*
        gac.setAuthoritiesClaimName("realm_access.roles");
        gac.setAuthorityPrefix("ROLE_");

        // If you also use client roles, you can add another converter for
        // "resource_access.<client-id>.roles". For simplicity we keep realm roles here.

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // collect both realm and client roles if you want
            var authorities = gac.convert(jwt);

            // OPTIONAL: add client roles as authorities
            // Read roles from multiple client IDs, e.g. "Angular-Banking-App,Banking-App"
            var clientRoles = jwt.getClaimAsMap("resource_access");
            if (clientRoles != null) {
                // Read roles from multiple client IDs, e.g. "Angular-Banking-App,Banking-App"
                Arrays.stream(clientRoleClients == null ? new String[0] : clientRoleClients.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(clientId -> {
                            Object entryObj = clientRoles.get(clientId);
                            if (entryObj instanceof java.util.Map<?, ?> entry) {
                                Object rolesObj = entry.get("roles");
                                if (rolesObj instanceof java.util.List<?> list) {
                                    list.stream()
                                            .filter(String.class::isInstance)
                                            .map(String.class::cast)
                                            .forEach(r -> authorities.add(() -> "ROLE_" + r));
                                }
                            }
                        });
            }

            return authorities;
        });
        return converter;
    }

    /** Enforce that token audience contains our API’s client-id */
    private JwtDecoder jwtDecoderWithAudience() {
        NimbusJwtDecoder base = JwtDecoders.fromIssuerLocation(issuerUri);

        List<String> allowedAudiences = Arrays.asList(expectedAudience.split(","));

        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            var aud = jwt.getAudience();
            boolean match = allowedAudiences.stream().anyMatch(aud::contains);
            if (match) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token",
                    "The required audience is missing", null));
        };

        base.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), audienceValidator));
        return base;
    }
}