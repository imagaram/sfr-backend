package com.sfr.tokyo.sfr_backend.service.crypto.reward;

import com.sfr.tokyo.sfr_backend.entity.crypto.reward.ContributionRecord;
import com.sfr.tokyo.sfr_backend.entity.crypto.reward.RewardFactor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 報酬係数サービス
 * B係数（基本報酬係数）の管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RewardFactorService {

    // TODO: RewardFactorRepositoryを作成後に追加
    // private final RewardFactorRepository rewardFactorRepository;

    /**
     * 指定された貢献タイプとタイミングに対応するB係数を取得
     *
     * @param contributionType 貢献タイプ
     * @param targetDate 対象日時
     * @return B係数
     */
    public BigDecimal getBaseFactor(ContributionRecord.ContributionType contributionType, LocalDateTime targetDate) {
        log.debug("B係数取得: contributionType={}, targetDate={}", contributionType, targetDate);

        // TODO: データベースから取得するロジックを実装
        // 現在は固定値を返す
        switch (contributionType) {
            case DEVELOPMENT:
                return new BigDecimal("1.4");
            case LIQUIDITY:
                return new BigDecimal("1.3");
            case GOVERNANCE:
                return new BigDecimal("1.0");
            case EDUCATION:
                return new BigDecimal("1.3");
            case COMMERCE:
                return new BigDecimal("1.2");
            case UX:
                return new BigDecimal("0.9");
            default:
                log.warn("未知の貢献タイプ: {}", contributionType);
                return BigDecimal.ONE;
        }
    }

    /**
     * 現在有効なB係数を全て取得
     *
     * @return 貢献タイプごとのB係数マップ
     */
    public java.util.Map<ContributionRecord.ContributionType, BigDecimal> getCurrentBaseFactors() {
        java.util.Map<ContributionRecord.ContributionType, BigDecimal> factors = new java.util.HashMap<>();
        
        for (ContributionRecord.ContributionType type : ContributionRecord.ContributionType.values()) {
            factors.put(type, getBaseFactor(type, LocalDateTime.now()));
        }
        
        return factors;
    }

    /**
     * B係数を更新（管理者用）
     *
     * @param contributionType 貢献タイプ
     * @param newFactor 新しいB係数
     * @param effectiveFrom 有効開始日時
     * @return 更新されたRewardFactor
     */
    @Transactional
    public RewardFactor updateBaseFactor(
            ContributionRecord.ContributionType contributionType,
            BigDecimal newFactor,
            LocalDateTime effectiveFrom) {
        
        log.info("B係数更新: contributionType={}, newFactor={}, effectiveFrom={}", 
                contributionType, newFactor, effectiveFrom);

        // TODO: 既存の係数を無効化し、新しい係数を作成
        RewardFactor rewardFactor = RewardFactor.builder()
                .contributionType(contributionType)
                .baseFactor(newFactor)
                .effectiveFrom(effectiveFrom)
                .build();

        // TODO: リポジトリで保存
        // return rewardFactorRepository.save(rewardFactor);
        
        return rewardFactor;
    }

    /**
     * 希少性に基づいてB係数を動的調整
     *
     * @param contributionType 貢献タイプ
     * @param recentActivityVolume 最近の活動量
     * @return 調整されたB係数
     */
    public BigDecimal getAdjustedBaseFactor(
            ContributionRecord.ContributionType contributionType,
            int recentActivityVolume) {
        
        BigDecimal baseFactor = getBaseFactor(contributionType, LocalDateTime.now());
        
        // 活動量に基づく調整（仮の閾値: 100）
        int threshold = 100;
        
        if (recentActivityVolume > threshold) {
            // 活動量が多い場合は係数を下げる（希少性低下）
            return baseFactor.multiply(new BigDecimal("0.9"));
        } else if (recentActivityVolume < threshold / 2) {
            // 活動量が少ない場合は係数を上げる（希少性向上）
            return baseFactor.multiply(new BigDecimal("1.1"));
        }
        
        return baseFactor;
    }
}
