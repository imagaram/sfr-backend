package com.sfr.tokyo.sfr_backend.controller.compat;

import com.sfr.tokyo.sfr_backend.dto.crypto.api.ApiErrorResponse;
import com.sfr.tokyo.sfr_backend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackageClasses = TokenCompatController.class)
public class GlobalCompatExceptionHandler {

    private ApiErrorResponse body(String errorCode, String message, String path, Object details) {
        return ApiErrorResponse.builder()
                .error(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }

    private String path(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return swr.getRequest().getRequestURI();
        }
        return "";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body("NOT_FOUND", ex.getMessage(), path(req), null));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficient(InsufficientBalanceException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body("BAD_REQUEST", ex.getMessage(), path(req), null));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalid(InvalidRequestException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body("BAD_REQUEST", ex.getMessage(), path(req), null));
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedOperationException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(body("UNAUTHORIZED", ex.getMessage(), path(req), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        List<Object> details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> {
                    return java.util.Map.of(
                            "field", f.getField(),
                            "message", f.getDefaultMessage());
                })
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body("VALIDATION_ERROR", "Validation failed", path(req), details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("INTERNAL_SERVER_ERROR", ex.getMessage(), path(req), null));
    }
}
