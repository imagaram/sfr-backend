package com.sfr.tokyo.sfr_backend.entity.config;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFRポイントシステム設定エンティティ
 * Phase 1 実装: デュアルトークンシステムの基盤設定
 * 
 * 1SFR = 150円固定レート、手数料率6.4％のポイントシステム管理
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Entity
@Table(name = "sfr_point_config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"config_key", "space_id"})
}, indexes = {
    @Index(name = "idx_sfr_config_space", columnList = "space_id"),
    @Index(name = "idx_sfr_config_key", columnList = "config_key"),
    @Index(name = "idx_sfr_config_updated", columnList = "updated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SfrPointConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * スペースID（デフォルト: 1）
     */
    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    @Builder.Default
    private Long spaceId = 1L;

    /**
     * 設定キー
     */
    @Column(name = "config_key", nullable = false, length = 100)
    @NotBlank(message = "設定キーは必須です")
    private String configKey;

    /**
     * 設定値（JSON形式）
     */
    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "設定値は必須です")
    private String configValue;

    /**
     * 設定説明
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 有効フラグ
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * バージョン番号（楽観ロック）
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 設定者ID
     */
    @Column(name = "updated_by", length = 36)
    private String updatedBy;

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
     * SFRポイント設定のキー定数
     */
    public static class ConfigKeys {
        public static final String SFR_EXCHANGE_RATE = "sfr.exchange.rate";
        public static final String SFR_FEE_RATE = "sfr.fee.rate";
        public static final String SFR_MIN_PURCHASE = "sfr.min.purchase";
        public static final String SFR_MAX_PURCHASE = "sfr.max.purchase";
        public static final String SFR_DAILY_LIMIT = "sfr.daily.limit";
        public static final String SFR_MONTHLY_LIMIT = "sfr.monthly.limit";
        public static final String SFRT_REWARD_RATE_BUYER = "sfrt.reward.rate.buyer";
        public static final String SFRT_REWARD_RATE_SELLER = "sfrt.reward.rate.seller";
        public static final String SFRT_REWARD_RATE_PLATFORM = "sfrt.reward.rate.platform";
        public static final String SFR_POINT_ENABLED = "sfr.point.enabled";
        public static final String SFR_STRIPE_ENABLED = "sfr.stripe.enabled";
    }

    /**
     * デフォルト設定値
     */
    public static class DefaultValues {
        public static final String EXCHANGE_RATE = "150.00";
        public static final String FEE_RATE = "0.064";
        public static final String MIN_PURCHASE = "100.0";
        public static final String MAX_PURCHASE = "100000.0";
        public static final String DAILY_LIMIT = "50000.0";
        public static final String MONTHLY_LIMIT = "500000.0";
        public static final String SFRT_BUYER_RATE = "0.0125";
        public static final String SFRT_SELLER_RATE = "0.0125";
        public static final String SFRT_PLATFORM_RATE = "0.025";
        public static final String POINT_ENABLED = "true";
        public static final String STRIPE_ENABLED = "true";
    }

    /**
     * BigDecimal値として設定値を取得
     */
    public BigDecimal getValueAsBigDecimal() {
        try {
            return new BigDecimal(configValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("設定値がBigDecimal形式ではありません: " + configValue);
        }
    }

    /**
     * Boolean値として設定値を取得
     */
    public Boolean getValueAsBoolean() {
        return Boolean.parseBoolean(configValue);
    }

    /**
     * Integer値として設定値を取得
     */
    public Integer getValueAsInteger() {
        try {
            return Integer.parseInt(configValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("設定値がInteger形式ではありません: " + configValue);
        }
    }
}
