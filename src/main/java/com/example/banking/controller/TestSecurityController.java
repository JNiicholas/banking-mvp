package com.example.banking.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestSecurityController {

    @GetMapping("/public/hello")
    public Map<String, String> publicHello() {
        return Map.of("message", "Hello from public endpoint (no token required)");
    }

    @GetMapping("/secure/hello")
    public Map<String, String> secureHello(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "message", "Hello from secure endpoint (token required)",
                "subject", jwt.getSubject(),
                "audience", jwt.getAudience().toString()
        );
    }

    @GetMapping("/secure/admin")
    public Map<String, String> adminOnly(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "message", "Hello Admin — you reached a ROLE_admin protected endpoint!",
                "subject", jwt.getSubject()
        );
    }
}