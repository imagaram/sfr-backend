package com.sfr.tokyo.sfr_backend.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 標準化エラー応答 (フロント共通利用)
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {
    @Schema(description = "固定値: false")
    private boolean success;
    @Schema(description = "エラー分類コード (VALIDATION_ERROR, NOT_FOUND, UNAUTHORIZED, FORBIDDEN, RATE_LIMIT, INTERNAL_ERROR 等)")
    private String error;
    @Schema(description = "人間可読メッセージ")
    private String message;
    @Schema(description = "HTTP ステータスコード")
    private int status;
    @Schema(description = "リクエストトレースID (MDC など)")
    private String traceId;
    @Schema(description = "タイムスタンプ ISO8601")
    private OffsetDateTime timestamp;
    @Schema(description = "フィールド単位のバリデーション詳細")
    private List<FieldError> details;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
    }

    public static ApiErrorResponse of(String code, String message, int status, String traceId,
            List<FieldError> details) {
        return ApiErrorResponse.builder()
                .success(false)
                .error(code)
                .message(message)
                .status(status)
                .traceId(traceId)
                .timestamp(OffsetDateTime.now())
                .details(details)
                .build();
    }
}
