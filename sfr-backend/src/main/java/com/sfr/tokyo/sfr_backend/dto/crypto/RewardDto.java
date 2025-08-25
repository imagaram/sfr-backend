package com.sfr.tokyo.sfr_backend.dto.crypto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFR報酬システム関連のDTO集合
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-20
 */
public class RewardDto {

    /**
     * 報酬配布リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributeRequest {
        @NotBlank(message = "ユーザーIDは必須です")
        private String userId;

        @NotNull
        @DecimalMin(value = "0.01", message = "報酬金額は0.01以上である必要があります")
        @DecimalMax(value = "100000.0", message = "報酬金額は100,000以下である必要があります")
        private BigDecimal amount;

        @NotBlank(message = "カテゴリは必須です")
        private String category; // POST_CREATION, COMMENT, VOTE, LEARNING, EVALUATION

        @NotBlank(message = "トリガータイプは必須です")
        private String triggerType; // MANUAL, AUTO_DAILY, AUTO_WEEKLY, AUTO_MONTHLY, EVENT_BASED

        private String referenceId; // 関連する投稿、コメント等のID

        @NotBlank(message = "理由は必須です")
        @Size(max = 500, message = "理由は500文字以内で入力してください")
        private String reason;

        private BigDecimal qualityScore; // 品質スコア

        private BigDecimal engagementScore; // エンゲージメントスコア

        private String calculationDetails; // 計算詳細（JSON）
    }

    /**
     * 報酬配布レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributeResponse {
        @NotNull
        private Long distributionId;

        @NotNull
        private String userId;

        @NotNull
        private BigDecimal amount;

        @NotNull
        private String category;

        @NotNull
        private String status; // PENDING, PROCESSED, FAILED

        @NotNull
        private LocalDateTime distributionDate;

        private LocalDateTime processedAt;

        private String transactionHash;

        private String reason;

        private BigDecimal balanceAfter; // 配布後残高
    }

    /**
     * バッチ報酬配布リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchDistributeRequest {
        @NotBlank(message = "カテゴリは必須です")
        private String category;

        @NotBlank(message = "トリガータイプは必須です")
        private String triggerType;

        @NotBlank(message = "理由は必須です")
        @Size(max = 500, message = "理由は500文字以内で入力してください")
        private String reason;

        @Builder.Default
        private Boolean processImmediately = false; // 即座に処理するか

        private LocalDateTime scheduledDate; // スケジュール日時

        private String calculationRule; // 計算ルール（JSON）
    }

    /**
     * バッチ報酬配布レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchDistributeResponse {
        @NotNull
        private String batchId;

        @NotNull
        private Integer totalUsers;

        @NotNull
        private BigDecimal totalAmount;

        @NotNull
        private String category;

        @NotNull
        private String status;

        @NotNull
        private LocalDateTime createdAt;

        private LocalDateTime processedAt;

        private Integer successCount;

        private Integer failureCount;
    }

    /**
     * 報酬履歴取得リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryRequest {
        @Min(value = 0, message = "ページ番号は0以上である必要があります")
        @Builder.Default
        private Integer page = 0;

        @Min(value = 1, message = "ページサイズは1以上である必要があります")
        @Max(value = 100, message = "ページサイズは100以下である必要があります")
        @Builder.Default
        private Integer size = 20;

        private String category; // フィルタ用カテゴリ

        private String status; // フィルタ用ステータス

        private LocalDateTime startDate;

        private LocalDateTime endDate;

        @Builder.Default
        private String sortBy = "distributionDate"; // ソート項目

        @Builder.Default
        private String sortDirection = "DESC"; // ASC or DESC
    }

    /**
     * 報酬履歴アイテム
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItem {
        @NotNull
        private Long distributionId;

        @NotNull
        private BigDecimal amount;

        @NotNull
        private String category;

        @NotNull
        private String triggerType;

        @NotNull
        private String status;

        @NotNull
        private LocalDateTime distributionDate;

        private LocalDateTime processedAt;

        private String reason;

        private String referenceId;

        private BigDecimal qualityScore;

        private BigDecimal engagementScore;

        private String transactionHash;

        private String displayCategory; // 表示用カテゴリ名

        private String displayStatus; // 表示用ステータス名
    }

    /**
     * 報酬統計リクエスト
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

        private String category; // 特定カテゴリの統計
    }

    /**
     * 報酬統計レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsResponse {
        @NotNull
        private Integer totalDistributions;

        @NotNull
        private BigDecimal totalAmount;

        @NotNull
        private BigDecimal averageAmount;

        @NotNull
        private BigDecimal maxAmount;

        @NotNull
        private BigDecimal minAmount;

        @NotNull
        private LocalDateTime periodStart;

        @NotNull
        private LocalDateTime periodEnd;

        private CategoryBreakdown postCreation;

        private CategoryBreakdown comments;

        private CategoryBreakdown votes;

        private CategoryBreakdown learning;

        private CategoryBreakdown evaluation;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CategoryBreakdown {
            private Integer count;
            private BigDecimal amount;
            private BigDecimal percentage;
        }
    }

    /**
     * 報酬処理リクエスト（管理者専用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessRequest {
        @NotNull
        private Long distributionId;

        private String processedBy; // 処理者ID

        private String notes; // 処理ノート
    }

    /**
     * 報酬処理レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessResponse {
        @NotNull
        private Long distributionId;

        @NotNull
        private String status; // PROCESSED, FAILED

        @NotNull
        private LocalDateTime processedAt;

        private String transactionHash;

        private String errorMessage; // エラー時のメッセージ

        private BigDecimal balanceAfter; // 処理後残高
    }

    /**
     * ユーザー報酬サマリー
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRewardSummary {
        @NotNull
        private String userId;

        @NotNull
        private BigDecimal totalEarned;

        @NotNull
        private BigDecimal thisMonthEarned;

        @NotNull
        private BigDecimal lastMonthEarned;

        @NotNull
        private Integer totalDistributions;

        @NotNull
        private Integer thisMonthDistributions;

        @NotNull
        private LocalDateTime lastRewardDate;

        private BigDecimal averageReward;

        private String topCategory; // 最も多い報酬カテゴリ

        private BigDecimal currentBalance; // 現在残高
    }
}
