package com.serendipity.tokyo.sfrbackend.entity.sfrt;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFRT（暗号資産）残高管理エンティティ
 * - ユーザーのSFRT保有残高
 * - 獲得・出金履歴
 * - 投資・流動性提供可能
 */
@Entity
@Table(name = "sfrt_tokens", 
       indexes = {
           @Index(name = "idx_sfrt_tokens_user_id", columnList = "user_id"),
           @Index(name = "idx_sfrt_tokens_space_id", columnList = "space_id"),
           @Index(name = "idx_sfrt_tokens_balance", columnList = "current_balance"),
           @Index(name = "idx_sfrt_tokens_updated", columnList = "updated_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "space_id"})
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SfrtBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ユーザーID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /**
     * スペースID（マルチテナント対応）
     */
    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    /**
     * 現在のSFRT残高（18桁精度）
     */
    @Column(name = "current_balance", precision = 18, scale = 8, nullable = false)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    /**
     * 累計獲得SFRT（購入報酬）
     */
    @Column(name = "total_earned_purchase", precision = 18, scale = 8, nullable = false)
    @Builder.Default
    private BigDecimal totalEarnedPurchase = BigDecimal.ZERO;

    /**
     * 累計獲得SFRT（販売報酬）
     */
    @Column(name = "total_earned_sales", precision = 18, scale = 8, nullable = false)
    @Builder.Default
    private BigDecimal totalEarnedSales = BigDecimal.ZERO;

    /**
     * 累計換金済SFRT
     */
    @Column(name = "total_redeemed", precision = 18, scale = 8, nullable = false)
    @Builder.Default
    private BigDecimal totalRedeemed = BigDecimal.ZERO;

    /**
     * 累計転送受信SFRT
     */
    @Column(name = "total_transferred_in", precision = 18, scale = 8, nullable = false)
    @Builder.Default
    private BigDecimal totalTransferredIn = BigDecimal.ZERO;

    /**
     * 累計転送送信SFRT
     */
    @Column(name = "total_transferred_out", precision = 18, scale = 8, nullable = false)
    @Builder.Default
    private BigDecimal totalTransferredOut = BigDecimal.ZERO;

    /**
     * 外部取引所交換許可
     */
    @Column(name = "external_exchange_enabled", nullable = false)
    @Builder.Default
    private Boolean externalExchangeEnabled = true;

    /**
     * 最後の報酬配布日時
     */
    @Column(name = "last_reward_distribution")
    private LocalDateTime lastRewardDistribution;

    /**
     * 最後の配布取引ID
     */
    @Column(name = "last_distribution_transaction_id")
    private Long lastDistributionTransactionId;

    /**
     * SFRT状態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SfrtStatus status = SfrtStatus.ACTIVE;

    /**
     * 作成日時
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * SFRT状態列挙型
     */
    public enum SfrtStatus {
        ACTIVE,        // 通常利用可能
        FROZEN,        // 一時停止
        EXCHANGE_ONLY, // 交換のみ許可
        SUSPENDED      // 利用停止
    }

    /**
     * SFRT残高を増加
     */
    public void increaseBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.currentBalance = this.currentBalance.add(amount);
        }
    }

    /**
     * SFRT残高を減少
     */
    public void decreaseBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0 && 
            this.currentBalance.compareTo(amount) >= 0) {
            this.currentBalance = this.currentBalance.subtract(amount);
        }
    }

    /**
     * 残高不足チェック
     */
    public boolean hasInsufficientBalance(BigDecimal requiredAmount) {
        return this.currentBalance.compareTo(requiredAmount) < 0;
    }

    /**
     * アクティブ状態チェック
     */
    public boolean isActive() {
        return this.status == SfrtStatus.ACTIVE;
    }

    /**
     * 交換可能状態チェック
     */
    public boolean canExchange() {
        return (this.status == SfrtStatus.ACTIVE || this.status == SfrtStatus.EXCHANGE_ONLY) &&
               this.externalExchangeEnabled;
    }
}
