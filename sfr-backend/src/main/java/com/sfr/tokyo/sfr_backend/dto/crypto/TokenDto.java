package com.sfr.tokyo.sfr_backend.dto.crypto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFRトークン関連のDTO集合
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-20
 */
public class TokenDto {

    /**
     * トークン残高取得レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceResponse {
        @NotNull
        private String userId;

        @NotNull
        @DecimalMin(value = "0.0", message = "残高は0以上である必要があります")
        private BigDecimal balance;

        @NotNull
        private LocalDateTime lastUpdated;

        private String displayBalance; // フォーマット済み残高（表示用）
    }

    /**
     * トークン転送リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferRequest {
        @NotBlank(message = "受取人IDは必須です")
        private String recipientId;

        @NotNull
        @DecimalMin(value = "0.01", message = "転送金額は0.01以上である必要があります")
        @DecimalMax(value = "1000000.0", message = "転送金額は1,000,000以下である必要があります")
        private BigDecimal amount;

        @Size(max = 500, message = "メッセージは500文字以下である必要があります")
        private String message;
    }

    /**
     * トークン転送レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferResponse {
        @NotNull
        private String transactionId;

        @NotNull
        private String senderId;

        @NotNull
        private String recipientId;

        @NotNull
        private BigDecimal amount;

        @NotNull
        private BigDecimal senderBalanceAfter;

        @NotNull
        private BigDecimal recipientBalanceAfter;

        @NotNull
        private LocalDateTime processedAt;

        private String message;

        private String status; // SUCCESS, FAILED, PENDING
    }

    /**
     * トークン発行リクエスト（ADMIN専用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueRequest {
        @NotBlank(message = "ユーザーIDは必須です")
        private String userId;

        @NotNull
        @DecimalMin(value = "0.01", message = "発行金額は0.01以上である必要があります")
        @DecimalMax(value = "100000.0", message = "発行金額は100,000以下である必要があります")
        private BigDecimal amount;

        @NotBlank(message = "発行理由は必須です")
        @Size(max = 1000, message = "発行理由は1000文字以下である必要があります")
        private String reason;

        private String activityType; // 活動種別（任意）
    }

    /**
     * トークン発行レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueResponse {
        @NotNull
        private String transactionId;

        @NotNull
        private String userId;

        @NotNull
        private BigDecimal issuedAmount;

        @NotNull
        private BigDecimal balanceAfter;

        @NotNull
        private LocalDateTime issuedAt;

        private String reason;

        private String status; // SUCCESS, FAILED
    }

    /**
     * トークン徴収リクエスト（ADMIN専用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectRequest {
        @NotBlank(message = "ユーザーIDは必須です")
        private String userId;

        @NotNull
        @DecimalMin(value = "0.01", message = "徴収金額は0.01以上である必要があります")
        private BigDecimal amount;

        @NotBlank(message = "徴収理由は必須です")
        @Size(max = 1000, message = "徴収理由は1000文字以下である必要があります")
        private String reason;

        @Builder.Default
        private Boolean forceCollection = false; // 強制徴収フラグ
    }

    /**
     * トークン徴収レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectResponse {
        @NotNull
        private String transactionId;

        @NotNull
        private String userId;

        @NotNull
        private BigDecimal collectedAmount;

        @NotNull
        private BigDecimal balanceAfter;

        @NotNull
        private LocalDateTime collectedAt;

        private String reason;

        private String status; // SUCCESS, FAILED, INSUFFICIENT_BALANCE
    }

    /**
     * 取引履歴リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionHistoryRequest {
        @Min(value = 0, message = "ページ番号は0以上である必要があります")
        @Builder.Default
        private Integer page = 0;

        @Min(value = 1, message = "ページサイズは1以上である必要があります")
        @Max(value = 100, message = "ページサイズは100以下である必要があります")
        @Builder.Default
        private Integer size = 20;

        private String transactionType; // EARN, SPEND, COLLECT, BURN, TRANSFER

        private LocalDateTime startDate;

        private LocalDateTime endDate;
    }

    /**
     * 取引履歴アイテム
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionHistoryItem {
        @NotNull
        private String historyId;

        @NotNull
        private String transactionType;

        @NotNull
        private BigDecimal amount;

        @NotNull
        private BigDecimal balanceBefore;

        @NotNull
        private BigDecimal balanceAfter;

        @NotNull
        private LocalDateTime createdAt;

        private String reason;

        private String referenceId;

        private String displayType; // 表示用種別名
    }

    /**
     * 統計情報リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsRequest {
        private LocalDateTime startDate;

        private LocalDateTime endDate;

        @Builder.Default
        private String period = "MONTH"; // DAY, WEEK, MONTH, YEAR
    }

    /**
     * 統計情報レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsResponse {
        @NotNull
        private Integer totalTransactions;

        @NotNull
        private BigDecimal totalEarnings;

        @NotNull
        private BigDecimal totalSpendings;

        @NotNull
        private BigDecimal totalCollections;

        @NotNull
        private BigDecimal netChange;

        @NotNull
        private BigDecimal currentBalance;

        @NotNull
        private LocalDateTime periodStart;

        @NotNull
        private LocalDateTime periodEnd;

        private BigDecimal averageTransactionAmount;

        private BigDecimal maxIncrease;

        private BigDecimal maxDecrease;
    }
}
