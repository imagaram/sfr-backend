package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.TokenPool;
import com.sfr.tokyo.sfr_backend.repository.crypto.TokenPoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TokenPoolService
 * SFR トークンプールのビジネスロジック層
 * プール管理、トークン発行・バーン、プール統計などの中核機能を提供
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenPoolService {

    private final TokenPoolRepository tokenPoolRepository;

    // ===== 基本CRUD操作 =====

    /**
     * トークンプールをIDで取得
     */
    @Transactional(readOnly = true)
    public Optional<TokenPool> findById(Long id) {
        log.debug("Finding token pool by ID: {}", id);
        return tokenPoolRepository.findById(id);
    }

    /**
     * スペースIDでトークンプールを取得
     */
    @Transactional(readOnly = true)
    public Optional<TokenPool> findBySpaceId(Long spaceId) {
        log.debug("Finding token pool by space ID: {}", spaceId);
        return tokenPoolRepository.findBySpaceId(spaceId);
    }

    /**
     * 全トークンプールを取得（ページネーション付き）
     */
    @Transactional(readOnly = true)
    public Page<TokenPool> findAll(int page, int size, String sortBy) {
        log.debug("Finding all token pools with pagination");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return tokenPoolRepository.findAll(pageable);
    }

    /**
     * 複数のスペースIDでトークンプールを取得
     */
    @Transactional(readOnly = true)
    public List<TokenPool> findBySpaceIds(List<Long> spaceIds) {
        log.debug("Finding token pools by space IDs: {}", spaceIds);
        return tokenPoolRepository.findBySpaceIdIn(spaceIds);
    }

    /**
     * 新しいトークンプールを作成
     */
    @Transactional
    public TokenPool createTokenPool(Long spaceId, UUID adminUserId, BigDecimal maxSupply) {
        log.info("Creating new token pool for spaceId: {}, adminUserId: {}, maxSupply: {}",
                spaceId, adminUserId, maxSupply);

        // 既存のプールがないことを確認
        Optional<TokenPool> existing = tokenPoolRepository.findBySpaceId(spaceId);
        if (existing.isPresent()) {
            log.warn("Token pool already exists for spaceId: {}", spaceId);
            throw new IllegalStateException("Token pool already exists for this space");
        }

        TokenPool tokenPool = TokenPool.builder()
                .spaceId(spaceId)
                .totalSupply(BigDecimal.ZERO)
                .issuedAmount(BigDecimal.ZERO)
                .burnedAmount(BigDecimal.ZERO)
                .circulatingSupply(BigDecimal.ZERO)
                .reservePool(BigDecimal.ZERO)
                .rewardPool(BigDecimal.ZERO)
                .governancePool(BigDecimal.ZERO)
                .ecosystemPool(BigDecimal.ZERO)
                .issueRate(new BigDecimal("0.001000"))
                .burnRate(new BigDecimal("0.000500"))
                .collectionThreshold(new BigDecimal("1000.00000000"))
                .maxSupply(maxSupply)
                .status(TokenPool.PoolStatus.ACTIVE)
                .adminUserId(adminUserId)
                .build();

        TokenPool saved = tokenPoolRepository.save(tokenPool);
        log.info("Created token pool with ID: {}", saved.getId());
        return saved;
    }

    /**
     * トークンプールを更新
     */
    @Transactional
    public TokenPool updateTokenPool(TokenPool tokenPool) {
        log.debug("Updating token pool with ID: {}", tokenPool.getId());
        return tokenPoolRepository.save(tokenPool);
    }

    /**
     * トークンプールを削除
     */
    @Transactional
    public void deleteTokenPool(Long id) {
        log.info("Deleting token pool with ID: {}", id);
        tokenPoolRepository.deleteById(id);
    }

    // ===== トークン発行・バーン操作 =====

    /**
     * トークンを発行
     */
    @Transactional
    public TokenPool issueTokens(Long spaceId, BigDecimal amount, String reason) {
        log.info("Issuing tokens for spaceId: {}, amount: {}, reason: {}",
                spaceId, amount, reason);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Issue amount must be positive");
        }

        TokenPool tokenPool = getTokenPool(spaceId);

        if (tokenPool.getStatus() != TokenPool.PoolStatus.ACTIVE) {
            throw new IllegalStateException("Cannot issue tokens for inactive pool");
        }

        // エンティティのビジネスメソッドを使用
        boolean success = tokenPool.issueTokens(amount);
        if (!success) {
            throw new IllegalStateException("Token issuance failed. Check pool limits and status.");
        }

        TokenPool updated = tokenPoolRepository.save(tokenPool);
        log.info("Tokens issued successfully. New total supply: {}", updated.getTotalSupply());
        return updated;
    }

    /**
     * トークンをバーン
     */
    @Transactional
    public TokenPool burnTokens(Long spaceId, BigDecimal amount, String reason) {
        log.info("Burning tokens for spaceId: {}, amount: {}, reason: {}",
                spaceId, amount, reason);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Burn amount must be positive");
        }

        TokenPool tokenPool = getTokenPool(spaceId);

        if (tokenPool.getStatus() != TokenPool.PoolStatus.ACTIVE) {
            throw new IllegalStateException("Cannot burn tokens for inactive pool");
        }

        // エンティティのビジネスメソッドを使用
        boolean success = tokenPool.burnTokens(amount);
        if (!success) {
            throw new IllegalStateException("Token burn failed. Check circulating supply.");
        }

        TokenPool updated = tokenPoolRepository.save(tokenPool);
        log.info("Tokens burned successfully. New circulating supply: {}", updated.getCirculatingSupply());
        return updated;
    }

    /**
     * リワードを配布
     */
    @Transactional
    public TokenPool distributeRewards(Long spaceId, BigDecimal amount, String reason) {
        log.info("Distributing rewards for spaceId: {}, amount: {}, reason: {}",
                spaceId, amount, reason);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Reward amount must be positive");
        }

        TokenPool tokenPool = getTokenPool(spaceId);

        if (tokenPool.getStatus() != TokenPool.PoolStatus.ACTIVE) {
            throw new IllegalStateException("Cannot distribute rewards for inactive pool");
        }

        // エンティティのビジネスメソッドを使用
        boolean success = tokenPool.distributeRewards(amount);
        if (!success) {
            throw new IllegalStateException("Reward distribution failed. Check reward pool balance.");
        }

        TokenPool updated = tokenPoolRepository.save(tokenPool);
        log.info("Rewards distributed successfully. Remaining reward pool: {}", updated.getRewardPool());
        return updated;
    }

    // ===== プール管理操作 =====

    /**
     * プールステータスを変更
     */
    @Transactional
    public TokenPool changePoolStatus(Long spaceId, TokenPool.PoolStatus newStatus, String reason) {
        log.info("Changing pool status for spaceId: {}, newStatus: {}, reason: {}",
                spaceId, newStatus, reason);

        TokenPool tokenPool = getTokenPool(spaceId);
        tokenPool.setStatus(newStatus);

        TokenPool updated = tokenPoolRepository.save(tokenPool);
        log.info("Pool status changed successfully");
        return updated;
    }

    /**
     * プール設定を更新
     */
    @Transactional
    public TokenPool updatePoolSettings(Long spaceId, BigDecimal issueRate, BigDecimal burnRate,
            BigDecimal collectionThreshold) {
        log.info("Updating pool settings for spaceId: {}, issueRate: {}, burnRate: {}, collectionThreshold: {}",
                spaceId, issueRate, burnRate, collectionThreshold);

        TokenPool tokenPool = getTokenPool(spaceId);

        if (issueRate != null) {
            tokenPool.setIssueRate(issueRate);
        }
        if (burnRate != null) {
            tokenPool.setBurnRate(burnRate);
        }
        if (collectionThreshold != null) {
            tokenPool.setCollectionThreshold(collectionThreshold);
        }

        TokenPool updated = tokenPoolRepository.save(tokenPool);
        log.info("Pool settings updated successfully");
        return updated;
    }

    /**
     * プール状態を更新
     */
    @Transactional
    public TokenPool updatePoolStatus(Long spaceId) {
        log.debug("Updating pool status for spaceId: {}", spaceId);

        TokenPool tokenPool = getTokenPool(spaceId);
        tokenPool.updatePoolStatus();

        TokenPool updated = tokenPoolRepository.save(tokenPool);
        log.debug("Pool status updated successfully");
        return updated;
    }

    // ===== 統計・分析メソッド =====

    /**
     * 総トークン供給量を取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalSupplyAcrossAllPools() {
        log.debug("Calculating total supply across all pools");
        List<TokenPool> allPools = tokenPoolRepository.findAll();
        return allPools.stream()
                .map(TokenPool::getTotalSupply)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 総流通量を取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalCirculatingSupply() {
        log.debug("Calculating total circulating supply");
        List<TokenPool> allPools = tokenPoolRepository.findAll();
        return allPools.stream()
                .map(TokenPool::getCirculatingSupply)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 総バーン量を取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBurnedAmount() {
        log.debug("Calculating total burned amount");
        List<TokenPool> allPools = tokenPoolRepository.findAll();
        return allPools.stream()
                .map(TokenPool::getBurnedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * アクティブプール数を取得
     */
    @Transactional(readOnly = true)
    public Long getActivePoolCount() {
        log.debug("Counting active pools");
        List<TokenPool> allPools = tokenPoolRepository.findAll();
        return allPools.stream()
                .filter(pool -> pool.getStatus() == TokenPool.PoolStatus.ACTIVE)
                .count();
    }

    /**
     * プール健全性チェック
     */
    @Transactional(readOnly = true)
    public List<TokenPool> getUnhealthyPools() {
        log.debug("Finding unhealthy pools");
        List<TokenPool> allPools = tokenPoolRepository.findAll();
        return allPools.stream()
                .filter(pool -> !pool.isHealthy())
                .toList();
    }

    /**
     * 回収対象プールを取得
     */
    @Transactional(readOnly = true)
    public List<TokenPool> getCollectionTargetPools(BigDecimal userBalance) {
        log.debug("Finding collection target pools for balance: {}", userBalance);
        List<TokenPool> allPools = tokenPoolRepository.findAll();
        return allPools.stream()
                .filter(pool -> pool.isCollectionTarget(userBalance))
                .toList();
    }

    // ===== バリデーション・ヘルパーメソッド =====

    /**
     * トークンプールを取得（存在しない場合は例外をスロー）
     */
    private TokenPool getTokenPool(Long spaceId) {
        return tokenPoolRepository.findBySpaceId(spaceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Token pool not found for spaceId: %s", spaceId)));
    }

    /**
     * プールがアクティブかチェック
     */
    @Transactional(readOnly = true)
    public boolean isPoolActive(Long spaceId) {
        log.debug("Checking if pool is active for spaceId: {}", spaceId);

        Optional<TokenPool> tokenPool = tokenPoolRepository.findBySpaceId(spaceId);
        return tokenPool.map(pool -> pool.getStatus() == TokenPool.PoolStatus.ACTIVE).orElse(false);
    }

    /**
     * 発行可能量をチェック
     */
    @Transactional(readOnly = true)
    public BigDecimal getIssuableAmount(Long spaceId) {
        log.debug("Getting issuable amount for spaceId: {}", spaceId);

        TokenPool tokenPool = getTokenPool(spaceId);
        return tokenPool.getIssuableAmount();
    }

    /**
     * プールが健全かチェック
     */
    @Transactional(readOnly = true)
    public boolean isPoolHealthy(Long spaceId) {
        log.debug("Checking pool health for spaceId: {}", spaceId);

        TokenPool tokenPool = getTokenPool(spaceId);
        return tokenPool.isHealthy();
    }

    // ===== 詳細統計メソッド =====

    /**
     * プール統計を取得
     */
    @Transactional(readOnly = true)
    public PoolStatistics getPoolStatistics(Long spaceId) {
        log.debug("Getting pool statistics for spaceId: {}", spaceId);

        TokenPool tokenPool = getTokenPool(spaceId);

        return PoolStatistics.builder()
                .spaceId(spaceId)
                .totalSupply(tokenPool.getTotalSupply())
                .issuedAmount(tokenPool.getIssuedAmount())
                .burnedAmount(tokenPool.getBurnedAmount())
                .circulatingSupply(tokenPool.getCirculatingSupply())
                .reservePool(tokenPool.getReservePool())
                .rewardPool(tokenPool.getRewardPool())
                .governancePool(tokenPool.getGovernancePool())
                .ecosystemPool(tokenPool.getEcosystemPool())
                .issueRate(tokenPool.getIssueRate())
                .burnRate(tokenPool.getBurnRate())
                .collectionThreshold(tokenPool.getCollectionThreshold())
                .maxSupply(tokenPool.getMaxSupply())
                .issuableAmount(tokenPool.getIssuableAmount())
                .isHealthy(tokenPool.isHealthy())
                .status(tokenPool.getStatus())
                .lastRewardDistribution(tokenPool.getLastRewardDistribution())
                .lastCollectionCheck(tokenPool.getLastCollectionCheck())
                .lastBurnDecision(tokenPool.getLastBurnDecision())
                .build();
    }

    /**
     * システム全体のプール統計を取得
     */
    @Transactional(readOnly = true)
    public SystemPoolStatistics getSystemPoolStatistics() {
        log.debug("Getting system pool statistics");

        List<TokenPool> allPools = tokenPoolRepository.findAll();

        if (allPools.isEmpty()) {
            return SystemPoolStatistics.builder()
                    .totalPools(0L)
                    .activePools(0L)
                    .pausedPools(0L)
                    .totalSupply(BigDecimal.ZERO)
                    .totalCirculatingSupply(BigDecimal.ZERO)
                    .totalBurnedAmount(BigDecimal.ZERO)
                    .totalRewardPool(BigDecimal.ZERO)
                    .totalGovernancePool(BigDecimal.ZERO)
                    .totalEcosystemPool(BigDecimal.ZERO)
                    .totalReservePool(BigDecimal.ZERO)
                    .averageIssueRate(BigDecimal.ZERO)
                    .averageBurnRate(BigDecimal.ZERO)
                    .healthyPoolCount(0L)
                    .build();
        }

        long totalPools = allPools.size();
        long activePools = allPools.stream()
                .filter(pool -> pool.getStatus() == TokenPool.PoolStatus.ACTIVE)
                .count();
        long pausedPools = allPools.stream()
                .filter(pool -> pool.getStatus() == TokenPool.PoolStatus.PAUSED)
                .count();

        BigDecimal totalSupply = allPools.stream()
                .map(TokenPool::getTotalSupply)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCirculatingSupply = allPools.stream()
                .map(TokenPool::getCirculatingSupply)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBurnedAmount = allPools.stream()
                .map(TokenPool::getBurnedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRewardPool = allPools.stream()
                .map(TokenPool::getRewardPool)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGovernancePool = allPools.stream()
                .map(TokenPool::getGovernancePool)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEcosystemPool = allPools.stream()
                .map(TokenPool::getEcosystemPool)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReservePool = allPools.stream()
                .map(TokenPool::getReservePool)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageIssueRate = allPools.stream()
                .map(TokenPool::getIssueRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalPools), 8, RoundingMode.HALF_UP);

        BigDecimal averageBurnRate = allPools.stream()
                .map(TokenPool::getBurnRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalPools), 8, RoundingMode.HALF_UP);

        long healthyPoolCount = allPools.stream()
                .filter(TokenPool::isHealthy)
                .count();

        return SystemPoolStatistics.builder()
                .totalPools(totalPools)
                .activePools(activePools)
                .pausedPools(pausedPools)
                .totalSupply(totalSupply)
                .totalCirculatingSupply(totalCirculatingSupply)
                .totalBurnedAmount(totalBurnedAmount)
                .totalRewardPool(totalRewardPool)
                .totalGovernancePool(totalGovernancePool)
                .totalEcosystemPool(totalEcosystemPool)
                .totalReservePool(totalReservePool)
                .averageIssueRate(averageIssueRate)
                .averageBurnRate(averageBurnRate)
                .healthyPoolCount(healthyPoolCount)
                .build();
    }

    // ===== 内部DTO クラス =====

    @lombok.Data
    @lombok.Builder
    public static class PoolStatistics {
        private Long spaceId;
        private BigDecimal totalSupply;
        private BigDecimal issuedAmount;
        private BigDecimal burnedAmount;
        private BigDecimal circulatingSupply;
        private BigDecimal reservePool;
        private BigDecimal rewardPool;
        private BigDecimal governancePool;
        private BigDecimal ecosystemPool;
        private BigDecimal issueRate;
        private BigDecimal burnRate;
        private BigDecimal collectionThreshold;
        private BigDecimal maxSupply;
        private BigDecimal issuableAmount;
        private boolean isHealthy;
        private TokenPool.PoolStatus status;
        private LocalDateTime lastRewardDistribution;
        private LocalDateTime lastCollectionCheck;
        private LocalDateTime lastBurnDecision;
    }

    @lombok.Data
    @lombok.Builder
    public static class SystemPoolStatistics {
        private Long totalPools;
        private Long activePools;
        private Long pausedPools;
        private BigDecimal totalSupply;
        private BigDecimal totalCirculatingSupply;
        private BigDecimal totalBurnedAmount;
        private BigDecimal totalRewardPool;
        private BigDecimal totalGovernancePool;
        private BigDecimal totalEcosystemPool;
        private BigDecimal totalReservePool;
        private BigDecimal averageIssueRate;
        private BigDecimal averageBurnRate;
        private Long healthyPoolCount;
    }
}
