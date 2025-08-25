package com.sfr.tokyo.sfr_backend.entity.crypto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BurnDecision Entity - SFRトークンバーン決定の管理
 * AIによるバーン判定とガバナンス決定を記録
 */
@Entity
@Table(name = "burn_decisions", indexes = {
        @Index(name = "idx_burn_decisions_space_id", columnList = "space_id"),
        @Index(name = "idx_burn_decisions_decision_date", columnList = "decision_date"),
        @Index(name = "idx_burn_decisions_status", columnList = "status"),
        @Index(name = "idx_burn_decisions_decision_type", columnList = "decision_type"),
        @Index(name = "idx_burn_decisions_proposal_id", columnList = "proposal_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BurnDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    private Long spaceId;

    @Column(name = "proposed_burn_amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "提案バーン量は必須です")
    @DecimalMin(value = "0.00000001", message = "提案バーン量は0.00000001以上である必要があります")
    private BigDecimal proposedBurnAmount;

    @Column(name = "actual_burn_amount", precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "実際のバーン量は0以上である必要があります")
    private BigDecimal actualBurnAmount;

    @Column(name = "circulating_supply_before", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "バーン前流通量は必須です")
    @DecimalMin(value = "0.0", message = "バーン前流通量は0以上である必要があります")
    private BigDecimal circulatingSupplyBefore;

    @Column(name = "circulating_supply_after", precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "バーン後流通量は0以上である必要があります")
    private BigDecimal circulatingSupplyAfter;

    @Column(name = "burn_rate_proposed", nullable = false, precision = 10, scale = 6)
    @NotNull(message = "提案バーン率は必須です")
    @DecimalMin(value = "0.000001", message = "提案バーン率は0.000001以上である必要があります")
    @DecimalMax(value = "0.100000", message = "提案バーン率は0.100000以下である必要があります")
    private BigDecimal burnRateProposed;

    @Column(name = "burn_rate_actual", precision = 10, scale = 6)
    @DecimalMin(value = "0.0", message = "実際のバーン率は0以上である必要があります")
    @DecimalMax(value = "0.100000", message = "実際のバーン率は0.100000以下である必要があります")
    private BigDecimal burnRateActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false, length = 30)
    @NotNull(message = "決定タイプは必須です")
    private DecisionType decisionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_reason", nullable = false, length = 30)
    @NotNull(message = "トリガー理由は必須です")
    private TriggerReason triggerReason;

    @Column(name = "ai_confidence_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "AI信頼度は0以上である必要があります")
    @DecimalMax(value = "100.0", message = "AI信頼度は100以下である必要があります")
    private BigDecimal aiConfidenceScore;

    @Column(name = "economic_indicators", columnDefinition = "TEXT")
    private String economicIndicators; // JSON形式での経済指標

    @Column(name = "market_conditions", columnDefinition = "TEXT")
    private String marketConditions; // JSON形式での市場状況

    @Column(name = "user_activity_metrics", columnDefinition = "TEXT")
    private String userActivityMetrics; // JSON形式でのユーザー活動指標

    @Column(name = "deflation_impact_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "デフレ影響スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "デフレ影響スコアは100以下である必要があります")
    private BigDecimal deflationImpactScore;

    @Column(name = "community_health_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "コミュニティ健全性スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "コミュニティ健全性スコアは100以下である必要があります")
    private BigDecimal communityHealthScore;

    @Column(name = "algorithm_version", nullable = false, length = 20)
    @NotBlank(message = "アルゴリズムバージョンは必須です")
    @Size(max = 20, message = "アルゴリズムバージョンは20文字以内で入力してください")
    @Builder.Default
    private String algorithmVersion = "v1.0";

    @Column(name = "decision_rationale", columnDefinition = "TEXT")
    private String decisionRationale; // 決定根拠

    @Column(name = "risk_assessment", columnDefinition = "TEXT")
    private String riskAssessment; // リスク評価

    @Column(name = "expected_outcomes", columnDefinition = "TEXT")
    private String expectedOutcomes; // 期待される結果

    @Column(name = "decision_date", nullable = false)
    @NotNull(message = "決定日は必須です")
    private LocalDateTime decisionDate;

    @Column(name = "scheduled_execution_date", nullable = true)
    private LocalDateTime scheduledExecutionDate;

    @Column(name = "actual_execution_date", nullable = true)
    private LocalDateTime actualExecutionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private BurnStatus status = BurnStatus.PROPOSED;

    @Column(name = "proposal_id", nullable = true)
    private Long proposalId; // ガバナンス提案ID（該当する場合）

    @Column(name = "voting_end_date", nullable = true)
    private LocalDateTime votingEndDate; // 投票終了日

    @Column(name = "votes_for", nullable = false)
    @Min(value = 0, message = "賛成票は0以上である必要があります")
    @Builder.Default
    private Integer votesFor = 0;

    @Column(name = "votes_against", nullable = false)
    @Min(value = 0, message = "反対票は0以上である必要があります")
    @Builder.Default
    private Integer votesAgainst = 0;

    @Column(name = "votes_abstain", nullable = false)
    @Min(value = 0, message = "棄権票は0以上である必要があります")
    @Builder.Default
    private Integer votesAbstain = 0;

    @Column(name = "quorum_reached", nullable = false)
    @Builder.Default
    private Boolean quorumReached = false;

    @Column(name = "decision_maker_id", nullable = true)
    private UUID decisionMakerId; // 決定者ID（手動決定の場合）

    @Column(name = "approved_by", nullable = true)
    private UUID approvedBy; // 承認者

    @Column(name = "approved_at", nullable = true)
    private LocalDateTime approvedAt;

    @Column(name = "executed_by", nullable = true)
    private UUID executedBy; // 実行者

    @Column(name = "transaction_hash", nullable = true, length = 100)
    @Size(max = 100, message = "トランザクションハッシュは100文字以内で入力してください")
    private String transactionHash;

    @Column(name = "batch_id", nullable = true)
    private UUID batchId; // バッチ処理でのグループ化

    @Column(name = "rollback_reason", columnDefinition = "TEXT")
    private String rollbackReason; // ロールバック理由

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes; // 管理者メモ

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 列挙型定義

    /**
     * 決定タイプ
     */
    public enum DecisionType {
        AI_AUTOMATIC, // AI自動判定
        GOVERNANCE_PROPOSAL, // ガバナンス提案
        ADMIN_DECISION, // 管理者決定
        EMERGENCY_BURN, // 緊急バーン
        SCHEDULED_BURN, // スケジュールドバーン
        COMMUNITY_REQUEST // コミュニティリクエスト
    }

    /**
     * トリガー理由
     */
    public enum TriggerReason {
        INFLATION_CONTROL, // インフレ制御
        EXCESS_SUPPLY, // 供給過多
        LOW_ACTIVITY, // 活動低下
        MARKET_CORRECTION, // 市場修正
        TOKENOMICS_BALANCE, // トークノミクスバランス
        GOVERNANCE_MANDATE, // ガバナンス指令
        SECURITY_MEASURE, // セキュリティ対策
        ECOSYSTEM_HEALTH // エコシステム健全性
    }

    /**
     * バーンステータス
     */
    public enum BurnStatus {
        PROPOSED, // 提案済み
        UNDER_REVIEW, // 審査中
        VOTING, // 投票中
        APPROVED, // 承認済み
        SCHEDULED, // スケジュール済み
        EXECUTING, // 実行中
        COMPLETED, // 完了
        FAILED, // 失敗
        REJECTED, // 拒否
        CANCELLED, // キャンセル
        ROLLED_BACK // ロールバック
    }

    // ビジネスロジックメソッド

    /**
     * 投票を開始する
     * 
     * @param votingDurationHours 投票期間（時間）
     */
    public void startVoting(int votingDurationHours) {
        if (status == BurnStatus.PROPOSED || status == BurnStatus.UNDER_REVIEW) {
            this.status = BurnStatus.VOTING;
            this.votingEndDate = LocalDateTime.now().plusHours(votingDurationHours);
        }
    }

    /**
     * 投票が終了したかチェック
     * 
     * @return 投票が終了したか
     */
    public boolean isVotingEnded() {
        return votingEndDate != null && LocalDateTime.now().isAfter(votingEndDate);
    }

    /**
     * 投票を追加する
     * 
     * @param voteType 投票タイプ（FOR/AGAINST/ABSTAIN）
     */
    public void addVote(String voteType) {
        switch (voteType.toUpperCase()) {
            case "FOR":
                this.votesFor++;
                break;
            case "AGAINST":
                this.votesAgainst++;
                break;
            case "ABSTAIN":
                this.votesAbstain++;
                break;
        }
    }

    /**
     * 総投票数を取得
     * 
     * @return 総投票数
     */
    public int getTotalVotes() {
        return votesFor + votesAgainst + votesAbstain;
    }

    /**
     * 賛成率を計算
     * 
     * @return 賛成率（0-100）
     */
    public BigDecimal getApprovalRate() {
        int total = getTotalVotes();
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(votesFor)
                .divide(new BigDecimal(total), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * クォーラムに達したかチェック
     * 
     * @param requiredVotes 必要投票数
     * @return クォーラムに達したか
     */
    public boolean checkQuorum(int requiredVotes) {
        this.quorumReached = getTotalVotes() >= requiredVotes;
        return this.quorumReached;
    }

    /**
     * 承認する
     * 
     * @param approver 承認者
     */
    public void approve(UUID approver) {
        if (status == BurnStatus.VOTING && isVotingEnded() && quorumReached) {
            // 賛成が過半数の場合のみ承認
            if (votesFor > votesAgainst) {
                this.status = BurnStatus.APPROVED;
                this.approvedBy = approver;
                this.approvedAt = LocalDateTime.now();
            } else {
                this.status = BurnStatus.REJECTED;
            }
        } else if (status == BurnStatus.PROPOSED || status == BurnStatus.UNDER_REVIEW) {
            // 直接承認の場合
            this.status = BurnStatus.APPROVED;
            this.approvedBy = approver;
            this.approvedAt = LocalDateTime.now();
        }
    }

    /**
     * スケジュールする
     * 
     * @param executionDate 実行予定日
     */
    public void schedule(LocalDateTime executionDate) {
        if (status == BurnStatus.APPROVED) {
            this.status = BurnStatus.SCHEDULED;
            this.scheduledExecutionDate = executionDate;
        }
    }

    /**
     * 実行中にマークする
     */
    public void markAsExecuting() {
        if (status == BurnStatus.APPROVED || status == BurnStatus.SCHEDULED) {
            this.status = BurnStatus.EXECUTING;
        }
    }

    /**
     * 完了にマークする
     * 
     * @param actualAmount     実際のバーン量
     * @param circulatingAfter バーン後流通量
     * @param transactionHash  トランザクションハッシュ
     * @param executor         実行者
     */
    public void markAsCompleted(
            BigDecimal actualAmount,
            BigDecimal circulatingAfter,
            String transactionHash,
            UUID executor) {
        if (status == BurnStatus.EXECUTING) {
            this.status = BurnStatus.COMPLETED;
            this.actualBurnAmount = actualAmount;
            this.circulatingSupplyAfter = circulatingAfter;
            this.burnRateActual = actualAmount.divide(circulatingSupplyBefore, 6, java.math.RoundingMode.HALF_UP);
            this.transactionHash = transactionHash;
            this.executedBy = executor;
            this.actualExecutionDate = LocalDateTime.now();
        }
    }

    /**
     * 失敗にマークする
     * 
     * @param reason 失敗理由
     */
    public void markAsFailed(String reason) {
        if (status == BurnStatus.EXECUTING) {
            this.status = BurnStatus.FAILED;
            this.adminNotes = this.adminNotes != null ? this.adminNotes + "\n失敗理由: " + reason : "失敗理由: " + reason;
        }
    }

    /**
     * 拒否する
     * 
     * @param rejector 拒否者
     * @param reason   拒否理由
     */
    public void reject(UUID rejector, String reason) {
        if (status == BurnStatus.PROPOSED || status == BurnStatus.UNDER_REVIEW || status == BurnStatus.VOTING) {
            this.status = BurnStatus.REJECTED;
            this.decisionMakerId = rejector;
            this.adminNotes = this.adminNotes != null ? this.adminNotes + "\n拒否理由: " + reason : "拒否理由: " + reason;
        }
    }

    /**
     * キャンセルする
     * 
     * @param canceller キャンセル者
     * @param reason    キャンセル理由
     */
    public void cancel(UUID canceller, String reason) {
        if (status != BurnStatus.COMPLETED && status != BurnStatus.FAILED) {
            this.status = BurnStatus.CANCELLED;
            this.decisionMakerId = canceller;
            this.adminNotes = this.adminNotes != null ? this.adminNotes + "\nキャンセル理由: " + reason : "キャンセル理由: " + reason;
        }
    }

    /**
     * ロールバックする
     * 
     * @param executor ロールバック実行者
     * @param reason   ロールバック理由
     */
    public void rollback(UUID executor, String reason) {
        if (status == BurnStatus.COMPLETED) {
            this.status = BurnStatus.ROLLED_BACK;
            this.executedBy = executor;
            this.rollbackReason = reason;
        }
    }

    /**
     * 決定の妥当性をチェックする
     * 
     * @return 妥当かどうか
     */
    public boolean isDecisionValid() {
        return proposedBurnAmount.compareTo(BigDecimal.ZERO) > 0 &&
                proposedBurnAmount.compareTo(circulatingSupplyBefore) <= 0 &&
                burnRateProposed.compareTo(BigDecimal.ZERO) > 0 &&
                burnRateProposed.compareTo(new BigDecimal("0.1")) <= 0;
    }

    /**
     * 実行可能かチェックする
     * 
     * @return 実行可能かどうか
     */
    public boolean isExecutable() {
        return status == BurnStatus.APPROVED || status == BurnStatus.SCHEDULED;
    }

    /**
     * AI自動バーン決定ファクトリメソッド
     */
    public static BurnDecision createAiDecision(
            Long spaceId,
            BigDecimal proposedAmount,
            BigDecimal circulatingSupply,
            BigDecimal burnRate,
            TriggerReason reason,
            BigDecimal confidenceScore,
            String economicData) {
        return BurnDecision.builder()
                .spaceId(spaceId)
                .proposedBurnAmount(proposedAmount)
                .circulatingSupplyBefore(circulatingSupply)
                .burnRateProposed(burnRate)
                .decisionType(DecisionType.AI_AUTOMATIC)
                .triggerReason(reason)
                .aiConfidenceScore(confidenceScore)
                .economicIndicators(economicData)
                .decisionDate(LocalDateTime.now())
                .decisionRationale("AI analysis based on current market conditions and tokenomics")
                .build();
    }

    /**
     * ガバナンス提案バーン決定ファクトリメソッド
     */
    public static BurnDecision createGovernanceProposal(
            Long spaceId,
            Long proposalId,
            BigDecimal proposedAmount,
            BigDecimal circulatingSupply,
            BigDecimal burnRate,
            TriggerReason reason,
            String rationale,
            int votingHours) {
        BurnDecision decision = BurnDecision.builder()
                .spaceId(spaceId)
                .proposalId(proposalId)
                .proposedBurnAmount(proposedAmount)
                .circulatingSupplyBefore(circulatingSupply)
                .burnRateProposed(burnRate)
                .decisionType(DecisionType.GOVERNANCE_PROPOSAL)
                .triggerReason(reason)
                .decisionDate(LocalDateTime.now())
                .decisionRationale(rationale)
                .build();

        decision.startVoting(votingHours);
        return decision;
    }
}
