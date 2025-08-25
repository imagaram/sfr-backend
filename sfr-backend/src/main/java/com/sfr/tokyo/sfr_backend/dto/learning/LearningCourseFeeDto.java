package com.sfr.tokyo.sfr_backend.dto.learning;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.FeeType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.CurrencyType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.SfrRewardCondition;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LearningCourseFee DTO
 * コース料金データ転送オブジェクト
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningCourseFeeDto {

    /**
     * コース料金ID
     */
    private String feeId;

    /**
     * コースID
     */
    @NotNull(message = "コースIDは必須です")
    private Long courseId;

    /**
     * 料金タイプ
     */
    @NotNull(message = "料金タイプは必須です")
    private FeeType feeType;

    /**
     * 通貨タイプ
     */
    @NotNull(message = "通貨タイプは必須です")
    private CurrencyType currencyType;

    /**
     * 料金額
     */
    @NotNull(message = "料金額は必須です")
    @DecimalMin(value = "0.00", message = "料金額は0以上である必要があります")
    @DecimalMax(value = "9999999999999.99", message = "料金額が上限を超えています")
    private BigDecimal priceAmount;

    /**
     * 無料フラグ
     */
    private Boolean isFree;

    /**
     * アクティブフラグ
     */
    private Boolean isActive;

    /**
     * SFR報酬額
     */
    @DecimalMin(value = "0.00000000", message = "SFR報酬額は0以上である必要があります")
    private BigDecimal sfrRewardAmount;

    /**
     * SFR報酬条件
     */
    private SfrRewardCondition sfrRewardCondition;

    /**
     * 割引率（%）
     */
    @DecimalMin(value = "0.00", message = "割引率は0以上である必要があります")
    @DecimalMax(value = "100.00", message = "割引率は100以下である必要があります")
    private BigDecimal discountPercent;

    /**
     * 割引適用開始日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime discountStartAt;

    /**
     * 割引適用終了日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime discountEndAt;

    /**
     * 早期割引フラグ
     */
    private Boolean earlyBirdDiscount;

    /**
     * 早期割引率（%）
     */
    @DecimalMin(value = "0.00", message = "早期割引率は0以上である必要があります")
    @DecimalMax(value = "100.00", message = "早期割引率は100以下である必要があります")
    private BigDecimal earlyBirdPercent;

    /**
     * 早期割引締切日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime earlyBirdDeadline;

    /**
     * 最低料金額
     */
    @DecimalMin(value = "0.00", message = "最低料金額は0以上である必要があります")
    private BigDecimal minimumAmount;

    /**
     * 最高料金額
     */
    @DecimalMin(value = "0.00", message = "最高料金額は0以上である必要があります")
    private BigDecimal maximumAmount;

    /**
     * 支払い方法
     */
    private PaymentMethod paymentMethod;

    /**
     * 分割払い可能フラグ
     */
    private Boolean installmentAvailable;

    /**
     * 分割回数
     */
    @Min(value = 1, message = "分割回数は1以上である必要があります")
    @Max(value = 36, message = "分割回数は36回以下である必要があります")
    private Integer installmentCount;

    /**
     * 返金可能フラグ
     */
    private Boolean refundable;

    /**
     * 返金期間（日数）
     */
    @Min(value = 0, message = "返金期間は0以上である必要があります")
    @Max(value = 365, message = "返金期間は365日以下である必要があります")
    private Integer refundPeriodDays;

    /**
     * 特別価格フラグ
     */
    private Boolean specialPrice;

    /**
     * 特別価格説明
     */
    @Size(max = 500, message = "特別価格説明は500文字以内で入力してください")
    private String specialPriceDescription;

    /**
     * 税込み価格フラグ
     */
    private Boolean taxIncluded;

    /**
     * 税率（%）
     */
    @DecimalMin(value = "0.00", message = "税率は0以上である必要があります")
    @DecimalMax(value = "100.00", message = "税率は100以下である必要があります")
    private BigDecimal taxRate;

    /**
     * 備考
     */
    @Size(max = 1000, message = "備考は1000文字以内で入力してください")
    private String notes;

    /**
     * 作成日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========== 計算済みフィールド ==========

    /**
     * 実効価格（割引適用後）
     */
    private BigDecimal effectivePrice;

    /**
     * 税込み価格
     */
    private BigDecimal taxIncludedPrice;

    /**
     * 分割払い月額
     */
    private BigDecimal installmentAmount;

    /**
     * 総割引率
     */
    private BigDecimal totalDiscountPercent;

    /**
     * 節約金額
     */
    private BigDecimal savingsAmount;

    /**
     * 価値スコア
     */
    private BigDecimal valueScore;

    /**
     * 表示用価格文字列
     */
    private String priceDisplay;

    /**
     * 詳細説明
     */
    private String detailedDescription;

    /**
     * 通貨記号
     */
    private String currencySymbol;

    // ========== UI支援フィールド ==========

    /**
     * 割引バッジテキスト
     */
    private String discountBadgeText;

    /**
     * 割引バッジ色
     */
    private String discountBadgeColor;

    /**
     * SFR報酬バッジテキスト
     */
    private String sfrRewardBadgeText;

    /**
     * SFR報酬バッジ色
     */
    private String sfrRewardBadgeColor;

    /**
     * 特別価格バッジテキスト
     */
    private String specialPriceBadgeText;

    /**
     * 料金タイプ表示名
     */
    private String feeTypeDisplayName;

    /**
     * 通貨タイプ表示名
     */
    private String currencyTypeDisplayName;

    /**
     * 支払い方法表示名
     */
    private String paymentMethodDisplayName;

    /**
     * SFR報酬条件表示名
     */
    private String sfrRewardConditionDisplayName;

    /**
     * 早期割引残り時間
     */
    private String earlyBirdTimeRemaining;

    /**
     * 割引残り時間
     */
    private String discountTimeRemaining;

    /**
     * 返金可能残り時間
     */
    private String refundTimeRemaining;

    /**
     * おすすめ度
     */
    private Integer recommendationLevel;

    /**
     * コストパフォーマンス評価
     */
    private String costPerformanceRating;

    // ========== ビジネスロジックメソッド ==========

    /**
     * DTOの初期化（計算済みフィールドの設定）
     */
    public void initialize() {
        calculatePrices();
        setDisplayNames();
        generateBadges();
        calculateTimeRemaining();
        evaluateRecommendation();
    }

    /**
     * 価格関連の計算
     */
    private void calculatePrices() {
        // 実効価格の計算
        if (isFree != null && isFree) {
            this.effectivePrice = BigDecimal.ZERO;
        } else if (priceAmount != null) {
            this.effectivePrice = priceAmount;

            // 早期割引の適用
            if (isEarlyBirdDiscountActive() && earlyBirdPercent != null) {
                BigDecimal earlyDiscount = effectivePrice.multiply(earlyBirdPercent).divide(new BigDecimal("100"));
                this.effectivePrice = effectivePrice.subtract(earlyDiscount);
            }

            // 通常割引の適用
            if (isDiscountActive() && discountPercent != null) {
                BigDecimal normalDiscount = effectivePrice.multiply(discountPercent).divide(new BigDecimal("100"));
                this.effectivePrice = effectivePrice.subtract(normalDiscount);
            }

            // 最低料金額の適用
            if (minimumAmount != null && effectivePrice.compareTo(minimumAmount) < 0) {
                this.effectivePrice = minimumAmount;
            }
        }

        // 税込み価格の計算
        if (effectivePrice != null) {
            if (taxIncluded != null && !taxIncluded && taxRate != null) {
                BigDecimal taxAmount = effectivePrice.multiply(taxRate).divide(new BigDecimal("100"));
                this.taxIncludedPrice = effectivePrice.add(taxAmount);
            } else {
                this.taxIncludedPrice = effectivePrice;
            }
        }

        // 分割払い月額の計算
        if (installmentAvailable != null && installmentAvailable &&
                installmentCount != null && installmentCount > 1 && taxIncludedPrice != null) {
            this.installmentAmount = taxIncludedPrice.divide(new BigDecimal(installmentCount), 2,
                    java.math.RoundingMode.HALF_UP);
        }

        // 総割引率の計算
        if (priceAmount != null && effectivePrice != null && priceAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = priceAmount.subtract(effectivePrice);
            this.totalDiscountPercent = discountAmount.multiply(new BigDecimal("100"))
                    .divide(priceAmount, 2, java.math.RoundingMode.HALF_UP);
        }

        // 節約金額の計算
        if (priceAmount != null && effectivePrice != null) {
            this.savingsAmount = priceAmount.subtract(effectivePrice);
        }

        // 価値スコアの計算
        calculateValueScore();

        // 表示用価格文字列の生成
        generatePriceDisplay();
    }

    /**
     * 表示名の設定
     */
    private void setDisplayNames() {
        if (feeType != null) {
            this.feeTypeDisplayName = feeType.getDisplayName();
        }
        if (currencyType != null) {
            this.currencyTypeDisplayName = currencyType.getDisplayName();
            this.currencySymbol = getCurrencySymbol();
        }
        if (paymentMethod != null) {
            this.paymentMethodDisplayName = paymentMethod.getDisplayName();
        }
        if (sfrRewardCondition != null) {
            this.sfrRewardConditionDisplayName = sfrRewardCondition.getDisplayName();
        }
    }

    /**
     * バッジの生成
     */
    private void generateBadges() {
        // 割引バッジ
        if (isDiscountActive() || isEarlyBirdDiscountActive()) {
            if (totalDiscountPercent != null) {
                this.discountBadgeText = totalDiscountPercent.intValue() + "% OFF";
                this.discountBadgeColor = getDiscountBadgeColor(totalDiscountPercent);
            }
        }

        // SFR報酬バッジ
        if (hasSfrReward()) {
            this.sfrRewardBadgeText = "SFR " + sfrRewardAmount;
            this.sfrRewardBadgeColor = "#FFD700"; // ゴールド
        }

        // 特別価格バッジ
        if (specialPrice != null && specialPrice) {
            this.specialPriceBadgeText = "特別価格";
        }
    }

    /**
     * 残り時間の計算
     */
    private void calculateTimeRemaining() {
        LocalDateTime now = LocalDateTime.now();

        // 早期割引残り時間
        if (earlyBirdDeadline != null && now.isBefore(earlyBirdDeadline)) {
            this.earlyBirdTimeRemaining = formatTimeRemaining(java.time.Duration.between(now, earlyBirdDeadline));
        }

        // 割引残り時間
        if (discountEndAt != null && now.isBefore(discountEndAt)) {
            this.discountTimeRemaining = formatTimeRemaining(java.time.Duration.between(now, discountEndAt));
        }

        // 返金可能残り時間
        if (refundable != null && refundable && refundPeriodDays != null && createdAt != null) {
            LocalDateTime refundDeadline = createdAt.plusDays(refundPeriodDays);
            if (now.isBefore(refundDeadline)) {
                this.refundTimeRemaining = formatTimeRemaining(java.time.Duration.between(now, refundDeadline));
            }
        }
    }

    /**
     * 推奨度の評価
     */
    private void evaluateRecommendation() {
        int score = 0;

        // 無料は最高評価
        if (isFree != null && isFree) {
            this.recommendationLevel = 5;
            this.costPerformanceRating = "最高";
            return;
        }

        // SFR報酬
        if (hasSfrReward())
            score += 2;

        // 割引
        if (totalDiscountPercent != null) {
            if (totalDiscountPercent.compareTo(new BigDecimal("30")) >= 0)
                score += 2;
            else if (totalDiscountPercent.compareTo(new BigDecimal("15")) >= 0)
                score += 1;
        }

        // 返金可能
        if (refundable != null && refundable)
            score += 1;

        // 分割払い可能
        if (installmentAvailable != null && installmentAvailable)
            score += 1;

        this.recommendationLevel = Math.min(score, 5);
        this.costPerformanceRating = getCostPerformanceRating(score);
    }

    /**
     * 価値スコアの計算
     */
    private void calculateValueScore() {
        if (effectivePrice == null)
            return;

        if (effectivePrice.compareTo(BigDecimal.ZERO) == 0) {
            this.valueScore = new BigDecimal("100.00");
            return;
        }

        BigDecimal score = new BigDecimal("50.00");

        // SFR報酬ボーナス
        if (hasSfrReward()) {
            score = score.add(new BigDecimal("20.00"));
        }

        // 割引ボーナス
        if (totalDiscountPercent != null) {
            BigDecimal discountBonus = totalDiscountPercent.multiply(new BigDecimal("0.3"));
            score = score.add(discountBonus);
        }

        // 返金可能ボーナス
        if (refundable != null && refundable) {
            score = score.add(new BigDecimal("10.00"));
        }

        // 分割払い可能ボーナス
        if (installmentAvailable != null && installmentAvailable) {
            score = score.add(new BigDecimal("5.00"));
        }

        this.valueScore = score.min(new BigDecimal("100.00"));
    }

    /**
     * 表示用価格文字列の生成
     */
    private void generatePriceDisplay() {
        if (isFree != null && isFree) {
            this.priceDisplay = "無料";
            return;
        }

        if (taxIncludedPrice == null)
            return;

        StringBuilder display = new StringBuilder();
        display.append(getCurrencySymbol()).append(taxIncludedPrice);

        if (installmentAvailable != null && installmentAvailable &&
                installmentCount != null && installmentCount > 1 && installmentAmount != null) {
            display.append(" (分割: ").append(getCurrencySymbol()).append(installmentAmount)
                    .append(" × ").append(installmentCount).append("回)");
        }

        if (hasSfrReward()) {
            display.append(" + SFR ").append(sfrRewardAmount);
        }

        this.priceDisplay = display.toString();
    }

    // ========== ヘルパーメソッド ==========

    /**
     * 早期割引が有効かチェック
     */
    public boolean isEarlyBirdDiscountActive() {
        if (earlyBirdDiscount == null || !earlyBirdDiscount ||
                earlyBirdPercent == null || earlyBirdDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(earlyBirdDeadline);
    }

    /**
     * 通常割引が有効かチェック
     */
    public boolean isDiscountActive() {
        if (discountPercent == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (discountStartAt != null && now.isBefore(discountStartAt)) {
            return false;
        }

        if (discountEndAt != null && now.isAfter(discountEndAt)) {
            return false;
        }

        return true;
    }

    /**
     * SFR報酬が有効かチェック
     */
    public boolean hasSfrReward() {
        return sfrRewardAmount != null &&
                sfrRewardAmount.compareTo(BigDecimal.ZERO) > 0 &&
                sfrRewardCondition != null &&
                sfrRewardCondition != SfrRewardCondition.NONE;
    }

    /**
     * 通貨記号を取得
     */
    private String getCurrencySymbol() {
        if (currencyType == null)
            return "";

        switch (currencyType) {
            case JPY:
                return "¥";
            case USD:
                return "$";
            case EUR:
                return "€";
            case SFR:
                return "SFR";
            case BTC:
                return "₿";
            case ETH:
                return "Ξ";
            case POINT:
                return "pt";
            default:
                return "";
        }
    }

    /**
     * 割引バッジの色を取得
     */
    private String getDiscountBadgeColor(BigDecimal discountPercent) {
        if (discountPercent.compareTo(new BigDecimal("50")) >= 0) {
            return "#FF0000"; // 赤（大幅割引）
        } else if (discountPercent.compareTo(new BigDecimal("30")) >= 0) {
            return "#FF8C00"; // オレンジ（中程度割引）
        } else if (discountPercent.compareTo(new BigDecimal("15")) >= 0) {
            return "#FFA500"; // 薄いオレンジ（軽度割引）
        } else {
            return "#32CD32"; // 緑（軽微割引）
        }
    }

    /**
     * 時間残り表示のフォーマット
     */
    private String formatTimeRemaining(java.time.Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) {
            return days + "日" + hours + "時間";
        } else if (hours > 0) {
            return hours + "時間" + minutes + "分";
        } else {
            return minutes + "分";
        }
    }

    /**
     * コストパフォーマンス評価の取得
     */
    private String getCostPerformanceRating(int score) {
        if (score >= 5)
            return "最高";
        else if (score >= 4)
            return "非常に良い";
        else if (score >= 3)
            return "良い";
        else if (score >= 2)
            return "普通";
        else
            return "標準";
    }

    /**
     * 詳細説明の生成
     */
    public void generateDetailedDescription() {
        StringBuilder description = new StringBuilder();

        description.append("料金タイプ: ").append(feeTypeDisplayName).append("\n");
        description.append("価格: ").append(priceDisplay).append("\n");

        if (isDiscountActive() || isEarlyBirdDiscountActive()) {
            description.append("割引適用中 (").append(totalDiscountPercent).append("% OFF)\n");
        }

        if (hasSfrReward()) {
            description.append("SFR報酬: ").append(sfrRewardAmount)
                    .append(" (条件: ").append(sfrRewardConditionDisplayName).append(")\n");
        }

        if (refundable != null && refundable) {
            description.append("返金可能期間: ").append(refundPeriodDays).append("日\n");
        }

        if (notes != null && !notes.trim().isEmpty()) {
            description.append("備考: ").append(notes);
        }

        this.detailedDescription = description.toString();
    }

    /**
     * JSON初期化用メソッド
     */
    public void initializeFromJson() {
        initialize();
        generateDetailedDescription();
    }
}
