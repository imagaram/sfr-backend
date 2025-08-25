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
 * GovernanceProposal Entity - SFRガバナンス提案の管理
 * トークン関連の重要な決定に関する提案と投票を管理
 */
@Entity
@Table(name = "governance_proposals", indexes = {
        @Index(name = "idx_governance_proposals_space_id", columnList = "space_id"),
        @Index(name = "idx_governance_proposals_proposer_id", columnList = "proposer_id"),
        @Index(name = "idx_governance_proposals_category", columnList = "category"),
        @Index(name = "idx_governance_proposals_status", columnList = "status"),
        @Index(name = "idx_governance_proposals_voting_end", columnList = "voting_end_date"),
        @Index(name = "idx_governance_proposals_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovernanceProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    private Long spaceId;

    @Column(name = "proposer_id", nullable = false)
    @NotNull(message = "提案者IDは必須です")
    private UUID proposerId;

    @Column(name = "title", nullable = false, length = 200)
    @NotBlank(message = "タイトルは必須です")
    @Size(min = 10, max = 200, message = "タイトルは10文字以上200文字以内で入力してください")
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "説明は必須です")
    @Size(min = 50, message = "説明は50文字以上で入力してください")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    @NotNull(message = "カテゴリは必須です")
    private ProposalCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposal_type", nullable = false, length = 30)
    @NotNull(message = "提案タイプは必須です")
    private ProposalType proposalType;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters; // JSON形式での提案パラメータ

    @Column(name = "impact_assessment", columnDefinition = "TEXT")
    private String impactAssessment; // 影響評価

    @Column(name = "implementation_plan", columnDefinition = "TEXT")
    private String implementationPlan; // 実装計画

    @Column(name = "minimum_quorum", nullable = false)
    @NotNull(message = "最小クォーラムは必須です")
    @Min(value = 1, message = "最小クォーラムは1以上である必要があります")
    @Builder.Default
    private Integer minimumQuorum = 100;

    @Column(name = "approval_threshold", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "承認閾値は必須です")
    @DecimalMin(value = "50.0", message = "承認閾値は50.0以上である必要があります")
    @DecimalMax(value = "100.0", message = "承認閾値は100.0以下である必要があります")
    @Builder.Default
    private BigDecimal approvalThreshold = new BigDecimal("66.67"); // デフォルト2/3

    @Column(name = "voting_start_date", nullable = false)
    @NotNull(message = "投票開始日は必須です")
    private LocalDateTime votingStartDate;

    @Column(name = "voting_end_date", nullable = false)
    @NotNull(message = "投票終了日は必須です")
    private LocalDateTime votingEndDate;

    @Column(name = "voting_duration_hours", nullable = false)
    @NotNull(message = "投票期間は必須です")
    @Min(value = 24, message = "投票期間は24時間以上である必要があります")
    @Max(value = 8760, message = "投票期間は8760時間以下である必要があります") // 1年以内
    @Builder.Default
    private Integer votingDurationHours = 168; // デフォルト1週間

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private ProposalStatus status = ProposalStatus.DRAFT;

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

    @Column(name = "total_voting_power", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "総投票権は必須です")
    @DecimalMin(value = "0.0", message = "総投票権は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalVotingPower = BigDecimal.ZERO;

    @Column(name = "voting_power_for", nullable = false, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "賛成投票権は0以上である必要があります")
    @Builder.Default
    private BigDecimal votingPowerFor = BigDecimal.ZERO;

    @Column(name = "voting_power_against", nullable = false, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "反対投票権は0以上である必要があります")
    @Builder.Default
    private BigDecimal votingPowerAgainst = BigDecimal.ZERO;

    @Column(name = "voting_power_abstain", nullable = false, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "棄権投票権は0以上である必要があります")
    @Builder.Default
    private BigDecimal votingPowerAbstain = BigDecimal.ZERO;

    @Column(name = "quorum_reached", nullable = false)
    @Builder.Default
    private Boolean quorumReached = false;

    @Column(name = "quorum_threshold", precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "クォーラム閾値は0以上である必要があります")
    private BigDecimal quorumThreshold;

    @Column(name = "execution_delay_hours", nullable = false)
    @Min(value = 0, message = "実行遅延時間は0以上である必要があります")
    @Max(value = 2160, message = "実行遅延時間は2160時間以下である必要があります") // 90日以内
    @Builder.Default
    private Integer executionDelayHours = 24; // デフォルト24時間

    @Column(name = "execution_deadline", nullable = true)
    private LocalDateTime executionDeadline;

    @Column(name = "executed_at", nullable = true)
    private LocalDateTime executedAt;

    @Column(name = "executed_by", nullable = true)
    private UUID executedBy;

    @Column(name = "execution_transaction_hash", nullable = true, length = 100)
    @Size(max = 100, message = "実行トランザクションハッシュは100文字以内で入力してください")
    private String executionTransactionHash;

    @Column(name = "cancelled_by", nullable = true)
    private UUID cancelledBy;

    @Column(name = "cancelled_at", nullable = true)
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "discussion_thread_id", nullable = true)
    private Long discussionThreadId; // 関連ディスカッション

    @Column(name = "related_proposal_ids", columnDefinition = "TEXT")
    private String relatedProposalIds; // JSON形式での関連提案ID

    @Column(name = "review_period_hours", nullable = false)
    @Min(value = 0, message = "レビュー期間は0以上である必要があります")
    @Max(value = 720, message = "レビュー期間は720時間以下である必要があります") // 30日以内
    @Builder.Default
    private Integer reviewPeriodHours = 72; // デフォルト3日

    @Column(name = "reviewed_by", nullable = true)
    private UUID reviewedBy;

    @Column(name = "reviewed_at", nullable = true)
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "snapshot_block_number", nullable = true)
    private Long snapshotBlockNumber; // 投票権のスナップショット基準ブロック

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON形式での追加メタデータ

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 列挙型定義

    /**
     * 提案カテゴリ
     */
    public enum ProposalCategory {
        TOKENOMICS, // トークノミクス
        GOVERNANCE, // ガバナンス
        TECHNICAL, // 技術
        ECONOMIC, // 経済
        COMMUNITY, // コミュニティ
        SECURITY, // セキュリティ
        PARTNERSHIP, // パートナーシップ
        TREASURY, // 財務
        PROTOCOL_UPGRADE, // プロトコルアップグレード
        EMERGENCY // 緊急
    }

    /**
     * 提案タイプ
     */
    public enum ProposalType {
        PARAMETER_CHANGE, // パラメータ変更
        PROTOCOL_UPGRADE, // プロトコルアップグレード
        TREASURY_ALLOCATION, // 財務配分
        BURN_DECISION, // バーン決定
        REWARD_ADJUSTMENT, // リワード調整
        GOVERNANCE_CHANGE, // ガバナンス変更
        PARTNERSHIP_APPROVAL, // パートナーシップ承認
        EMERGENCY_ACTION, // 緊急アクション
        FEATURE_REQUEST, // 機能リクエスト
        POLICY_CHANGE // ポリシー変更
    }

    /**
     * 提案ステータス
     */
    public enum ProposalStatus {
        DRAFT, // 下書き
        SUBMITTED, // 提出済み
        UNDER_REVIEW, // レビュー中
        APPROVED_FOR_VOTING, // 投票承認済み
        VOTING_ACTIVE, // 投票中
        VOTING_ENDED, // 投票終了
        PASSED, // 可決
        REJECTED, // 否決
        QUEUED, // 実行待ち
        EXECUTED, // 実行済み
        CANCELLED, // キャンセル
        EXPIRED // 期限切れ
    }

    // ビジネスロジックメソッド

    /**
     * 提案を提出する
     */
    public void submit() {
        if (status == ProposalStatus.DRAFT) {
            this.status = ProposalStatus.SUBMITTED;
        }
    }

    /**
     * レビューを開始する
     * 
     * @param reviewer レビュー者
     */
    public void startReview(UUID reviewer) {
        if (status == ProposalStatus.SUBMITTED) {
            this.status = ProposalStatus.UNDER_REVIEW;
            this.reviewedBy = reviewer;
            this.reviewedAt = LocalDateTime.now();
        }
    }

    /**
     * 投票を承認する
     * 
     * @param approver 承認者
     */
    public void approveForVoting(UUID approver) {
        if (status == ProposalStatus.UNDER_REVIEW) {
            this.status = ProposalStatus.APPROVED_FOR_VOTING;
            this.reviewedBy = approver;
            this.reviewedAt = LocalDateTime.now();
        }
    }

    /**
     * 投票を開始する
     */
    public void startVoting() {
        if (status == ProposalStatus.APPROVED_FOR_VOTING) {
            this.status = ProposalStatus.VOTING_ACTIVE;
            this.votingStartDate = LocalDateTime.now();
            this.votingEndDate = this.votingStartDate.plusHours(votingDurationHours);
            this.executionDeadline = this.votingEndDate.plusHours(executionDelayHours);
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
     * @param voteType    投票タイプ（FOR/AGAINST/ABSTAIN）
     * @param votingPower 投票権
     */
    public void addVote(String voteType, BigDecimal votingPower) {
        switch (voteType.toUpperCase()) {
            case "FOR":
                this.votesFor++;
                this.votingPowerFor = this.votingPowerFor.add(votingPower);
                break;
            case "AGAINST":
                this.votesAgainst++;
                this.votingPowerAgainst = this.votingPowerAgainst.add(votingPower);
                break;
            case "ABSTAIN":
                this.votesAbstain++;
                this.votingPowerAbstain = this.votingPowerAbstain.add(votingPower);
                break;
        }
        this.totalVotingPower = this.totalVotingPower.add(votingPower);
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
     * 賛成率を計算（投票権ベース）
     * 
     * @return 賛成率（0-100）
     */
    public BigDecimal getApprovalRateByPower() {
        BigDecimal validVotingPower = votingPowerFor.add(votingPowerAgainst);
        if (validVotingPower.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return votingPowerFor
                .divide(validVotingPower, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 参加率を計算
     * 
     * @return 参加率（0-100）
     */
    public BigDecimal getParticipationRate() {
        if (quorumThreshold == null || quorumThreshold.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalVotingPower
                .divide(quorumThreshold, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * クォーラムをチェックする
     * 
     * @return クォーラムに達したか
     */
    public boolean checkQuorum() {
        if (quorumThreshold != null) {
            this.quorumReached = totalVotingPower.compareTo(quorumThreshold) >= 0;
        } else {
            this.quorumReached = getTotalVotes() >= minimumQuorum;
        }
        return this.quorumReached;
    }

    /**
     * 投票を終了し結果を確定する
     */
    public void finalizeVoting() {
        if (status == ProposalStatus.VOTING_ACTIVE && isVotingEnded()) {
            this.status = ProposalStatus.VOTING_ENDED;

            checkQuorum();

            if (quorumReached) {
                BigDecimal approvalRate = getApprovalRateByPower();
                if (approvalRate.compareTo(approvalThreshold) >= 0) {
                    this.status = ProposalStatus.PASSED;
                    // 実行待ちキューに追加
                    queueForExecution();
                } else {
                    this.status = ProposalStatus.REJECTED;
                }
            } else {
                this.status = ProposalStatus.REJECTED;
            }
        }
    }

    /**
     * 実行待ちキューに追加
     */
    private void queueForExecution() {
        if (status == ProposalStatus.PASSED) {
            this.status = ProposalStatus.QUEUED;
        }
    }

    /**
     * 提案を実行する
     * 
     * @param executor        実行者
     * @param transactionHash トランザクションハッシュ
     */
    public void execute(UUID executor, String transactionHash) {
        if (status == ProposalStatus.QUEUED &&
                LocalDateTime.now().isAfter(votingEndDate.plusHours(executionDelayHours))) {
            this.status = ProposalStatus.EXECUTED;
            this.executedBy = executor;
            this.executedAt = LocalDateTime.now();
            this.executionTransactionHash = transactionHash;
        }
    }

    /**
     * 提案をキャンセルする
     * 
     * @param canceller キャンセル者
     * @param reason    キャンセル理由
     */
    public void cancel(UUID canceller, String reason) {
        if (status != ProposalStatus.EXECUTED) {
            this.status = ProposalStatus.CANCELLED;
            this.cancelledBy = canceller;
            this.cancelledAt = LocalDateTime.now();
            this.cancellationReason = reason;
        }
    }

    /**
     * 提案が実行可能かチェック
     * 
     * @return 実行可能かどうか
     */
    public boolean isExecutable() {
        return status == ProposalStatus.QUEUED &&
                executionDeadline != null &&
                LocalDateTime.now().isBefore(executionDeadline) &&
                LocalDateTime.now().isAfter(votingEndDate.plusHours(executionDelayHours));
    }

    /**
     * 提案が期限切れかチェック
     * 
     * @return 期限切れかどうか
     */
    public boolean isExpired() {
        return executionDeadline != null &&
                LocalDateTime.now().isAfter(executionDeadline) &&
                status == ProposalStatus.QUEUED;
    }

    /**
     * 期限切れにマークする
     */
    public void markAsExpired() {
        if (isExpired()) {
            this.status = ProposalStatus.EXPIRED;
        }
    }

    /**
     * パラメータ変更提案ファクトリメソッド
     */
    public static GovernanceProposal createParameterChangeProposal(
            Long spaceId,
            UUID proposerId,
            String title,
            String description,
            String parameters,
            int votingHours) {
        return GovernanceProposal.builder()
                .spaceId(spaceId)
                .proposerId(proposerId)
                .title(title)
                .description(description)
                .category(ProposalCategory.TOKENOMICS)
                .proposalType(ProposalType.PARAMETER_CHANGE)
                .parameters(parameters)
                .votingDurationHours(votingHours)
                .minimumQuorum(100)
                .approvalThreshold(new BigDecimal("66.67"))
                .build();
    }

    /**
     * バーン決定提案ファクトリメソッド
     */
    public static GovernanceProposal createBurnProposal(
            Long spaceId,
            UUID proposerId,
            String title,
            String description,
            BigDecimal burnAmount,
            String rationale,
            int votingHours) {
        return GovernanceProposal.builder()
                .spaceId(spaceId)
                .proposerId(proposerId)
                .title(title)
                .description(description)
                .category(ProposalCategory.ECONOMIC)
                .proposalType(ProposalType.BURN_DECISION)
                .parameters("{\"burnAmount\":\"" + burnAmount + "\",\"rationale\":\"" + rationale + "\"}")
                .votingDurationHours(votingHours)
                .minimumQuorum(200)
                .approvalThreshold(new BigDecimal("75.0"))
                .build();
    }

    /**
     * 緊急提案ファクトリメソッド
     */
    public static GovernanceProposal createEmergencyProposal(
            Long spaceId,
            UUID proposerId,
            String title,
            String description,
            String emergencyAction,
            int votingHours) {
        return GovernanceProposal.builder()
                .spaceId(spaceId)
                .proposerId(proposerId)
                .title(title)
                .description(description)
                .category(ProposalCategory.EMERGENCY)
                .proposalType(ProposalType.EMERGENCY_ACTION)
                .parameters("{\"emergencyAction\":\"" + emergencyAction + "\"}")
                .votingDurationHours(votingHours)
                .minimumQuorum(50) // 緊急時は低いクォーラム
                .approvalThreshold(new BigDecimal("80.0")) // ただし高い承認閾値
                .executionDelayHours(1) // 最小遅延
                .reviewPeriodHours(6) // 短いレビュー期間
                .build();
    }
}
