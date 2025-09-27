
package com.example.banking.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Ensures every request has a trace id available in logs (MDC) and response headers.
 * Reads an incoming X-Trace-Id header when present; otherwise generates one.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TraceIdFilter.class);

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = Optional.ofNullable(request.getHeader(TRACE_HEADER))
                .filter(h -> !h.isBlank())
                .orElse(UUID.randomUUID().toString().replace("-", ""));

        // Put into MDC for logging and make it visible to clients
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(TRACE_HEADER, traceId);
        log.info("Request started: method={}, uri={}, traceId={}, authHeaderPresent={}",
                request.getMethod(),
                request.getRequestURI(),
                traceId,
                request.getHeader("Authorization") != null);

        try {
            filterChain.doFilter(request, response);
            // After the security filter chain runs, try to log key JWT properties if present
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                    Jwt jwt = jwtAuth.getToken();

                    String subject = safeClaim(jwt, "sub");
                    String preferredUsername = safeClaim(jwt, "preferred_username");
                    String email = safeClaim(jwt, "email");
                    String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
                    String azp = safeClaim(jwt, "azp");
                    Object audience = jwt.getAudience(); // usually a List<String>
                    String scope = safeClaim(jwt, "scope"); // space-delimited string if present
                    String jti = safeClaim(jwt, "jti");

                    log.info("JWT details: sub={}, preferred_username={}, email={}, iss={}, azp={}, aud={}, scope={}, jti={}, traceId={}",
                            subject, preferredUsername, email, issuer, azp, audience, scope, jti, traceId);
                } else {
                    log.debug("No JwtAuthenticationToken found in SecurityContext for traceId={}", traceId);
                }
            } catch (Exception e) {
                log.warn("Failed to log JWT details for traceId={}: {}", traceId, e.getMessage());
            }
            log.info("Request completed: method={}, uri={}, status={}, traceId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), traceId);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private static String safeClaim(Jwt jwt, String claim) {
        Object v = jwt.getClaims().get(claim);
        return v == null ? null : String.valueOf(v);
    }
}
