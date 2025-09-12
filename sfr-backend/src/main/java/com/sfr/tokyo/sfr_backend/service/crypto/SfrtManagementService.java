package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.repository.crypto.SfrtBalanceRepository;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrtTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * SFRT管理システムサービス
 * Phase 3.3: SFRT総供給量・価格指標・統計管理
 * 
 * 主要機能:
 * - SFRT総供給量・流通量管理
 * - 価格指標・メトリクス計算
 * - プラットフォーム統計・分析
 * - 経済圏健全性監視
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Service
@Slf4j
public class SfrtManagementService {

    private static final BigDecimal SFR_FIXED_RATE = new BigDecimal("150"); // 1 SFR = 150円
    private static final String PLATFORM_USER_ID = "PLATFORM";

    @Autowired
    private SfrtBalanceRepository sfrtBalanceRepository;

    @Autowired
    private SfrtTransactionRepository sfrtTransactionRepository;

    /**
     * SFRT総供給量情報取得
     */
    public SfrtSupplyInfo getSfrtSupplyInfo() {
        BigDecimal totalSupply = sfrtBalanceRepository.getTotalSfrtSupply();
        BigDecimal platformReserve = sfrtBalanceRepository.findByUserId(PLATFORM_USER_ID)
            .map(balance -> balance.getBalance())
            .orElse(BigDecimal.ZERO);
        BigDecimal circulatingSupply = sfrtBalanceRepository.getCirculatingSfrtSupply(PLATFORM_USER_ID);
        Long activeHolders = sfrtBalanceRepository.getActiveHolderCount();
        BigDecimal averageBalance = sfrtBalanceRepository.getAverageSfrtBalance();

        return SfrtSupplyInfo.builder()
            .totalSupply(totalSupply)
            .circulatingSupply(circulatingSupply)
            .platformReserve(platformReserve)
            .activeHolders(activeHolders)
            .averageBalance(averageBalance != null ? averageBalance : BigDecimal.ZERO)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    /**
     * SFRT価格指標計算
     */
    public SfrtPriceMetrics calculatePriceMetrics() {
        SfrtSupplyInfo supplyInfo = getSfrtSupplyInfo();
        
        // 期間別報酬配布統計
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime lastWeek = now.minusDays(7);
        LocalDateTime lastMonth = now.minusDays(30);

        BigDecimal totalRewards24h = sfrtTransactionRepository.getTotalRewardsInPeriod(yesterday, now);
        BigDecimal totalRewards7d = sfrtTransactionRepository.getTotalRewardsInPeriod(lastWeek, now);
        BigDecimal totalRewards30d = sfrtTransactionRepository.getTotalRewardsInPeriod(lastMonth, now);

        // 基準価格計算（簡易モデル）
        BigDecimal basePrice = calculateBasePrice(supplyInfo, totalRewards30d);
        
        // 成長率計算
        BigDecimal growthRate24h = calculateGrowthRate(totalRewards24h, BigDecimal.ONE);
        BigDecimal growthRate7d = calculateGrowthRate(totalRewards7d, new BigDecimal("7"));
        BigDecimal growthRate30d = calculateGrowthRate(totalRewards30d, new BigDecimal("30"));

        return SfrtPriceMetrics.builder()
            .basePrice(basePrice)
            .totalRewards24h(totalRewards24h)
            .totalRewards7d(totalRewards7d)
            .totalRewards30d(totalRewards30d)
            .growthRate24h(growthRate24h)
            .growthRate7d(growthRate7d)
            .growthRate30d(growthRate30d)
            .circulatingSupply(supplyInfo.getCirculatingSupply())
            .platformReserve(supplyInfo.getPlatformReserve())
            .lastCalculated(LocalDateTime.now())
            .build();
    }

    /**
     * 基準価格計算
     */
    private BigDecimal calculateBasePrice(SfrtSupplyInfo supplyInfo, BigDecimal totalRewards30d) {
        // 基準価格 = (月間報酬価値 * 12) / 流通供給量
        // 月間報酬価値 = 月間報酬SFRT * SFR固定価格
        
        if (supplyInfo.getCirculatingSupply().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE; // デフォルト価格 1円
        }

        BigDecimal monthlyRewardValue = totalRewards30d.multiply(SFR_FIXED_RATE);
        BigDecimal annualizedValue = monthlyRewardValue.multiply(new BigDecimal("12"));
        
        return annualizedValue.divide(supplyInfo.getCirculatingSupply(), 2, RoundingMode.HALF_UP);
    }

    /**
     * 成長率計算
     */
    private BigDecimal calculateGrowthRate(BigDecimal rewardAmount, BigDecimal periodDays) {
        // 簡易成長率 = (期間内報酬 / 期間日数) * 365 / 総供給量 * 100
        BigDecimal totalSupply = sfrtBalanceRepository.getTotalSfrtSupply();
        
        if (totalSupply.compareTo(BigDecimal.ZERO) == 0 || periodDays.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyReward = rewardAmount.divide(periodDays, 8, RoundingMode.HALF_UP);
        BigDecimal annualizedReward = dailyReward.multiply(new BigDecimal("365"));
        
        return annualizedReward.divide(totalSupply, 4, RoundingMode.HALF_UP)
                              .multiply(new BigDecimal("100"));
    }

    /**
     * SFRT保有者分析
     */
    public SfrtHolderAnalysis analyzeSfrtHolders() {
        // 残高分布統計
        var distributionData = sfrtBalanceRepository.getSfrtBalanceDistribution();
        
        // 上位保有者（例：残高上位100名）
        var topHolders = sfrtBalanceRepository.findUsersWithMinimumBalance(new BigDecimal("1000"));
        
        // 非アクティブユーザー（90日間残高変動なし）
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(90);
        var inactiveUsers = sfrtBalanceRepository.findInactiveUsers(thresholdDate);

        return SfrtHolderAnalysis.builder()
            .totalHolders(sfrtBalanceRepository.getActiveHolderCount())
            .distributionData(distributionData)
            .topHoldersCount((long) topHolders.size())
            .inactiveUsersCount((long) inactiveUsers.size())
            .averageBalance(sfrtBalanceRepository.getAverageSfrtBalance())
            .lastAnalyzed(LocalDateTime.now())
            .build();
    }

    /**
     * プラットフォーム経済指標
     */
    public PlatformEconomicMetrics calculateEconomicMetrics() {
        SfrtSupplyInfo supplyInfo = getSfrtSupplyInfo();
        SfrtPriceMetrics priceMetrics = calculatePriceMetrics();
        
        // プラットフォーム収益率
        BigDecimal platformRevenueRate = supplyInfo.getPlatformReserve()
            .divide(supplyInfo.getTotalSupply(), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));

        // 健全性指標
        HealthIndicator healthIndicator = calculateHealthIndicator(supplyInfo, priceMetrics);

        return PlatformEconomicMetrics.builder()
            .totalMarketValue(supplyInfo.getTotalSupply().multiply(priceMetrics.getBasePrice()))
            .platformRevenueRate(platformRevenueRate)
            .userParticipationRate(calculateUserParticipationRate())
            .rewardDistributionEfficiency(calculateRewardEfficiency())
            .healthIndicator(healthIndicator)
            .lastCalculated(LocalDateTime.now())
            .build();
    }

    /**
     * ユーザー参加率計算
     */
    private BigDecimal calculateUserParticipationRate() {
        // 簡易実装：アクティブホルダー数 / 全登録ユーザー数の仮想値
        Long activeHolders = sfrtBalanceRepository.getActiveHolderCount();
        // TODO: 実際のユーザー総数を取得する実装が必要
        Long estimatedTotalUsers = activeHolders * 3; // 仮の値
        
        return new BigDecimal(activeHolders)
            .divide(new BigDecimal(estimatedTotalUsers), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }

    /**
     * 報酬配布効率計算
     */
    private BigDecimal calculateRewardEfficiency() {
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();
        
        BigDecimal totalRewards = sfrtTransactionRepository.getTotalRewardsInPeriod(lastMonth, now);
        BigDecimal totalSupply = sfrtBalanceRepository.getTotalSfrtSupply();
        
        if (totalSupply.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalRewards.divide(totalSupply, 4, RoundingMode.HALF_UP)
                          .multiply(new BigDecimal("100"));
    }

    /**
     * システム健全性指標計算
     */
    private HealthIndicator calculateHealthIndicator(SfrtSupplyInfo supplyInfo, SfrtPriceMetrics priceMetrics) {
        // 複数指標の総合評価
        int score = 0;
        
        // 1. 流通性（流通供給量 vs プラットフォーム保有量）
        if (supplyInfo.getCirculatingSupply().compareTo(supplyInfo.getPlatformReserve()) > 0) {
            score += 25;
        }
        
        // 2. 成長性（30日成長率）
        if (priceMetrics.getGrowthRate30d().compareTo(BigDecimal.ZERO) > 0) {
            score += 25;
        }
        
        // 3. 参加度（アクティブホルダー数）
        if (supplyInfo.getActiveHolders() > 10) {
            score += 25;
        }
        
        // 4. 安定性（平均残高）
        if (supplyInfo.getAverageBalance().compareTo(new BigDecimal("100")) > 0) {
            score += 25;
        }
        
        return HealthIndicator.fromScore(score);
    }

    // ===== 結果クラス定義 =====

    @lombok.Data
    @lombok.Builder
    public static class SfrtSupplyInfo {
        private BigDecimal totalSupply;
        private BigDecimal circulatingSupply;
        private BigDecimal platformReserve;
        private Long activeHolders;
        private BigDecimal averageBalance;
        private LocalDateTime lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    public static class SfrtPriceMetrics {
        private BigDecimal basePrice;
        private BigDecimal totalRewards24h;
        private BigDecimal totalRewards7d;
        private BigDecimal totalRewards30d;
        private BigDecimal growthRate24h;
        private BigDecimal growthRate7d;
        private BigDecimal growthRate30d;
        private BigDecimal circulatingSupply;
        private BigDecimal platformReserve;
        private LocalDateTime lastCalculated;
    }

    @lombok.Data
    @lombok.Builder
    public static class SfrtHolderAnalysis {
        private Long totalHolders;
        private java.util.List<Object[]> distributionData;
        private Long topHoldersCount;
        private Long inactiveUsersCount;
        private BigDecimal averageBalance;
        private LocalDateTime lastAnalyzed;
    }

    @lombok.Data
    @lombok.Builder
    public static class PlatformEconomicMetrics {
        private BigDecimal totalMarketValue;
        private BigDecimal platformRevenueRate;
        private BigDecimal userParticipationRate;
        private BigDecimal rewardDistributionEfficiency;
        private HealthIndicator healthIndicator;
        private LocalDateTime lastCalculated;
    }

    public enum HealthIndicator {
        EXCELLENT(80, 100, "優秀"),
        GOOD(60, 79, "良好"),
        FAIR(40, 59, "普通"),
        POOR(20, 39, "注意"),
        CRITICAL(0, 19, "危険");

        private final int minScore;
        private final int maxScore;
        private final String displayName;

        HealthIndicator(int minScore, int maxScore, String displayName) {
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.displayName = displayName;
        }

        public static HealthIndicator fromScore(int score) {
            for (HealthIndicator indicator : values()) {
                if (score >= indicator.minScore && score <= indicator.maxScore) {
                    return indicator;
                }
            }
            return CRITICAL;
        }

        public String getDisplayName() { return displayName; }
        public int getMinScore() { return minScore; }
        public int getMaxScore() { return maxScore; }
    }
}
