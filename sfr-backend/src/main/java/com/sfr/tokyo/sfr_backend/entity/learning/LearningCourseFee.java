package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * LearningCourseFee Entity
 * コース料金管理エンティティ
 */
@Entity
@Table(name = "learning_course_fees", indexes = {
        @Index(name = "idx_course_fee_course_id", columnList = "course_id"),
        @Index(name = "idx_course_fee_fee_type", columnList = "fee_type"),
        @Index(name = "idx_course_fee_is_free", columnList = "is_free"),
        @Index(name = "idx_course_fee_is_active", columnList = "is_active"),
        @Index(name = "idx_course_fee_currency", columnList = "currency_type"),
        @Index(name = "idx_course_fee_created_at", columnList = "created_at"),
        @Index(name = "idx_course_fee_price_range", columnList = "price_amount, currency_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningCourseFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * コースID（外部キー）
     */
    @Column(name = "course_id", nullable = false)
    @NotNull(message = "コースIDは必須です")
    private Long courseId;

    /**
     * 料金タイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 50)
    @NotNull(message = "料金タイプは必須です")
    private FeeType feeType;

    /**
     * 通貨タイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false, length = 20)
    @NotNull(message = "通貨タイプは必須です")
    private CurrencyType currencyType;

    /**
     * 料金額
     */
    @Column(name = "price_amount", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "料金額は必須です")
    @DecimalMin(value = "0.00", message = "料金額は0以上である必要があります")
    @DecimalMax(value = "9999999999999.99", message = "料金額が上限を超えています")
    private BigDecimal priceAmount;

    /**
     * 無料フラグ
     */
    @Column(name = "is_free", nullable = false)
    @Builder.Default
    private Boolean isFree = false;

    /**
     * アクティブフラグ
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * SFR報酬額
     */
    @Column(name = "sfr_reward_amount", precision = 15, scale = 8)
    @DecimalMin(value = "0.00000000", message = "SFR報酬額は0以上である必要があります")
    private BigDecimal sfrRewardAmount;

    /**
     * SFR報酬条件
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sfr_reward_condition", length = 50)
    private SfrRewardCondition sfrRewardCondition;

    /**
     * 割引率（%）
     */
    @Column(name = "discount_percent", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "割引率は0以上である必要があります")
    @DecimalMax(value = "100.00", message = "割引率は100以下である必要があります")
    private BigDecimal discountPercent;

    /**
     * 割引適用開始日時
     */
    @Column(name = "discount_start_at")
    private LocalDateTime discountStartAt;

    /**
     * 割引適用終了日時
     */
    @Column(name = "discount_end_at")
    private LocalDateTime discountEndAt;

    /**
     * 早期割引フラグ
     */
    @Column(name = "early_bird_discount")
    @Builder.Default
    private Boolean earlyBirdDiscount = false;

    /**
     * 早期割引率（%）
     */
    @Column(name = "early_bird_percent", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "早期割引率は0以上である必要があります")
    @DecimalMax(value = "100.00", message = "早期割引率は100以下である必要があります")
    private BigDecimal earlyBirdPercent;

    /**
     * 早期割引締切日時
     */
    @Column(name = "early_bird_deadline")
    private LocalDateTime earlyBirdDeadline;

    /**
     * 最低料金額
     */
    @Column(name = "minimum_amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "最低料金額は0以上である必要があります")
    private BigDecimal minimumAmount;

    /**
     * 最高料金額
     */
    @Column(name = "maximum_amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "最高料金額は0以上である必要があります")
    private BigDecimal maximumAmount;

    /**
     * 支払い方法
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    /**
     * 分割払い可能フラグ
     */
    @Column(name = "installment_available")
    @Builder.Default
    private Boolean installmentAvailable = false;

    /**
     * 分割回数
     */
    @Column(name = "installment_count")
    @Min(value = 1, message = "分割回数は1以上である必要があります")
    @Max(value = 36, message = "分割回数は36回以下である必要があります")
    private Integer installmentCount;

    /**
     * 返金可能フラグ
     */
    @Column(name = "refundable")
    @Builder.Default
    private Boolean refundable = false;

    /**
     * 返金期間（日数）
     */
    @Column(name = "refund_period_days")
    @Min(value = 0, message = "返金期間は0以上である必要があります")
    @Max(value = 365, message = "返金期間は365日以下である必要があります")
    private Integer refundPeriodDays;

    /**
     * 特別価格フラグ
     */
    @Column(name = "special_price")
    @Builder.Default
    private Boolean specialPrice = false;

    /**
     * 特別価格説明
     */
    @Column(name = "special_price_description", length = 500)
    @Size(max = 500, message = "特別価格説明は500文字以内で入力してください")
    private String specialPriceDescription;

    /**
     * 税込み価格フラグ
     */
    @Column(name = "tax_included")
    @Builder.Default
    private Boolean taxIncluded = true;

    /**
     * 税率（%）
     */
    @Column(name = "tax_rate", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "税率は0以上である必要があります")
    @DecimalMax(value = "100.00", message = "税率は100以下である必要があります")
    @Builder.Default
    private BigDecimal taxRate = new BigDecimal("10.00");

    /**
     * 備考
     */
    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "備考は1000文字以内で入力してください")
    private String notes;

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
     * 削除日時（論理削除）
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ========== Enum定義 ==========

    /**
     * 料金タイプ
     */
    public enum FeeType {
        FREE("無料"),
        FIXED("固定料金"),
        VARIABLE("変動料金"),
        SUBSCRIPTION("サブスクリプション"),
        ONE_TIME("単発料金"),
        PREMIUM("プレミアム"),
        ENTERPRISE("エンタープライズ"),
        STUDENT("学生料金"),
        BULK("一括料金"),
        CUSTOM("カスタム料金");

        private final String displayName;

        FeeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 通貨タイプ
     */
    public enum CurrencyType {
        JPY("日本円"),
        USD("米ドル"),
        EUR("ユーロ"),
        SFR("SFR暗号資産"),
        BTC("ビットコイン"),
        ETH("イーサリアム"),
        POINT("ポイント");

        private final String displayName;

        CurrencyType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * SFR報酬条件
     */
    public enum SfrRewardCondition {
        COMPLETION("コース完了"),
        ATTENDANCE("出席率"),
        EVALUATION("評価基準"),
        PARTICIPATION("参加度"),
        ACHIEVEMENT("達成度"),
        CERTIFICATION("認定取得"),
        REFERRAL("紹介"),
        FEEDBACK("フィードバック"),
        COMMUNITY("コミュニティ活動"),
        NONE("報酬なし");

        private final String displayName;

        SfrRewardCondition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 支払い方法
     */
    public enum PaymentMethod {
        CREDIT_CARD("クレジットカード"),
        BANK_TRANSFER("銀行振込"),
        PAYPAL("PayPal"),
        CRYPTOCURRENCY("暗号資産"),
        SFR_TOKEN("SFRトークン"),
        POINT("ポイント"),
        CASH("現金"),
        DEBIT_CARD("デビットカード"),
        MOBILE_PAYMENT("モバイル決済"),
        INSTALLMENT("分割払い");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========== ビジネスロジックメソッド ==========

    /**
     * 実効価格を計算（割引適用後）
     */
    public BigDecimal calculateEffectivePrice() {
        if (isFree) {
            return BigDecimal.ZERO;
        }

        BigDecimal effectivePrice = priceAmount;

        // 早期割引の適用
        if (isEarlyBirdDiscountActive()) {
            BigDecimal earlyDiscount = effectivePrice.multiply(earlyBirdPercent).divide(new BigDecimal("100"));
            effectivePrice = effectivePrice.subtract(earlyDiscount);
        }

        // 通常割引の適用
        if (isDiscountActive()) {
            BigDecimal normalDiscount = effectivePrice.multiply(discountPercent).divide(new BigDecimal("100"));
            effectivePrice = effectivePrice.subtract(normalDiscount);
        }

        // 最低料金額の適用
        if (minimumAmount != null && effectivePrice.compareTo(minimumAmount) < 0) {
            effectivePrice = minimumAmount;
        }

        return effectivePrice;
    }

    /**
     * 税込み価格を計算
     */
    public BigDecimal calculateTaxIncludedPrice() {
        BigDecimal effectivePrice = calculateEffectivePrice();

        if (taxIncluded || taxRate == null) {
            return effectivePrice;
        }

        BigDecimal taxAmount = effectivePrice.multiply(taxRate).divide(new BigDecimal("100"));
        return effectivePrice.add(taxAmount);
    }

    /**
     * 分割払い月額を計算
     */
    public BigDecimal calculateInstallmentAmount() {
        if (!installmentAvailable || installmentCount == null || installmentCount <= 1) {
            return calculateTaxIncludedPrice();
        }

        return calculateTaxIncludedPrice().divide(new BigDecimal(installmentCount), 2, RoundingMode.HALF_UP);
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
     * 早期割引が有効かチェック
     */
    public boolean isEarlyBirdDiscountActive() {
        if (!earlyBirdDiscount || earlyBirdPercent == null || earlyBirdDeadline == null) {
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
     * 返金可能期間内かチェック
     */
    public boolean isRefundable() {
        if (!refundable || refundPeriodDays == null) {
            return false;
        }

        LocalDateTime refundDeadline = createdAt.plusDays(refundPeriodDays);
        return LocalDateTime.now().isBefore(refundDeadline);
    }

    /**
     * 割引率を計算（元価格に対する）
     */
    public BigDecimal calculateTotalDiscountPercent() {
        if (isFree || priceAmount.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100.00");
        }

        BigDecimal effectivePrice = calculateEffectivePrice();
        BigDecimal discountAmount = priceAmount.subtract(effectivePrice);

        return discountAmount.multiply(new BigDecimal("100"))
                .divide(priceAmount, 2, RoundingMode.HALF_UP);
    }

    /**
     * 節約金額を計算
     */
    public BigDecimal calculateSavingsAmount() {
        if (isFree) {
            return priceAmount;
        }
        return priceAmount.subtract(calculateEffectivePrice());
    }

    /**
     * 表示用価格文字列を生成
     */
    public String generatePriceDisplay() {
        if (isFree) {
            return "無料";
        }

        BigDecimal price = calculateTaxIncludedPrice();
        String currencySymbol = getCurrencySymbol();

        StringBuilder display = new StringBuilder();
        display.append(currencySymbol).append(price);

        if (installmentAvailable && installmentCount != null && installmentCount > 1) {
            BigDecimal installmentAmount = calculateInstallmentAmount();
            display.append(" (分割: ").append(currencySymbol).append(installmentAmount)
                    .append(" × ").append(installmentCount).append("回)");
        }

        if (hasSfrReward()) {
            display.append(" + SFR ").append(sfrRewardAmount);
        }

        return display.toString();
    }

    /**
     * 通貨記号を取得
     */
    public String getCurrencySymbol() {
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
     * 詳細説明文を生成
     */
    public String generateDetailedDescription() {
        StringBuilder description = new StringBuilder();

        description.append("料金タイプ: ").append(feeType.getDisplayName()).append("\n");
        description.append("価格: ").append(generatePriceDisplay()).append("\n");

        if (isDiscountActive() || isEarlyBirdDiscountActive()) {
            description.append("割引適用中 (").append(calculateTotalDiscountPercent()).append("% OFF)\n");
        }

        if (hasSfrReward()) {
            description.append("SFR報酬: ").append(sfrRewardAmount)
                    .append(" (条件: ").append(sfrRewardCondition.getDisplayName()).append(")\n");
        }

        if (refundable) {
            description.append("返金可能期間: ").append(refundPeriodDays).append("日\n");
        }

        if (notes != null && !notes.trim().isEmpty()) {
            description.append("備考: ").append(notes);
        }

        return description.toString();
    }

    /**
     * 料金比較スコアを計算（コストパフォーマンス指標）
     */
    public BigDecimal calculateValueScore() {
        BigDecimal effectivePrice = calculateEffectivePrice();

        if (effectivePrice.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100.00"); // 無料は最高スコア
        }

        BigDecimal score = new BigDecimal("50.00"); // ベーススコア

        // SFR報酬ボーナス
        if (hasSfrReward()) {
            score = score.add(new BigDecimal("20.00"));
        }

        // 割引ボーナス
        if (isDiscountActive() || isEarlyBirdDiscountActive()) {
            BigDecimal discountBonus = calculateTotalDiscountPercent().multiply(new BigDecimal("0.3"));
            score = score.add(discountBonus);
        }

        // 返金可能ボーナス
        if (refundable) {
            score = score.add(new BigDecimal("10.00"));
        }

        // 分割払い可能ボーナス
        if (installmentAvailable) {
            score = score.add(new BigDecimal("5.00"));
        }

        return score.min(new BigDecimal("100.00"));
    }
}
