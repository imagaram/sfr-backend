package com.sfr.tokyo.sfr_backend.entity.crypto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFRTトークン管理エンティティ
 * 投資対象となるSFR Tokyo Token（SFRT）の管理
 * 
 * Phase 1 実装: デュアルトークンシステムの投資トークン部分
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Entity
@Table(name = "sfrt_tokens", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "space_id"})
}, indexes = {
    @Index(name = "idx_sfrt_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_sfrt_tokens_space_id", columnList = "space_id"),
    @Index(name = "idx_sfrt_tokens_balance", columnList = "current_balance"),
    @Index(name = "idx_sfrt_tokens_updated", columnList = "updated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SfrtToken {

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
     * スペースID（デフォルト: 1）
     */
    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    @Builder.Default
    private Long spaceId = 1L;

    /**
     * 現在のSFRTトークン残高（18桁精度）
     * SFR取引量の1.25%が報酬として付与
     */
    @Column(name = "current_balance", nullable = false, precision = 18, scale = 8)
    @NotNull(message = "残高は必須です")
    @DecimalMin(value = "0.0", message = "残高は0以上である必要があります")
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    /**
     * 累計獲得SFRT（購入報酬）
     */
    @Column(name = "total_earned_purchase", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "累計購入報酬は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalEarnedPurchase = BigDecimal.ZERO;

    /**
     * 累計獲得SFRT（販売報酬）
     */
    @Column(name = "total_earned_sales", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "累計販売報酬は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalEarnedSales = BigDecimal.ZERO;

    /**
     * 累計換金SFRT
     */
    @Column(name = "total_redeemed", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "累計換金額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalRedeemed = BigDecimal.ZERO;

    /**
     * 累計転送SFRT（送信）
     */
    @Column(name = "total_transferred_out", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "累計送信額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalTransferredOut = BigDecimal.ZERO;

    /**
     * 累計転送SFRT（受信）
     */
    @Column(name = "total_transferred_in", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "累計受信額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalTransferredIn = BigDecimal.ZERO;

    /**
     * 最後の報酬配布日時
     */
    @Column(name = "last_reward_distribution")
    private LocalDateTime lastRewardDistribution;

    /**
     * 最後の配布トランザクションID
     */
    @Column(name = "last_distribution_transaction_id")
    private Long lastDistributionTransactionId;

    /**
     * ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private SfrtTokenStatus status = SfrtTokenStatus.ACTIVE;

    /**
     * 外部取引所連携フラグ
     */
    @Column(name = "external_exchange_enabled", nullable = false)
    @Builder.Default
    private Boolean externalExchangeEnabled = false;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * SFRTトークンステータス列挙型
     */
    public enum SfrtTokenStatus {
        ACTIVE,        // アクティブ
        FROZEN,        // 凍結
        SUSPENDED,     // 停止
        EXCHANGE_ONLY  // 外部取引所のみ
    }

    /**
     * SFRTトークンの150円換算価値
     * 現在は1SFRT = 150円で算出（将来的に変動レート対応）
     */
    public BigDecimal getValueInYen() {
        return currentBalance.multiply(new BigDecimal("150"));
    }

    /**
     * 指定金額のSFRTトークンを加算
     */
    public void addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.currentBalance = this.currentBalance.add(amount);
        }
    }

    /**
     * 指定金額のSFRTトークンを減算
     * @param amount 減算額
     * @return 減算成功可否
     */
    public boolean subtractBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0 && 
            this.currentBalance.compareTo(amount) >= 0) {
            this.currentBalance = this.currentBalance.subtract(amount);
            return true;
        }
        return false;
    }

    /**
     * 購入報酬配布処理
     * @param sfrPurchaseAmount SFR購入金額
     * @return 配布されたSFRT額（購入額の1.25%）
     */
    public BigDecimal distributePurchaseReward(BigDecimal sfrPurchaseAmount) {
        BigDecimal rewardAmount = sfrPurchaseAmount.multiply(new BigDecimal("0.0125"));
        addBalance(rewardAmount);
        this.totalEarnedPurchase = this.totalEarnedPurchase.add(rewardAmount);
        this.lastRewardDistribution = LocalDateTime.now();
        return rewardAmount;
    }

    /**
     * 販売報酬配布処理
     * @param sfrSalesAmount SFR販売金額（買い手の支払い額）
     * @return 配布されたSFRT額（販売額の1.25%）
     */
    public BigDecimal distributeSalesReward(BigDecimal sfrSalesAmount) {
        BigDecimal rewardAmount = sfrSalesAmount.multiply(new BigDecimal("0.0125"));
        addBalance(rewardAmount);
        this.totalEarnedSales = this.totalEarnedSales.add(rewardAmount);
        this.lastRewardDistribution = LocalDateTime.now();
        return rewardAmount;
    }

    /**
     * SFRT換金処理
     */
    public boolean redeemSfrt(BigDecimal amount) {
        if (subtractBalance(amount)) {
            this.totalRedeemed = this.totalRedeemed.add(amount);
            return true;
        }
        return false;
    }

    /**
     * SFRT転送処理（送信）
     */
    public boolean transferOut(BigDecimal amount) {
        if (subtractBalance(amount)) {
            this.totalTransferredOut = this.totalTransferredOut.add(amount);
            return true;
        }
        return false;
    }

    /**
     * SFRT転送処理（受信）
     */
    public void transferIn(BigDecimal amount) {
        addBalance(amount);
        this.totalTransferredIn = this.totalTransferredIn.add(amount);
    }

    /**
     * 累計獲得SFRT（全種類合計）
     */
    public BigDecimal getTotalEarned() {
        return totalEarnedPurchase.add(totalEarnedSales);
    }

    /**
     * 正味転送額（受信 - 送信）
     */
    public BigDecimal getNetTransferred() {
        return totalTransferredIn.subtract(totalTransferredOut);
    }
}
