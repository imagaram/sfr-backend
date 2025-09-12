package com.serendipity.tokyo.sfrbackend.service.sfrt;

// import com.serendipity.tokyo.sfrbackend.entity.sfr.SfrPurchaseTransaction;
import com.serendipity.tokyo.sfrbackend.entity.sfrt.SfrtTransaction;
// import com.sfr.tokyo.sfr_backend.service.config.SfrPointConfigService;
// TODO: SfrtBalanceServiceとの連携は今後実装
// import com.serendipity.tokyo.sfrbackend.service.sfrt.SfrtBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.context.event.EventListener;
// import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SFRT報酬配布サービス
 * - SFR取引に連動したSFRT配布
 * - 購入者・販売者・プラットフォーム報酬
 * - イベント駆動型報酬処理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SfrtRewardService {

    private final SfrtBalanceService sfrtBalanceService;
    // TODO: 設定サービス統合後に有効化
    // private final SfrPointConfigService sfrPointConfigService;

    // プラットフォーム用の特別なユーザーID
    private static final String PLATFORM_USER_ID = "platform-reserve";

    /**
     * SFR購入取引に対するSFRT報酬配布
     * TODO: SfrPurchaseTransactionエンティティ実装後に有効化
     */
    /*
    @EventListener
    @Async
    @Transactional
    public void onSfrPurchaseTransaction(SfrPurchaseTransaction transaction) {
        if (transaction.getStatus() != SfrPurchaseTransaction.TransactionStatus.COMPLETED) {
            return; // 完了していない取引は対象外
        }

        log.info("SFR購入取引に対するSFRT報酬配布を開始: transactionId={}, sfrAmount={}", 
            transaction.getId(), transaction.getSfrAmount());

        try {
            distributePurchaseReward(transaction);
        } catch (Exception e) {
            log.error("SFR購入報酬の配布に失敗しました: transactionId={}", transaction.getId(), e);
        }
    }
    */

    /**
     * SFR決済取引に対するSFRT報酬配布
     * TODO: SFR決済エンティティができたら実装
     */
    public void onSfrPaymentTransaction(Object paymentTransaction) {
        // SFR決済取引（限定品購入など）に対する報酬配布
        // 実装予定：購入者1.25% + 販売者1.25% + プラットフォーム2.5%
    }

    /**
     * SFR購入報酬の配布
     * TODO: SfrPurchaseTransactionエンティティ実装後に有効化
     */
    /*
    private void distributePurchaseReward(SfrPurchaseTransaction transaction) {
        Long spaceId = transaction.getSpaceId();
        BigDecimal sfrAmount = transaction.getSfrAmount();

        // 購入者報酬率を取得（デフォルト: 1.25%）
        BigDecimal buyerRewardRate = new BigDecimal("0.0125");
        // TODO: sfrPointConfigService.getBigDecimalValue(spaceId, "sfrt.reward.rate.buyer", new BigDecimal("0.0125"));

        // プラットフォーム報酬率を取得（デフォルト: 2.5%）
        BigDecimal platformRewardRate = new BigDecimal("0.025");
        // TODO: sfrPointConfigService.getBigDecimalValue(spaceId, "sfrt.reward.rate.platform", new BigDecimal("0.025"));

        // 購入者への報酬
        BigDecimal buyerReward = sfrAmount.multiply(buyerRewardRate)
            .setScale(8, RoundingMode.HALF_UP);

        if (buyerReward.compareTo(BigDecimal.ZERO) > 0) {
            sfrtBalanceService.addSfrtBalance(
                transaction.getUserId(),
                spaceId,
                buyerReward,
                SfrtTransaction.SfrtTransactionType.PURCHASE_REWARD,
                "SFR購入報酬: " + transaction.getSfrAmount() + " SFR",
                transaction.getId()
            );

            log.info("購入者報酬を配布しました: userId={}, reward={}, sfrAmount={}", 
                transaction.getUserId(), buyerReward, sfrAmount);
        }

        // プラットフォーム報酬
        BigDecimal platformReward = sfrAmount.multiply(platformRewardRate)
            .setScale(8, RoundingMode.HALF_UP);

        if (platformReward.compareTo(BigDecimal.ZERO) > 0) {
            sfrtBalanceService.addSfrtBalance(
                PLATFORM_USER_ID,
                spaceId,
                platformReward,
                SfrtTransaction.SfrtTransactionType.PLATFORM_RESERVE,
                "プラットフォーム報酬: " + transaction.getSfrAmount() + " SFR購入",
                transaction.getId()
            );

            log.info("プラットフォーム報酬を蓄積しました: reward={}, sfrAmount={}", 
                platformReward, sfrAmount);
        }

        log.info("SFR購入報酬配布完了: transactionId={}, buyerReward={}, platformReward={}", 
            transaction.getId(), buyerReward, platformReward);
    }
    */

    /**
     * SFR決済報酬の配布（限定品購入など）
     */
    public void distributeSalesReward(
        String buyerUserId,
        String sellerUserId,
        Long spaceId,
        BigDecimal sfrAmount,
        Long relatedTransactionId
    ) {
        // 購入者報酬率を取得
        BigDecimal buyerRewardRate = new BigDecimal("0.0125");
        // TODO: sfrPointConfigService.getBigDecimalValue(spaceId, "sfrt.reward.rate.buyer", new BigDecimal("0.0125"));

        // 販売者報酬率を取得
        BigDecimal sellerRewardRate = new BigDecimal("0.0125");
        // TODO: sfrPointConfigService.getBigDecimalValue(spaceId, "sfrt.reward.rate.seller", new BigDecimal("0.0125"));

        // プラットフォーム報酬率を取得
        BigDecimal platformRewardRate = new BigDecimal("0.025");
        // TODO: sfrPointConfigService.getBigDecimalValue(spaceId, "sfrt.reward.rate.platform", new BigDecimal("0.025"));

        // 購入者への報酬
        BigDecimal buyerReward = sfrAmount.multiply(buyerRewardRate)
            .setScale(8, RoundingMode.HALF_UP);

        if (buyerReward.compareTo(BigDecimal.ZERO) > 0) {
            sfrtBalanceService.addSfrtBalance(
                buyerUserId,
                spaceId,
                buyerReward,
                SfrtTransaction.SfrtTransactionType.PURCHASE_REWARD,
                "限定品購入報酬: " + sfrAmount + " SFR",
                relatedTransactionId
            );
        }

        // 販売者への報酬
        BigDecimal sellerReward = sfrAmount.multiply(sellerRewardRate)
            .setScale(8, RoundingMode.HALF_UP);

        if (sellerReward.compareTo(BigDecimal.ZERO) > 0) {
            sfrtBalanceService.addSfrtBalance(
                sellerUserId,
                spaceId,
                sellerReward,
                SfrtTransaction.SfrtTransactionType.SALES_REWARD,
                "限定品販売報酬: " + sfrAmount + " SFR",
                relatedTransactionId
            );
        }

        // プラットフォーム報酬
        BigDecimal platformReward = sfrAmount.multiply(platformRewardRate)
            .setScale(8, RoundingMode.HALF_UP);

        if (platformReward.compareTo(BigDecimal.ZERO) > 0) {
            sfrtBalanceService.addSfrtBalance(
                PLATFORM_USER_ID,
                spaceId,
                platformReward,
                SfrtTransaction.SfrtTransactionType.PLATFORM_RESERVE,
                "プラットフォーム報酬: " + sfrAmount + " SFR決済",
                relatedTransactionId
            );
        }

        log.info("SFR決済報酬配布完了: buyerId={}, sellerId={}, sfrAmount={}, " +
            "buyerReward={}, sellerReward={}, platformReward={}", 
            buyerUserId, sellerUserId, sfrAmount, buyerReward, sellerReward, platformReward);
    }

    /**
     * ステーキング報酬の配布
     */
    public void distributeStakingReward(
        String userId,
        Long spaceId,
        BigDecimal rewardAmount,
        String description
    ) {
        sfrtBalanceService.addSfrtBalance(
            userId,
            spaceId,
            rewardAmount,
            SfrtTransaction.SfrtTransactionType.STAKING_REWARD,
            description
        );

        log.info("ステーキング報酬を配布しました: userId={}, reward={}", userId, rewardAmount);
    }

    /**
     * ガバナンス参加報酬の配布
     */
    public void distributeGovernanceReward(
        String userId,
        Long spaceId,
        BigDecimal rewardAmount,
        String description
    ) {
        sfrtBalanceService.addSfrtBalance(
            userId,
            spaceId,
            rewardAmount,
            SfrtTransaction.SfrtTransactionType.GOVERNANCE_REWARD,
            description
        );

        log.info("ガバナンス報酬を配布しました: userId={}, reward={}", userId, rewardAmount);
    }

    /**
     * 流動性提供報酬の配布
     */
    public void distributeLiquidityReward(
        String userId,
        Long spaceId,
        BigDecimal rewardAmount,
        String description
    ) {
        sfrtBalanceService.addSfrtBalance(
            userId,
            spaceId,
            rewardAmount,
            SfrtTransaction.SfrtTransactionType.LIQUIDITY_REWARD,
            description
        );

        log.info("流動性提供報酬を配布しました: userId={}, reward={}", userId, rewardAmount);
    }

    /**
     * 報酬配布の設定確認
     */
    public boolean isRewardDistributionEnabled(Long spaceId) {
        return true; // TODO: sfrPointConfigService.getBooleanValue(spaceId, "sfrt.reward.enabled", true);
    }

    /**
     * 最小報酬配布額チェック
     */
    public boolean meetsMinimumRewardThreshold(BigDecimal rewardAmount, Long spaceId) {
        BigDecimal minReward = new BigDecimal("0.00000001"); // 0.00000001 SFRT
        // TODO: sfrPointConfigService.getBigDecimalValue(spaceId, "sfrt.reward.minimum", new BigDecimal("0.00000001"));
        return rewardAmount.compareTo(minReward) >= 0;
    }
}
