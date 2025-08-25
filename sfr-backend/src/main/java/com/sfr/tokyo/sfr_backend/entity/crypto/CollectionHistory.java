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
 * CollectionHistory Entity - SFRトークン回収履歴の管理
 * 閾値を超えたユーザーからのトークン回収を記録
 */
@Entity
@Table(name = "collection_history", indexes = {
        @Index(name = "idx_collection_history_space_id", columnList = "space_id"),
        @Index(name = "idx_collection_history_user_id", columnList = "user_id"),
        @Index(name = "idx_collection_history_collected_at", columnList = "collected_at"),
        @Index(name = "idx_collection_history_status", columnList = "status"),
        @Index(name = "idx_collection_history_trigger_type", columnList = "trigger_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    private Long spaceId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @Column(name = "collected_amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "回収量は必須です")
    @DecimalMin(value = "0.00000001", message = "回収量は0.00000001以上である必要があります")
    private BigDecimal collectedAmount;

    @Column(name = "balance_before", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "回収前残高は必須です")
    @DecimalMin(value = "0.0", message = "回収前残高は0以上である必要があります")
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "回収後残高は必須です")
    @DecimalMin(value = "0.0", message = "回収後残高は0以上である必要があります")
    private BigDecimal balanceAfter;

    @Column(name = "threshold_at_collection", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "回収時閾値は必須です")
    @DecimalMin(value = "0.0", message = "回収時閾値は0以上である必要があります")
    private BigDecimal thresholdAtCollection;

    @Column(name = "collection_rate", nullable = false, precision = 5, scale = 4)
    @NotNull(message = "回収率は必須です")
    @DecimalMin(value = "0.0001", message = "回収率は0.0001以上である必要があります")
    @DecimalMax(value = "1.0000", message = "回収率は1.0000以下である必要があります")
    private BigDecimal collectionRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    @NotNull(message = "トリガータイプは必須です")
    private CollectionTrigger triggerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_reason", nullable = false, length = 30)
    @NotNull(message = "回収理由は必須です")
    private CollectionReason collectionReason;

    @Column(name = "algorithm_version", nullable = false, length = 20)
    @NotBlank(message = "アルゴリズムバージョンは必須です")
    @Size(max = 20, message = "アルゴリズムバージョンは20文字以内で入力してください")
    @Builder.Default
    private String algorithmVersion = "v1.0";

    @Column(name = "calculation_details", columnDefinition = "TEXT")
    private String calculationDetails; // JSON形式での計算詳細

    @Column(name = "ai_confidence_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "AI信頼度は0以上である必要があります")
    @DecimalMax(value = "100.0", message = "AI信頼度は100以下である必要があります")
    private BigDecimal aiConfidenceScore;

    @Column(name = "user_activity_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "ユーザー活動スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "ユーザー活動スコアは100以下である必要があります")
    private BigDecimal userActivityScore;

    @Column(name = "holding_period_days", nullable = false)
    @Min(value = 0, message = "保有期間は0以上である必要があります")
    private Integer holdingPeriodDays;

    @Column(name = "consecutive_threshold_days", nullable = false)
    @Min(value = 0, message = "連続閾値超過日数は0以上である必要があります")
    private Integer consecutiveThresholdDays;

    @Column(name = "collected_at", nullable = false)
    @NotNull(message = "回収実行日時は必須です")
    private LocalDateTime collectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private CollectionStatus status = CollectionStatus.PENDING;

    @Column(name = "executed_by", nullable = true)
    private UUID executedBy; // 実行者（手動実行の場合）

    @Column(name = "transaction_hash", nullable = true, length = 100)
    @Size(max = 100, message = "トランザクションハッシュは100文字以内で入力してください")
    private String transactionHash;

    @Column(name = "grace_period_start", nullable = true)
    private LocalDateTime gracePeriodStart; // 猶予期間開始

    @Column(name = "grace_period_end", nullable = true)
    private LocalDateTime gracePeriodEnd; // 猶予期間終了

    @Column(name = "notification_sent_at", nullable = true)
    private LocalDateTime notificationSentAt; // 通知送信日時

    @Column(name = "user_response", length = 500)
    @Size(max = 500, message = "ユーザーレスポンスは500文字以内で入力してください")
    private String userResponse; // ユーザーからの応答

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes; // 管理者メモ

    @Column(name = "batch_id", nullable = true)
    private UUID batchId; // バッチ処理でのグループ化

    @Column(name = "is_appealed", nullable = false)
    @Builder.Default
    private Boolean isAppealed = false; // 異議申し立てされたか

    @Column(name = "appeal_reason", columnDefinition = "TEXT")
    private String appealReason; // 異議申し立て理由

    @Column(name = "appeal_result", length = 50)
    @Size(max = 50, message = "異議申し立て結果は50文字以内で入力してください")
    private String appealResult; // 異議申し立て結果

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 列挙型定義

    /**
     * 回収トリガー
     */
    public enum CollectionTrigger {
        AUTOMATIC_THRESHOLD, // 自動（閾値超過）
        SCHEDULED_COLLECTION, // スケジュール回収
        MANUAL_TRIGGER, // 手動トリガー
        AI_DECISION, // AI判定
        EMERGENCY_COLLECTION, // 緊急回収
        GOVERNANCE_DECISION // ガバナンス決定
    }

    /**
     * 回収理由
     */
    public enum CollectionReason {
        THRESHOLD_EXCEEDED, // 閾値超過
        INACTIVE_USER, // 非アクティブユーザー
        WHALE_PREVENTION, // ホエール防止
        REDISTRIBUTION, // 再分配
        SYSTEM_MAINTENANCE, // システムメンテナンス
        GOVERNANCE_MANDATE, // ガバナンス指令
        SECURITY_MEASURE, // セキュリティ対策
        POLICY_VIOLATION // ポリシー違反
    }

    /**
     * 回収ステータス
     */
    public enum CollectionStatus {
        PENDING, // 保留中
        GRACE_PERIOD, // 猶予期間中
        APPROVED, // 承認済み
        EXECUTING, // 実行中
        COMPLETED, // 完了
        FAILED, // 失敗
        CANCELLED, // キャンセル
        APPEALED, // 異議申し立て中
        APPEAL_APPROVED, // 異議申し立て承認
        APPEAL_REJECTED // 異議申し立て却下
    }

    // ビジネスロジックメソッド

    /**
     * 猶予期間を設定する
     * 
     * @param gracePeriodHours 猶予期間（時間）
     */
    public void setGracePeriod(int gracePeriodHours) {
        this.gracePeriodStart = LocalDateTime.now();
        this.gracePeriodEnd = this.gracePeriodStart.plusHours(gracePeriodHours);
        this.status = CollectionStatus.GRACE_PERIOD;
    }

    /**
     * 猶予期間が終了したかチェック
     * 
     * @return 猶予期間が終了したか
     */
    public boolean isGracePeriodEnded() {
        return gracePeriodEnd != null && LocalDateTime.now().isAfter(gracePeriodEnd);
    }

    /**
     * 回収を承認する
     * 
     * @param approver 承認者
     */
    public void approve(UUID approver) {
        if (status == CollectionStatus.PENDING ||
                (status == CollectionStatus.GRACE_PERIOD && isGracePeriodEnded())) {
            this.status = CollectionStatus.APPROVED;
            this.executedBy = approver;
        }
    }

    /**
     * 回収を実行中にマークする
     */
    public void markAsExecuting() {
        if (status == CollectionStatus.APPROVED) {
            this.status = CollectionStatus.EXECUTING;
        }
    }

    /**
     * 回収を完了にマークする
     * 
     * @param transactionHash トランザクションハッシュ
     */
    public void markAsCompleted(String transactionHash) {
        if (status == CollectionStatus.EXECUTING) {
            this.status = CollectionStatus.COMPLETED;
            this.transactionHash = transactionHash;
            this.collectedAt = LocalDateTime.now();
        }
    }

    /**
     * 回収を失敗にマークする
     * 
     * @param reason 失敗理由
     */
    public void markAsFailed(String reason) {
        if (status == CollectionStatus.EXECUTING) {
            this.status = CollectionStatus.FAILED;
            this.adminNotes = this.adminNotes != null ? this.adminNotes + "\n失敗理由: " + reason : "失敗理由: " + reason;
        }
    }

    /**
     * 異議申し立てを行う
     * 
     * @param reason 異議申し立て理由
     */
    public void submitAppeal(String reason) {
        if (status == CollectionStatus.COMPLETED || status == CollectionStatus.APPROVED) {
            this.isAppealed = true;
            this.appealReason = reason;
            this.status = CollectionStatus.APPEALED;
        }
    }

    /**
     * 異議申し立てを承認する
     * 
     * @param approver 承認者
     * @param result   結果詳細
     */
    public void approveAppeal(UUID approver, String result) {
        if (status == CollectionStatus.APPEALED) {
            this.status = CollectionStatus.APPEAL_APPROVED;
            this.appealResult = result;
            this.executedBy = approver;
        }
    }

    /**
     * 異議申し立てを却下する
     * 
     * @param rejector 却下者
     * @param result   結果詳細
     */
    public void rejectAppeal(UUID rejector, String result) {
        if (status == CollectionStatus.APPEALED) {
            this.status = CollectionStatus.APPEAL_REJECTED;
            this.appealResult = result;
            this.executedBy = rejector;
        }
    }

    /**
     * 通知送信をマークする
     */
    public void markNotificationSent() {
        this.notificationSentAt = LocalDateTime.now();
    }

    /**
     * 回収効率を計算する（回収量/回収前残高）
     * 
     * @return 回収効率
     */
    public BigDecimal getCollectionEfficiency() {
        if (balanceBefore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return collectedAmount.divide(balanceBefore, 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 回収の妥当性をチェックする
     * 
     * @return 妥当かどうか
     */
    public boolean isCollectionValid() {
        // 基本的な整合性チェック
        return balanceBefore.subtract(collectedAmount).compareTo(balanceAfter) == 0 &&
                balanceBefore.compareTo(thresholdAtCollection) > 0 &&
                collectedAmount.compareTo(BigDecimal.ZERO) > 0 &&
                holdingPeriodDays >= 0 &&
                consecutiveThresholdDays >= 0;
    }

    /**
     * 自動回収ファクトリメソッド
     */
    public static CollectionHistory createAutomaticCollection(
            Long spaceId,
            UUID userId,
            BigDecimal collectedAmount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            BigDecimal threshold,
            BigDecimal collectionRate,
            int holdingDays,
            int consecutiveDays) {
        return CollectionHistory.builder()
                .spaceId(spaceId)
                .userId(userId)
                .collectedAmount(collectedAmount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .thresholdAtCollection(threshold)
                .collectionRate(collectionRate)
                .triggerType(CollectionTrigger.AUTOMATIC_THRESHOLD)
                .collectionReason(CollectionReason.THRESHOLD_EXCEEDED)
                .holdingPeriodDays(holdingDays)
                .consecutiveThresholdDays(consecutiveDays)
                .collectedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 手動回収ファクトリメソッド
     */
    public static CollectionHistory createManualCollection(
            Long spaceId,
            UUID userId,
            UUID executor,
            BigDecimal collectedAmount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            CollectionReason reason,
            String notes) {
        return CollectionHistory.builder()
                .spaceId(spaceId)
                .userId(userId)
                .executedBy(executor)
                .collectedAmount(collectedAmount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .thresholdAtCollection(BigDecimal.ZERO)
                .collectionRate(collectedAmount.divide(balanceBefore, 4, java.math.RoundingMode.HALF_UP))
                .triggerType(CollectionTrigger.MANUAL_TRIGGER)
                .collectionReason(reason)
                .holdingPeriodDays(0)
                .consecutiveThresholdDays(0)
                .collectedAt(LocalDateTime.now())
                .adminNotes(notes)
                .build();
    }
}
