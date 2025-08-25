package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalanceId;
import com.sfr.tokyo.sfr_backend.test.integration.BaseRepositoryIntegrationTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserBalanceRepository 統合テスト
 * H2データベースを使用したRepository層のテスト
 */
@DisplayName("UserBalance Repository 統合テスト")
class UserBalanceRepositoryIntegrationTest extends BaseRepositoryIntegrationTest {

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Test
    @DisplayName("ユーザー残高の作成と検索のテスト")
    void testCreateAndFindUserBalance() {
        // Given: テストデータの準備
        UserBalance userBalance = UserBalance.builder()
                .userId("test-user-123")
                .spaceId(1L)
                .currentBalance(new BigDecimal("1000.50000000"))
                .totalEarned(new BigDecimal("1000.50000000"))
                .totalSpent(BigDecimal.ZERO)
                .frozen(false)
                .build();

        // When: データベースに保存
        UserBalance savedBalance = userBalanceRepository.save(userBalance);
        entityManager.flush();
        clearEntityManager();

        // Then: 保存されたデータの検証
        assertThat(savedBalance).isNotNull();
        assertThat(savedBalance.getUserId()).isEqualTo("test-user-123");
        assertThat(savedBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1000.50000000"));

        // When: 検索実行
        UserBalanceId id = new UserBalanceId("test-user-123", 1L);
        Optional<UserBalance> found = userBalanceRepository.findById(id);

        // Then: 検索結果の検証
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("test-user-123");
        assertThat(found.get().getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1000.50000000"));
    }

    @Test
    @DisplayName("複数ユーザーの残高検索のテスト")
    void testFindMultipleUsers() {
        // Given: 複数のユーザー残高を作成
        UserBalance balance1 = UserBalance.builder()
                .userId("user-1")
                .spaceId(1L)
                .currentBalance(new BigDecimal("500.00000000"))
                .totalEarned(new BigDecimal("500.00000000"))
                .build();

        UserBalance balance2 = UserBalance.builder()
                .userId("user-2")
                .spaceId(1L)
                .currentBalance(new BigDecimal("300.00000000"))
                .totalEarned(new BigDecimal("300.00000000"))
                .build();

        userBalanceRepository.save(balance1);
        userBalanceRepository.save(balance2);
        entityManager.flush();
        clearEntityManager();

        // When: 全ユーザーを検索
        List<UserBalance> allBalances = userBalanceRepository.findAll();

        // Then: 結果の検証
        assertThat(allBalances).hasSize(2);
        assertThat(allBalances)
                .extracting("userId")
                .containsExactlyInAnyOrder("user-1", "user-2");
    }

    @Test
    @DisplayName("ページネーション検索のテスト")
    void testFindAllWithPagination() {
        // Given: 複数のユーザー残高を作成
        for (int i = 1; i <= 5; i++) {
            UserBalance balance = UserBalance.builder()
                    .userId("page-user-" + i)
                    .spaceId(1L)
                    .currentBalance(new BigDecimal(i * 100 + ".00000000"))
                    .totalEarned(new BigDecimal(i * 100 + ".00000000"))
                    .build();
            userBalanceRepository.save(balance);
        }
        entityManager.flush();
        clearEntityManager();

        // When: ページネーションで検索
        Pageable pageable = PageRequest.of(0, 3);
        Page<UserBalance> page = userBalanceRepository.findAll(pageable);

        // Then: ページング結果の検証
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("残高の更新テスト")
    void testUpdateBalance() {
        // Given: 初期データの作成
        UserBalance userBalance = UserBalance.builder()
                .userId("update-test-user")
                .spaceId(1L)
                .currentBalance(new BigDecimal("1000.00000000"))
                .totalEarned(new BigDecimal("1000.00000000"))
                .build();

        UserBalance savedBalance = userBalanceRepository.save(userBalance);
        entityManager.flush();
        clearEntityManager();

        // When: 残高を更新
        savedBalance.setCurrentBalance(new BigDecimal("1500.00000000"));
        savedBalance.setTotalEarned(new BigDecimal("1500.00000000"));

        userBalanceRepository.save(savedBalance);
        entityManager.flush();
        clearEntityManager();

        // Then: 更新結果の検証
        UserBalanceId updateId = new UserBalanceId("update-test-user", 1L);
        Optional<UserBalance> found = userBalanceRepository.findById(updateId);

        assertThat(found).isPresent();
        assertThat(found.get().getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1500.00000000"));
        assertThat(found.get().getTotalEarned()).isEqualByComparingTo(new BigDecimal("1500.00000000"));
    }

    @Test
    @DisplayName("残高増加ビジネスメソッドのテスト")
    void testAddBalance() {
        // Given: 初期データの作成
        UserBalance userBalance = UserBalance.builder()
                .userId("business-test-user")
                .spaceId(1L)
                .currentBalance(new BigDecimal("1000.00000000"))
                .totalEarned(new BigDecimal("1000.00000000"))
                .build();

        UserBalance savedBalance = userBalanceRepository.save(userBalance);
        entityManager.flush();
        clearEntityManager();

        // When: ビジネスメソッドで残高を増加
        savedBalance.addBalance(new BigDecimal("500.00000000"));
        userBalanceRepository.save(savedBalance);
        entityManager.flush();
        clearEntityManager();

        // Then: 結果の検証
        UserBalanceId businessId = new UserBalanceId("business-test-user", 1L);
        Optional<UserBalance> found = userBalanceRepository.findById(businessId);

        assertThat(found).isPresent();
        assertThat(found.get().getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1500.00000000"));
        assertThat(found.get().getTotalEarned()).isEqualByComparingTo(new BigDecimal("1500.00000000"));
    }

    @Test
    @DisplayName("存在しないユーザーの検索テスト")
    void testFindNonExistentUser() {
        // When: 存在しないユーザーを検索
        UserBalanceId nonExistentId = new UserBalanceId("non-existent-user", 1L);
        Optional<UserBalance> result = userBalanceRepository.findById(nonExistentId);

        // Then: 結果がemptyであることを確認
        assertThat(result).isEmpty();
    }
}
