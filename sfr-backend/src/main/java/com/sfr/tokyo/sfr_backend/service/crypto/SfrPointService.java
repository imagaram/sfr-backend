package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPoint;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SFRポイント管理サービス
 * 
 * Phase 1 実装: SFRサイト内ポイントシステムのビジネスロジック
 * 1SFR = 150円固定レートでの残高管理
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SfrPointService {

    private final SfrPointRepository sfrPointRepository;

    /**
     * ユーザーのSFRポイントを取得または作成
     */
    public SfrPoint getOrCreateSfrPoint(String userId, Long spaceId) {
        return sfrPointRepository.findByUserIdAndSpaceId(userId, spaceId)
                .orElseGet(() -> createSfrPoint(userId, spaceId));
    }

    /**
     * 新規SFRポイントアカウントを作成
     */
    public SfrPoint createSfrPoint(String userId, Long spaceId) {
        log.info("Creating new SFR point account for user: {} in space: {}", userId, spaceId);
        
        SfrPoint sfrPoint = SfrPoint.builder()
                .userId(userId)
                .spaceId(spaceId)
                .currentBalance(BigDecimal.ZERO)
                .totalPurchased(BigDecimal.ZERO)
                .totalSpent(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .sfrtEligible(false)
                .status(SfrPoint.SfrPointStatus.ACTIVE)
                .build();
        
        return sfrPointRepository.save(sfrPoint);
    }

    /**
     * SFRポイント残高を取得
     */
    @Transactional(readOnly = true)
    public Optional<SfrPoint> getSfrPoint(String userId, Long spaceId) {
        return sfrPointRepository.findByUserIdAndSpaceId(userId, spaceId);
    }

    /**
     * ユーザーの全スペースSFRポイントを取得
     */
    @Transactional(readOnly = true)
    public List<SfrPoint> getUserSfrPoints(String userId) {
        return sfrPointRepository.findByUserId(userId);
    }

    /**
     * SFRポイント購入処理
     * @param userId ユーザーID
     * @param spaceId スペースID
     * @param sfrAmount 購入SFR金額
     * @return 更新されたSFRポイント
     */
    public SfrPoint purchaseSfrPoints(String userId, Long spaceId, BigDecimal sfrAmount) {
        log.info("Processing SFR point purchase: user={}, space={}, amount={}", userId, spaceId, sfrAmount);
        
        if (sfrAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("購入金額は0より大きい必要があります");
        }

        SfrPoint sfrPoint = getOrCreateSfrPoint(userId, spaceId);
        sfrPoint.recordPurchase(sfrAmount);
        
        SfrPoint saved = sfrPointRepository.save(sfrPoint);
        
        log.info("SFR point purchase completed: user={}, space={}, new_balance={}", 
                userId, spaceId, saved.getCurrentBalance());
        
        return saved;
    }

    /**
     * SFRポイント使用処理
     * @param userId ユーザーID
     * @param spaceId スペースID
     * @param sfrAmount 使用SFR金額
     * @return 使用成功可否
     */
    public boolean spendSfrPoints(String userId, Long spaceId, BigDecimal sfrAmount) {
        log.info("Processing SFR point spending: user={}, space={}, amount={}", userId, spaceId, sfrAmount);
        
        if (sfrAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("使用金額は0より大きい必要があります");
        }

        Optional<SfrPoint> sfrPointOpt = getSfrPoint(userId, spaceId);
        if (sfrPointOpt.isEmpty()) {
            log.warn("SFR point account not found: user={}, space={}", userId, spaceId);
            return false;
        }

        SfrPoint sfrPoint = sfrPointOpt.get();
        boolean success = sfrPoint.recordSpending(sfrAmount);
        
        if (success) {
            sfrPointRepository.save(sfrPoint);
            log.info("SFR point spending completed: user={}, space={}, new_balance={}", 
                    userId, spaceId, sfrPoint.getCurrentBalance());
        } else {
            log.warn("Insufficient SFR balance: user={}, space={}, balance={}, required={}", 
                    userId, spaceId, sfrPoint.getCurrentBalance(), sfrAmount);
        }
        
        return success;
    }

    /**
     * SFRポイント獲得処理（報酬等）
     * @param userId ユーザーID
     * @param spaceId スペースID
     * @param sfrAmount 獲得SFR金額
     * @return 更新されたSFRポイント
     */
    public SfrPoint earnSfrPoints(String userId, Long spaceId, BigDecimal sfrAmount) {
        log.info("Processing SFR point earning: user={}, space={}, amount={}", userId, spaceId, sfrAmount);
        
        if (sfrAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("獲得金額は0より大きい必要があります");
        }

        SfrPoint sfrPoint = getOrCreateSfrPoint(userId, spaceId);
        sfrPoint.recordEarning(sfrAmount);
        
        SfrPoint saved = sfrPointRepository.save(sfrPoint);
        
        log.info("SFR point earning completed: user={}, space={}, new_balance={}", 
                userId, spaceId, saved.getCurrentBalance());
        
        return saved;
    }

    /**
     * SFRポイント残高チェック
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(String userId, Long spaceId, BigDecimal requiredAmount) {
        return getSfrPoint(userId, spaceId)
                .map(sfrPoint -> sfrPoint.getCurrentBalance().compareTo(requiredAmount) >= 0)
                .orElse(false);
    }

    /**
     * SFRポイント残高を円換算で取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalanceInYen(String userId, Long spaceId) {
        return getSfrPoint(userId, spaceId)
                .map(SfrPoint::getBalanceInYen)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * ユーザーの全SFRポイント残高を円換算で取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBalanceInYen(String userId) {
        return getUserSfrPoints(userId).stream()
                .map(SfrPoint::getBalanceInYen)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * SFRT配布対象ユーザーを取得
     */
    @Transactional(readOnly = true)
    public List<SfrPoint> getSfrtEligibleUsers() {
        return sfrPointRepository.findBySfrtEligibleAndStatus(SfrPoint.SfrPointStatus.ACTIVE);
    }

    /**
     * SFRT配布処理完了マーク
     */
    public void markSfrtDistributed(String userId, Long spaceId) {
        getSfrPoint(userId, spaceId).ifPresent(sfrPoint -> {
            sfrPoint.setLastSfrtDistribution(LocalDateTime.now());
            sfrPointRepository.save(sfrPoint);
            log.info("Marked SFRT distribution completed: user={}, space={}", userId, spaceId);
        });
    }

    /**
     * SFRポイントステータス更新
     */
    public boolean updateStatus(String userId, Long spaceId, SfrPoint.SfrPointStatus newStatus) {
        Optional<SfrPoint> sfrPointOpt = getSfrPoint(userId, spaceId);
        if (sfrPointOpt.isPresent()) {
            SfrPoint sfrPoint = sfrPointOpt.get();
            SfrPoint.SfrPointStatus oldStatus = sfrPoint.getStatus();
            sfrPoint.setStatus(newStatus);
            sfrPointRepository.save(sfrPoint);
            
            log.info("SFR point status updated: user={}, space={}, status: {} -> {}", 
                    userId, spaceId, oldStatus, newStatus);
            return true;
        }
        return false;
    }

    /**
     * スペース統計情報取得
     */
    @Transactional(readOnly = true)
    public SpaceStatistics getSpaceStatistics(Long spaceId) {
        BigDecimal totalBalance = sfrPointRepository.calculateTotalBalanceBySpace(spaceId, SfrPoint.SfrPointStatus.ACTIVE);
        long activeUsers = sfrPointRepository.countBySpaceIdAndStatus(spaceId, SfrPoint.SfrPointStatus.ACTIVE);
        
        return new SpaceStatistics(spaceId, totalBalance, activeUsers);
    }

    /**
     * 高額保有者リスト取得
     */
    @Transactional(readOnly = true)
    public List<SfrPoint> getTopBalanceHolders(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return sfrPointRepository.findTopBalanceHolders(SfrPoint.SfrPointStatus.ACTIVE, pageable);
    }

    /**
     * スペース統計情報クラス
     */
    public static class SpaceStatistics {
        private final Long spaceId;
        private final BigDecimal totalBalance;
        private final long activeUsers;

        public SpaceStatistics(Long spaceId, BigDecimal totalBalance, long activeUsers) {
            this.spaceId = spaceId;
            this.totalBalance = totalBalance;
            this.activeUsers = activeUsers;
        }

        // Getters
        public Long getSpaceId() { return spaceId; }
        public BigDecimal getTotalBalance() { return totalBalance; }
        public long getActiveUsers() { return activeUsers; }
        public BigDecimal getTotalBalanceInYen() { return totalBalance.multiply(new BigDecimal("150")); }
    }
}
