package com.example.banking.exception;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        String traceId
) {
    public ApiError(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null, null);
    }

    public ApiError(int status, String error, String message, String path, String code, String traceId) {
        this(Instant.now(), status, error, message, path, code, traceId);
    }
}
