package com.sfr.tokyo.sfr_backend.entity.crypto.reward;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 保有インセンティブエンティティ
 * H係数（保有インセンティブ係数）を計算するための保有履歴
 */
@Entity
@Table(name = "holding_incentives", indexes = {
        @Index(name = "idx_holding_user_calculation", columnList = "user_id, calculation_date"),
        @Index(name = "idx_holding_days", columnList = "holding_days"),
        @Index(name = "idx_holding_staking", columnList = "staking_months, staking_amount")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingIncentive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @Column(name = "holding_start_date", nullable = false)
    @NotNull(message = "保有開始日は必須です")
    private LocalDateTime holdingStartDate;

    @Column(name = "holding_days", nullable = false)
    @NotNull(message = "保有日数は必須です")
    @Min(value = 0, message = "保有日数は0以上である必要があります")
    @Builder.Default
    private Integer holdingDays = 0;

    @Column(name = "average_holding_price", precision = 15, scale = 8)
    @DecimalMin(value = "0.00000001", message = "平均保有時価格は0.00000001以上である必要があります")
    private BigDecimal averageHoldingPrice;

    @Column(name = "current_price", precision = 15, scale = 8)
    @DecimalMin(value = "0.00000001", message = "現在価格は0.00000001以上である必要があります")
    private BigDecimal currentPrice;

    @Column(name = "price_support_ratio", precision = 10, scale = 6)
    @DecimalMin(value = "0.000001", message = "価格支持力比率は0.000001以上である必要があります")
    private BigDecimal priceSupportRatio;

    @Column(name = "staking_months")
    @Min(value = 0, message = "ステーキング期間は0以上である必要があります")
    @Builder.Default
    private Integer stakingMonths = 0;

    @Column(name = "staking_amount", precision = 20, scale = 8)
    @DecimalMin(value = "0", message = "ステーキング量は0以上である必要があります")
    @Builder.Default
    private BigDecimal stakingAmount = BigDecimal.ZERO;

    @Column(name = "payment_usage_count")
    @Min(value = 0, message = "SFR決済利用回数は0以上である必要があります")
    @Builder.Default
    private Integer paymentUsageCount = 0;

    @Column(name = "payment_usage_amount", precision = 20, scale = 8)
    @DecimalMin(value = "0", message = "SFR決済利用総額は0以上である必要があります")
    @Builder.Default
    private BigDecimal paymentUsageAmount = BigDecimal.ZERO;

    @Column(name = "holding_factor", nullable = false, precision = 10, scale = 6)
    @NotNull(message = "保有インセンティブ係数は必須です")
    @DecimalMin(value = "0.000001", message = "保有インセンティブ係数は0.000001以上である必要があります")
    @DecimalMax(value = "9999.999999", message = "保有インセンティブ係数は9999.999999以下である必要があります")
    private BigDecimal holdingFactor;

    @Column(name = "calculation_date", nullable = false)
    @NotNull(message = "係数計算日時は必須です")
    private LocalDateTime calculationDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * H係数の内訳を計算して返す
     *
     * @return H係数の内訳情報
     */
    public HoldingFactorBreakdown calculateBreakdown() {
        // 基本保有係数: 1.0 + α × log₁₀(保有日数)
        BigDecimal alpha = new BigDecimal("0.05");
        BigDecimal baseHoldingFactor = BigDecimal.ONE;
        if (holdingDays > 0) {
            double logHoldingDays = Math.log10(holdingDays);
            baseHoldingFactor = baseHoldingFactor.add(alpha.multiply(new BigDecimal(logHoldingDays)));
        }

        // ステーキングボーナス
        BigDecimal stakingBonus = BigDecimal.ZERO;
        if (stakingMonths >= 3) {
            stakingBonus = new BigDecimal("0.1");
        }
        if (stakingMonths >= 6) {
            stakingBonus = new BigDecimal("0.3");
        }

        // 決済利用ボーナス
        BigDecimal paymentBonus = BigDecimal.ZERO;
        if (paymentUsageCount > 0) {
            paymentBonus = new BigDecimal("0.1");
        }

        // 価格支持ボーナス
        BigDecimal priceSupportBonus = BigDecimal.ZERO;
        if (priceSupportRatio != null) {
            BigDecimal beta = new BigDecimal("0.2");
            priceSupportBonus = beta.multiply(priceSupportRatio);
        }

        return HoldingFactorBreakdown.builder()
                .baseHolding(baseHoldingFactor)
                .stakingBonus(stakingBonus)
                .paymentUsageBonus(paymentBonus)
                .priceSupportBonus(priceSupportBonus)
                .totalFactor(baseHoldingFactor.add(stakingBonus).add(paymentBonus).add(priceSupportBonus))
                .build();
    }

    /**
     * 価格支持力比率を計算・更新
     */
    public void calculatePriceSupportRatio() {
        if (averageHoldingPrice != null && currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.priceSupportRatio = averageHoldingPrice.divide(currentPrice, 6, RoundingMode.HALF_UP);
        }
    }

    /**
     * H係数の内訳情報を格納するクラス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingFactorBreakdown {
        private BigDecimal baseHolding;
        private BigDecimal stakingBonus;
        private BigDecimal paymentUsageBonus;
        private BigDecimal priceSupportBonus;
        private BigDecimal totalFactor;
    }
}
