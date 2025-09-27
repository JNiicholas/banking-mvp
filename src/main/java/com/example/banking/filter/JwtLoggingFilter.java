
package com.example.banking.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Logs selected, non-sensitive JWT claims AFTER Spring Security has authenticated the request.
 * Runs late in the chain so the SecurityContext is populated.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class JwtLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtLoggingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken tokenAuth) {
                Jwt jwt = tokenAuth.getToken();

                String traceId = Optional.ofNullable(MDC.get("traceId")).orElse("-");
                String sub = jwt.getSubject();
                String username = jwt.getClaimAsString("preferred_username");
                String email = jwt.getClaimAsString("email");
                String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
                String azp = jwt.getClaimAsString("azp");
                List<String> aud = jwt.getAudience();
                String scope = jwt.getClaimAsString("scope");
                String jti = jwt.getId();

                // Optional realm roles (Keycloak specific)
                @SuppressWarnings("unchecked")
                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                Object roles = realmAccess != null ? realmAccess.get("roles") : null;

                log.info("JWT details: sub={}, preferred_username={}, email={}, iss={}, azp={}, aud={}, scope={}, jti={}, roles={}, traceId={}",
                        sub, username, email, iss, azp, aud, scope, jti, roles, traceId);
            }
        } catch (Exception e) {
            // Never block the request due to logging problems
            log.debug("JWT logging skipped due to: {}", e.toString());
        }

        filterChain.doFilter(request, response);
    }
}
