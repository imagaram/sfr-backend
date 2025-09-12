package com.sfr.tokyo.sfr_backend.service.crypto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrtTransactionType;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SfrtRewardService {

    private final SfrtBalanceService sfrtBalanceService;

    private static final BigDecimal BUYER_RATE = new BigDecimal("0.0125");
    private static final BigDecimal SELLER_RATE = new BigDecimal("0.0125");
    private static final BigDecimal PLATFORM_RATE = new BigDecimal("0.025");

    @Transactional
    public void distribute(String buyerUserId, String sellerUserId, BigDecimal sfrAmount, Long relatedSfrTxId) {
        if (sfrAmount == null || sfrAmount.signum() <= 0) return;

        BigDecimal buyerReward = sfrAmount.multiply(BUYER_RATE).setScale(8, RoundingMode.HALF_UP);
        BigDecimal sellerReward = sfrAmount.multiply(SELLER_RATE).setScale(8, RoundingMode.HALF_UP);
        BigDecimal platformReward = sfrAmount.multiply(PLATFORM_RATE).setScale(8, RoundingMode.HALF_UP);

        sfrtBalanceService.addSfrtReward(buyerUserId, buyerReward, SfrtTransactionType.REWARD_BUYER, "購入報酬", relatedSfrTxId, null);
        sfrtBalanceService.addSfrtReward(sellerUserId, sellerReward, SfrtTransactionType.REWARD_SELLER, "販売報酬", relatedSfrTxId, null);
        sfrtBalanceService.addSfrtReward("PLATFORM", platformReward, SfrtTransactionType.PLATFORM_RESERVE, "プラットフォーム蓄積", relatedSfrTxId, null);
    }

    public SfrtRewardSimulation simulateRewards(BigDecimal sfrAmount) {
        BigDecimal buyerReward = sfrAmount.multiply(BUYER_RATE).setScale(8, RoundingMode.HALF_UP);
        BigDecimal sellerReward = sfrAmount.multiply(SELLER_RATE).setScale(8, RoundingMode.HALF_UP);
        BigDecimal platformReward = sfrAmount.multiply(PLATFORM_RATE).setScale(8, RoundingMode.HALF_UP);
        return SfrtRewardSimulation.builder()
                .sfrAmount(sfrAmount)
                .buyerReward(buyerReward)
                .sellerReward(sellerReward)
                .platformReserve(platformReward)
                .total(buyerReward.add(sellerReward).add(platformReward))
                .build();
    }

    @Data
    @Builder
    public static class SfrtRewardSimulation {
        private BigDecimal sfrAmount;
        private BigDecimal buyerReward;
        private BigDecimal sellerReward;
        private BigDecimal platformReserve;
        private BigDecimal total;
    }

    @Data
    @Builder
    public static class ManualRewardResult {
        private boolean success;
        private String message;
        private BigDecimal amount;

        public static ManualRewardResult failure(String msg) { return ManualRewardResult.builder().success(false).message(msg).build(); }
    }

    @Transactional
    public ManualRewardResult distributeManualReward(String userId, BigDecimal amount, String reason, String adminUserId) {
        if (amount == null || amount.signum() <= 0) return ManualRewardResult.failure("amount <= 0");
        sfrtBalanceService.addSfrtReward(userId, amount, SfrtTransactionType.ADJUSTMENT, reason);
        return ManualRewardResult.builder().success(true).amount(amount).message("OK").build();
    }
}
