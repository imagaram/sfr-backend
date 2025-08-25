package com.sfr.tokyo.sfr_backend.controller.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * TokenController のユニットテスト
 * Spring コンテキストを使わずに純粋なモック テストとして実装
 */
@ExtendWith(MockitoExtension.class)
class TokenControllerSimpleTest {

    @Mock
    private UserBalanceService userBalanceService;

    @Mock
    private BalanceHistoryService balanceHistoryService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TokenController tokenController;

    @Test
    void testGetUserBalance() {
        // Given
        String userId = "test-user-123";
        Long spaceId = 1L;

        UserBalance userBalance = new UserBalance();
        userBalance.setUserId(userId);
        userBalance.setSpaceId(spaceId);
        userBalance.setCurrentBalance(BigDecimal.valueOf(1000.0));

        // When
        when(userBalanceService.getUserBalance(userId, spaceId)).thenReturn(Optional.of(userBalance));

        ResponseEntity<?> response = tokenController.getUserBalance(userId);

        // Then
        assertEquals(200, response.getStatusCode().value());
        verify(userBalanceService).getUserBalance(userId, spaceId);
    }

    @Test
    void testGetBalanceWithAuthentication() {
        // Given
        String userId = "test-user-456";
        Long spaceId = 1L;

        UserBalance userBalance = new UserBalance();
        userBalance.setUserId(userId);
        userBalance.setSpaceId(spaceId);
        userBalance.setCurrentBalance(BigDecimal.valueOf(1500.0));

        // Mock Authentication to return username
        when(authentication.getName()).thenReturn(userId);
        when(userBalanceService.getUserBalance(userId, spaceId)).thenReturn(Optional.of(userBalance));

        ResponseEntity<?> response = tokenController.getBalance(authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
        verify(userBalanceService).getUserBalance(userId, spaceId);
    }

    @Test
    void testControllerCreation() {
        // Test that the controller can be created
        assertNotNull(tokenController);
        assertNotNull(userBalanceService);
        assertNotNull(balanceHistoryService);
    }
}
