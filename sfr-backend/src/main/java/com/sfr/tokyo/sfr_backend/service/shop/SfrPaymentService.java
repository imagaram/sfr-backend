package com.sfr.tokyo.sfr_backend.service.shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sfr.tokyo.sfr_backend.service.crypto.SfrtRewardService;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SFR決済処理サービス
 * 購入処理とSFRT報酬配布の連携を担当
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SfrPaymentService {

    private final UserBalanceService userBalanceService;
    private final SfrtRewardService sfrtRewardService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 限定アイテム購入処理
     * SFR決済 + SFRT報酬配布の統合フロー
     */
    @Transactional
    public PurchaseResult processPurchase(PurchaseRequest request) {
        try {
            log.info("購入処理開始: userId={}, itemId={}, amount={}", 
                request.getBuyerId(), request.getItemId(), request.getTotalPrice());

            // 1. SFR残高確認・決済処理
            if (!userBalanceService.hasSufficientBalance(request.getBuyerId(), request.getSpaceId(), request.getTotalPrice())) {
                return PurchaseResult.failure("SFR残高不足");
            }

            userBalanceService.subtractBalance(request.getBuyerId(), request.getSpaceId(), request.getTotalPrice());

            // 2. 販売者への支払い（手数料差し引き後）
            BigDecimal platformFee = request.getTotalPrice().multiply(new BigDecimal("0.05")); // 5%手数料
            BigDecimal sellerAmount = request.getTotalPrice().subtract(platformFee);
            
            userBalanceService.addBalance(
                request.getSellerId(),
                request.getSpaceId(),
                sellerAmount
            );

            // 3. SFRT報酬配布トリガー（修正されたメソッドシグネチャ）
            triggerSfrtRewardsFixed(request);

            // 4. 購入完了イベント発行
            PurchaseCompletedEvent event = new PurchaseCompletedEvent(
                request.getBuyerId(),
                request.getSellerId(),
                request.getItemId(),
                request.getTotalPrice(),
                LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);

            log.info("購入処理完了: userId={}, sfrtRewardsTriggered=true", request.getBuyerId());
            
            return PurchaseResult.success("購入完了。SFRT報酬を獲得しました！");

        } catch (Exception e) {
            log.error("購入処理失敗: userId={}, error={}", request.getBuyerId(), e.getMessage(), e);
            return PurchaseResult.failure("購入処理でエラーが発生しました");
        }
    }

    /**
     * SFRT報酬配布の実行（修正版）
     * 既存の distribute(buyerUserId, sellerUserId, sfrAmount, relatedSfrTxId) メソッドを使用
     */
    private void triggerSfrtRewardsFixed(PurchaseRequest request) {
        try {
            // SfrtRewardService.distribute の既存シグネチャに合わせる
            sfrtRewardService.distribute(
                request.getBuyerId(),
                request.getSellerId(),
                request.getTotalPrice(),
                null // SFR取引IDは後で連携
            );

            log.info("SFRT報酬配布完了: purchase amount={} SFR", request.getTotalPrice());

        } catch (Exception e) {
            log.error("SFRT報酬配布失敗: {}", e.getMessage(), e);
            // 報酬配布失敗は購入自体は継続（別途補償処理）
        }
    }

    // 内部クラス：購入リクエスト
    public static class PurchaseRequest {
        private final String buyerId;
        private final String sellerId;
        private final Long itemId;
        private final Long spaceId;
        private final BigDecimal totalPrice;

        public PurchaseRequest(String buyerId, String sellerId, Long itemId, Long spaceId, BigDecimal totalPrice) {
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.itemId = itemId;
            this.spaceId = spaceId;
            this.totalPrice = totalPrice;
        }

        public String getBuyerId() { return buyerId; }
        public String getSellerId() { return sellerId; }
        public Long getItemId() { return itemId; }
        public Long getSpaceId() { return spaceId; }
        public BigDecimal getTotalPrice() { return totalPrice; }
    }

    // 内部クラス：購入結果
    public static class PurchaseResult {
        private final boolean success;
        private final String message;

        private PurchaseResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static PurchaseResult success(String message) {
            return new PurchaseResult(true, message);
        }

        public static PurchaseResult failure(String message) {
            return new PurchaseResult(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    // 内部クラス：購入完了イベント
    public static class PurchaseCompletedEvent {
        private final String buyerId;
        private final String sellerId;
        private final Long itemId;
        private final BigDecimal amount;
        private final LocalDateTime timestamp;

        public PurchaseCompletedEvent(String buyerId, String sellerId, Long itemId, BigDecimal amount, LocalDateTime timestamp) {
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.itemId = itemId;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        public String getBuyerId() { return buyerId; }
        public String getSellerId() { return sellerId; }
        public Long getItemId() { return itemId; }
        public BigDecimal getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}