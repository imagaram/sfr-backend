package com.sfr.tokyo.sfr_backend.dto.crypto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFRガバナンスシステム関連のDTO集合
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-20
 */
public class GovernanceDto {

    /**
     * 提案作成リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProposalRequest {
        @NotBlank(message = "タイトルは必須です")
        @Size(min = 10, max = 200, message = "タイトルは10文字以上200文字以内で入力してください")
        private String title;

        @NotBlank(message = "説明は必須です")
        @Size(min = 50, message = "説明は50文字以上で入力してください")
        private String description;

        @NotBlank(message = "カテゴリは必須です")
        private String category; // TOKENOMICS, GOVERNANCE, TECHNICAL, etc.

        @NotBlank(message = "提案タイプは必須です")
        private String proposalType; // PARAMETER_CHANGE, PROTOCOL_UPGRADE, etc.

        private String parameters; // JSON形式での提案パラメータ

        private String impactAssessment; // 影響評価

        private String implementationPlan; // 実装計画

        @Min(value = 1, message = "最小クォーラムは1以上である必要があります")
        @Builder.Default
        private Integer minimumQuorum = 100;

        @DecimalMin(value = "50.0", message = "承認閾値は50.0以上である必要があります")
        @DecimalMax(value = "100.0", message = "承認閾値は100.0以下である必要があります")
        @Builder.Default
        private BigDecimal approvalThreshold = new BigDecimal("66.67");

        @Min(value = 24, message = "投票期間は24時間以上である必要があります")
        @Max(value = 8760, message = "投票期間は8760時間以下である必要があります")
        @Builder.Default
        private Integer votingDurationHours = 168; // 1週間

        @Min(value = 0, message = "実行遅延時間は0以上である必要があります")
        @Max(value = 2160, message = "実行遅延時間は2160時間以下である必要があります")
        @Builder.Default
        private Integer executionDelayHours = 24;

        @Min(value = 0, message = "レビュー期間は0以上である必要があります")
        @Max(value = 720, message = "レビュー期間は720時間以下である必要があります")
        @Builder.Default
        private Integer reviewPeriodHours = 72; // 3日
    }

    /**
     * 提案作成レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProposalResponse {
        @NotNull
        private Long proposalId;

        @NotNull
        private String title;

        @NotNull
        private String proposerId;

        @NotNull
        private String status;

        @NotNull
        private LocalDateTime createdAt;

        @NotNull
        private LocalDateTime votingStartDate;

        @NotNull
        private LocalDateTime votingEndDate;
    }

    /**
     * 投票リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoteRequest {
        @NotNull
        private Long proposalId;

        @NotBlank(message = "投票タイプは必須です")
        private String voteType; // FOR, AGAINST, ABSTAIN

        @Size(max = 1000, message = "理由は1000文字以内で入力してください")
        private String reason;

        @Min(value = 1, message = "信頼度は1以上である必要があります")
        @Max(value = 100, message = "信頼度は100以下である必要があります")
        @Builder.Default
        private Integer confidence = 100;

        @Builder.Default
        private Boolean isDelegate = false; // 委任投票かどうか
    }

    /**
     * 投票レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoteResponse {
        @NotNull
        private Long voteId;

        @NotNull
        private Long proposalId;

        @NotNull
        private String voterId;

        @NotNull
        private String voteType;

        @NotNull
        private BigDecimal votingPower;

        @NotNull
        private LocalDateTime votedAt;

        private String reason;

        private Integer confidence;

        private Boolean isDelegate;
    }

    /**
     * 提案詳細レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposalDetailResponse {
        @NotNull
        private Long proposalId;

        @NotNull
        private String title;

        @NotNull
        private String description;

        @NotNull
        private String category;

        @NotNull
        private String proposalType;

        @NotNull
        private String status;

        @NotNull
        private String proposerId;

        @NotNull
        private LocalDateTime createdAt;

        @NotNull
        private LocalDateTime votingStartDate;

        @NotNull
        private LocalDateTime votingEndDate;

        @NotNull
        private Integer minimumQuorum;

        @NotNull
        private BigDecimal approvalThreshold;

        private String parameters;

        private String impactAssessment;

        private String implementationPlan;

        // 投票統計
        private VotingStatistics votingStatistics;

        // 実行情報
        private LocalDateTime executionDeadline;

        private LocalDateTime executedAt;

        private String executedBy;

        private String executionTransactionHash;

        // 表示用
        private String displayCategory;

        private String displayStatus;

        private Boolean canVote; // 現在のユーザーが投票可能か

        private Boolean hasVoted; // 現在のユーザーが投票済みか
    }

    /**
     * 投票統計
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VotingStatistics {
        @NotNull
        private Integer totalVotes;

        @NotNull
        private BigDecimal totalVotingPower;

        @NotNull
        private Integer forVotes;

        @NotNull
        private BigDecimal forVotingPower;

        @NotNull
        private Integer againstVotes;

        @NotNull
        private BigDecimal againstVotingPower;

        @NotNull
        private Integer abstainVotes;

        @NotNull
        private BigDecimal abstainVotingPower;

        @NotNull
        private BigDecimal participationRate; // 参加率

        @NotNull
        private BigDecimal approvalRate; // 承認率

        @NotNull
        private Boolean quorumMet; // クォーラム達成

        @NotNull
        private Boolean thresholdMet; // 閾値達成
    }

    /**
     * 提案一覧リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposalListRequest {
        @Min(value = 0, message = "ページ番号は0以上である必要があります")
        @Builder.Default
        private Integer page = 0;

        @Min(value = 1, message = "ページサイズは1以上である必要があります")
        @Max(value = 100, message = "ページサイズは100以下である必要があります")
        @Builder.Default
        private Integer size = 20;

        private String category; // フィルタ用カテゴリ

        private String status; // フィルタ用ステータス

        private String proposalType; // フィルタ用提案タイプ

        @Builder.Default
        private String sortBy = "createdAt"; // ソート項目

        @Builder.Default
        private String sortDirection = "DESC"; // ASC or DESC
    }

    /**
     * 提案一覧アイテム
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposalListItem {
        @NotNull
        private Long proposalId;

        @NotNull
        private String title;

        @NotNull
        private String category;

        @NotNull
        private String proposalType;

        @NotNull
        private String status;

        @NotNull
        private String proposerId;

        @NotNull
        private LocalDateTime createdAt;

        @NotNull
        private LocalDateTime votingEndDate;

        private Integer totalVotes;

        private BigDecimal approvalRate;

        private Boolean quorumMet;

        private String displayCategory;

        private String displayStatus;

        private Boolean canVote;

        private Boolean hasVoted;
    }

    /**
     * 投票履歴リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoteHistoryRequest {
        @Min(value = 0, message = "ページ番号は0以上である必要があります")
        @Builder.Default
        private Integer page = 0;

        @Min(value = 1, message = "ページサイズは1以上である必要があります")
        @Max(value = 100, message = "ページサイズは100以下である必要があります")
        @Builder.Default
        private Integer size = 20;

        private String voteType; // フィルタ用投票タイプ

        private LocalDateTime startDate;

        private LocalDateTime endDate;
    }

    /**
     * 投票履歴アイテム
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoteHistoryItem {
        @NotNull
        private Long voteId;

        @NotNull
        private Long proposalId;

        @NotNull
        private String proposalTitle;

        @NotNull
        private String voteType;

        @NotNull
        private BigDecimal votingPower;

        @NotNull
        private LocalDateTime votedAt;

        private String reason;

        private Integer confidence;

        private String displayVoteType;

        private String proposalStatus;
    }

    /**
     * 提案実行リクエスト（ADMIN専用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecuteProposalRequest {
        @NotNull
        private Long proposalId;

        private String executionNotes; // 実行ノート
    }

    /**
     * 提案実行レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecuteProposalResponse {
        @NotNull
        private Long proposalId;

        @NotNull
        private String status; // EXECUTED, FAILED

        @NotNull
        private LocalDateTime executedAt;

        @NotNull
        private String executedBy;

        private String transactionHash;

        private String errorMessage; // エラー時のメッセージ
    }

    /**
     * ガバナンス統計レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GovernanceStatisticsResponse {
        @NotNull
        private Integer totalProposals;

        @NotNull
        private Integer activeProposals;

        @NotNull
        private Integer executedProposals;

        @NotNull
        private Integer rejectedProposals;

        @NotNull
        private BigDecimal averageParticipationRate;

        @NotNull
        private BigDecimal averageApprovalRate;

        @NotNull
        private Integer totalVoters;

        @NotNull
        private BigDecimal totalVotingPower;

        private CategoryBreakdown categoryBreakdown;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CategoryBreakdown {
            private Integer tokenomics;
            private Integer governance;
            private Integer technical;
            private Integer economic;
            private Integer community;
            private Integer security;
            private Integer partnership;
            private Integer treasury;
            private Integer protocolUpgrade;
            private Integer emergency;
        }
    }
}
