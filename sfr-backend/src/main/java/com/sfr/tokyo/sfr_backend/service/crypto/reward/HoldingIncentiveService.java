package com.sfr.tokyo.sfr_backend.service.crypto.reward;

import com.sfr.tokyo.sfr_backend.entity.crypto.reward.HoldingIncentive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * 保有インセンティブサービス
 * H係数（保有インセンティブ係数）の管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HoldingIncentiveService {

    // TODO: HoldingIncentiveRepositoryを作成後に追加
    // private final HoldingIncentiveRepository holdingIncentiveRepository;
    // private final UserBalanceService userBalanceService;

    /**
     * 指定されたユーザーの保有インセンティブ係数を取得
     *
     * @param userId ユーザーID
     * @param targetDate 対象日時
     * @return H係数
     */
    public BigDecimal getHoldingFactor(UUID userId, LocalDateTime targetDate) {
        log.debug("H係数取得: userId={}, targetDate={}", userId, targetDate);

        // TODO: データベースから最新の保有情報を取得
        // 現在は仮の計算を返す
        HoldingIncentive holdingIncentive = calculateHoldingIncentive(userId, targetDate);
        
        return holdingIncentive.getHoldingFactor();
    }

    /**
     * 保有インセンティブを計算
     *
     * @param userId ユーザーID
     * @param calculationDate 計算日時
     * @return 計算された保有インセンティブ
     */
    @Transactional
    public HoldingIncentive calculateHoldingIncentive(UUID userId, LocalDateTime calculationDate) {
        log.info("保有インセンティブ計算開始: userId={}, calculationDate={}", userId, calculationDate);

        // TODO: 実際のユーザー残高情報から取得
        // 現在は仮の値で計算
        LocalDateTime holdingStartDate = calculationDate.minusDays(365); // 1年前から保有と仮定
        int holdingDays = (int) ChronoUnit.DAYS.between(holdingStartDate, calculationDate);
        
        // 仮の保有情報
        BigDecimal averageHoldingPrice = new BigDecimal("140.00");
        BigDecimal currentPrice = new BigDecimal("145.50");
        int stakingMonths = 6;
        BigDecimal stakingAmount = new BigDecimal("1000.00");
        int paymentUsageCount = 25;
        BigDecimal paymentUsageAmount = new BigDecimal("500.00");

        // H係数の計算
        BigDecimal holdingFactor = calculateHoldingFactorFormula(
                holdingDays, averageHoldingPrice, currentPrice,
                stakingMonths, paymentUsageCount);

        HoldingIncentive incentive = HoldingIncentive.builder()
                .userId(userId)
                .holdingStartDate(holdingStartDate)
                .holdingDays(holdingDays)
                .averageHoldingPrice(averageHoldingPrice)
                .currentPrice(currentPrice)
                .stakingMonths(stakingMonths)
                .stakingAmount(stakingAmount)
                .paymentUsageCount(paymentUsageCount)
                .paymentUsageAmount(paymentUsageAmount)
                .holdingFactor(holdingFactor)
                .calculationDate(calculationDate)
                .build();

        // 価格支持力比率を計算
        incentive.calculatePriceSupportRatio();

        // TODO: リポジトリで保存
        // return holdingIncentiveRepository.save(incentive);

        log.info("保有インセンティブ計算完了: userId={}, holdingFactor={}", userId, holdingFactor);
        return incentive;
    }

    /**
     * H係数の計算式を実行
     *
     * @param holdingDays 保有日数
     * @param averageHoldingPrice 平均保有時価格
     * @param currentPrice 現在価格
     * @param stakingMonths ステーキング期間（月）
     * @param paymentUsageCount SFR決済利用回数
     * @return 計算されたH係数
     */
    private BigDecimal calculateHoldingFactorFormula(
            int holdingDays,
            BigDecimal averageHoldingPrice,
            BigDecimal currentPrice,
            int stakingMonths,
            int paymentUsageCount) {

        // H = 1.0 + α × log₁₀(保有日数) + β × (平均保有時価格 ÷ 現在価格) + ステーキングボーナス + 決済利用ボーナス
        
        BigDecimal alpha = new BigDecimal("0.05");
        BigDecimal beta = new BigDecimal("0.2");
        
        // 基本係数
        BigDecimal baseFactor = BigDecimal.ONE;
        
        // 保有期間ボーナス: α × log₁₀(保有日数)
        BigDecimal holdingBonus = BigDecimal.ZERO;
        if (holdingDays > 0) {
            double logHoldingDays = Math.log10(holdingDays);
            holdingBonus = alpha.multiply(new BigDecimal(logHoldingDays));
        }
        
        // 価格支持ボーナス: β × (平均保有時価格 ÷ 現在価格)
        BigDecimal priceSupportBonus = BigDecimal.ZERO;
        if (averageHoldingPrice != null && currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceRatio = averageHoldingPrice.divide(currentPrice, 6, RoundingMode.HALF_UP);
            priceSupportBonus = beta.multiply(priceRatio);
        }
        
        // ステーキングボーナス
        BigDecimal stakingBonus = BigDecimal.ZERO;
        if (stakingMonths >= 3) {
            stakingBonus = new BigDecimal("0.1");
        }
        if (stakingMonths >= 6) {
            stakingBonus = new BigDecimal("0.3");
        }
        
        // 決済利用ボーナス
        BigDecimal paymentBonus = BigDecimal.ZERO;
        if (paymentUsageCount > 0) {
            paymentBonus = new BigDecimal("0.1");
        }
        
        // 最終H係数
        BigDecimal totalFactor = baseFactor
                .add(holdingBonus)
                .add(priceSupportBonus)
                .add(stakingBonus)
                .add(paymentBonus);
        
        // 最小値・最大値の制限
        BigDecimal minFactor = new BigDecimal("0.1");
        BigDecimal maxFactor = new BigDecimal("10.0");
        
        if (totalFactor.compareTo(minFactor) < 0) {
            return minFactor;
        }
        if (totalFactor.compareTo(maxFactor) > 0) {
            return maxFactor;
        }
        
        return totalFactor.setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * ユーザーの保有インセンティブ履歴を取得
     *
     * @param userId ユーザーID
     * @param fromDate 開始日
     * @param toDate 終了日
     * @return 保有インセンティブのリスト
     */
    public java.util.List<HoldingIncentive> getHoldingIncentiveHistory(
            UUID userId, LocalDateTime fromDate, LocalDateTime toDate) {
        
        log.debug("保有インセンティブ履歴取得: userId={}, fromDate={}, toDate={}", userId, fromDate, toDate);
        
        // TODO: リポジトリから履歴を取得
        // return holdingIncentiveRepository.findByUserIdAndCalculationDateBetween(userId, fromDate, toDate);
        
        return java.util.Collections.emptyList();
    }

    /**
     * ユーザーの保有インセンティブサマリーを取得
     *
     * @param userId ユーザーID
     * @return 保有インセンティブサマリー
     */
    public HoldingIncentiveSummary getHoldingIncentiveSummary(UUID userId) {
        HoldingIncentive latest = calculateHoldingIncentive(userId, LocalDateTime.now());
        HoldingIncentive.HoldingFactorBreakdown breakdown = latest.calculateBreakdown();
        
        return HoldingIncentiveSummary.builder()
                .userId(userId)
                .holdingFactor(latest.getHoldingFactor())
                .breakdown(breakdown)
                .holdingDays(latest.getHoldingDays())
                .stakingMonths(latest.getStakingMonths())
                .stakingAmount(latest.getStakingAmount())
                .paymentUsageCount(latest.getPaymentUsageCount())
                .averageHoldingPrice(latest.getAverageHoldingPrice())
                .currentPrice(latest.getCurrentPrice())
                .priceSupportRatio(latest.getPriceSupportRatio())
                .calculationDate(latest.getCalculationDate())
                .build();
    }

    /**
     * 保有インセンティブサマリークラス
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HoldingIncentiveSummary {
        private UUID userId;
        private BigDecimal holdingFactor;
        private HoldingIncentive.HoldingFactorBreakdown breakdown;
        private int holdingDays;
        private int stakingMonths;
        private BigDecimal stakingAmount;
        private int paymentUsageCount;
        private BigDecimal averageHoldingPrice;
        private BigDecimal currentPrice;
        private BigDecimal priceSupportRatio;
        private LocalDateTime calculationDate;
    }
}
