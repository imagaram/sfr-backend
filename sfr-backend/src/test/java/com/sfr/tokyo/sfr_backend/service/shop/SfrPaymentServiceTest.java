package com.sfr.tokyo.sfr_backend.service.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.sfr.tokyo.sfr_backend.service.crypto.SfrtRewardService;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;

/**
 * SfrPaymentService ユニットテスト
 */
@ExtendWith(MockitoExtension.class)
class SfrPaymentServiceTest {

    @Mock
    private UserBalanceService userBalanceService;

    @Mock
    private SfrtRewardService sfrtRewardService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SfrPaymentService sfrPaymentService;

    private SfrPaymentService.PurchaseRequest validRequest;
    private static final String BUYER_ID = "buyer123";
    private static final String SELLER_ID = "seller456";
    private static final Long ITEM_ID = 789L;
    private static final Long SPACE_ID = 1L;
    private static final BigDecimal PURCHASE_AMOUNT = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        validRequest = new SfrPaymentService.PurchaseRequest(
            BUYER_ID, SELLER_ID, ITEM_ID, SPACE_ID, PURCHASE_AMOUNT
        );
    }

    @Test
    void testProcessPurchase_正常系() {
        // Given
        when(userBalanceService.hasSufficientBalance(BUYER_ID, SPACE_ID, PURCHASE_AMOUNT))
            .thenReturn(true);

        // When
        SfrPaymentService.PurchaseResult result = sfrPaymentService.processPurchase(validRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("購入完了。SFRT報酬を獲得しました！", result.getMessage());

        // 残高確認・減算の呼び出し確認
        verify(userBalanceService, times(1)).hasSufficientBalance(BUYER_ID, SPACE_ID, PURCHASE_AMOUNT);
        verify(userBalanceService, times(1)).subtractBalance(BUYER_ID, SPACE_ID, PURCHASE_AMOUNT);

        // 販売者への支払い確認（手数料5%差し引き後）
        BigDecimal expectedSellerAmount = PURCHASE_AMOUNT.multiply(new BigDecimal("0.95"));
        verify(userBalanceService, times(1)).addBalance(SELLER_ID, SPACE_ID, expectedSellerAmount);

        // SFRT報酬配布確認
        verify(sfrtRewardService, times(1)).distribute(BUYER_ID, SELLER_ID, PURCHASE_AMOUNT, null);

        // イベント発行確認
        verify(eventPublisher, times(1)).publishEvent(any(SfrPaymentService.PurchaseCompletedEvent.class));
    }

    @Test
    void testProcessPurchase_残高不足() {
        // Given
        when(userBalanceService.hasSufficientBalance(BUYER_ID, SPACE_ID, PURCHASE_AMOUNT))
            .thenReturn(false);

        // When
        SfrPaymentService.PurchaseResult result = sfrPaymentService.processPurchase(validRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("SFR残高不足", result.getMessage());

        // 残高確認のみ実行、減算・支払い・報酬配布は実行されない
        verify(userBalanceService, times(1)).hasSufficientBalance(BUYER_ID, SPACE_ID, PURCHASE_AMOUNT);
        verify(userBalanceService, times(0)).subtractBalance(anyString(), any(), any());
        verify(userBalanceService, times(0)).addBalance(anyString(), any(), any());
        verify(sfrtRewardService, times(0)).distribute(anyString(), anyString(), any(), any());
        verify(eventPublisher, times(0)).publishEvent(any());
    }

    @Test
    void testProcessPurchase_SFRT報酬配布失敗でも購入は成功() {
        // Given
        when(userBalanceService.hasSufficientBalance(BUYER_ID, SPACE_ID, PURCHASE_AMOUNT))
            .thenReturn(true);
        
        // SFRT報酬配布で例外発生（購入処理は継続すべき）
        doThrow(new RuntimeException("SFRT報酬システム障害"))
            .when(sfrtRewardService).distribute(anyString(), anyString(), any(), any());

        // When
        SfrPaymentService.PurchaseResult result = sfrPaymentService.processPurchase(validRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess()); // 報酬配布失敗でも購入は成功
        assertEquals("購入完了。SFRT報酬を獲得しました！", result.getMessage());

        // SFR決済・販売者支払いは正常実行
        verify(userBalanceService, times(1)).subtractBalance(BUYER_ID, SPACE_ID, PURCHASE_AMOUNT);
        verify(userBalanceService, times(1)).addBalance(eq(SELLER_ID), eq(SPACE_ID), any());
        
        // イベント発行も正常実行
        verify(eventPublisher, times(1)).publishEvent(any(SfrPaymentService.PurchaseCompletedEvent.class));
    }

    @Test
    void testPurchaseRequest_データ整合性() {
        // Given & When & Then
        assertEquals(BUYER_ID, validRequest.getBuyerId());
        assertEquals(SELLER_ID, validRequest.getSellerId());
        assertEquals(ITEM_ID, validRequest.getItemId());
        assertEquals(SPACE_ID, validRequest.getSpaceId());
        assertEquals(PURCHASE_AMOUNT, validRequest.getTotalPrice());
    }

    @Test
    void testPurchaseResult_成功結果() {
        // Given & When
        SfrPaymentService.PurchaseResult successResult = 
            SfrPaymentService.PurchaseResult.success("購入成功");

        // Then
        assertTrue(successResult.isSuccess());
        assertEquals("購入成功", successResult.getMessage());
    }

    @Test
    void testPurchaseResult_失敗結果() {
        // Given & When
        SfrPaymentService.PurchaseResult failureResult = 
            SfrPaymentService.PurchaseResult.failure("エラー発生");

        // Then
        assertFalse(failureResult.isSuccess());
        assertEquals("エラー発生", failureResult.getMessage());
    }
}
