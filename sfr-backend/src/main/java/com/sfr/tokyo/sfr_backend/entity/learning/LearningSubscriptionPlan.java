package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学習サブスクリプションプラン エンティティ - LearningSubscriptionPlan
 * 学習プラットフォームのサブスクリプションプラン管理
 */
@Entity
@Table(name = "learning_subscription_plans", indexes = {
        @Index(name = "idx_subscription_plan_name", columnList = "name"),
        @Index(name = "idx_subscription_plan_monthly_fee", columnList = "monthly_fee"),
        @Index(name = "idx_subscription_plan_is_active", columnList = "is_active"),
        @Index(name = "idx_subscription_plan_sort_order", columnList = "sort_order"),
        @Index(name = "idx_subscription_plan_tier", columnList = "tier_level")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningSubscriptionPlan {

    /**
     * プランID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * プラン名
     */
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "プラン名は必須です")
    @Size(min = 2, max = 100, message = "プラン名は2文字以上100文字以内で入力してください")
    private String name;

    /**
     * プラン説明
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 2000, message = "プラン説明は2000文字以内で入力してください")
    private String description;

    /**
     * 月額料金
     */
    @Column(name = "monthly_fee", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "月額料金は必須です")
    @Min(value = 0, message = "月額料金は0以上である必要があります")
    @DecimalMin(value = "0.00", message = "月額料金は0.00以上である必要があります")
    @DecimalMax(value = "999999.99", message = "月額料金は999999.99以下である必要があります")
    private BigDecimal monthlyFee;

    /**
     * 機能一覧（JSON形式で保存）
     */
    @Column(name = "features", columnDefinition = "JSON")
    @Size(max = 5000, message = "機能一覧は5000文字以内で入力してください")
    private String features;

    /**
     * プラン層級
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tier_level", nullable = false, length = 50)
    @NotNull(message = "プラン層級は必須です")
    private TierLevel tierLevel;

    /**
     * アクティブフラグ
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 推奨フラグ
     */
    @Column(name = "is_recommended", nullable = false)
    @Builder.Default
    private Boolean isRecommended = false;

    /**
     * 人気フラグ
     */
    @Column(name = "is_popular", nullable = false)
    @Builder.Default
    private Boolean isPopular = false;

    /**
     * 試用期間フラグ
     */
    @Column(name = "has_trial", nullable = false)
    @Builder.Default
    private Boolean hasTrial = false;

    /**
     * 試用期間（日数）
     */
    @Column(name = "trial_days")
    @Min(value = 0, message = "試用期間は0以上である必要があります")
    @Max(value = 365, message = "試用期間は365日以下である必要があります")
    private Integer trialDays;

    /**
     * 最大ユーザー数
     */
    @Column(name = "max_users")
    @Min(value = 1, message = "最大ユーザー数は1以上である必要があります")
    private Integer maxUsers;

    /**
     * 最大ストレージ容量（GB）
     */
    @Column(name = "max_storage_gb")
    @Min(value = 0, message = "最大ストレージ容量は0以上である必要があります")
    private Integer maxStorageGb;

    /**
     * 年額料金
     */
    @Column(name = "yearly_fee", precision = 10, scale = 2)
    @Min(value = 0, message = "年額料金は0以上である必要があります")
    @DecimalMin(value = "0.00", message = "年額料金は0.00以上である必要があります")
    @DecimalMax(value = "9999999.99", message = "年額料金は9999999.99以下である必要があります")
    private BigDecimal yearlyFee;

    /**
     * 年額割引率
     */
    @Column(name = "yearly_discount_percent", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "年額割引率は0.00以上である必要があります")
    @DecimalMax(value = "100.00", message = "年額割引率は100.00以下である必要があります")
    private BigDecimal yearlyDiscountPercent;

    /**
     * 表示順序
     */
    @Column(name = "sort_order", nullable = false)
    @NotNull(message = "表示順序は必須です")
    @Min(value = 0, message = "表示順序は0以上である必要があります")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * プラン色（UI表示用）
     */
    @Column(name = "plan_color", length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "プラン色は有効な16進数カラーコードである必要があります")
    private String planColor;

    /**
     * プランアイコン（UI表示用）
     */
    @Column(name = "plan_icon", length = 100)
    @Size(max = 100, message = "プランアイコンは100文字以内で入力してください")
    private String planIcon;

    /**
     * 制限事項（JSON形式で保存）
     */
    @Column(name = "limitations", columnDefinition = "JSON")
    @Size(max = 2000, message = "制限事項は2000文字以内で入力してください")
    private String limitations;

    /**
     * 特別機能（JSON形式で保存）
     */
    @Column(name = "special_features", columnDefinition = "JSON")
    @Size(max = 3000, message = "特別機能は3000文字以内で入力してください")
    private String specialFeatures;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 削除日時（論理削除用）
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ========== Enum定義 ==========

    /**
     * プラン層級
     */
    public enum TierLevel {
        FREE("無料"),
        BASIC("ベーシック"),
        STANDARD("スタンダード"),
        PREMIUM("プレミアム"),
        ENTERPRISE("エンタープライズ"),
        UNLIMITED("無制限"),
        STUDENT("学生"),
        TEACHER("教師"),
        BUSINESS("ビジネス"),
        CUSTOM("カスタム");

        private final String displayName;

        TierLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========== ビジネスロジックメソッド ==========

    /**
     * 年額節約額計算
     */
    public BigDecimal calculateYearlySavings() {
        if (monthlyFee == null || yearlyFee == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyTotal = monthlyFee.multiply(BigDecimal.valueOf(12));
        return monthlyTotal.subtract(yearlyFee);
    }

    /**
     * 年額割引率計算
     */
    public BigDecimal calculateYearlyDiscountPercent() {
        if (monthlyFee == null || yearlyFee == null || monthlyFee.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyTotal = monthlyFee.multiply(BigDecimal.valueOf(12));
        BigDecimal savings = monthlyTotal.subtract(yearlyFee);

        return savings.divide(monthlyTotal, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 1日あたりの料金計算
     */
    public BigDecimal calculateDailyFee() {
        if (monthlyFee == null) {
            return BigDecimal.ZERO;
        }

        return monthlyFee.divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * プラン価値スコア計算
     */
    public BigDecimal calculateValueScore() {
        double score = 0.0;

        // 基本スコア（層級による）
        switch (tierLevel) {
            case FREE -> score += 20.0;
            case BASIC -> score += 40.0;
            case STANDARD -> score += 60.0;
            case PREMIUM -> score += 80.0;
            case ENTERPRISE -> score += 90.0;
            case UNLIMITED -> score += 100.0;
            case STUDENT -> score += 45.0;
            case TEACHER -> score += 55.0;
            case BUSINESS -> score += 85.0;
            case CUSTOM -> score += 75.0;
        }

        // 特典によるボーナス
        if (Boolean.TRUE.equals(hasTrial))
            score += 10.0;
        if (Boolean.TRUE.equals(isRecommended))
            score += 5.0;
        if (Boolean.TRUE.equals(isPopular))
            score += 5.0;

        // 年額割引によるボーナス
        if (yearlyFee != null && monthlyFee != null) {
            BigDecimal discount = calculateYearlyDiscountPercent();
            if (discount.compareTo(BigDecimal.valueOf(10)) > 0) {
                score += 10.0;
            }
        }

        return BigDecimal.valueOf(Math.min(100.0, score))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 機能数カウント
     */
    public int getFeatureCount() {
        if (features == null || features.trim().isEmpty()) {
            return 0;
        }

        // JSON配列の要素数をカウント（簡易実装）
        return features.split(",").length;
    }

    /**
     * 制限事項数カウント
     */
    public int getLimitationCount() {
        if (limitations == null || limitations.trim().isEmpty()) {
            return 0;
        }

        // JSON配列の要素数をカウント（簡易実装）
        return limitations.split(",").length;
    }

    /**
     * プラン表示名取得
     */
    public String getDisplayName() {
        return tierLevel.getDisplayName() + "プラン";
    }

    /**
     * 価格表示用文字列
     */
    public String getPriceDisplay() {
        if (monthlyFee == null) {
            return "価格未設定";
        }

        if (monthlyFee.compareTo(BigDecimal.ZERO) == 0) {
            return "無料";
        }

        return String.format("¥%,d/月", monthlyFee.intValue());
    }

    /**
     * 年額価格表示用文字列
     */
    public String getYearlyPriceDisplay() {
        if (yearlyFee == null) {
            return null;
        }

        return String.format("¥%,d/年", yearlyFee.intValue());
    }

    /**
     * 節約額表示用文字列
     */
    public String getSavingsDisplay() {
        BigDecimal savings = calculateYearlySavings();
        if (savings.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return String.format("年間¥%,d節約", savings.intValue());
    }

    /**
     * 試用期間表示用文字列
     */
    public String getTrialDisplay() {
        if (!Boolean.TRUE.equals(hasTrial) || trialDays == null || trialDays <= 0) {
            return null;
        }

        return String.format("%d日間無料試用", trialDays);
    }

    /**
     * プランアクティブ判定
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive) && deletedAt == null;
    }

    /**
     * 推奨プラン判定
     */
    public boolean isRecommended() {
        return Boolean.TRUE.equals(isRecommended) && isActive();
    }

    /**
     * 人気プラン判定
     */
    public boolean isPopular() {
        return Boolean.TRUE.equals(isPopular) && isActive();
    }

    /**
     * 無料プラン判定
     */
    public boolean isFree() {
        return monthlyFee != null && monthlyFee.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * エンタープライズプラン判定
     */
    public boolean isEnterprise() {
        return tierLevel == TierLevel.ENTERPRISE || tierLevel == TierLevel.BUSINESS;
    }

    /**
     * 教育プラン判定
     */
    public boolean isEducational() {
        return tierLevel == TierLevel.STUDENT || tierLevel == TierLevel.TEACHER;
    }

    /**
     * プラン比較用スコア計算
     */
    public BigDecimal getComparisonScore() {
        double score = 0.0;

        // 価格スコア（安いほど高スコア）
        if (monthlyFee != null) {
            if (monthlyFee.compareTo(BigDecimal.ZERO) == 0) {
                score += 30.0;
            } else if (monthlyFee.compareTo(BigDecimal.valueOf(1000)) <= 0) {
                score += 25.0;
            } else if (monthlyFee.compareTo(BigDecimal.valueOf(3000)) <= 0) {
                score += 20.0;
            } else {
                score += 10.0;
            }
        }

        // 機能スコア
        score += Math.min(30.0, getFeatureCount() * 2.0);

        // 制限スコア（制限が少ないほど高スコア）
        score += Math.max(0.0, 20.0 - getLimitationCount() * 2.0);

        // 特典スコア
        score += calculateValueScore().doubleValue() * 0.2;

        return BigDecimal.valueOf(Math.min(100.0, score))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * プラン詳細説明生成
     */
    public String generateDetailedDescription() {
        StringBuilder description = new StringBuilder();

        description.append(getDisplayName());
        if (this.description != null && !this.description.trim().isEmpty()) {
            description.append(" - ").append(this.description);
        }

        description.append("\n価格: ").append(getPriceDisplay());

        if (yearlyFee != null) {
            description.append("\n年額: ").append(getYearlyPriceDisplay());
            String savings = getSavingsDisplay();
            if (savings != null) {
                description.append(" (").append(savings).append(")");
            }
        }

        String trial = getTrialDisplay();
        if (trial != null) {
            description.append("\n").append(trial);
        }

        if (maxUsers != null) {
            description.append("\n最大ユーザー数: ").append(maxUsers).append("人");
        }

        if (maxStorageGb != null) {
            description.append("\nストレージ容量: ").append(maxStorageGb).append("GB");
        }

        return description.toString();
    }
}
