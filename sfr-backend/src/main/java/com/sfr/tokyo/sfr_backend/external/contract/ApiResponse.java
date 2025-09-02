package com.sfr.tokyo.sfr_backend.external.contract;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 外部API共通レスポンス形式
 * 
 * 外部システム・SDK・DAOとの連携で使用する統一レスポンス形式です。
 * 成功・失敗の状態、データ、メタデータを含む標準的なAPI応答を提供します。
 * 
 * @param <T> レスポンスデータの型
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "外部API統一レスポンス形式")
public class ApiResponse<T> {

    /**
     * 処理成功フラグ
     */
    @Schema(description = "処理成功フラグ", example = "true")
    private boolean success;

    /**
     * レスポンスデータ
     */
    @Schema(description = "レスポンスデータ")
    private T data;

    /**
     * メッセージ
     */
    @Schema(description = "処理結果メッセージ", example = "Data retrieved successfully")
    private String message;

    /**
     * エラーコード（エラー時のみ）
     */
    @Schema(description = "エラーコード", example = "VALIDATION_ERROR")
    private String errorCode;

    /**
     * タイムスタンプ
     */
    @Schema(description = "レスポンス生成時刻", example = "2025-09-02T08:30:00Z")
    private Instant timestamp;

    /**
     * メタデータ（ページング情報など）
     */
    @Schema(description = "追加メタデータ（ページング情報など）")
    private Map<String, Object> metadata;

    /**
     * 成功レスポンスの生成
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * 成功レスポンスの生成（メタデータ付き）
     */
    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * エラーレスポンスの生成
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * バリデーションエラーレスポンスの生成
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return error(message, "VALIDATION_ERROR");
    }

    /**
     * 内部エラーレスポンスの生成
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return error(message, "INTERNAL_SERVER_ERROR");
    }

    /**
     * 見つからないエラーレスポンスの生成
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(message, "NOT_FOUND");
    }
}
