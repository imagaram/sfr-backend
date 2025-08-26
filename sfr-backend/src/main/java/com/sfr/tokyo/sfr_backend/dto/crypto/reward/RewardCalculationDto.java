package com.sfr.tokyo.sfr_backend.dto.crypto.reward;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.tokyo.sfr_backend.entity.crypto.reward.RewardCalculation;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 報酬計算DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardCalculationDto {

    private Long id;
    private UUID userId;
    private Long contributionRecordId;
    private RewardFactorsDto factors;
    private BigDecimal calculatedAmount;
    private BigDecimal finalAmount;
    private String calculationFormula;
    private BigDecimal marketPriceJpy;
    private RewardCalculation.CalculationStatus status;
    private Long distributionId;
    private UUID approvedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime distributedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculatedAt;

    /**
     * 報酬係数DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RewardFactorsDto {
        private BigDecimal baseFactor;
        private BigDecimal contributionScore;
        private BigDecimal marketFactor;
        private BigDecimal holdingFactor;
    }

    /**
     * 報酬計算リクエストDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalculateRequest {
        private Long contributionRecordId;
        @Builder.Default
        private boolean forceRecalculate = false;
    }

    /**
     * バッチ報酬計算リクエストDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchCalculateRequest {
        private List<Long> contributionRecordIds;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime calculationDate;
    }

    /**
     * バッチ報酬計算レスポンスDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchCalculateResponse {
        private String batchId;
        private int processedCount;
        private int successCount;
        private int failureCount;
        private BigDecimal totalCalculatedAmount;
        private List<BatchResultDto> results;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class BatchResultDto {
            private Long contributionRecordId;
            private Long calculationId;
            private BigDecimal finalAmount;
            private RewardCalculation.CalculationStatus status;
            private String errorMessage;
        }
    }

    /**
     * 報酬承認リクエストDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApprovalRequest {
        private List<Long> calculationIds;
        private String approvalComment;
    }

    /**
     * 報酬承認レスポンスDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApprovalResponse {
        private int approvedCount;
        private BigDecimal totalApprovedAmount;
        private List<Long> distributionIds;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime approvedAt;

        private UUID approvedBy;
    }

    /**
     * 報酬配布リクエストDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistributionRequest {
        private List<Long> distributionIds;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime executionDate;
    }
}
