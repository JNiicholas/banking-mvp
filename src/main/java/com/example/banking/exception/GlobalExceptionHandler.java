package com.example.banking.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, ServletWebRequest req) {
        ApiError err = new ApiError(404, "Not Found", ex.getMessage(), req.getRequest().getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, ServletWebRequest req) {
        ApiError err = new ApiError(400, "Bad Request", ex.getMessage(), req.getRequest().getRequestURI());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, ServletWebRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("Validation error");
        ApiError err = new ApiError(400, "Bad Request", msg, req.getRequest().getRequestURI());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, ServletWebRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("Validation error");
        ApiError err = new ApiError(400, "Bad Request", msg, req.getRequest().getRequestURI());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, ServletWebRequest req) {
        ApiError err = new ApiError(400, "Bad Request", "Malformed JSON request.", req.getRequest().getRequestURI());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ApiError> handleOptimistic(Exception ex, ServletWebRequest req) {
        log.warn("Optimistic lock conflict at {}", req.getRequest().getRequestURI(), ex);
        ApiError err = new ApiError(409, "Conflict", "The resource was modified; please retry.", req.getRequest().getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler({PessimisticLockException.class})
    public ResponseEntity<ApiError> handlePessimistic(PessimisticLockException ex, ServletWebRequest req) {
        log.warn("Pessimistic lock/timeout at {}", req.getRequest().getRequestURI(), ex);
        ApiError err = new ApiError(423, "Locked", "The resource is currently locked; please retry.", req.getRequest().getRequestURI());
        return ResponseEntity.status(423).body(err);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex, ServletWebRequest req) {
        log.warn("Data integrity violation at {}", req.getRequest().getRequestURI(), ex);
        ApiError err = new ApiError(409, "Conflict", "Request violates a data constraint.", req.getRequest().getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, ServletWebRequest req) {
        // Log the real exception, but do not leak internal class names/messages to clients
        log.error("Unhandled error at {}", req.getRequest().getRequestURI(), ex);
        ApiError err = new ApiError(500, "Internal Server Error", "Unexpected error. Please try again later.", req.getRequest().getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
