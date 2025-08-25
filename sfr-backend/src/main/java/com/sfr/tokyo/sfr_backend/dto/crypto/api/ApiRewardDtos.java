package com.sfr.tokyo.sfr_backend.dto.crypto.api;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

public class ApiRewardDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardIssueRequestDto {
        @NotBlank
        private String user_id;
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        private Double activity_score;
        @DecimalMin("1.0")
        @DecimalMax("5.0")
        private Double evaluation_score;
        @NotBlank
        private String reward_reason;
        private Boolean force_issue = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardIssueResponseDto {
        @NotBlank
        private String reward_id;
        @NotBlank
        private String user_id;
        @NotBlank
        private String reward_amount; // 8桁小数
        @NotNull
        private LocalDate pool_date;
        @NotNull
        private Double combined_score;
        @NotNull
        private Double total_pool_score;
        @Schema(description = "計算詳細: 重み範囲(0.0-1.0), 丸めモード(TRUNCATE_DOWN), フォーミュラバージョン(enum: v1)")
        private RewardCalculationDetails calculation_details;
        @NotNull
        private LocalDateTime issued_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardCalculateRequestDto {
        @NotBlank
        private String user_id;
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        private Double activity_score;
        @DecimalMin("1.0")
        @DecimalMax("5.0")
        private Double evaluation_score;
        private LocalDate target_date; // nullable
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardCalculateResponseDto {
        @NotBlank
        private String user_id;
        @NotBlank
        private String estimated_reward; // 8桁小数
        @NotNull
        private Double combined_score;
        @NotBlank
        private String current_pool_total; // 8桁小数
        @NotBlank
        private String current_pool_remaining; // 8桁小数
        @NotNull
        private LocalDateTime calculated_at;
        @Schema(description = "計算詳細: 重み範囲(0.0-1.0), 丸めモード(TRUNCATE_DOWN), フォーミュラバージョン(enum: v1)")
        private RewardCalculationDetails calculation_details;
    }

    // ================= Calculation Detail Structured Schemas =================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardCalculationWeights {
        @Schema(description = "活動スコア重み (0.0〜1.0) ※ activity + evaluation = 1.0", minimum = "0.0", maximum = "1.0", example = "0.6")
        private Double activity;
        @Schema(description = "評価スコア重み (0.0〜1.0) ※ activity + evaluation = 1.0", minimum = "0.0", maximum = "1.0", example = "0.4")
        private Double evaluation;
        @Schema(description = "重み下限固定値", example = "0.0")
        private Double range_min;
        @Schema(description = "重み上限固定値", example = "1.0")
        private Double range_max;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardCalculationDivisors {
        @Schema(description = "推定報酬計算除数", example = "1000")
        private Integer calculate;
        @Schema(description = "発行報酬計算除数", example = "500")
        private Integer issue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardCalculationDetails {
        @Schema(description = "入力活動スコア")
        private Double activity_score;
        @Schema(description = "入力評価スコア")
        private Double evaluation_score;
        @Schema(description = "評価スコア正規化後 (evaluation_score * SCALE)")
        private Double normalized_evaluation_score;
        @Schema(description = "重み情報 (activity + evaluation = 1.0)")
        private RewardCalculationWeights weights;
        @Schema(description = "除数情報")
        private RewardCalculationDivisors divisors;
        @Schema(description = "除数適用前の合成値")
        private Double combined_before_divisor;
        @Schema(description = "丸めモード enum", allowableValues = { "TRUNCATE_DOWN" })
        private String rounding_mode;
        @Schema(description = "フォーミュラバージョン enum", allowableValues = { "v1" })
        private String formula_version;
        @Schema(description = "実行時利用した式 (発行APIのみ)", example = "(act*0.5 + eval*20.0*0.5)/500")
        private String formula; // issue のみ設定
    }
}
