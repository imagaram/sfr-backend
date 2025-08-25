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
 * GovernanceVote Entity - ガバナンス投票の記録
 * 個々の投票行動と投票権を管理
 */
@Entity
@Table(name = "governance_votes", indexes = {
        @Index(name = "idx_governance_votes_proposal_id", columnList = "proposal_id"),
        @Index(name = "idx_governance_votes_voter_id", columnList = "voter_id"),
        @Index(name = "idx_governance_votes_vote_type", columnList = "vote_type"),
        @Index(name = "idx_governance_votes_voted_at", columnList = "voted_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_governance_votes_proposal_voter", columnNames = { "proposal_id", "voter_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovernanceVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proposal_id", nullable = false)
    @NotNull(message = "提案IDは必須です")
    private Long proposalId;

    @Column(name = "voter_id", nullable = false)
    @NotNull(message = "投票者IDは必須です")
    private UUID voterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 10)
    @NotNull(message = "投票タイプは必須です")
    private VoteType voteType;

    @Column(name = "voting_power", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "投票権は必須です")
    @DecimalMin(value = "0.00000001", message = "投票権は0.00000001以上である必要があります")
    private BigDecimal votingPower;

    @Column(name = "token_balance_snapshot", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "トークン残高スナップショットは必須です")
    @DecimalMin(value = "0.0", message = "トークン残高スナップショットは0以上である必要があります")
    private BigDecimal tokenBalanceSnapshot;

    @Column(name = "delegation_multiplier", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "委任マルチプライヤーは必須です")
    @DecimalMin(value = "1.0", message = "委任マルチプライヤーは1.0以上である必要があります")
    @DecimalMax(value = "10.0", message = "委任マルチプライヤーは10.0以下である必要があります")
    @Builder.Default
    private BigDecimal delegationMultiplier = BigDecimal.ONE;

    @Column(name = "delegated_voting_power", nullable = false, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "委任投票権は0以上である必要があります")
    @Builder.Default
    private BigDecimal delegatedVotingPower = BigDecimal.ZERO;

    @Column(name = "activity_bonus", nullable = false, precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "活動ボーナスは0以上である必要があります")
    @DecimalMax(value = "5.0", message = "活動ボーナスは5.0以下である必要があります")
    @Builder.Default
    private BigDecimal activityBonus = BigDecimal.ZERO;

    @Column(name = "reputation_score", nullable = false, precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "評判スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "評判スコアは100以下である必要があります")
    @Builder.Default
    private BigDecimal reputationScore = new BigDecimal("50.0");

    @Column(name = "vote_reason", columnDefinition = "TEXT")
    private String voteReason;

    @Column(name = "confidence_level", precision = 3, scale = 0)
    @Min(value = 1, message = "確信度は1以上である必要があります")
    @Max(value = 100, message = "確信度は100以下である必要があります")
    private Integer confidenceLevel;

    @Column(name = "voted_at", nullable = false)
    @NotNull(message = "投票日時は必須です")
    private LocalDateTime votedAt;

    @Column(name = "last_changed_at", nullable = true)
    private LocalDateTime lastChangedAt;

    @Column(name = "is_delegate_vote", nullable = false)
    @Builder.Default
    private Boolean isDelegateVote = false; // 委任者による投票かどうか

    @Column(name = "delegator_id", nullable = true)
    private UUID delegatorId; // 委任者ID（委任投票の場合）

    @Column(name = "vote_weight_factors", columnDefinition = "TEXT")
    private String voteWeightFactors; // JSON形式での投票権重計算要因

    @Column(name = "transaction_hash", nullable = true, length = 100)
    @Size(max = 100, message = "トランザクションハッシュは100文字以内で入力してください")
    private String transactionHash;

    @Column(name = "vote_signature", nullable = true, length = 200)
    @Size(max = 200, message = "投票署名は200文字以内で入力してください")
    private String voteSignature; // デジタル署名

    @Column(name = "ip_address", nullable = true, length = 45)
    @Size(max = 45, message = "IPアドレスは45文字以内で入力してください")
    private String ipAddress; // IPv6対応

    @Column(name = "user_agent", nullable = true, length = 500)
    @Size(max = 500, message = "ユーザーエージェントは500文字以内で入力してください")
    private String userAgent;

    @Column(name = "is_changed", nullable = false)
    @Builder.Default
    private Boolean isChanged = false; // 投票を変更したかどうか

    @Column(name = "previous_vote_type", nullable = true, length = 10)
    private String previousVoteType; // 変更前の投票タイプ

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason; // 変更理由

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 列挙型定義

    /**
     * 投票タイプ
     */
    public enum VoteType {
        FOR, // 賛成
        AGAINST, // 反対
        ABSTAIN // 棄権
    }

    // ビジネスロジックメソッド

    /**
     * 最終的な投票権を計算する
     * 
     * @return 最終投票権
     */
    public BigDecimal getFinalVotingPower() {
        BigDecimal basePower = tokenBalanceSnapshot.add(delegatedVotingPower);
        BigDecimal multiplier = delegationMultiplier.add(activityBonus);
        BigDecimal reputationFactor = reputationScore.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);

        return basePower.multiply(multiplier).multiply(reputationFactor);
    }

    /**
     * 投票を変更する
     * 
     * @param newVoteType 新しい投票タイプ
     * @param reason      変更理由
     */
    public void changeVote(VoteType newVoteType, String reason) {
        if (this.voteType != newVoteType) {
            this.previousVoteType = this.voteType.name();
            this.voteType = newVoteType;
            this.isChanged = true;
            this.changeReason = reason;
            this.lastChangedAt = LocalDateTime.now();
        }
    }

    /**
     * 委任投票として設定する
     * 
     * @param delegatorId 委任者ID
     */
    public void setAsDelegateVote(UUID delegatorId) {
        this.isDelegateVote = true;
        this.delegatorId = delegatorId;
    }

    /**
     * 投票権重の詳細を設定する
     * 
     * @param factors 重み要因のJSON
     */
    public void setVoteWeightFactors(String factors) {
        this.voteWeightFactors = factors;
    }

    /**
     * 投票の妥当性をチェックする
     * 
     * @return 妥当かどうか
     */
    public boolean isVoteValid() {
        return votingPower.compareTo(BigDecimal.ZERO) > 0 &&
                tokenBalanceSnapshot.compareTo(BigDecimal.ZERO) >= 0 &&
                delegationMultiplier.compareTo(BigDecimal.ONE) >= 0 &&
                reputationScore.compareTo(BigDecimal.ZERO) >= 0 &&
                confidenceLevel != null && confidenceLevel >= 1 && confidenceLevel <= 100;
    }

    /**
     * 委任投票かどうかをチェック
     * 
     * @return 委任投票かどうか
     */
    public boolean isDelegateVote() {
        return isDelegateVote != null && isDelegateVote && delegatorId != null;
    }

    /**
     * 投票が変更されたかチェック
     * 
     * @return 変更されたかどうか
     */
    public boolean hasBeenChanged() {
        return isChanged != null && isChanged && previousVoteType != null;
    }

    /**
     * 投票の影響力スコアを計算する
     * 
     * @return 影響力スコア（0-100）
     */
    public BigDecimal getInfluenceScore() {
        BigDecimal powerFactor = votingPower.divide(new BigDecimal("1000"), 4, java.math.RoundingMode.HALF_UP);
        BigDecimal confidenceFactor = new BigDecimal(confidenceLevel).divide(new BigDecimal("100"), 4,
                java.math.RoundingMode.HALF_UP);
        BigDecimal reputationFactor = reputationScore.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);

        return powerFactor.multiply(confidenceFactor).multiply(reputationFactor)
                .multiply(new BigDecimal("100"))
                .min(new BigDecimal("100"));
    }

    /**
     * 標準投票ファクトリメソッド
     */
    public static GovernanceVote createStandardVote(
            Long proposalId,
            UUID voterId,
            VoteType voteType,
            BigDecimal tokenBalance,
            BigDecimal reputationScore,
            String reason,
            Integer confidence) {

        GovernanceVote vote = GovernanceVote.builder()
                .proposalId(proposalId)
                .voterId(voterId)
                .voteType(voteType)
                .tokenBalanceSnapshot(tokenBalance)
                .reputationScore(reputationScore)
                .voteReason(reason)
                .confidenceLevel(confidence)
                .votedAt(LocalDateTime.now())
                .build();

        // 投票権を計算
        vote.votingPower = vote.getFinalVotingPower();
        return vote;
    }

    /**
     * 委任投票ファクトリメソッド
     */
    public static GovernanceVote createDelegateVote(
            Long proposalId,
            UUID delegateId,
            UUID delegatorId,
            VoteType voteType,
            BigDecimal delegatedPower,
            BigDecimal delegateReputation,
            String reason) {

        GovernanceVote vote = GovernanceVote.builder()
                .proposalId(proposalId)
                .voterId(delegateId)
                .voteType(voteType)
                .tokenBalanceSnapshot(BigDecimal.ZERO)
                .delegatedVotingPower(delegatedPower)
                .reputationScore(delegateReputation)
                .voteReason(reason)
                .confidenceLevel(90) // 委任投票は高い確信度
                .votedAt(LocalDateTime.now())
                .isDelegateVote(true)
                .delegatorId(delegatorId)
                .build();

        vote.votingPower = vote.getFinalVotingPower();
        return vote;
    }

    /**
     * 高活動ユーザー投票ファクトリメソッド
     */
    public static GovernanceVote createHighActivityVote(
            Long proposalId,
            UUID voterId,
            VoteType voteType,
            BigDecimal tokenBalance,
            BigDecimal activityBonus,
            BigDecimal reputationScore,
            String reason,
            Integer confidence) {

        GovernanceVote vote = GovernanceVote.builder()
                .proposalId(proposalId)
                .voterId(voterId)
                .voteType(voteType)
                .tokenBalanceSnapshot(tokenBalance)
                .activityBonus(activityBonus)
                .reputationScore(reputationScore)
                .voteReason(reason)
                .confidenceLevel(confidence)
                .votedAt(LocalDateTime.now())
                .build();

        vote.votingPower = vote.getFinalVotingPower();
        return vote;
    }

    /**
     * 投票権重要因を計算してJSONとして返す
     * 
     * @return JSON形式の重み要因
     */
    public String calculateWeightFactors() {
        return String.format(
                "{\"tokenBalance\":\"%s\",\"delegatedPower\":\"%s\",\"delegationMultiplier\":\"%s\",\"activityBonus\":\"%s\",\"reputationScore\":\"%s\",\"finalPower\":\"%s\"}",
                tokenBalanceSnapshot,
                delegatedVotingPower,
                delegationMultiplier,
                activityBonus,
                reputationScore,
                getFinalVotingPower());
    }
}
