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
 * RewardDistribution Entity - リワード配布の管理
 * SFRトークンのリワード配布を記録・管理
 */
@Entity
@Table(name = "reward_distributions", indexes = {
        @Index(name = "idx_reward_distributions_space_id", columnList = "space_id"),
        @Index(name = "idx_reward_distributions_user_id", columnList = "user_id"),
        @Index(name = "idx_reward_distributions_distribution_date", columnList = "distribution_date"),
        @Index(name = "idx_reward_distributions_category", columnList = "category"),
        @Index(name = "idx_reward_distributions_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    private Long spaceId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "配布量は必須です")
    @DecimalMin(value = "0.00000001", message = "配布量は0.00000001以上である必要があります")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    @NotNull(message = "カテゴリは必須です")
    private RewardCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    @NotNull(message = "トリガータイプは必須です")
    private TriggerType triggerType;

    @Column(name = "reference_id", nullable = true)
    private String referenceId; // 関連する投稿、コメント、アクションのID

    @Column(name = "reason", nullable = false, length = 500)
    @NotBlank(message = "理由は必須です")
    @Size(max = 500, message = "理由は500文字以内で入力してください")
    private String reason;

    @Column(name = "calculation_details", columnDefinition = "TEXT")
    private String calculationDetails; // JSON形式での計算詳細

    @Column(name = "distribution_date", nullable = false)
    @NotNull(message = "配布日は必須です")
    private LocalDateTime distributionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private DistributionStatus status = DistributionStatus.PENDING;

    @Column(name = "processed_at", nullable = true)
    private LocalDateTime processedAt;

    @Column(name = "processed_by", nullable = true)
    private UUID processedBy;

    @Column(name = "transaction_hash", nullable = true, length = 100)
    @Size(max = 100, message = "トランザクションハッシュは100文字以内で入力してください")
    private String transactionHash;

    @Column(name = "batch_id", nullable = true)
    private UUID batchId; // バッチ処理でのグループ化

    @Column(name = "quality_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "品質スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "品質スコアは100以下である必要があります")
    private BigDecimal qualityScore;

    @Column(name = "engagement_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "エンゲージメントスコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "エンゲージメントスコアは100以下である必要があります")
    private BigDecimal engagementScore;

    @Column(name = "multiplier", precision = 5, scale = 2)
    @DecimalMin(value = "0.1", message = "マルチプライヤーは0.1以上である必要があります")
    @DecimalMax(value = "10.0", message = "マルチプライヤーは10.0以下である必要があります")
    @Builder.Default
    private BigDecimal multiplier = BigDecimal.ONE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "expires_at", nullable = true)
    private LocalDateTime expiresAt; // リワードの有効期限（該当する場合）

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 列挙型定義

    /**
     * リワードカテゴリ
     */
    public enum RewardCategory {
        CONTENT_CREATION, // コンテンツ作成
        CONTENT_CURATION, // コンテンツキュレーション
        COMMUNITY_ENGAGEMENT, // コミュニティエンゲージメント
        LEARNING_PROGRESS, // 学習進捗
        SKILL_DEMONSTRATION, // スキル実証
        KNOWLEDGE_SHARING, // 知識共有
        MENTORING, // メンタリング
        GOVERNANCE, // ガバナンス参加
        REFERRAL, // 紹介
        ACHIEVEMENT, // 達成
        SPECIAL_EVENT, // 特別イベント
        BONUS, // ボーナス
        SYSTEM_REWARD // システムリワード
    }

    /**
     * トリガータイプ
     */
    public enum TriggerType {
    AUTOMATIC, // 自動
    MANUAL, // 手動
    AI_DECISION, // AI判定
    COMMUNITY_VOTE, // コミュニティ投票
    ADMIN_APPROVAL, // 管理者承認
    SCHEDULED, // スケジュール
    EVENT_BASED, // イベントベース
    SHOP_PURCHASE // ショップ購入
    }

    /**
     * 配布ステータス
     */
    public enum DistributionStatus {
        PENDING, // 保留中
        APPROVED, // 承認済み
        PROCESSING, // 処理中
        COMPLETED, // 完了
        FAILED, // 失敗
        CANCELLED, // キャンセル
        EXPIRED // 期限切れ
    }

    // ビジネスロジックメソッド

    /**
     * リワードを承認する
     * 
     * @param approver 承認者
     * @return 承認が成功したかどうか
     */
    public boolean approve(UUID approver) {
        if (status != DistributionStatus.PENDING) {
            return false;
        }

        this.status = DistributionStatus.APPROVED;
        this.processedBy = approver;
        this.processedAt = LocalDateTime.now();
        return true;
    }

    /**
     * リワードを処理中にマークする
     */
    public void markAsProcessing() {
        if (status == DistributionStatus.APPROVED) {
            this.status = DistributionStatus.PROCESSING;
        }
    }

    /**
     * リワードを完了にマークする
     * 
     * @param transactionHash トランザクションハッシュ
     */
    public void markAsCompleted(String transactionHash) {
        if (status == DistributionStatus.PROCESSING) {
            this.status = DistributionStatus.COMPLETED;
            this.transactionHash = transactionHash;
            this.processedAt = LocalDateTime.now();
        }
    }

    /**
     * リワードを失敗にマークする
     * 
     * @param reason 失敗理由
     */
    public void markAsFailed(String reason) {
        if (status == DistributionStatus.PROCESSING) {
            this.status = DistributionStatus.FAILED;
            this.notes = this.notes != null ? this.notes + "\n失敗理由: " + reason : "失敗理由: " + reason;
        }
    }

    /**
     * リワードをキャンセルする
     * 
     * @param canceller キャンセルした人
     * @param reason    キャンセル理由
     */
    public void cancel(UUID canceller, String reason) {
        if (status == DistributionStatus.PENDING || status == DistributionStatus.APPROVED) {
            this.status = DistributionStatus.CANCELLED;
            this.processedBy = canceller;
            this.processedAt = LocalDateTime.now();
            this.notes = this.notes != null ? this.notes + "\nキャンセル理由: " + reason : "キャンセル理由: " + reason;
        }
    }

    /**
     * 有効期限をチェックする
     * 
     * @return 期限切れかどうか
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 期限切れにマークする
     */
    public void markAsExpired() {
        if (isExpired() && (status == DistributionStatus.PENDING || status == DistributionStatus.APPROVED)) {
            this.status = DistributionStatus.EXPIRED;
        }
    }

    /**
     * 最終的な配布金額を計算する（マルチプライヤー適用後）
     * 
     * @return 最終配布金額
     */
    public BigDecimal getFinalAmount() {
        return amount.multiply(multiplier);
    }

    /**
     * リワードが処理可能な状態かチェックする
     * 
     * @return 処理可能かどうか
     */
    public boolean isProcessable() {
        return status == DistributionStatus.APPROVED &&
                !isExpired() &&
                amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * コンテンツ関連のリワードファクトリメソッド
     */
    public static RewardDistribution createContentReward(
            Long spaceId,
            UUID userId,
            BigDecimal amount,
            String contentId,
            String reason) {
        return RewardDistribution.builder()
                .spaceId(spaceId)
                .userId(userId)
                .amount(amount)
                .category(RewardCategory.CONTENT_CREATION)
                .triggerType(TriggerType.AI_DECISION)
                .referenceId(contentId)
                .reason(reason)
                .distributionDate(LocalDateTime.now())
                .qualityScore(new BigDecimal("75.0"))
                .build();
    }

    /**
     * 学習進捗リワードファクトリメソッド
     */
    public static RewardDistribution createLearningReward(
            Long spaceId,
            UUID userId,
            BigDecimal amount,
            String progressId,
            BigDecimal progressScore) {
        return RewardDistribution.builder()
                .spaceId(spaceId)
                .userId(userId)
                .amount(amount)
                .category(RewardCategory.LEARNING_PROGRESS)
                .triggerType(TriggerType.AUTOMATIC)
                .referenceId(progressId)
                .reason("学習進捗に基づくリワード")
                .distributionDate(LocalDateTime.now())
                .qualityScore(progressScore)
                .build();
    }

    /**
     * ガバナンス参加リワードファクトリメソッド
     */
    public static RewardDistribution createGovernanceReward(
            Long spaceId,
            UUID userId,
            BigDecimal amount,
            String proposalId) {
        return RewardDistribution.builder()
                .spaceId(spaceId)
                .userId(userId)
                .amount(amount)
                .category(RewardCategory.GOVERNANCE)
                .triggerType(TriggerType.AUTOMATIC)
                .referenceId(proposalId)
                .reason("ガバナンス提案への参加")
                .distributionDate(LocalDateTime.now())
                .engagementScore(new BigDecimal("80.0"))
                .build();
    }
}
