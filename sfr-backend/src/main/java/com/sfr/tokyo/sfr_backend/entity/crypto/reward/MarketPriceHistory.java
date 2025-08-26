package com.sfr.tokyo.sfr_backend.entity.crypto.reward;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 市場価格履歴エンティティ
 * M係数計算用の市場価格データ
 */
@Entity
@Table(name = "market_price_history", indexes = {
        @Index(name = "idx_market_price_timestamp", columnList = "price_timestamp"),
        @Index(name = "idx_market_price_value", columnList = "price_jpy"),
        @Index(name = "idx_market_factor", columnList = "market_factor")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_jpy", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "SFR/JPY価格は必須です")
    @DecimalMin(value = "0.01", message = "SFR/JPY価格は0.01以上である必要があります")
    private BigDecimal priceJpy;

    @Column(name = "price_source", nullable = false, length = 50)
    @NotBlank(message = "価格取得元は必須です")
    @Size(max = 50, message = "価格取得元は50文字以内で入力してください")
    private String priceSource;

    @Column(name = "volume_24h", precision = 20, scale = 8)
    @DecimalMin(value = "0", message = "24時間取引量は0以上である必要があります")
    private BigDecimal volume24h;

    @Column(name = "market_cap", precision = 20, scale = 2)
    @DecimalMin(value = "0", message = "時価総額は0以上である必要があります")
    private BigDecimal marketCap;

    @Column(name = "market_factor", nullable = false, precision = 6, scale = 4)
    @NotNull(message = "市場係数は必須です")
    @DecimalMin(value = "0.0001", message = "市場係数は0.0001以上である必要があります")
    @DecimalMax(value = "99.9999", message = "市場係数は99.9999以下である必要があります")
    private BigDecimal marketFactor;

    @Column(name = "target_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "基準となる目標価格は必須です")
    @DecimalMin(value = "0.01", message = "目標価格は0.01以上である必要があります")
    private BigDecimal targetPrice;

    @Column(name = "price_timestamp", nullable = false)
    @NotNull(message = "価格取得日時は必須です")
    private LocalDateTime priceTimestamp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 価格に基づいてM係数を計算
     *
     * @param currentPrice 現在価格
     * @param targetPrice 目標価格
     * @return 計算されたM係数
     */
    public static BigDecimal calculateMarketFactor(BigDecimal currentPrice, BigDecimal targetPrice) {
        if (currentPrice == null || targetPrice == null || targetPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }

        BigDecimal ratio = currentPrice.divide(targetPrice, 4, RoundingMode.HALF_UP);

        // 目標価格を基準とした係数計算
        if (ratio.compareTo(new BigDecimal("0.8")) < 0) {
            // 価格が目標の80%未満 -> 係数1.5-2.0
            return new BigDecimal("2.0");
        } else if (ratio.compareTo(new BigDecimal("1.0")) < 0) {
            // 価格が目標の80%-100% -> 係数1.0
            return BigDecimal.ONE;
        } else if (ratio.compareTo(new BigDecimal("1.13")) < 0) {
            // 価格が目標の100%-113% -> 係数0.5-0.8
            return new BigDecimal("0.8");
        } else {
            // 価格が目標の113%以上 -> 係数0.3以下
            return new BigDecimal("0.3");
        }
    }

    /**
     * M係数を自動計算して設定
     */
    public void calculateAndSetMarketFactor() {
        this.marketFactor = calculateMarketFactor(this.priceJpy, this.targetPrice);
    }

    /**
     * 価格の変動率を計算
     *
     * @param previousPrice 前回価格
     * @return 変動率（小数）
     */
    public BigDecimal calculatePriceChangeRate(BigDecimal previousPrice) {
        if (previousPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return priceJpy.subtract(previousPrice)
                .divide(previousPrice, 6, RoundingMode.HALF_UP);
    }

    /**
     * 価格トレンドを判定
     *
     * @param previousPrice 前回価格
     * @return トレンド（UP, DOWN, STABLE）
     */
    public PriceTrend determinePriceTrend(BigDecimal previousPrice) {
        BigDecimal changeRate = calculatePriceChangeRate(previousPrice);
        BigDecimal threshold = new BigDecimal("0.02"); // 2%

        if (changeRate.compareTo(threshold) > 0) {
            return PriceTrend.UP;
        } else if (changeRate.compareTo(threshold.negate()) < 0) {
            return PriceTrend.DOWN;
        } else {
            return PriceTrend.STABLE;
        }
    }

    /**
     * 価格トレンドの列挙型
     */
    public enum PriceTrend {
        UP("up", "上昇"),
        DOWN("down", "下降"),
        STABLE("stable", "安定");

        private final String code;
        private final String displayName;

        PriceTrend(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * エンティティ保存前にM係数を自動計算
     */
    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (this.marketFactor == null && this.priceJpy != null && this.targetPrice != null) {
            calculateAndSetMarketFactor();
        }
    }
}
