package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserBalanceService 統合テスト
 * Repository層とService層の統合動作をテスト
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserBalanceService 統合テスト")
class UserBalanceServiceSimpleIntegrationTest {

    @Autowired
    private UserBalanceService userBalanceService;

    @Test
    @DisplayName("ユーザー残高の作成と取得のテスト")
    void testCreateAndGetUserBalance() {
        // Given: テストデータの準備
        String userId = "test-user-service";
        Long spaceId = 1L;
        BigDecimal initialBalance = new BigDecimal("1000.00000000");

        // When: ユーザー残高を作成
        UserBalance created = userBalanceService.createUserBalance(userId, spaceId, initialBalance);

        // Then: 作成結果の検証
        assertThat(created).isNotNull();
        assertThat(created.getUserId()).isEqualTo(userId);
        assertThat(created.getSpaceId()).isEqualTo(spaceId);
        assertThat(created.getCurrentBalance()).isEqualByComparingTo(initialBalance);
        assertThat(created.getTotalEarned()).isEqualByComparingTo(initialBalance);

        // When: 作成した残高を取得
        Optional<UserBalance> found = userBalanceService.getUserBalance(userId, spaceId);

        // Then: 取得結果の検証
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getCurrentBalance()).isEqualByComparingTo(initialBalance);
    }

    @Test
    @DisplayName("残高の増加と減少のテスト")
    void testAddAndSubtractBalance() {
        // Given: 初期残高を持つユーザーを作成
        String userId = "test-user-operations";
        Long spaceId = 1L;
        BigDecimal initialBalance = new BigDecimal("1000.00000000");

        userBalanceService.createUserBalance(userId, spaceId, initialBalance);

        // When: 残高を増加
        BigDecimal addAmount = new BigDecimal("500.00000000");
        UserBalance afterAdd = userBalanceService.addBalance(userId, spaceId, addAmount);

        // Then: 増加後の検証
        assertThat(afterAdd.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1500.00000000"));
        assertThat(afterAdd.getTotalEarned()).isEqualByComparingTo(new BigDecimal("1500.00000000"));

        // When: 残高を減少
        BigDecimal subtractAmount = new BigDecimal("300.00000000");
        UserBalance afterSubtract = userBalanceService.subtractBalance(userId, spaceId, subtractAmount);

        // Then: 減少後の検証
        assertThat(afterSubtract.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1200.00000000"));
        assertThat(afterSubtract.getTotalSpent()).isEqualByComparingTo(subtractAmount);
    }

    @Test
    @DisplayName("残高チェック機能のテスト")
    void testBalanceChecking() {
        // Given: 初期残高を持つユーザーを作成
        String userId = "test-user-check";
        Long spaceId = 1L;
        BigDecimal initialBalance = new BigDecimal("1000.00000000");

        userBalanceService.createUserBalance(userId, spaceId, initialBalance);

        // When/Then: 残高取得のテスト
        Optional<UserBalance> userBalance = userBalanceService.getUserBalance(userId, spaceId);
        assertThat(userBalance).isPresent();
        assertThat(userBalance.get().getCurrentBalance()).isEqualByComparingTo(initialBalance);

        // When/Then: 十分な残高があるかのテスト
        BigDecimal testAmount = new BigDecimal("500.00000000");
        boolean sufficient = userBalance.get().getCurrentBalance().compareTo(testAmount) >= 0;
        assertThat(sufficient).isTrue();

        BigDecimal largeAmount = new BigDecimal("2000.00000000");
        boolean insufficient = userBalance.get().getCurrentBalance().compareTo(largeAmount) >= 0;
        assertThat(insufficient).isFalse();
    }

    @Test
    @DisplayName("重複作成エラーのテスト")
    void testDuplicateCreationError() {
        // Given: 既存のユーザー残高
        String userId = "test-user-duplicate";
        Long spaceId = 1L;
        BigDecimal initialBalance = new BigDecimal("1000.00000000");

        userBalanceService.createUserBalance(userId, spaceId, initialBalance);

        // When/Then: 同じユーザーとスペースで再作成しようとするとエラー
        assertThatThrownBy(() -> {
            userBalanceService.createUserBalance(userId, spaceId, initialBalance);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ユーザー残高が既に存在します");
    }

    @Test
    @DisplayName("存在しないユーザーへの操作エラーのテスト")
    void testNonExistentUserOperationError() {
        // Given: 存在しないユーザー
        String nonExistentUserId = "non-existent-user";
        Long spaceId = 1L;
        BigDecimal amount = new BigDecimal("100.00000000");

        // When/Then: 存在しないユーザーの残高を増加しようとするとエラー
        assertThatThrownBy(() -> {
            userBalanceService.addBalance(nonExistentUserId, spaceId, amount);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ユーザー残高が見つかりません");

        // When/Then: 存在しないユーザーの残高を減少しようとするとエラー
        assertThatThrownBy(() -> {
            userBalanceService.subtractBalance(nonExistentUserId, spaceId, amount);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ユーザー残高が見つかりません");
    }

    @Test
    @DisplayName("残高不足エラーのテスト")
    void testInsufficientBalanceError() {
        // Given: 少額残高のユーザーを作成
        String userId = "test-user-insufficient";
        Long spaceId = 1L;
        BigDecimal initialBalance = new BigDecimal("100.00000000");

        userBalanceService.createUserBalance(userId, spaceId, initialBalance);

        // When/Then: 残高以上の金額を減少しようとするとエラー
        BigDecimal excessiveAmount = new BigDecimal("200.00000000");
        assertThatThrownBy(() -> {
            userBalanceService.subtractBalance(userId, spaceId, excessiveAmount);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("残高が不足しています");
    }
}
