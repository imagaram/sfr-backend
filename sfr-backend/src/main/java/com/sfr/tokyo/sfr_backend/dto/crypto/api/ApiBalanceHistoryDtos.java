package com.sfr.tokyo.sfr_backend.dto.crypto.api;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OpenAPI仕様に合わせた互換用 BalanceHistory DTOs
 */
public class ApiBalanceHistoryDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceHistoryItemDto {
        @NotBlank
        private String history_id;
        @NotBlank
        private String user_id;
        @NotBlank
        private String transaction_type; // EARN, SPEND, COLLECT, BURN, TRANSFER
        @NotBlank
        private String amount; // 8桁小数の文字列
        @NotBlank
        private String balance_before; // 8桁小数の文字列
        @NotBlank
        private String balance_after; // 8桁小数の文字列
        @NotBlank
        private String reason;
        private String reference_id; // nullable
        @NotNull
        private LocalDateTime created_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfoDto {
        @NotNull
        private Integer page;
        @NotNull
        private Integer limit;
        @NotNull
        private Integer total_pages;
        @NotNull
        private Integer total_count;
        @NotNull
        private Boolean has_next;
        @NotNull
        private Boolean has_previous;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceHistoryResponseDto {
        @NotNull
        private List<BalanceHistoryItemDto> data;
        @NotNull
        private PaginationInfoDto pagination;
    }
}
