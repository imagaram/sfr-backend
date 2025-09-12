package com.sfr.tokyo.sfr_backend.service.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SfrtRewardService ユニットテスト
 */
@ExtendWith(MockitoExtension.class)
class SfrtRewardServiceTest {

    @Mock
    private SfrtBalanceService sfrtBalanceService;

    @InjectMocks
    private SfrtRewardService sfrtRewardService;

    private static final String BUYER_ID = "buyer123";
    private static final String SELLER_ID = "seller456";
    private static final BigDecimal SFR_AMOUNT = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        // Mock初期化は@ExtendWith(MockitoExtension.class)で自動実行
    }

    @Test
    void testDistribute_正常系() {
        // Given
        Long relatedSfrTxId = 12345L;

        // When
        sfrtRewardService.distribute(BUYER_ID, SELLER_ID, SFR_AMOUNT, relatedSfrTxId);

        // Then
        // 購入者・販売者・プラットフォームの3回呼び出し確認
        verify(sfrtBalanceService, times(3)).addSfrtReward(
            anyString(), 
            any(BigDecimal.class), 
            any(), 
            anyString(), 
            eq(relatedSfrTxId), 
            any()
        );
    }

    @Test
    void testSimulateRewards_正常系() {
        // Given
        BigDecimal sfrAmount = new BigDecimal("1000.00");

        // When
        SfrtRewardService.SfrtRewardSimulation result = sfrtRewardService.simulateRewards(sfrAmount);

        // Then
        assertNotNull(result);
        assertEquals(sfrAmount, result.getSfrAmount());
        
        // 期待値計算（1.25% x 2 + 2.5% = 5%）
        BigDecimal expectedBuyerReward = sfrAmount.multiply(new BigDecimal("0.0125"));
        BigDecimal expectedSellerReward = sfrAmount.multiply(new BigDecimal("0.0125"));
        BigDecimal expectedPlatformReward = sfrAmount.multiply(new BigDecimal("0.025"));
        BigDecimal expectedTotal = expectedBuyerReward.add(expectedSellerReward).add(expectedPlatformReward);

        assertEquals(expectedBuyerReward.setScale(8), result.getBuyerReward());
        assertEquals(expectedSellerReward.setScale(8), result.getSellerReward());
        assertEquals(expectedPlatformReward.setScale(8), result.getPlatformReserve());
        assertEquals(expectedTotal.setScale(8), result.getTotal());
    }

    @Test
    void testDistribute_SFR金額がnull() {
        // Given
        BigDecimal nullAmount = null;

        // When
        sfrtRewardService.distribute(BUYER_ID, SELLER_ID, nullAmount, null);

        // Then
        // 無効な金額の場合は処理されない
        verify(sfrtBalanceService, times(0)).addSfrtReward(
            anyString(), any(BigDecimal.class), any(), anyString(), any(), any()
        );
    }

    @Test
    void testDistribute_SFR金額がゼロ以下() {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;
        BigDecimal negativeAmount = new BigDecimal("-10.00");

        // When
        sfrtRewardService.distribute(BUYER_ID, SELLER_ID, zeroAmount, null);
        sfrtRewardService.distribute(BUYER_ID, SELLER_ID, negativeAmount, null);

        // Then
        // ゼロ以下の場合は処理されない
        verify(sfrtBalanceService, times(0)).addSfrtReward(
            anyString(), any(BigDecimal.class), any(), anyString(), any(), any()
        );
    }

    @Test
    void testDistributeManualReward_正常系() {
        // Given
        String userId = "user123";
        BigDecimal rewardAmount = new BigDecimal("50.00");
        String reason = "特別報酬";
        String adminUserId = "admin";

        // When
        SfrtRewardService.ManualRewardResult result = 
            sfrtRewardService.distributeManualReward(userId, rewardAmount, reason, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(true, result.isSuccess());
        assertEquals(rewardAmount, result.getAmount());
        assertEquals("OK", result.getMessage());

        verify(sfrtBalanceService, times(1)).addSfrtReward(
            eq(userId), eq(rewardAmount), any(), eq(reason)
        );
    }

    @Test
    void testDistributeManualReward_無効な金額() {
        // Given
        String userId = "user123";
        BigDecimal invalidAmount = BigDecimal.ZERO;
        String reason = "特別報酬";
        String adminUserId = "admin";

        // When
        SfrtRewardService.ManualRewardResult result = 
            sfrtRewardService.distributeManualReward(userId, invalidAmount, reason, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(false, result.isSuccess());
        assertEquals("amount <= 0", result.getMessage());

        verify(sfrtBalanceService, times(0)).addSfrtReward(anyString(), any(), any(), anyString());
    }
}
