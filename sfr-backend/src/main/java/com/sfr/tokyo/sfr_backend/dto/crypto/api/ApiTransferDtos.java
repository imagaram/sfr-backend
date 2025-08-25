package com.sfr.tokyo.sfr_backend.dto.crypto.api;

import lombok.*;
import jakarta.validation.constraints.*;
import com.sfr.tokyo.sfr_backend.compat.CompatApiConstants;
import java.time.LocalDateTime;

/**
 * OpenAPI仕様に合わせた互換用 Transfer DTOs
 */
public class ApiTransferDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferRequestDto {
        @NotBlank
        private String from_user_id; // 認証ID優先、指定があれば使用
        @NotBlank
        private String to_user_id;
        @NotBlank
        @Pattern(regexp = CompatApiConstants.AMOUNT_INPUT_PATTERN, message = "invalid amount format")
        private String amount; // 8桁以下小数の正規化文字列（必ず少なくとも1桁の小数部）
        @NotBlank
        private String reason;
        private String note; // nullable
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferResponseDto {
        @NotBlank
        private String transfer_id;
        @NotBlank
        private String from_user_id;
        @NotBlank
        private String to_user_id;
        @NotBlank
        private String amount; // 8桁小数の文字列
        @NotBlank
        private String from_balance_after; // 8桁小数の文字列
        @NotBlank
        private String to_balance_after; // 8桁小数の文字列
        @NotNull
        private LocalDateTime processed_at;
    }
}
