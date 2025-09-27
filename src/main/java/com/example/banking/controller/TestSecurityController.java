package com.example.banking.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@RestController
public class TestSecurityController {

    @GetMapping("/public/hello")
    public Map<String, String> publicHello() {
        return Map.of("message", "Hello from public endpoint (no token required)");
    }

    @GetMapping("/secure/hello")
    public Map<String, Object> secureHello(@AuthenticationPrincipal Jwt jwt) {
        String subject = jwt.getSubject();
        List<String> audience = jwt.getAudience();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
        String azp = jwt.getClaimAsString("azp");

        @SuppressWarnings("unchecked")
        List<String> realmRoles = Optional.ofNullable((Map<String, Object>) jwt.getClaim("realm_access"))
                .map(m -> (List<String>) m.get("roles"))
                .orElse(List.of());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Hello from secure endpoint (token required)");
        body.put("subject", subject);
        body.put("username", username);
        body.put("email", email);
        body.put("issuer", issuer);
        body.put("audience", audience);
        body.put("azp", azp);
        body.put("realm_roles", realmRoles);
        return body;
    }

    @GetMapping("/secure/admin")
    public Map<String, Object> adminOnly(@AuthenticationPrincipal Jwt jwt) {
        String subject = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");

        @SuppressWarnings("unchecked")
        List<String> realmRoles = Optional.ofNullable((Map<String, Object>) jwt.getClaim("realm_access"))
                .map(m -> (List<String>) m.get("roles"))
                .orElse(List.of());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Hello Admin — you reached a ROLE_admin protected endpoint!");
        body.put("subject", subject);
        body.put("username", username);
        body.put("realm_roles", realmRoles);
        return body;
    }
}