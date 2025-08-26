package com.sfr.tokyo.sfr_backend.entity.crypto.reward;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 報酬計算履歴エンティティ
 * 実際の報酬計算結果を記録
 */
@Entity
@Table(name = "reward_calculations", indexes = {
        @Index(name = "idx_reward_calc_user_status", columnList = "user_id, status"),
        @Index(name = "idx_reward_calc_contribution", columnList = "contribution_record_id"),
        @Index(name = "idx_reward_calc_date", columnList = "calculated_at"),
        @Index(name = "idx_reward_calc_distribution", columnList = "distribution_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @Column(name = "contribution_record_id", nullable = false)
    @NotNull(message = "貢献記録IDは必須です")
    private Long contributionRecordId;

    @Column(name = "base_factor", nullable = false, precision = 6, scale = 4)
    @NotNull(message = "基本報酬係数は必須です")
    @DecimalMin(value = "0.0001", message = "基本報酬係数は0.0001以上である必要があります")
    private BigDecimal baseFactor;

    @Column(name = "contribution_score", nullable = false, precision = 10, scale = 4)
    @NotNull(message = "貢献度スコアは必須です")
    @DecimalMin(value = "0.0001", message = "貢献度スコアは0.0001以上である必要があります")
    private BigDecimal contributionScore;

    @Column(name = "market_factor", nullable = false, precision = 6, scale = 4)
    @NotNull(message = "市場状況係数は必須です")
    @DecimalMin(value = "0.0001", message = "市場状況係数は0.0001以上である必要があります")
    private BigDecimal marketFactor;

    @Column(name = "holding_factor", nullable = false, precision = 10, scale = 6)
    @NotNull(message = "保有インセンティブ係数は必須です")
    @DecimalMin(value = "0.000001", message = "保有インセンティブ係数は0.000001以上である必要があります")
    private BigDecimal holdingFactor;

    @Column(name = "calculated_amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "計算された報酬量は必須です")
    @DecimalMin(value = "0.00000001", message = "計算された報酬量は0.00000001以上である必要があります")
    private BigDecimal calculatedAmount;

    @Column(name = "final_amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "最終配布量は必須です")
    @DecimalMin(value = "0.00000001", message = "最終配布量は0.00000001以上である必要があります")
    private BigDecimal finalAmount;

    @Column(name = "calculation_formula", columnDefinition = "TEXT")
    private String calculationFormula;

    @Column(name = "market_price_jpy", precision = 10, scale = 2)
    @DecimalMin(value = "0.01", message = "市場価格は0.01以上である必要があります")
    private BigDecimal marketPriceJpy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private CalculationStatus status = CalculationStatus.CALCULATED;

    @Column(name = "distribution_id")
    private Long distributionId;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "distributed_at")
    private LocalDateTime distributedAt;

    @Column(name = "calculated_at", nullable = false)
    @NotNull(message = "計算日時は必須です")
    private LocalDateTime calculatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 計算ステータスの列挙型
     */
    public enum CalculationStatus {
        CALCULATED("calculated", "計算完了"),
        APPROVED("approved", "承認済み"),
        DISTRIBUTED("distributed", "配布完了"),
        REJECTED("rejected", "却下");

        private final String code;
        private final String displayName;

        CalculationStatus(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static CalculationStatus fromCode(String code) {
            for (CalculationStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("不正な計算ステータスコード: " + code);
        }
    }

    /**
     * 承認処理を実行
     *
     * @param approver 承認者ID
     */
    public void approve(UUID approver) {
        if (this.status != CalculationStatus.CALCULATED) {
            throw new IllegalStateException("計算完了状態でないものは承認できません");
        }
        this.status = CalculationStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 却下処理を実行
     *
     * @param rejector 却下者ID
     */
    public void reject(UUID rejector) {
        if (this.status == CalculationStatus.DISTRIBUTED) {
            throw new IllegalStateException("配布完了済みのものは却下できません");
        }
        this.status = CalculationStatus.REJECTED;
        this.approvedBy = rejector;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 配布完了処理を実行
     *
     * @param distributionId 配布記録ID
     */
    public void markAsDistributed(Long distributionId) {
        if (this.status != CalculationStatus.APPROVED) {
            throw new IllegalStateException("承認済み状態でないものは配布完了にできません");
        }
        this.status = CalculationStatus.DISTRIBUTED;
        this.distributionId = distributionId;
        this.distributedAt = LocalDateTime.now();
    }

    /**
     * 計算式を生成
     *
     * @return 計算式の文字列
     */
    public String generateCalculationFormula() {
        return String.format("%.4f × %.4f × %.4f × %.6f = %.8f SFR",
                baseFactor, contributionScore, marketFactor, holdingFactor, calculatedAmount);
    }

    /**
     * 計算式の設定（自動生成）
     */
    @PrePersist
    @PreUpdate
    private void setCalculationFormula() {
        if (this.calculationFormula == null || this.calculationFormula.isEmpty()) {
            this.calculationFormula = generateCalculationFormula();
        }
    }
}
