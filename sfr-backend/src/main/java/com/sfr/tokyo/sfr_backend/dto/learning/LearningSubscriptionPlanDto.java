package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSubscriptionPlan.TierLevel;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * LearningSubscriptionPlan DTO
 * サブスクリプションプラン情報の転送用オブジェクト
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningSubscriptionPlanDto {

    /**
     * プランID
     */
    private String planId;

    /**
     * プラン名
     */
    @NotBlank(message = "プラン名は必須です")
    @Size(min = 2, max = 100, message = "プラン名は2文字以上100文字以内で入力してください")
    private String name;

    /**
     * プラン説明
     */
    @Size(max = 2000, message = "プラン説明は2000文字以内で入力してください")
    private String description;

    /**
     * 月額料金
     */
    @NotNull(message = "月額料金は必須です")
    @DecimalMin(value = "0.00", message = "月額料金は0.00以上である必要があります")
    @DecimalMax(value = "999999.99", message = "月額料金は999999.99以下である必要があります")
    private BigDecimal monthlyFee;

    /**
     * 機能一覧
     */
    @Size(max = 5000, message = "機能一覧は5000文字以内で入力してください")
    private String features;

    /**
     * プラン層級
     */
    @NotNull(message = "プラン層級は必須です")
    private TierLevel tierLevel;

    /**
     * アクティブフラグ
     */
    private Boolean isActive;

    /**
     * 推奨フラグ
     */
    private Boolean isRecommended;

    /**
     * 人気フラグ
     */
    private Boolean isPopular;

    /**
     * 試用期間フラグ
     */
    private Boolean hasTrial;

    /**
     * 試用期間（日数）
     */
    @Min(value = 0, message = "試用期間は0以上である必要があります")
    @Max(value = 365, message = "試用期間は365日以下である必要があります")
    private Integer trialDays;

    /**
     * 最大ユーザー数
     */
    @Min(value = 1, message = "最大ユーザー数は1以上である必要があります")
    private Integer maxUsers;

    /**
     * 最大ストレージ容量（GB）
     */
    @Min(value = 0, message = "最大ストレージ容量は0以上である必要があります")
    private Integer maxStorageGb;

    /**
     * 年額料金
     */
    @DecimalMin(value = "0.00", message = "年額料金は0.00以上である必要があります")
    @DecimalMax(value = "9999999.99", message = "年額料金は9999999.99以下である必要があります")
    private BigDecimal yearlyFee;

    /**
     * 年額割引率
     */
    @DecimalMin(value = "0.00", message = "年額割引率は0.00以上である必要があります")
    @DecimalMax(value = "100.00", message = "年額割引率は100.00以下である必要があります")
    private BigDecimal yearlyDiscountPercent;

    /**
     * 表示順序
     */
    @Min(value = 0, message = "表示順序は0以上である必要があります")
    private Integer sortOrder;

    /**
     * プラン色（UI表示用）
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "プラン色は有効な16進数カラーコードである必要があります")
    private String planColor;

    /**
     * プランアイコン（UI表示用）
     */
    @Size(max = 100, message = "プランアイコンは100文字以内で入力してください")
    private String planIcon;

    /**
     * 制限事項
     */
    @Size(max = 2000, message = "制限事項は2000文字以内で入力してください")
    private String limitations;

    /**
     * 特別機能
     */
    @Size(max = 3000, message = "特別機能は3000文字以内で入力してください")
    private String specialFeatures;

    /**
     * 作成日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========== 計算済みフィールド ==========

    /**
     * 年額節約額
     */
    private BigDecimal yearlySavings;

    /**
     * 計算済み年額割引率
     */
    private BigDecimal calculatedYearlyDiscountPercent;

    /**
     * 1日あたりの料金
     */
    private BigDecimal dailyFee;

    /**
     * プラン価値スコア
     */
    private BigDecimal valueScore;

    /**
     * 機能数
     */
    private Integer featureCount;

    /**
     * 制限事項数
     */
    private Integer limitationCount;

    /**
     * プラン比較用スコア
     */
    private BigDecimal comparisonScore;

    /**
     * 詳細説明
     */
    private String detailedDescription;

    // ========== 機能一覧（パース済み） ==========

    /**
     * 機能一覧（リスト形式）
     */
    private List<String> featureList;

    /**
     * 制限事項一覧（リスト形式）
     */
    private List<String> limitationList;

    /**
     * 特別機能一覧（リスト形式）
     */
    private List<String> specialFeatureList;

    // ========== UI表示支援フィールド ==========

    /**
     * 層級表示名
     */
    private String tierDisplayName;

    /**
     * バッジテキスト
     */
    private String badgeText;

    /**
     * バッジ色
     */
    private String badgeColor;

    /**
     * CTA（Call to Action）テキスト
     */
    private String ctaText;

    /**
     * 強調表示フラグ
     */
    private Boolean isHighlighted;

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
        if (tierLevel != null) {
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
     * プラン表示名取得
     */
    public String getDisplayName() {
        if (tierLevel == null)
            return name;
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
     * バッジテキスト生成
     */
    public String generateBadgeText() {
        if (Boolean.TRUE.equals(isRecommended)) {
            return "おすすめ";
        } else if (Boolean.TRUE.equals(isPopular)) {
            return "人気";
        } else if (Boolean.TRUE.equals(hasTrial)) {
            return "無料試用";
        } else if (isFree()) {
            return "無料";
        }
        return null;
    }

    /**
     * バッジ色生成
     */
    public String generateBadgeColor() {
        if (Boolean.TRUE.equals(isRecommended)) {
            return "#ff6b6b";
        } else if (Boolean.TRUE.equals(isPopular)) {
            return "#4ecdc4";
        } else if (Boolean.TRUE.equals(hasTrial)) {
            return "#45b7d1";
        } else if (isFree()) {
            return "#96ceb4";
        }
        return "#95a5a6";
    }

    /**
     * CTA（Call to Action）テキスト生成
     */
    public String generateCtaText() {
        if (isFree()) {
            return "無料で始める";
        } else if (Boolean.TRUE.equals(hasTrial)) {
            return "無料試用を始める";
        } else {
            return "プランを選択";
        }
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
    public BigDecimal calculateComparisonScore() {
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
        if (featureCount != null) {
            score += Math.min(30.0, featureCount * 2.0);
        }

        // 制限スコア（制限が少ないほど高スコア）
        if (limitationCount != null) {
            score += Math.max(0.0, 20.0 - limitationCount * 2.0);
        }

        // 特典スコア
        BigDecimal valueScore = calculateValueScore();
        score += valueScore.doubleValue() * 0.2;

        return BigDecimal.valueOf(Math.min(100.0, score))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 機能一覧のパース
     */
    public List<String> parseFeatures() {
        if (features == null || features.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 簡易JSON配列パース（実際の実装では適切なJSONライブラリを使用）
        return List.of(features.replaceAll("[\\[\\]\"]", "").split(","));
    }

    /**
     * 制限事項一覧のパース
     */
    public List<String> parseLimitations() {
        if (limitations == null || limitations.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return List.of(limitations.replaceAll("[\\[\\]\"]", "").split(","));
    }

    /**
     * 特別機能一覧のパース
     */
    public List<String> parseSpecialFeatures() {
        if (specialFeatures == null || specialFeatures.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return List.of(specialFeatures.replaceAll("[\\[\\]\"]", "").split(","));
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

    /**
     * UI表示用フィールドの初期化
     */
    public void initializeUiFields() {
        // 計算済みフィールドの設定
        this.yearlySavings = calculateYearlySavings();
        this.calculatedYearlyDiscountPercent = calculateYearlyDiscountPercent();
        this.dailyFee = calculateDailyFee();
        this.valueScore = calculateValueScore();
        this.comparisonScore = calculateComparisonScore();

        // 詳細説明の設定
        this.detailedDescription = generateDetailedDescription();

        // UIフィールドの設定
        this.tierDisplayName = tierLevel != null ? tierLevel.getDisplayName() : null;
        this.badgeText = generateBadgeText();
        this.badgeColor = generateBadgeColor();
        this.ctaText = generateCtaText();
        this.isHighlighted = Boolean.TRUE.equals(isRecommended) || Boolean.TRUE.equals(isPopular);

        // リストフィールドの設定
        this.featureList = parseFeatures();
        this.limitationList = parseLimitations();
        this.specialFeatureList = parseSpecialFeatures();
        this.featureCount = featureList.size();
        this.limitationCount = limitationList.size();
    }

    /**
     * プラン比較用の要約情報
     */
    public String getComparisonSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(getDisplayName()).append(": ");
        summary.append(getPriceDisplay());

        if (Boolean.TRUE.equals(hasTrial)) {
            summary.append(" (").append(getTrialDisplay()).append(")");
        }

        if (featureCount != null && featureCount > 0) {
            summary.append(" - ").append(featureCount).append("機能");
        }

        return summary.toString();
    }

    /**
     * 初期化メソッド
     */
    public void initialize() {
        if (isActive == null) {
            isActive = true;
        }
        if (isRecommended == null) {
            isRecommended = false;
        }
        if (isPopular == null) {
            isPopular = false;
        }
        if (hasTrial == null) {
            hasTrial = false;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (featureList == null) {
            featureList = new ArrayList<>();
        }
        if (limitationList == null) {
            limitationList = new ArrayList<>();
        }
        if (specialFeatureList == null) {
            specialFeatureList = new ArrayList<>();
        }

        // UI表示用フィールドの初期化
        initializeUiFields();
    }
}
