package com.sfr.tokyo.sfr_backend.entity.crypto.reward;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 活動別メトリクスエンティティ
 * 各活動タイプごとの詳細メトリクス
 */
@Entity
@Table(name = "activity_metrics", indexes = {
        @Index(name = "idx_activity_metrics_contribution", columnList = "contribution_record_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contribution_record_id", nullable = false)
    @NotNull(message = "貢献記録IDは必須です")
    private Long contributionRecordId;

    // 開発関連メトリクス
    @Column(name = "commits_count")
    @Min(value = 0, message = "コミット数は0以上である必要があります")
    @Builder.Default
    private Integer commitsCount = 0;

    @Column(name = "merged_prs_count")
    @Min(value = 0, message = "マージされたPR数は0以上である必要があります")
    @Builder.Default
    private Integer mergedPrsCount = 0;

    @Column(name = "issues_closed_count")
    @Min(value = 0, message = "クローズしたIssue数は0以上である必要があります")
    @Builder.Default
    private Integer issuesClosedCount = 0;

    @Column(name = "code_lines_added")
    @Min(value = 0, message = "追加コード行数は0以上である必要があります")
    @Builder.Default
    private Integer codeLinesAdded = 0;

    @Column(name = "code_lines_deleted")
    @Min(value = 0, message = "削除コード行数は0以上である必要があります")
    @Builder.Default
    private Integer codeLinesDeleted = 0;

    // 流動性提供関連
    @Column(name = "lp_amount", precision = 20, scale = 8)
    @DecimalMin(value = "0", message = "LP提供量は0以上である必要があります")
    @Builder.Default
    private BigDecimal lpAmount = BigDecimal.ZERO;

    @Column(name = "lp_duration_days")
    @Min(value = 0, message = "LP提供期間は0以上である必要があります")
    @Builder.Default
    private Integer lpDurationDays = 0;

    @Column(name = "trading_volume", precision = 20, scale = 8)
    @DecimalMin(value = "0", message = "取引量は0以上である必要があります")
    @Builder.Default
    private BigDecimal tradingVolume = BigDecimal.ZERO;

    // ガバナンス関連
    @Column(name = "votes_cast_count")
    @Min(value = 0, message = "投票回数は0以上である必要があります")
    @Builder.Default
    private Integer votesCastCount = 0;

    @Column(name = "proposals_submitted_count")
    @Min(value = 0, message = "提案提出数は0以上である必要があります")
    @Builder.Default
    private Integer proposalsSubmittedCount = 0;

    @Column(name = "discussion_contributions")
    @Min(value = 0, message = "議論貢献数は0以上である必要があります")
    @Builder.Default
    private Integer discussionContributions = 0;

    // 教育・普及関連
    @Column(name = "course_attendees")
    @Min(value = 0, message = "講座参加者数は0以上である必要があります")
    @Builder.Default
    private Integer courseAttendees = 0;

    @Column(name = "course_rating", precision = 3, scale = 2)
    @DecimalMin(value = "0.00", message = "講座評価は0.00以上である必要があります")
    @DecimalMax(value = "5.00", message = "講座評価は5.00以下である必要があります")
    @Builder.Default
    private BigDecimal courseRating = BigDecimal.ZERO;

    @Column(name = "completion_rate", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "完了率は0.00以上である必要があります")
    @DecimalMax(value = "100.00", message = "完了率は100.00以下である必要があります")
    @Builder.Default
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(name = "community_followers")
    @Min(value = 0, message = "コミュニティフォロワー数は0以上である必要があります")
    @Builder.Default
    private Integer communityFollowers = 0;

    // 商用利用関連
    @Column(name = "sfr_payment_count")
    @Min(value = 0, message = "SFR決済回数は0以上である必要があります")
    @Builder.Default
    private Integer sfrPaymentCount = 0;

    @Column(name = "sfr_payment_amount", precision = 20, scale = 8)
    @DecimalMin(value = "0", message = "SFR決済金額は0以上である必要があります")
    @Builder.Default
    private BigDecimal sfrPaymentAmount = BigDecimal.ZERO;

    @Column(name = "sales_revenue", precision = 15, scale = 2)
    @DecimalMin(value = "0", message = "売上高は0以上である必要があります")
    @Builder.Default
    private BigDecimal salesRevenue = BigDecimal.ZERO;

    @Column(name = "usage_continuity_months")
    @Min(value = 0, message = "継続利用月数は0以上である必要があります")
    @Builder.Default
    private Integer usageContinuityMonths = 0;

    // UX改善関連
    @Column(name = "feedback_submitted_count")
    @Min(value = 0, message = "フィードバック提出数は0以上である必要があります")
    @Builder.Default
    private Integer feedbackSubmittedCount = 0;

    @Column(name = "feedback_accepted_count")
    @Min(value = 0, message = "フィードバック採用数は0以上である必要があります")
    @Builder.Default
    private Integer feedbackAcceptedCount = 0;

    @Column(name = "ui_proposals_count")
    @Min(value = 0, message = "UI提案数は0以上である必要があります")
    @Builder.Default
    private Integer uiProposalsCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 開発関連のスコアを計算
     *
     * @return 開発関連スコア
     */
    public BigDecimal calculateDevelopmentScore() {
        BigDecimal score = BigDecimal.ZERO;
        
        // コミット数: 0.05ポイント/コミット
        score = score.add(new BigDecimal(commitsCount).multiply(new BigDecimal("0.05")));
        
        // マージされたPR: 0.2ポイント/PR
        score = score.add(new BigDecimal(mergedPrsCount).multiply(new BigDecimal("0.2")));
        
        // クローズしたIssue: 0.1ポイント/Issue
        score = score.add(new BigDecimal(issuesClosedCount).multiply(new BigDecimal("0.1")));
        
        // コード行数: 追加 - 削除の差分で評価
        int netCodeLines = codeLinesAdded - codeLinesDeleted;
        if (netCodeLines > 0) {
            score = score.add(new BigDecimal(netCodeLines).multiply(new BigDecimal("0.001")));
        }
        
        return score;
    }

    /**
     * 流動性提供関連のスコアを計算
     *
     * @return 流動性提供関連スコア
     */
    public BigDecimal calculateLiquidityScore() {
        BigDecimal score = BigDecimal.ZERO;
        
        // LP提供量と期間を考慮
        if (lpAmount.compareTo(BigDecimal.ZERO) > 0 && lpDurationDays > 0) {
            BigDecimal lpScore = lpAmount.divide(new BigDecimal("1000"), 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(lpDurationDays))
                    .divide(new BigDecimal("30"), 8, RoundingMode.HALF_UP);
            score = score.add(lpScore);
        }
        
        // 取引量
        score = score.add(tradingVolume.multiply(new BigDecimal("0.0001")));
        
        return score;
    }

    /**
     * ガバナンス関連のスコアを計算
     *
     * @return ガバナンス関連スコア
     */
    public BigDecimal calculateGovernanceScore() {
        BigDecimal score = BigDecimal.ZERO;
        
        // 投票回数: 0.05ポイント/投票
        score = score.add(new BigDecimal(votesCastCount).multiply(new BigDecimal("0.05")));
        
        // 提案提出: 0.3ポイント/提案
        score = score.add(new BigDecimal(proposalsSubmittedCount).multiply(new BigDecimal("0.3")));
        
        // 議論貢献: 0.1ポイント/貢献
        score = score.add(new BigDecimal(discussionContributions).multiply(new BigDecimal("0.1")));
        
        return score;
    }

    /**
     * 教育・普及関連のスコアを計算
     *
     * @return 教育・普及関連スコア
     */
    public BigDecimal calculateEducationScore() {
        BigDecimal score = BigDecimal.ZERO;
        
        // 講座参加者数と評価を考慮
        if (courseAttendees > 0 && courseRating.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal courseScore = new BigDecimal(courseAttendees)
                    .multiply(courseRating)
                    .multiply(new BigDecimal("0.1"));
            score = score.add(courseScore);
        }
        
        // 完了率ボーナス
        if (completionRate.compareTo(new BigDecimal("80")) >= 0) {
            score = score.multiply(new BigDecimal("1.2"));
        }
        
        // コミュニティフォロワー数
        score = score.add(new BigDecimal(communityFollowers).multiply(new BigDecimal("0.01")));
        
        return score;
    }

    /**
     * UX改善関連のスコアを計算
     *
     * @return UX改善関連スコア
     */
    public BigDecimal calculateUxScore() {
        BigDecimal score = BigDecimal.ZERO;
        
        // フィードバック提出: 0.05ポイント/件
        score = score.add(new BigDecimal(feedbackSubmittedCount).multiply(new BigDecimal("0.05")));
        
        // フィードバック採用: 0.2ポイント/件
        score = score.add(new BigDecimal(feedbackAcceptedCount).multiply(new BigDecimal("0.2")));
        
        // UI提案: 0.1ポイント/件
        score = score.add(new BigDecimal(uiProposalsCount).multiply(new BigDecimal("0.1")));
        
        return score;
    }
}
