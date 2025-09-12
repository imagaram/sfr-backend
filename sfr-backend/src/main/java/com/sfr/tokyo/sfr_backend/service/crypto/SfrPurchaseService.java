package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.config.SfrPointConfig;
import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPoint;
import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPurchaseTransaction;
import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPurchaseTransaction.PurchaseTransactionStatus;
import com.sfr.tokyo.sfr_backend.repository.config.SfrPointConfigRepository;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrPointRepository;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrPurchaseTransactionRepository;
// import com.sfr.tokyo.sfr_backend.service.stripe.PaymentIntentService; // Phase 2で実装

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SFRポイント購入サービス
 * Phase 1 実装: 1SFR = 150円固定レートでの購入機能
 * 
 * 主要機能:
 * - SFRポイント購入処理
 * - Stripe決済統合
 * - 取引履歴管理
 * - 残高更新
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SfrPurchaseService {

    private final SfrPointRepository sfrPointRepository;
    private final SfrPurchaseTransactionRepository purchaseTransactionRepository;
    private final SfrPointConfigRepository configRepository;
    private final SfrIntegrationService integrationService; // Phase 1.4: 既存システム統合
    // private final PaymentIntentService paymentIntentService; // Phase 2で実装

    /**
     * SFRポイント購入要求を作成
     * 
     * @param userId ユーザーID
     * @param yenAmount 円建て購入金額
     * @param spaceId スペースID（デフォルト: 1）
     * @return 購入取引情報
     */
    public SfrPurchaseTransaction createPurchaseRequest(String userId, BigDecimal yenAmount, Long spaceId) {
        log.info("SFR購入要求作成開始: userId={}, yenAmount={}, spaceId={}", userId, yenAmount, spaceId);

        // 設定値検証
        validatePurchaseAmount(yenAmount, spaceId);

        // SFR金額計算（1SFR = 150円固定）
        BigDecimal exchangeRate = getExchangeRate(spaceId);
        BigDecimal sfrAmount = yenAmount.divide(exchangeRate, 8, RoundingMode.HALF_UP);

        // 購入取引記録作成
        SfrPurchaseTransaction transaction = SfrPurchaseTransaction.builder()
                .userId(userId)
                .spaceId(spaceId)
                .sfrAmount(sfrAmount)
                .yenAmount(yenAmount)
                .exchangeRate(exchangeRate)
                .status(PurchaseTransactionStatus.PENDING)
                .build();

        transaction = purchaseTransactionRepository.save(transaction);

        // Phase 2で実装: Stripe PaymentIntent作成
        /*
        // Stripe PaymentIntent作成
        try {
            String paymentIntentId = paymentIntentService.createPaymentIntent(
                yenAmount.longValue(), // 円単位の金額
                userId,
                Map.of(
                    "purchase_type", "sfr_points",
                    "sfr_amount", sfrAmount.toString(),
                    "transaction_id", transaction.getId().toString()
                )
            );

            transaction.setStripePaymentIntentId(paymentIntentId);
            transaction = purchaseTransactionRepository.save(transaction);

            log.info("SFR購入要求作成完了: transactionId={}, paymentIntentId={}", 
                    transaction.getId(), paymentIntentId);

        } catch (Exception e) {
            log.error("Stripe PaymentIntent作成失敗: transactionId={}", transaction.getId(), e);
            transaction.setStatus(PurchaseTransactionStatus.FAILED);
            purchaseTransactionRepository.save(transaction);
            throw new RuntimeException("決済処理の初期化に失敗しました", e);
        }
        */

        // Phase 1では仮のPaymentIntent IDを設定
        transaction.setStripePaymentIntentId("pi_temp_" + transaction.getId());
        transaction = purchaseTransactionRepository.save(transaction);

        log.info("SFR購入要求作成完了: transactionId={}, tempPaymentIntentId={}", 
                transaction.getId(), transaction.getStripePaymentIntentId());

        return transaction;
    }

    /**
     * 決済完了後のSFRポイント付与処理
     * 
     * @param transactionId 購入取引ID
     * @param stripePaymentIntentId Stripe PaymentIntent ID
     */
    public void completePurchase(Long transactionId, String stripePaymentIntentId) {
        log.info("SFR購入完了処理開始: transactionId={}, paymentIntentId={}", 
                transactionId, stripePaymentIntentId);

        SfrPurchaseTransaction transaction = purchaseTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("購入取引が見つかりません: " + transactionId));

        // ステータス確認
        if (transaction.getStatus() != PurchaseTransactionStatus.PENDING) {
            log.warn("購入取引のステータスが無効です: transactionId={}, status={}", 
                    transactionId, transaction.getStatus());
            return;
        }

        // PaymentIntent ID確認
        if (!stripePaymentIntentId.equals(transaction.getStripePaymentIntentId())) {
            log.error("PaymentIntent IDが一致しません: expected={}, actual={}", 
                    transaction.getStripePaymentIntentId(), stripePaymentIntentId);
            throw new IllegalArgumentException("決済情報が一致しません");
        }

        try {
            // SFRポイント残高更新
            SfrPoint sfrPoint = getOrCreateSfrPoint(transaction.getUserId(), transaction.getSpaceId());
            sfrPoint.recordPurchase(transaction.getSfrAmount());
            sfrPointRepository.save(sfrPoint);

            // 取引ステータス更新
            transaction.setStatus(PurchaseTransactionStatus.COMPLETED);
            transaction.setStripePaymentStatus("succeeded");
            transaction.setCompletedAt(LocalDateTime.now());
            purchaseTransactionRepository.save(transaction);

            // Phase 1.4: 既存システム統合 - UserBalanceとの同期
            integrationService.syncPurchaseToLegacyBalance(
                transaction.getUserId(), 
                transaction.getSfrAmount(), 
                transaction.getId()
            );

            log.info("SFR購入完了（既存システム統合済み): userId={}, sfrAmount={}, newBalance={}", 
                    transaction.getUserId(), transaction.getSfrAmount(), sfrPoint.getCurrentBalance());

        } catch (Exception e) {
            log.error("SFR購入完了処理エラー: transactionId={}", transactionId, e);
            transaction.setStatus(PurchaseTransactionStatus.FAILED);
            purchaseTransactionRepository.save(transaction);
            throw new RuntimeException("SFRポイント付与に失敗しました", e);
        }
    }

    /**
     * 購入金額の妥当性検証
     */
    private void validatePurchaseAmount(BigDecimal yenAmount, Long spaceId) {
        // 最小購入額チェック
        BigDecimal minPurchase = getConfigValue(SfrPointConfig.ConfigKeys.SFR_MIN_PURCHASE, spaceId)
                .orElse(new BigDecimal(SfrPointConfig.DefaultValues.MIN_PURCHASE));
        
        if (yenAmount.compareTo(minPurchase) < 0) {
            throw new IllegalArgumentException("購入金額が最小額を下回っています: " + minPurchase + "円");
        }

        // 最大購入額チェック
        BigDecimal maxPurchase = getConfigValue(SfrPointConfig.ConfigKeys.SFR_MAX_PURCHASE, spaceId)
                .orElse(new BigDecimal(SfrPointConfig.DefaultValues.MAX_PURCHASE));
        
        if (yenAmount.compareTo(maxPurchase) > 0) {
            throw new IllegalArgumentException("購入金額が最大額を超えています: " + maxPurchase + "円");
        }
    }

    /**
     * 為替レート取得（固定150円）
     */
    private BigDecimal getExchangeRate(Long spaceId) {
        return getConfigValue(SfrPointConfig.ConfigKeys.SFR_EXCHANGE_RATE, spaceId)
                .orElse(new BigDecimal(SfrPointConfig.DefaultValues.EXCHANGE_RATE));
    }

    /**
     * SFRポイントエンティティ取得または作成
     */
    private SfrPoint getOrCreateSfrPoint(String userId, Long spaceId) {
        return sfrPointRepository.findByUserIdAndSpaceId(userId, spaceId)
                .orElseGet(() -> {
                    SfrPoint newPoint = SfrPoint.builder()
                            .userId(userId)
                            .spaceId(spaceId)
                            .build();
                    return sfrPointRepository.save(newPoint);
                });
    }

    /**
     * 設定値取得
     */
    private Optional<BigDecimal> getConfigValue(String configKey, Long spaceId) {
        return configRepository.findByConfigKeyAndSpaceIdAndIsActiveTrue(configKey, spaceId)
                .map(SfrPointConfig::getValueAsBigDecimal);
    }

    /**
     * ユーザーの購入履歴取得
     */
    @Transactional(readOnly = true)
    public List<SfrPurchaseTransaction> getUserPurchaseHistory(String userId, Long spaceId) {
        return purchaseTransactionRepository.findByUserIdAndSpaceIdOrderByCreatedAtDesc(userId, spaceId);
    }

    /**
     * 購入統計情報取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPurchaseStats(String userId, Long spaceId) {
        SfrPoint sfrPoint = sfrPointRepository.findByUserIdAndSpaceId(userId, spaceId)
                .orElse(SfrPoint.builder().userId(userId).spaceId(spaceId).build());

        return Map.of(
            "currentBalance", sfrPoint.getCurrentBalance(),
            "totalPurchased", sfrPoint.getTotalPurchased(),
            "totalSpent", sfrPoint.getTotalSpent(),
            "totalEarned", sfrPoint.getTotalEarned(),
            "balanceInYen", sfrPoint.getBalanceInYen()
        );
    }
}
