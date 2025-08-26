package com.sfr.tokyo.sfr_backend.dto.crypto.reward;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.tokyo.sfr_backend.entity.crypto.reward.ContributionRecord;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 報酬統計DTO
 */
public class RewardStatisticsDto {

    /**
     * ユーザー報酬サマリーDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRewardSummary {
        private UUID userId;
        private String period;
        private RewardSummaryDto summary;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime fromDate;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime toDate;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class RewardSummaryDto {
            private BigDecimal totalEarned;
            private BigDecimal totalDistributed;
            private BigDecimal pending;
            private Map<ContributionRecord.ContributionType, BigDecimal> contributionBreakdown;
            private BigDecimal averageHoldingFactor;
            private int contributionCount;
        }
    }

    /**
     * システム全体統計DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemStatistics {
        private String period;
        private BigDecimal totalCalculated;
        private BigDecimal totalDistributed;
        private BigDecimal pendingDistribution;
        private int participatingUsers;
        private List<ContributionTypeStatistics> topContributionTypes;
        private BigDecimal averageRewardPerUser;
        private MarketMetrics marketMetrics;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ContributionTypeStatistics {
            private ContributionRecord.ContributionType type;
            private BigDecimal amount;
            private BigDecimal percentage;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MarketMetrics {
            private BigDecimal averagePrice;
            private BigDecimal priceVolatility;
            private BigDecimal averageMarketFactor;
        }
    }

    /**
     * 保有インセンティブサマリーDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HoldingIncentiveSummary {
        private UUID userId;
        private BigDecimal holdingFactor;
        private HoldingFactorBreakdown breakdown;
        private HoldingDetails details;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime calculationDate;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class HoldingFactorBreakdown {
            private BigDecimal baseHolding;
            private BigDecimal stakingBonus;
            private BigDecimal paymentUsageBonus;
            private BigDecimal priceSupportBonus;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class HoldingDetails {
            private int holdingDays;
            private int stakingMonths;
            private BigDecimal stakingAmount;
            private int paymentUsageCount;
            private BigDecimal averageHoldingPrice;
            private BigDecimal currentPrice;
            private BigDecimal priceSupportRatio;
        }
    }

    /**
     * 報酬係数サマリーDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RewardFactorSummary {
        private Map<ContributionRecord.ContributionType, BigDecimal> baseFactors;
        private MarketFactorInfo marketFactor;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime effectiveFrom;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdated;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MarketFactorInfo {
            private BigDecimal currentPrice;
            private BigDecimal targetPrice;
            private BigDecimal factor;
        }
    }

    /**
     * 報酬制限情報DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RewardLimitInfo {
        private String limitType;
        private String category;
        private UUID userId;
        private BigDecimal maxAmount;
        private BigDecimal currentAmount;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime periodStart;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime periodEnd;

        private boolean isActive;

        public BigDecimal getRemainingAmount() {
            if (maxAmount != null && currentAmount != null) {
                return maxAmount.subtract(currentAmount);
            }
            return null;
        }
    }

    /**
     * 市場価格更新リクエストDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarketPriceUpdateRequest {
        private BigDecimal priceJpy;
        private String priceSource;
        private BigDecimal volume24h;
        private BigDecimal marketCap;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime priceTimestamp;
    }

    /**
     * 報酬配布制限設定リクエストDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RewardLimitSetRequest {
        private String limitType; // DAILY, WEEKLY, MONTHLY, CATEGORY, USER
        private String category;
        private UUID userId;
        private BigDecimal maxAmount;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime periodStart;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime periodEnd;
    }

    /**
     * 期間別統計検索条件DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticsPeriod {
        private String period; // daily, weekly, monthly
        private UUID userId;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime fromDate;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime toDate;
    }
}
