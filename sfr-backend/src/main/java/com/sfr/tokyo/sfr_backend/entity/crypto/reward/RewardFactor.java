package com.sfr.tokyo.sfr_backend.entity.crypto.reward;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 報酬係数エンティティ
 * B係数（基本報酬係数）とM係数（市場状況係数）を管理
 */
@Entity
@Table(name = "reward_factors", indexes = {
        @Index(name = "idx_reward_factors_type_date", columnList = "contribution_type, effective_from"),
        @Index(name = "idx_reward_factors_effective_period", columnList = "effective_from, effective_to"),
        @Index(name = "idx_reward_factors_price_range", columnList = "price_range_min, price_range_max")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "contribution_type", nullable = false, length = 20)
    @NotNull(message = "貢献タイプは必須です")
    private ContributionRecord.ContributionType contributionType;

    @Column(name = "base_factor", nullable = false, precision = 6, scale = 4)
    @NotNull(message = "基本報酬係数は必須です")
    @DecimalMin(value = "0.0001", message = "基本報酬係数は0.0001以上である必要があります")
    @DecimalMax(value = "99.9999", message = "基本報酬係数は99.9999以下である必要があります")
    @Builder.Default
    private BigDecimal baseFactor = BigDecimal.ONE;

    @Column(name = "target_price", precision = 10, scale = 2)
    @DecimalMin(value = "0.01", message = "目標価格は0.01以上である必要があります")
    private BigDecimal targetPrice;

    @Column(name = "price_range_min", precision = 10, scale = 2)
    @DecimalMin(value = "0.01", message = "価格帯最小値は0.01以上である必要があります")
    private BigDecimal priceRangeMin;

    @Column(name = "price_range_max", precision = 10, scale = 2)
    @DecimalMin(value = "0.01", message = "価格帯最大値は0.01以上である必要があります")
    private BigDecimal priceRangeMax;

    @Column(name = "market_factor", precision = 6, scale = 4)
    @DecimalMin(value = "0.0001", message = "市場状況係数は0.0001以上である必要があります")
    @DecimalMax(value = "99.9999", message = "市場状況係数は99.9999以下である必要があります")
    private BigDecimal marketFactor;

    @Column(name = "activity_volume_threshold")
    @Min(value = 0, message = "活動量閾値は0以上である必要があります")
    private Integer activityVolumeThreshold;

    @Column(name = "scarcity_multiplier", precision = 6, scale = 4)
    @DecimalMin(value = "0.0001", message = "希少性倍率は0.0001以上である必要があります")
    @DecimalMax(value = "99.9999", message = "希少性倍率は99.9999以下である必要があります")
    @Builder.Default
    private BigDecimal scarcityMultiplier = BigDecimal.ONE;

    @Column(name = "effective_from", nullable = false)
    @NotNull(message = "有効開始日時は必須です")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 指定された価格範囲に該当するかチェック
     *
     * @param price チェック対象の価格
     * @return 該当する場合true
     */
    public boolean isPriceInRange(BigDecimal price) {
        if (priceRangeMin == null || priceRangeMax == null) {
            return false;
        }
        return price.compareTo(priceRangeMin) >= 0 && price.compareTo(priceRangeMax) <= 0;
    }

    /**
     * 現在有効な係数かチェック
     *
     * @param checkTime チェック対象の時刻
     * @return 有効な場合true
     */
    public boolean isEffectiveAt(LocalDateTime checkTime) {
        boolean afterStart = checkTime.isAfter(effectiveFrom) || checkTime.isEqual(effectiveFrom);
        boolean beforeEnd = effectiveTo == null || checkTime.isBefore(effectiveTo);
        return afterStart && beforeEnd;
    }

    /**
     * 現在有効な係数かチェック（現在時刻基準）
     *
     * @return 有効な場合true
     */
    public boolean isCurrentlyEffective() {
        return isEffectiveAt(LocalDateTime.now());
    }

    /**
     * 価格帯の妥当性をチェック
     */
    @PrePersist
    @PreUpdate
    private void validatePriceRange() {
        if (priceRangeMin != null && priceRangeMax != null) {
            if (priceRangeMin.compareTo(priceRangeMax) > 0) {
                throw new IllegalArgumentException("価格帯最小値は最大値以下である必要があります");
            }
        }
    }
}
