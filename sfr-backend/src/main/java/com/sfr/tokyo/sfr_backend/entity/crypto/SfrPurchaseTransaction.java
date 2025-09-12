package com.sfr.tokyo.sfr_backend.entity.crypto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFR購入取引エンティティ
 * ユーザーによるSFRポイントの円建て購入取引を管理
 * 
 * Phase 1 実装: 1SFR = 150円固定レートでの購入システム
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Entity
@Table(name = "sfr_purchase_transactions", indexes = {
    @Index(name = "idx_sfr_purchase_user_id", columnList = "user_id"),
    @Index(name = "idx_sfr_purchase_space_id", columnList = "space_id"),
    @Index(name = "idx_sfr_purchase_status", columnList = "status"),
    @Index(name = "idx_sfr_purchase_created", columnList = "created_at"),
    @Index(name = "idx_sfr_purchase_stripe", columnList = "stripe_payment_intent_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SfrPurchaseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ユーザーID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    @NotBlank(message = "ユーザーIDは必須です")
    private String userId;

    /**
     * スペースID
     */
    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    @Builder.Default
    private Long spaceId = 1L;

    /**
     * 購入SFR金額（18桁精度）
     */
    @Column(name = "sfr_amount", nullable = false, precision = 18, scale = 8)
    @NotNull(message = "SFR金額は必須です")
    @DecimalMin(value = "0.00000001", message = "SFR金額は0.00000001以上である必要があります")
    private BigDecimal sfrAmount;

    /**
     * 円建て購入金額（150円/SFR固定レート）
     */
    @Column(name = "yen_amount", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "円金額は必須です")
    @DecimalMin(value = "1.00", message = "円金額は1.00以上である必要があります")
    private BigDecimal yenAmount;

    /**
     * 適用レート（1SFR = 150円固定）
     */
    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal exchangeRate = new BigDecimal("150.00");

    /**
     * Stripe PaymentIntent ID
     */
    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;

    /**
     * Stripe決済ステータス
     */
    @Column(name = "stripe_payment_status", length = 50)
    private String stripePaymentStatus;

    /**
     * 取引ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private PurchaseTransactionStatus status = PurchaseTransactionStatus.PENDING;

    /**
     * 関連するSFRTトークン配布ID
     */
    @Column(name = "sfrt_distribution_id")
    private Long sfrtDistributionId;

    /**
     * SFRT配布済みフラグ
     */
    @Column(name = "sfrt_distributed", nullable = false)
    @Builder.Default
    private Boolean sfrtDistributed = false;

    /**
     * 取引メモ・備考
     */
    @Column(name = "memo", length = 500)
    @Size(max = 500, message = "メモは500文字以内で入力してください")
    private String memo;

    /**
     * エラーメッセージ（失敗時）
     */
    @Column(name = "error_message", length = 1000)
    @Size(max = 1000, message = "エラーメッセージは1000文字以内です")
    private String errorMessage;

    /**
     * 処理完了日時
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 購入取引ステータス列挙型
     */
    public enum PurchaseTransactionStatus {
        PENDING,     // 処理待ち
        PROCESSING,  // 処理中
        COMPLETED,   // 完了
        FAILED,      // 失敗
        CANCELLED,   // キャンセル
        REFUNDED     // 返金済み
    }

    /**
     * 取引を完了状態に更新
     */
    public void markCompleted() {
        this.status = PurchaseTransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 取引を失敗状態に更新
     */
    public void markFailed(String errorMessage) {
        this.status = PurchaseTransactionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 取引をキャンセル状態に更新
     */
    public void markCancelled() {
        this.status = PurchaseTransactionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * SFRT配布完了マーク
     */
    public void markSfrtDistributed(Long distributionId) {
        this.sfrtDistributed = true;
        this.sfrtDistributionId = distributionId;
    }

    /**
     * 取引が完了済みかチェック
     */
    public boolean isCompleted() {
        return status == PurchaseTransactionStatus.COMPLETED;
    }

    /**
     * 取引が失敗済みかチェック
     */
    public boolean isFailed() {
        return status == PurchaseTransactionStatus.FAILED;
    }

    /**
     * SFRT配布が必要かチェック
     */
    public boolean needsSfrtDistribution() {
        return isCompleted() && !sfrtDistributed;
    }

    /**
     * 期待されるSFRT配布額を計算（1.25%）
     */
    public BigDecimal getExpectedSfrtAmount() {
        return sfrAmount.multiply(new BigDecimal("0.0125"));
    }
}
