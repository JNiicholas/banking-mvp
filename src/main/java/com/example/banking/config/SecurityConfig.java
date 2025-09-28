package com.example.banking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Arrays;
import java.util.Collection;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/customers/**").hasRole("ADMIN")
                        // Allow USER and ADMIN to read specific account resources; other account ops remain USER-only
                        .requestMatchers(HttpMethod.GET, "/accounts/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/accounts/*/balance").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/accounts/*/transactions").hasAnyRole("USER", "ADMIN")

                        // All other /accounts/** endpoints restricted to USER
                        .requestMatchers("/accounts/**").hasRole("USER")
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(realmRoleConverter())
                                .decoder(jwtDecoderWithAudience())
                        )
                );
        return http.build();
    }

    /** Map Keycloak realm roles (realm_access.roles) to ROLE_* authorities */
    private Converter<Jwt, JwtAuthenticationToken> realmRoleConverter() {
        return jwt -> {
            var realmAccess = jwt.getClaimAsMap("realm_access");
            @SuppressWarnings("unchecked")
            Collection<String> roles = realmAccess != null
                    ? (Collection<String>) realmAccess.getOrDefault("roles", List.of())
                    : List.of();
            var authorities = roles.stream()
                    .map(r -> "ROLE_" + r.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            return new JwtAuthenticationToken(jwt, authorities);
        };
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