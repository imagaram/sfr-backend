package com.sfr.tokyo.sfr_backend.entity.crypto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFR決済取引エンティティ
 * サイト内でのSFRポイント決済（商品購入・サービス利用）を管理
 * 
 * Phase 1 実装: SFRポイントによる決済システム
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Entity
@Table(name = "sfr_payment_transactions", indexes = {
    @Index(name = "idx_sfr_payment_buyer", columnList = "buyer_user_id"),
    @Index(name = "idx_sfr_payment_seller", columnList = "seller_user_id"),
    @Index(name = "idx_sfr_payment_space", columnList = "space_id"),
    @Index(name = "idx_sfr_payment_status", columnList = "status"),
    @Index(name = "idx_sfr_payment_created", columnList = "created_at"),
    @Index(name = "idx_sfr_payment_type", columnList = "payment_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SfrPaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 買い手ユーザーID
     */
    @Column(name = "buyer_user_id", nullable = false, length = 36)
    @NotBlank(message = "買い手ユーザーIDは必須です")
    private String buyerUserId;

    /**
     * 売り手ユーザーID（プラットフォーム決済の場合はnull）
     */
    @Column(name = "seller_user_id", length = 36)
    private String sellerUserId;

    /**
     * スペースID
     */
    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    @Builder.Default
    private Long spaceId = 1L;

    /**
     * 決済SFR金額（18桁精度）
     */
    @Column(name = "sfr_amount", nullable = false, precision = 18, scale = 8)
    @NotNull(message = "SFR金額は必須です")
    @DecimalMin(value = "0.00000001", message = "SFR金額は0.00000001以上である必要があります")
    private BigDecimal sfrAmount;

    /**
     * プラットフォーム手数料（5%）
     */
    @Column(name = "platform_fee", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "プラットフォーム手数料は0以上である必要があります")
    @Builder.Default
    private BigDecimal platformFee = BigDecimal.ZERO;

    /**
     * 売り手受取額（手数料差引後）
     */
    @Column(name = "seller_amount", precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "売り手受取額は0以上である必要があります")
    private BigDecimal sellerAmount;

    /**
     * 決済タイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    @NotNull(message = "決済タイプは必須です")
    private PaymentType paymentType;

    /**
     * 決済対象商品・サービスID
     */
    @Column(name = "product_id", length = 100)
    private String productId;

    /**
     * 決済対象説明
     */
    @Column(name = "product_description", length = 500)
    @Size(max = 500, message = "商品説明は500文字以内で入力してください")
    private String productDescription;

    /**
     * 取引ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private PaymentTransactionStatus status = PaymentTransactionStatus.PENDING;

    /**
     * 関連するSFRTトークン配布ID（買い手）
     */
    @Column(name = "buyer_sfrt_distribution_id")
    private Long buyerSfrtDistributionId;

    /**
     * 関連するSFRTトークン配布ID（売り手）
     */
    @Column(name = "seller_sfrt_distribution_id")
    private Long sellerSfrtDistributionId;

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
     * 決済タイプ列挙型
     */
    public enum PaymentType {
        PRODUCT_PURCHASE,    // 商品購入
        SERVICE_PAYMENT,     // サービス利用料
        SUBSCRIPTION,        // サブスクリプション
        DONATION,           // 寄付・投げ銭
        PLATFORM_FEE,       // プラットフォーム手数料
        OTHER              // その他
    }

    /**
     * 決済取引ステータス列挙型
     */
    public enum PaymentTransactionStatus {
        PENDING,     // 処理待ち
        PROCESSING,  // 処理中
        COMPLETED,   // 完了
        FAILED,      // 失敗
        CANCELLED,   // キャンセル
        REFUNDED     // 返金済み
    }

    /**
     * プラットフォーム手数料を計算して設定（5%）
     */
    public void calculatePlatformFee() {
        this.platformFee = sfrAmount.multiply(new BigDecimal("0.05"));
        if (sellerUserId != null) {
            this.sellerAmount = sfrAmount.subtract(platformFee);
        }
    }

    /**
     * 取引を完了状態に更新
     */
    public void markCompleted() {
        this.status = PaymentTransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 取引を失敗状態に更新
     */
    public void markFailed(String errorMessage) {
        this.status = PaymentTransactionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 取引をキャンセル状態に更新
     */
    public void markCancelled() {
        this.status = PaymentTransactionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * SFRT配布完了マーク
     */
    public void markSfrtDistributed(Long buyerDistributionId, Long sellerDistributionId) {
        this.sfrtDistributed = true;
        this.buyerSfrtDistributionId = buyerDistributionId;
        if (sellerDistributionId != null) {
            this.sellerSfrtDistributionId = sellerDistributionId;
        }
    }

    /**
     * 取引が完了済みかチェック
     */
    public boolean isCompleted() {
        return status == PaymentTransactionStatus.COMPLETED;
    }

    /**
     * 取引が失敗済みかチェック
     */
    public boolean isFailed() {
        return status == PaymentTransactionStatus.FAILED;
    }

    /**
     * SFRT配布が必要かチェック
     */
    public boolean needsSfrtDistribution() {
        return isCompleted() && !sfrtDistributed;
    }

    /**
     * 期待される買い手のSFRT配布額を計算（1.25%）
     */
    public BigDecimal getExpectedBuyerSfrtAmount() {
        return sfrAmount.multiply(new BigDecimal("0.0125"));
    }

    /**
     * 期待される売り手のSFRT配布額を計算（1.25%）
     */
    public BigDecimal getExpectedSellerSfrtAmount() {
        if (sellerUserId == null) return BigDecimal.ZERO;
        return sfrAmount.multiply(new BigDecimal("0.0125"));
    }

    /**
     * プラットフォームへのSFRT配布額を計算（2.5%）
     */
    public BigDecimal getPlatformSfrtAmount() {
        return sfrAmount.multiply(new BigDecimal("0.025"));
    }

    /**
     * P2P取引かどうかチェック
     */
    public boolean isP2PTransaction() {
        return sellerUserId != null && !sellerUserId.trim().isEmpty();
    }
}
