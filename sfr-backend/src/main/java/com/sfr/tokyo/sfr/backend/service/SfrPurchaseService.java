package com.sfr.tokyo.sfr.backend.service;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPoint;
import com.sfr.tokyo.sfr.backend.entity.SfrPurchaseTransaction;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrPointRepository;
import com.sfr.tokyo.sfr.backend.repository.SfrPurchaseTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class SfrPurchaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(SfrPurchaseService.class);
    
    @Autowired
    private SfrPurchaseTransactionRepository purchaseTransactionRepository;
    
    @Autowired
    private SfrPointRepository sfrPointRepository;
    
    // TODO: Stripe連携サービス（将来実装）
    // @Autowired
    // private StripePaymentService stripePaymentService;
    
    /**
     * SFR購入処理
     * ロードマップ Phase 1.2 の実装
     */
    @Transactional
    public SfrPurchaseResult purchaseSfr(String userId, Long spaceId, BigDecimal jpyAmount) {
        try {
            logger.info("SFR購入処理を開始します。userId: {}, spaceId: {}, jpyAmount: {}", 
                       userId, spaceId, jpyAmount);
            
            // 1. 購入限度チェック
            PurchaseLimitValidation limitValidation = validatePurchaseLimit(userId, spaceId, jpyAmount);
            if (!limitValidation.isValid()) {
                return SfrPurchaseResult.failure(limitValidation.getErrorMessage());
            }
            
            // 2. SFR数量計算（150円/SFR）
            BigDecimal exchangeRate = new BigDecimal("150.00"); // TODO: configService.getSfrExchangeRate(spaceId);
            BigDecimal sfrAmount = jpyAmount.divide(exchangeRate, 8, RoundingMode.HALF_UP);
            
            // 3. 取引レコード作成
            SfrPurchaseTransaction transaction = new SfrPurchaseTransaction(
                userId, spaceId, jpyAmount, sfrAmount, exchangeRate);
            transaction = purchaseTransactionRepository.save(transaction);
            
            // 4. 決済処理（Stripe連携）- 現在は仮実装
            PaymentResult paymentResult = processPayment(jpyAmount, transaction.getId());
            if (!paymentResult.isSuccess()) {
                transaction.failTransaction(paymentResult.getErrorMessage());
                purchaseTransactionRepository.save(transaction);
                return SfrPurchaseResult.failure("決済処理に失敗しました: " + paymentResult.getErrorMessage());
            }
            
            // 5. SFR残高更新
            updateSfrBalance(userId, spaceId, sfrAmount);
            
            // 6. 取引完了
            transaction.completeTransaction();
            transaction.setStripePaymentIntentId(paymentResult.getPaymentIntentId());
            transaction.setStripePaymentStatus("succeeded");
            purchaseTransactionRepository.save(transaction);
            
            logger.info("SFR購入処理が完了しました。transactionId: {}, sfrAmount: {}", 
                       transaction.getId(), sfrAmount);
            
            return SfrPurchaseResult.success(transaction.getId(), sfrAmount, exchangeRate);
            
        } catch (Exception e) {
            logger.error("SFR購入処理中にエラーが発生しました。userId: {}, spaceId: {}", userId, spaceId, e);
            return SfrPurchaseResult.failure("システムエラーが発生しました: " + e.getMessage());
        }
    }
    
    /**
     * 購入限度額チェック
     */
    private PurchaseLimitValidation validatePurchaseLimit(String userId, Long spaceId, BigDecimal jpyAmount) {
        // 最小購入額チェック
        BigDecimal minPurchase = new BigDecimal("150"); // TODO: configService.getMinPurchaseAmount(spaceId);
        if (jpyAmount.compareTo(minPurchase) < 0) {
            return PurchaseLimitValidation.invalid(
                String.format("最小購入額は%.0f円です", minPurchase));
        }
        
        // 最大購入額チェック
        BigDecimal maxPurchase = new BigDecimal("100000"); // TODO: configService.getMaxPurchaseAmount(spaceId);
        if (jpyAmount.compareTo(maxPurchase) > 0) {
            return PurchaseLimitValidation.invalid(
                String.format("最大購入額は%.0f円です", maxPurchase));
        }
        
        // 日次購入限度額チェック
        BigDecimal dailyLimit = new BigDecimal("50000"); // TODO: configService.getDailyPurchaseLimit(spaceId);
        BigDecimal todayPurchased = purchaseTransactionRepository.getDailyPurchaseAmount(userId);
        if (todayPurchased.add(jpyAmount).compareTo(dailyLimit) > 0) {
            BigDecimal remaining = dailyLimit.subtract(todayPurchased);
            return PurchaseLimitValidation.invalid(
                String.format("日次購入限度額を超過します。本日の残り購入可能額: %.0f円", remaining));
        }
        
        // 月次購入限度額チェック
        BigDecimal monthlyLimit = new BigDecimal("500000"); // TODO: configService.getMonthlyPurchaseLimit(spaceId);
        BigDecimal monthlyPurchased = purchaseTransactionRepository.getMonthlyPurchaseAmount(userId);
        if (monthlyPurchased.add(jpyAmount).compareTo(monthlyLimit) > 0) {
            BigDecimal remaining = monthlyLimit.subtract(monthlyPurchased);
            return PurchaseLimitValidation.invalid(
                String.format("月次購入限度額を超過します。今月の残り購入可能額: %.0f円", remaining));
        }
        
        return PurchaseLimitValidation.valid();
    }
    
    /**
     * 決済処理（現在は仮実装）
     */
    private PaymentResult processPayment(BigDecimal jpyAmount, Long transactionId) {
        // TODO: Stripe APIとの実際の連携を実装
        // 現在は成功を仮定
        logger.info("決済処理を実行します（仮実装）。金額: {}円, transactionId: {}", jpyAmount, transactionId);
        
        // 仮のPayment Intent ID生成
        String paymentIntentId = "pi_test_" + System.currentTimeMillis();
        
        return PaymentResult.success(paymentIntentId);
    }
    
    /**
     * SFR残高更新
     */
    private void updateSfrBalance(String userId, Long spaceId, BigDecimal sfrAmount) {
        Optional<SfrPoint> existingBalance = sfrPointRepository.findByUserIdAndSpaceId(userId, spaceId);
        
        if (existingBalance.isPresent()) {
            SfrPoint sfrPoint = existingBalance.get();
            sfrPoint.setCurrentBalance(sfrPoint.getCurrentBalance().add(sfrAmount));
            sfrPoint.setTotalPurchased(sfrPoint.getTotalPurchased().add(sfrAmount));
            sfrPointRepository.save(sfrPoint);
        } else {
            SfrPoint newSfrPoint = new SfrPoint();
            newSfrPoint.setUserId(userId);
            newSfrPoint.setSpaceId(spaceId);
            newSfrPoint.setCurrentBalance(sfrAmount);
            newSfrPoint.setTotalPurchased(sfrAmount);
            newSfrPoint.setTotalEarned(BigDecimal.ZERO);
            newSfrPoint.setTotalSpent(BigDecimal.ZERO);
            newSfrPoint.setStatus(SfrPoint.SfrPointStatus.ACTIVE);
            newSfrPoint.setSfrtEligible(true);
            sfrPointRepository.save(newSfrPoint);
        }
    }
    
    /**
     * ユーザーの購入履歴取得
     */
    public List<SfrPurchaseTransaction> getUserPurchaseHistory(String userId) {
        return purchaseTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 購入限度額情報取得
     */
    public PurchaseLimitInfo getPurchaseLimitInfo(String userId, Long spaceId) {
        BigDecimal dailyLimit = new BigDecimal("50000"); // TODO: configService.getDailyPurchaseLimit(spaceId);
        BigDecimal monthlyLimit = new BigDecimal("500000"); // TODO: configService.getMonthlyPurchaseLimit(spaceId);
        BigDecimal minPurchase = new BigDecimal("150"); // TODO: configService.getMinPurchaseAmount(spaceId);
        BigDecimal maxPurchase = new BigDecimal("100000"); // TODO: configService.getMaxPurchaseAmount(spaceId);
        
        BigDecimal dailyUsed = purchaseTransactionRepository.getDailyPurchaseAmount(userId);
        BigDecimal monthlyUsed = purchaseTransactionRepository.getMonthlyPurchaseAmount(userId);
        
        return new PurchaseLimitInfo(
            minPurchase, maxPurchase,
            dailyLimit, dailyUsed, dailyLimit.subtract(dailyUsed),
            monthlyLimit, monthlyUsed, monthlyLimit.subtract(monthlyUsed)
        );
    }
    
    // 内部クラス
    public static class SfrPurchaseResult {
        private final boolean success;
        private final String errorMessage;
        private final Long transactionId;
        private final BigDecimal sfrAmount;
        private final BigDecimal exchangeRate;
        
        private SfrPurchaseResult(boolean success, String errorMessage, Long transactionId, 
                                BigDecimal sfrAmount, BigDecimal exchangeRate) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.transactionId = transactionId;
            this.sfrAmount = sfrAmount;
            this.exchangeRate = exchangeRate;
        }
        
        public static SfrPurchaseResult success(Long transactionId, BigDecimal sfrAmount, BigDecimal exchangeRate) {
            return new SfrPurchaseResult(true, null, transactionId, sfrAmount, exchangeRate);
        }
        
        public static SfrPurchaseResult failure(String errorMessage) {
            return new SfrPurchaseResult(false, errorMessage, null, null, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public Long getTransactionId() { return transactionId; }
        public BigDecimal getSfrAmount() { return sfrAmount; }
        public BigDecimal getExchangeRate() { return exchangeRate; }
    }
    
    private static class PurchaseLimitValidation {
        private final boolean valid;
        private final String errorMessage;
        
        private PurchaseLimitValidation(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static PurchaseLimitValidation valid() {
            return new PurchaseLimitValidation(true, null);
        }
        
        public static PurchaseLimitValidation invalid(String errorMessage) {
            return new PurchaseLimitValidation(false, errorMessage);
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    private static class PaymentResult {
        private final boolean success;
        private final String errorMessage;
        private final String paymentIntentId;
        
        private PaymentResult(boolean success, String errorMessage, String paymentIntentId) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.paymentIntentId = paymentIntentId;
        }
        
        public static PaymentResult success(String paymentIntentId) {
            return new PaymentResult(true, null, paymentIntentId);
        }
        
        public static PaymentResult failure(String errorMessage) {
            return new PaymentResult(false, errorMessage, null);
        }
        
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getPaymentIntentId() { return paymentIntentId; }
    }
    
    public static class PurchaseLimitInfo {
        private final BigDecimal minPurchase;
        private final BigDecimal maxPurchase;
        private final BigDecimal dailyLimit;
        private final BigDecimal dailyUsed;
        private final BigDecimal dailyRemaining;
        private final BigDecimal monthlyLimit;
        private final BigDecimal monthlyUsed;
        private final BigDecimal monthlyRemaining;
        
        public PurchaseLimitInfo(BigDecimal minPurchase, BigDecimal maxPurchase,
                               BigDecimal dailyLimit, BigDecimal dailyUsed, BigDecimal dailyRemaining,
                               BigDecimal monthlyLimit, BigDecimal monthlyUsed, BigDecimal monthlyRemaining) {
            this.minPurchase = minPurchase;
            this.maxPurchase = maxPurchase;
            this.dailyLimit = dailyLimit;
            this.dailyUsed = dailyUsed;
            this.dailyRemaining = dailyRemaining;
            this.monthlyLimit = monthlyLimit;
            this.monthlyUsed = monthlyUsed;
            this.monthlyRemaining = monthlyRemaining;
        }
        
        // Getters
        public BigDecimal getMinPurchase() { return minPurchase; }
        public BigDecimal getMaxPurchase() { return maxPurchase; }
        public BigDecimal getDailyLimit() { return dailyLimit; }
        public BigDecimal getDailyUsed() { return dailyUsed; }
        public BigDecimal getDailyRemaining() { return dailyRemaining; }
        public BigDecimal getMonthlyLimit() { return monthlyLimit; }
        public BigDecimal getMonthlyUsed() { return monthlyUsed; }
        public BigDecimal getMonthlyRemaining() { return monthlyRemaining; }
    }
}
