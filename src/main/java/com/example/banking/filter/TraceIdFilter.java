
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
            log.info("Request completed: method={}, uri={}, status={}, traceId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), traceId);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
