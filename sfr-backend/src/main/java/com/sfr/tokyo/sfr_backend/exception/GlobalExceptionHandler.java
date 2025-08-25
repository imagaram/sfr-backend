package com.sfr.tokyo.sfr_backend.exception;

import com.sfr.tokyo.sfr_backend.dto.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String traceId() {
        return java.util.UUID.randomUUID().toString();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ApiErrorResponse body = ApiErrorResponse.of("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND.value(),
                traceId(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> ApiErrorResponse.FieldError.builder()
                        .field(f.getField())
                        .rejectedValue(f.getRejectedValue())
                        .message(f.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        ApiErrorResponse body = ApiErrorResponse.of("VALIDATION_ERROR", "入力値が不正です", HttpStatus.BAD_REQUEST.value(),
                traceId(), details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex) {
        List<ApiErrorResponse.FieldError> details = ex.getConstraintViolations().stream()
                .map(v -> ApiErrorResponse.FieldError.builder()
                        .field(v.getPropertyPath().toString())
                        .rejectedValue(v.getInvalidValue())
                        .message(v.getMessage())
                        .build())
                .collect(Collectors.toList());
        ApiErrorResponse body = ApiErrorResponse.of("CONSTRAINT_VIOLATION", "制約違反", HttpStatus.BAD_REQUEST.value(),
                traceId(), details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({ AuthorizationDeniedException.class, AccessDeniedException.class })
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex) {
        ApiErrorResponse body = ApiErrorResponse.of("FORBIDDEN", "アクセスが拒否されました", HttpStatus.FORBIDDEN.value(),
                traceId(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception ex) {
        ApiErrorResponse body = ApiErrorResponse.of("INTERNAL_ERROR", "サーバー内部エラー",
                HttpStatus.INTERNAL_SERVER_ERROR.value(), traceId(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
