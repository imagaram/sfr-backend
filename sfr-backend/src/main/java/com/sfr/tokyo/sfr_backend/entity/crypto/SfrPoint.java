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
 * SFRポイント管理エンティティ
 * 1SFR = 150円固定レートのサイト内ポイントシステム
 * 
 * Phase 1 実装: デュアルトークンシステムの基盤
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Entity
@Table(name = "sfr_points", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "space_id"})
}, indexes = {
    @Index(name = "idx_sfr_points_user_id", columnList = "user_id"),
    @Index(name = "idx_sfr_points_space_id", columnList = "space_id"),
    @Index(name = "idx_sfr_points_balance", columnList = "current_balance"),
    @Index(name = "idx_sfr_points_updated", columnList = "updated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SfrPoint {

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
     * 現在のSFRポイント残高（18桁精度）
     * 150円 = 1.00000000 SFR
     */
    @Column(name = "current_balance", nullable = false, precision = 18, scale = 8)
    @NotNull(message = "残高は必須です")
    @DecimalMin(value = "0.0", message = "残高は0以上である必要があります")
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    /**
     * 累計購入SFRポイント
     */
    @Column(name = "total_purchased", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "累計購入額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalPurchased = BigDecimal.ZERO;

    /**
     * 累計使用SFRポイント
     */
    @Column(name = "total_spent", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "累計使用額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    /**
     * 累計獲得SFRポイント（報酬等）
     */
    @Column(name = "total_earned", nullable = false, precision = 18, scale = 8)
    @DecimalMin(value = "0.0", message = "累計獲得額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalEarned = BigDecimal.ZERO;

    /**
     * 最後のSFRT配布日時
     */
    @Column(name = "last_sfrt_distribution")
    private LocalDateTime lastSfrtDistribution;

    /**
     * SFRTポイント算出用フラグ
     * true: SFRTの対象となる取引あり
     */
    @Column(name = "sfrt_eligible", nullable = false)
    @Builder.Default
    private Boolean sfrtEligible = false;

    /**
     * ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private SfrPointStatus status = SfrPointStatus.ACTIVE;

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
     * SFRポイントステータス列挙型
     */
    public enum SfrPointStatus {
        ACTIVE,    // アクティブ
        FROZEN,    // 凍結
        SUSPENDED  // 停止
    }

    /**
     * 残高を150円レートで円換算
     * @return 残高の円換算額
     */
    public BigDecimal getBalanceInYen() {
        return currentBalance.multiply(new BigDecimal("150"));
    }

    /**
     * 指定金額のSFRポイントを加算
     */
    public void addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.currentBalance = this.currentBalance.add(amount);
        }
    }

    /**
     * 指定金額のSFRポイントを減算
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
     * SFRポイント購入処理
     */
    public void recordPurchase(BigDecimal amount) {
        addBalance(amount);
        this.totalPurchased = this.totalPurchased.add(amount);
        this.sfrtEligible = true; // 購入によりSFRT対象となる
    }

    /**
     * SFRポイント使用処理
     */
    public boolean recordSpending(BigDecimal amount) {
        if (subtractBalance(amount)) {
            this.totalSpent = this.totalSpent.add(amount);
            this.sfrtEligible = true; // 使用によりSFRT対象となる
            return true;
        }
        return false;
    }

    /**
     * SFRポイント獲得処理（報酬等）
     */
    public void recordEarning(BigDecimal amount) {
        addBalance(amount);
        this.totalEarned = this.totalEarned.add(amount);
        this.sfrtEligible = true; // 獲得によりSFRT対象となる
    }
}
