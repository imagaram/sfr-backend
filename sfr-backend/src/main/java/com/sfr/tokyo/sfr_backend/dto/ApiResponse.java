package com.sfr.tokyo.sfr_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 統一API応答DTO
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Integer statusCode;
    private String timestamp;

    // 成功レスポンス作成
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(200)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    // データなし成功レスポンス作成
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(null)
                .statusCode(200)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    // エラーレスポンス作成
    public static <T> ApiResponse<T> error(String message, Integer statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .statusCode(statusCode)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
}
