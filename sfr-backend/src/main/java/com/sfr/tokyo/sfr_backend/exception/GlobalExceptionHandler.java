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
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        private String traceId() {
                return Optional.ofNullable(MDC.get("traceId")).orElseGet(() -> java.util.UUID.randomUUID().toString());
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
                ErrorCode code = ex.getErrorCode();
                String traceId = traceId();
                ApiErrorResponse body = ApiErrorResponse.of(code.name(), ex.getMessage(), code.getStatus().value(), traceId, null);
                // 構造化 WARN ログ（ユーザー操作起因の想定内エラー）
                log.warn("business_exception code={} status={} traceId={} path={} message={}", code.name(), code.getStatus().value(), traceId, request.getRequestURI(), ex.getMessage());
                return ResponseEntity.status(code.getStatus()).body(body);
        }



    @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND.value(),
                traceId(), null);
                log.warn("entity_not_found status=404 traceId={} path={} entityMessage={}", body.getTraceId(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> ApiErrorResponse.FieldError.builder()
                        .field(f.getField())
                        .rejectedValue(f.getRejectedValue())
                        .message(f.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        ApiErrorResponse body = ApiErrorResponse.of("VALIDATION_ERROR", "入力値が不正です", HttpStatus.BAD_REQUEST.value(),
                traceId(), details);
                log.warn("validation_error status=400 traceId={} path={} fieldErrorCount={}", body.getTraceId(), request.getRequestURI(), details.size());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> details = ex.getConstraintViolations().stream()
                .map(v -> ApiErrorResponse.FieldError.builder()
                        .field(v.getPropertyPath().toString())
                        .rejectedValue(v.getInvalidValue())
                        .message(v.getMessage())
                        .build())
                .collect(Collectors.toList());
        ApiErrorResponse body = ApiErrorResponse.of("CONSTRAINT_VIOLATION", "制約違反", HttpStatus.BAD_REQUEST.value(),
                traceId(), details);
                log.warn("constraint_violation status=400 traceId={} path={} violations={}", body.getTraceId(), request.getRequestURI(), details.size());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({ AuthorizationDeniedException.class, AccessDeniedException.class })
        public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex, HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of("FORBIDDEN", "アクセスが拒否されました", HttpStatus.FORBIDDEN.value(),
                traceId(), null);
                log.warn("access_denied status=403 traceId={} path={} message={}", body.getTraceId(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleOther(Exception ex, HttpServletRequest request) {
                String traceId = traceId();
                ApiErrorResponse body = ApiErrorResponse.of("INTERNAL_ERROR", "サーバー内部エラー",
                                HttpStatus.INTERNAL_SERVER_ERROR.value(), traceId, null);
                // 想定外は ERROR でスタックトレース出力
                log.error("unhandled_exception status=500 traceId={} path={} type={} message={}", traceId, request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
