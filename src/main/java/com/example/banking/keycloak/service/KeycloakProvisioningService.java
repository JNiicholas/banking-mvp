package com.example.banking.keycloak.service;


import com.example.banking.keycloak.dto.CreateUserRequest;
import com.example.banking.keycloak.dto.SetPasswordRequest;
import com.example.banking.keycloak.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakProvisioningService {

    @Qualifier("keycloakWebClient")
    private final WebClient kc;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    // simple in-memory token cache
    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    private static final Pattern USER_ID_PATH = Pattern.compile(".*/users/([0-9a-fA-F\\-]+)$");

    /**
     * Get a client_credentials token via Basic Auth (client_secret_basic).
     */
    public String getAdminAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(15))) {
            long secsLeft = tokenExpiresAt.minusSeconds(15).getEpochSecond() - Instant.now().getEpochSecond();
            log.debug("[KC] Using cached admin token ({}s until refresh threshold)", Math.max(secsLeft, 0));
            return cachedToken;
        }
        log.info("[KC] Fetching new admin access token via client_credentials for clientId={}", clientId);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");

        TokenResponse tr = kc.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .attribute("op", "kc-token")
                .bodyValue(form)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();

        log.debug("[KC] Token endpoint responded; hasAccessToken={} expiresIn={}s", tr != null && tr.getAccessToken() != null, tr != null ? tr.getExpiresIn() : null);

        if (tr == null || tr.getAccessToken() == null) {
            throw new IllegalStateException("Keycloak token response was empty");
        }

        cachedToken = tr.getAccessToken();
        int ttl = Optional.ofNullable(tr.getExpiresIn()).orElse(300);
        tokenExpiresAt = Instant.now().plusSeconds(ttl);
        log.info("[KC] Acquired admin token (ttl={}s)", ttl);
        return cachedToken;
    }

    /**
     * Create a user; returns the Keycloak userId from the Location header.
     */
    public String createUser(CreateUserRequest req) {
        log.info("[KC] Creating Keycloak user: username={} email={} enabled={}", req.username(), req.email(), req.enabled());
        String token = getAdminAccessToken();

        ClientResponse resp = kc.post()
                .uri("/admin/realms/{realm}/users", realm)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(req)
                .attribute("op", "kc-create-user")
                .exchangeToMono(Mono::just)
                .block();

        if (resp == null) {
            log.error("[KC] Null response from Keycloak on user create");
            throw new IllegalStateException("Null response from Keycloak");
        }
        log.debug("[KC] Create user HTTP status={}", resp.statusCode());
        if (resp.statusCode().is2xxSuccessful()) {
            log.info("[KC] User created successfully; parsing Location header");
            String location = resp.headers().asHttpHeaders().getFirst(HttpHeaders.LOCATION);
            log.debug("[KC] Location header={}", location);
            if (location == null) throw new IllegalStateException("No Location header on 201 Created");
            String userId = extractUserIdFromLocation(location)
                    .orElseThrow(() -> new IllegalStateException("Could not parse userId from Location: " + location));
            log.info("Created Keycloak user id={}", userId);
            return userId;
        } else if (resp.statusCode().value() == 409) {
            log.warn("[KC] Create user returned 409 CONFLICT (user may already exist): username={} email={}", req.username(), req.email());
            throw new IllegalStateException("User already exists (409). Consider searching by email and linking.");
        } else {
            String body = resp.bodyToMono(String.class).block();
            log.error("[KC] Create user failed: status={} body={} ", resp.statusCode(), body);
            throw new IllegalStateException("Keycloak create user failed: " + resp.statusCode() + " body=" + body);
        }
    }

    /**
     * Optionally set an initial password for the user.
     */
    public void setUserPassword(String userId, String password, boolean temporary) {
        log.info("[KC] Setting password for userId={} temporary={}", userId, temporary);
        String token = getAdminAccessToken();

        SetPasswordRequest body = new SetPasswordRequest();
        body.setValue(password);
        body.setTemporary(temporary);

        kc.put()
                .uri("/admin/realms/{realm}/users/{id}/reset-password", realm, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
        log.info("[KC] Password set for userId={} temporary={}", userId, temporary);
    }

    private Optional<String> extractUserIdFromLocation(String location) {
        try {
            URI uri = URI.create(location);
            Matcher m = USER_ID_PATH.matcher(uri.getPath());
            return m.find() ? Optional.of(m.group(1)) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}