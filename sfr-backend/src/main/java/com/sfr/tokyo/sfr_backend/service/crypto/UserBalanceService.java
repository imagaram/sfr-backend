package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalanceId;
import com.sfr.tokyo.sfr_backend.repository.crypto.UserBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing user balances with composite key support
 */
@Service
@Transactional
public class UserBalanceService {

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    /**
     * Create a new user balance
     */
    public UserBalance createUserBalance(String userId, Long spaceId, BigDecimal initialBalance) {
        UserBalance userBalance = new UserBalance();
        userBalance.setUserId(userId);
        userBalance.setSpaceId(spaceId);
        userBalance.setCurrentBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        return userBalanceRepository.save(userBalance);
    }

    /**
     * Get user balance by composite key
     */
    public Optional<UserBalance> getUserBalance(String userId, Long spaceId) {
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        return userBalanceRepository.findById(id);
    }

    /**
     * Get all user balances by user ID
     */
    public List<UserBalance> getUserBalancesByUserId(String userId) {
        return userBalanceRepository.findByUserId(userId);
    }

    /**
     * Get all user balances by space ID
     */
    public List<UserBalance> getUserBalancesBySpaceId(Long spaceId) {
        // PageableでfindBySpaceIdを呼び出す
        Pageable pageable = Pageable.unpaged();
        Page<UserBalance> balancePage = userBalanceRepository.findBySpaceId(spaceId, pageable);
        return balancePage.getContent();
    }

    /**
     * Get user balances by space ID with pagination
     */
    public Page<UserBalance> getUserBalancesBySpaceId(Long spaceId, Pageable pageable) {
        return userBalanceRepository.findBySpaceId(spaceId, pageable);
    }

    /**
     * Update user balance
     */
    public UserBalance updateUserBalance(String userId, Long spaceId, BigDecimal newBalance) {
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        Optional<UserBalance> existing = userBalanceRepository.findById(id);

        if (existing.isPresent()) {
            UserBalance userBalance = existing.get();
            userBalance.setCurrentBalance(newBalance);
            return userBalanceRepository.save(userBalance);
        } else {
            return createUserBalance(userId, spaceId, newBalance);
        }
    }

    /**
     * Add to user balance
     */
    public UserBalance addBalance(String userId, Long spaceId, BigDecimal amount) {
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        Optional<UserBalance> existing = userBalanceRepository.findById(id);

        if (existing.isPresent()) {
            UserBalance userBalance = existing.get();
            BigDecimal currentBalance = userBalance.getCurrentBalance() != null ? userBalance.getCurrentBalance()
                    : BigDecimal.ZERO;
            userBalance.setCurrentBalance(currentBalance.add(amount));
            return userBalanceRepository.save(userBalance);
        } else {
            return createUserBalance(userId, spaceId, amount);
        }
    }

    /**
     * Subtract from user balance
     */
    public UserBalance subtractBalance(String userId, Long spaceId, BigDecimal amount) {
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        Optional<UserBalance> existing = userBalanceRepository.findById(id);

        if (existing.isPresent()) {
            UserBalance userBalance = existing.get();
            BigDecimal currentBalance = userBalance.getCurrentBalance() != null ? userBalance.getCurrentBalance()
                    : BigDecimal.ZERO;
            BigDecimal newBalance = currentBalance.subtract(amount);

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient balance. Current: " + currentBalance + ", Requested: " + amount);
            }

            userBalance.setCurrentBalance(newBalance);
            return userBalanceRepository.save(userBalance);
        } else {
            throw new IllegalArgumentException(
                    "User balance not found for userId: " + userId + ", spaceId: " + spaceId);
        }
    }

    /**
     * Check if user has sufficient balance
     */
    public boolean hasSufficientBalance(String userId, Long spaceId, BigDecimal requiredAmount) {
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        Optional<UserBalance> userBalance = userBalanceRepository.findById(id);

        if (userBalance.isPresent()) {
            BigDecimal currentBalance = userBalance.get().getCurrentBalance() != null
                    ? userBalance.get().getCurrentBalance()
                    : BigDecimal.ZERO;
            return currentBalance.compareTo(requiredAmount) >= 0;
        }

        return false;
    }

    /**
     * Get all user balances
     */
    public List<UserBalance> getAllUserBalances() {
        return userBalanceRepository.findAll();
    }

    /**
     * Get all user balances with pagination
     */
    public Page<UserBalance> getAllUserBalances(Pageable pageable) {
        return userBalanceRepository.findAll(pageable);
    }

    /**
     * Delete user balance by composite key
     */
    public void deleteUserBalance(String userId, Long spaceId) {
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        if (userBalanceRepository.existsById(id)) {
            userBalanceRepository.deleteById(id);
        }
    }

    /**
     * Check if user balance exists
     */
    public boolean existsUserBalance(String userId, Long spaceId) {
        UserBalanceId id = new UserBalanceId(userId, spaceId);
        return userBalanceRepository.existsById(id);
    }

    /**
     * Get user balances with balance greater than specified amount
     */
    public List<UserBalance> getUserBalancesGreaterThan(BigDecimal amount) {
        // 簡略化：現在のRepositoryにはこのメソッドが存在しないため、基本的な実装を使用
        return userBalanceRepository.findAll().stream()
                .filter(balance -> balance.getCurrentBalance() != null &&
                        balance.getCurrentBalance().compareTo(amount) > 0)
                .toList();
    }

    /**
     * Get user balances with balance less than specified amount
     */
    public List<UserBalance> getUserBalancesLessThan(BigDecimal amount) {
        // 簡略化：現在のRepositoryにはこのメソッドが存在しないため、基本的な実装を使用
        return userBalanceRepository.findAll().stream()
                .filter(balance -> balance.getCurrentBalance() != null &&
                        balance.getCurrentBalance().compareTo(amount) < 0)
                .toList();
    }

    /**
     * Get total balance for a space
     */
    public BigDecimal getTotalBalanceForSpace(Long spaceId) {
        // PageableでfindBySpaceIdを呼び出す
        Pageable pageable = Pageable.unpaged();
        Page<UserBalance> balancePage = userBalanceRepository.findBySpaceId(spaceId, pageable);
        List<UserBalance> balances = balancePage.getContent();

        return balances.stream()
                .map(UserBalance::getCurrentBalance)
                .filter(balance -> balance != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get user count for a space
     */
    public long getUserCountForSpace(Long spaceId) {
        // PageableでfindBySpaceIdを呼び出してカウント
        Pageable pageable = Pageable.unpaged();
        Page<UserBalance> balancePage = userBalanceRepository.findBySpaceId(spaceId, pageable);
        return balancePage.getTotalElements();
    }

    /**
     * Get space count for a user
     */
    public long getSpaceCountForUser(String userId) {
        // UserIdでフィルタリングしてカウント
        List<UserBalance> userBalances = userBalanceRepository.findByUserId(userId);
        return userBalances.size();
    }
}
