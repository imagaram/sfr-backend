package com.sfr.tokyo.sfr_backend.service.crypto.reward;

import com.sfr.tokyo.sfr_backend.entity.crypto.reward.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 報酬計算サービス
 * SFR報酬量 = B × C × M × H の計算を実装
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RewardCalculationService {

    private final RewardFactorService rewardFactorService;
    private final HoldingIncentiveService holdingIncentiveService;
    private final MarketPriceService marketPriceService;

    /**
     * 貢献記録に基づいて報酬を計算
     *
     * @param contributionRecord 貢献記録
     * @return 計算された報酬
     */
    public RewardCalculation calculateReward(ContributionRecord contributionRecord) {
        log.info("報酬計算開始: contributionRecordId={}", contributionRecord.getId());

        try {
            // B係数（基本報酬係数）を取得
            BigDecimal baseFactor = rewardFactorService.getBaseFactor(
                    contributionRecord.getContributionType(),
                    contributionRecord.getActivityDate()
            );

            // C係数（貢献度スコア）を取得
            BigDecimal contributionScore = contributionRecord.getContributionScore();

            // M係数（市場状況係数）を取得
            BigDecimal marketFactor = marketPriceService.getCurrentMarketFactor();

            // H係数（保有インセンティブ係数）を取得
            BigDecimal holdingFactor = holdingIncentiveService.getHoldingFactor(
                    contributionRecord.getUserId(),
                    contributionRecord.getActivityDate()
            );

            // 報酬量を計算: B × C × M × H
            BigDecimal calculatedAmount = baseFactor
                    .multiply(contributionScore)
                    .multiply(marketFactor)
                    .multiply(holdingFactor)
                    .setScale(8, RoundingMode.HALF_UP);

            // 現在の市場価格を取得
            BigDecimal currentPrice = marketPriceService.getCurrentPrice();

            // 計算結果を保存
            RewardCalculation calculation = RewardCalculation.builder()
                    .userId(contributionRecord.getUserId())
                    .contributionRecordId(contributionRecord.getId())
                    .baseFactor(baseFactor)
                    .contributionScore(contributionScore)
                    .marketFactor(marketFactor)
                    .holdingFactor(holdingFactor)
                    .calculatedAmount(calculatedAmount)
                    .finalAmount(calculatedAmount) // 初期値は計算値と同じ
                    .marketPriceJpy(currentPrice)
                    .calculatedAt(LocalDateTime.now())
                    .build();

            log.info("報酬計算完了: amount={}, formula={}", 
                    calculatedAmount, calculation.generateCalculationFormula());

            return calculation;

        } catch (Exception e) {
            log.error("報酬計算エラー: contributionRecordId={}", contributionRecord.getId(), e);
            throw new RewardCalculationException("報酬計算に失敗しました", e);
        }
    }

    /**
     * 複数の貢献記録に対してバッチ計算を実行
     *
     * @param contributionRecords 貢献記録のリスト
     * @return 計算結果のリスト
     */
    public java.util.List<RewardCalculation> calculateBatchRewards(
            java.util.List<ContributionRecord> contributionRecords) {
        
        log.info("バッチ報酬計算開始: 件数={}", contributionRecords.size());

        return contributionRecords.stream()
                .map(this::calculateReward)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 報酬計算を再実行
     *
     * @param existingCalculation 既存の計算結果
     * @param contributionRecord 貢献記録
     * @return 再計算された報酬
     */
    public RewardCalculation recalculateReward(
            RewardCalculation existingCalculation,
            ContributionRecord contributionRecord) {
        
        log.info("報酬再計算開始: calculationId={}", existingCalculation.getId());

        if (existingCalculation.getStatus() == RewardCalculation.CalculationStatus.DISTRIBUTED) {
            throw new IllegalStateException("配布完了済みの報酬は再計算できません");
        }

        RewardCalculation newCalculation = calculateReward(contributionRecord);
        
        // 既存の承認情報を保持
        newCalculation.setId(existingCalculation.getId());
        if (existingCalculation.getApprovedBy() != null) {
            newCalculation.setApprovedBy(existingCalculation.getApprovedBy());
            newCalculation.setApprovedAt(existingCalculation.getApprovedAt());
            newCalculation.setStatus(RewardCalculation.CalculationStatus.APPROVED);
        }

        return newCalculation;
    }

    /**
     * 報酬計算の妥当性をチェック
     *
     * @param calculation 計算結果
     * @return 妥当性チェック結果
     */
    public RewardValidationResult validateCalculation(RewardCalculation calculation) {
        RewardValidationResult.RewardValidationResultBuilder resultBuilder = 
                RewardValidationResult.builder().valid(true);

        // 最大報酬量チェック
        BigDecimal maxRewardPerContribution = new BigDecimal("1000.00000000");
        if (calculation.getCalculatedAmount().compareTo(maxRewardPerContribution) > 0) {
            resultBuilder.valid(false);
            RewardValidationResult temp = resultBuilder.build();
            temp.addError("計算された報酬量が上限を超えています: " + calculation.getCalculatedAmount());
            return temp;
        }

        // 最小報酬量チェック
        BigDecimal minRewardPerContribution = new BigDecimal("0.00000001");
        if (calculation.getCalculatedAmount().compareTo(minRewardPerContribution) < 0) {
            resultBuilder.valid(false);
            RewardValidationResult temp = resultBuilder.build();
            temp.addError("計算された報酬量が最小値を下回っています: " + calculation.getCalculatedAmount());
            return temp;
        }

        // 係数の妥当性チェック
        if (calculation.getBaseFactor().compareTo(BigDecimal.ZERO) <= 0) {
            resultBuilder.valid(false);
            RewardValidationResult temp = resultBuilder.build();
            temp.addError("基本報酬係数が無効です: " + calculation.getBaseFactor());
            return temp;
        }

        if (calculation.getMarketFactor().compareTo(BigDecimal.ZERO) <= 0) {
            resultBuilder.valid(false);
            RewardValidationResult temp = resultBuilder.build();
            temp.addError("市場状況係数が無効です: " + calculation.getMarketFactor());
            return temp;
        }

        if (calculation.getHoldingFactor().compareTo(BigDecimal.ZERO) <= 0) {
            resultBuilder.valid(false);
            RewardValidationResult temp = resultBuilder.build();
            temp.addError("保有インセンティブ係数が無効です: " + calculation.getHoldingFactor());
            return temp;
        }

        return resultBuilder.build();
    }

    /**
     * 報酬計算例外クラス
     */
    public static class RewardCalculationException extends RuntimeException {
        public RewardCalculationException(String message) {
            super(message);
        }

        public RewardCalculationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 報酬妥当性チェック結果クラス
     */
    @lombok.Data
    @lombok.Builder
    public static class RewardValidationResult {
        private boolean valid;
        @lombok.Builder.Default
        private java.util.List<String> errors = new java.util.ArrayList<>();

        public void addError(String error) {
            this.errors.add(error);
        }
    }
}
