package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalanceId;
import com.sfr.tokyo.sfr_backend.repository.crypto.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * ユーザー残高サービス
 * SFRトークンのユーザー残高管理を行います
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserBalanceServiceSimple {

    private final UserBalanceRepository userBalanceRepository;

    // ===== 基本CRUD操作 =====

    /**
     * ユーザー残高を取得（スペース指定）
     */
    @Transactional(readOnly = true)
    public Optional<UserBalance> findByUserIdAndSpaceId(String userId, Long spaceId) {
        log.debug("Finding user balance by userId: {} and spaceId: {}", userId, spaceId);
        return userBalanceRepository.findBySpaceIdAndUserId(spaceId, userId);
    }

    /**
     * ユーザー残高を取得（ID指定）
     */
    @Transactional(readOnly = true)
    public Optional<UserBalance> findById(String userId, Long spaceId) {
        log.debug("Finding user balance by ID: userId={}, spaceId={}", userId, spaceId);
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        return userBalanceRepository.findById(id);
    }

    /**
     * 全ユーザー残高を取得（ページネーション付き）
     */
    @Transactional(readOnly = true)
    public Page<UserBalance> findAll(int page, int size, String sortBy) {
        log.debug("Finding all user balances with pagination");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return userBalanceRepository.findAll(pageable);
    }

    /**
     * スペース内の全ユーザー残高を取得（ページネーション付き）
     */
    @Transactional(readOnly = true)
    public Page<UserBalance> findBySpaceId(Long spaceId, int page, int size) {
        log.debug("Finding user balances by spaceId: {} with pagination", spaceId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "currentBalance"));
        return userBalanceRepository.findBySpaceId(spaceId, pageable);
    }

    /**
     * ユーザー残高を作成
     */
    @Transactional
    public UserBalance createUserBalance(String userId, Long spaceId, BigDecimal initialBalance) {
        log.debug("Creating user balance for userId: {}, spaceId: {}, initialBalance: {}",
                userId, spaceId, initialBalance);

        // 既存残高をチェック
        Optional<UserBalance> existing = userBalanceRepository.findBySpaceIdAndUserId(spaceId, userId);
        if (existing.isPresent()) {
            log.warn("User balance already exists for userId: {} and spaceId: {}", userId, spaceId);
            throw new IllegalArgumentException("ユーザー残高が既に存在します");
        }

        // 新しい残高を作成
        UserBalance userBalance = UserBalance.builder()
                .userId(userId)
                .spaceId(spaceId)
                .currentBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .totalEarned(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .totalSpent(BigDecimal.ZERO)
                .frozen(false)
                .collectionExempt(false)
                .build();

        UserBalance saved = userBalanceRepository.save(userBalance);
        log.info("Created user balance with ID: {}-{}", saved.getUserId(), saved.getSpaceId());
        return saved;
    }

    /**
     * ユーザー残高を更新
     */
    @Transactional
    public UserBalance updateUserBalance(UserBalance userBalance) {
        log.debug("Updating user balance: {}-{}", userBalance.getUserId(), userBalance.getSpaceId());
        return userBalanceRepository.save(userBalance);
    }

    /**
     * ユーザー残高を削除
     */
    @Transactional
    public void deleteById(String userId, Long spaceId) {
        log.debug("Deleting user balance by userId: {} and spaceId: {}", userId, spaceId);
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        userBalanceRepository.deleteById(id);
        log.info("Deleted user balance: {}-{}", userId, spaceId);
    }

    // ===== ビジネスロジック =====

    /**
     * 残高を増加させる
     */
    @Transactional
    public UserBalance addBalance(String userId, Long spaceId, BigDecimal amount) {
        log.debug("Adding balance: {} to user: {} in space: {}", amount, userId, spaceId);

        UserBalance userBalance = userBalanceRepository.findBySpaceIdAndUserId(spaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザー残高が見つかりません"));

        userBalance.addBalance(amount);
        UserBalance saved = userBalanceRepository.save(userBalance);

        log.info("Added balance: {} to user: {} in space: {}. New balance: {}",
                amount, userId, spaceId, saved.getCurrentBalance());
        return saved;
    }

    /**
     * 残高を減少させる
     */
    @Transactional
    public UserBalance subtractBalance(String userId, Long spaceId, BigDecimal amount) {
        log.debug("Subtracting balance: {} from user: {} in space: {}", amount, userId, spaceId);

        UserBalance userBalance = userBalanceRepository.findBySpaceIdAndUserId(spaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザー残高が見つかりません"));

        userBalance.subtractBalance(amount);
        UserBalance saved = userBalanceRepository.save(userBalance);

        log.info("Subtracted balance: {} from user: {} in space: {}. New balance: {}",
                amount, userId, spaceId, saved.getCurrentBalance());
        return saved;
    }

    /**
     * 残高を取得（検索用）
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId, Long spaceId) {
        log.debug("Getting balance for user: {} in space: {}", userId, spaceId);

        return userBalanceRepository.findBySpaceIdAndUserId(spaceId, userId)
                .map(UserBalance::getCurrentBalance)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 残高が十分かどうかをチェック
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(String userId, Long spaceId, BigDecimal requiredAmount) {
        log.debug("Checking sufficient balance for user: {} in space: {}, required: {}",
                userId, spaceId, requiredAmount);

        BigDecimal currentBalance = getBalance(userId, spaceId);
        boolean sufficient = currentBalance.compareTo(requiredAmount) >= 0;

        log.debug("User: {} has balance: {}, required: {}, sufficient: {}",
                userId, currentBalance, requiredAmount, sufficient);
        return sufficient;
    }
}
