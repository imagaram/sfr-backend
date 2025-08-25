package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.RewardDistribution;
import com.sfr.tokyo.sfr_backend.repository.crypto.RewardDistributionRepository;
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
 * RewardDistributionService
 * SFR リワード配布のビジネスロジック層
 * リワード管理、配布処理、統計分析などの中核機能を提供
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardDistributionService {

    private final RewardDistributionRepository rewardDistributionRepository;

    // ===== 基本CRUD操作 =====

    /**
     * リワード配布をIDで取得
     */
    @Transactional(readOnly = true)
    public Optional<RewardDistribution> findById(Long id) {
        log.debug("Finding reward distribution by ID: {}", id);
        return rewardDistributionRepository.findById(id);
    }

    /**
     * 全リワード配布を取得（ページネーション付き）
     */
    @Transactional(readOnly = true)
    public Page<RewardDistribution> findAll(int page, int size, String sortBy) {
        log.debug("Finding all reward distributions with pagination");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return rewardDistributionRepository.findAll(pageable);
    }

    /**
     * ユーザーのリワード配布履歴を取得
     */
    @Transactional(readOnly = true)
    public Page<RewardDistribution> findByUserId(UUID userId, int page, int size) {
        log.debug("Finding reward distributions for userId: {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "distributionDate"));
        return rewardDistributionRepository.findByUserId(userId, pageable);
    }

    /**
     * スペースのリワード配布履歴を取得
     */
    @Transactional(readOnly = true)
    public Page<RewardDistribution> findBySpaceId(Long spaceId, int page, int size) {
        log.debug("Finding reward distributions for spaceId: {}", spaceId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "distributionDate"));
        return rewardDistributionRepository.findBySpaceId(spaceId, pageable);
    }

    /**
     * ステータス別リワード配布を取得
     */
    @Transactional(readOnly = true)
    public Page<RewardDistribution> findByStatus(RewardDistribution.DistributionStatus status, int page, int size) {
        log.debug("Finding reward distributions by status: {}", status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "distributionDate"));
        // Repository にメソッドがないため、全件取得してフィルタリング
        List<RewardDistribution> allRewards = rewardDistributionRepository.findAll();
        List<RewardDistribution> filteredRewards = allRewards.stream()
                .filter(r -> r.getStatus() == status)
                .sorted((a, b) -> b.getDistributionDate().compareTo(a.getDistributionDate()))
                .toList();

        int start = page * size;
        int end = Math.min(start + size, filteredRewards.size());
        List<RewardDistribution> pageContent = filteredRewards.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filteredRewards.size());
    }

    /**
     * 新しいリワード配布を作成
     */
    @Transactional
    public RewardDistribution createRewardDistribution(RewardDistribution rewardDistribution) {
        log.info("Creating new reward distribution for userId: {}, spaceId: {}, amount: {}",
                rewardDistribution.getUserId(), rewardDistribution.getSpaceId(), rewardDistribution.getAmount());

        // バリデーション
        validateRewardDistribution(rewardDistribution);

        // 期限切れチェック
        if (rewardDistribution.isExpired()) {
            rewardDistribution.markAsExpired();
        }

        RewardDistribution saved = rewardDistributionRepository.save(rewardDistribution);
        log.info("Created reward distribution with ID: {}", saved.getId());
        return saved;
    }

    /**
     * リワード配布を更新
     */
    @Transactional
    public RewardDistribution updateRewardDistribution(RewardDistribution rewardDistribution) {
        log.debug("Updating reward distribution with ID: {}", rewardDistribution.getId());
        return rewardDistributionRepository.save(rewardDistribution);
    }

    /**
     * リワード配布を削除
     */
    @Transactional
    public void deleteRewardDistribution(Long id) {
        log.info("Deleting reward distribution with ID: {}", id);
        rewardDistributionRepository.deleteById(id);
    }

    // ===== リワード管理操作 =====

    /**
     * リワードを承認
     */
    @Transactional
    public RewardDistribution approveReward(Long id, UUID approver) {
        log.info("Approving reward distribution with ID: {}, approver: {}", id, approver);

        RewardDistribution reward = getRewardDistribution(id);

        boolean success = reward.approve(approver);
        if (!success) {
            throw new IllegalStateException("Cannot approve reward in current status: " + reward.getStatus());
        }

        RewardDistribution updated = rewardDistributionRepository.save(reward);
        log.info("Reward approved successfully");
        return updated;
    }

    /**
     * リワードを処理
     */
    @Transactional
    public RewardDistribution processReward(Long id, String transactionHash) {
        log.info("Processing reward distribution with ID: {}, transactionHash: {}", id, transactionHash);

        RewardDistribution reward = getRewardDistribution(id);

        if (!reward.isProcessable()) {
            throw new IllegalStateException("Reward is not processable");
        }

        reward.markAsProcessing();
        RewardDistribution processing = rewardDistributionRepository.save(reward);

        try {
            // ここで実際のトークン配布処理を行う
            // 成功した場合
            processing.markAsCompleted(transactionHash);
            RewardDistribution completed = rewardDistributionRepository.save(processing);
            log.info("Reward processed successfully");
            return completed;
        } catch (Exception e) {
            // 失敗した場合
            processing.markAsFailed(e.getMessage());
            rewardDistributionRepository.save(processing);
            log.error("Failed to process reward: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * リワードをキャンセル
     */
    @Transactional
    public RewardDistribution cancelReward(Long id, UUID canceller, String reason) {
        log.info("Cancelling reward distribution with ID: {}, canceller: {}, reason: {}",
                id, canceller, reason);

        RewardDistribution reward = getRewardDistribution(id);
        reward.cancel(canceller, reason);

        RewardDistribution updated = rewardDistributionRepository.save(reward);
        log.info("Reward cancelled successfully");
        return updated;
    }

    /**
     * 期限切れリワードを処理
     */
    @Transactional
    public List<RewardDistribution> processExpiredRewards() {
        log.info("Processing expired rewards");

        List<RewardDistribution> allRewards = rewardDistributionRepository.findAll();
        List<RewardDistribution> pendingRewards = allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.PENDING ||
                        r.getStatus() == RewardDistribution.DistributionStatus.APPROVED)
                .toList();

        List<RewardDistribution> expiredRewards = pendingRewards.stream()
                .filter(RewardDistribution::isExpired)
                .toList();

        for (RewardDistribution reward : expiredRewards) {
            reward.markAsExpired();
        }

        List<RewardDistribution> updated = rewardDistributionRepository.saveAll(expiredRewards);
        log.info("Processed {} expired rewards", updated.size());
        return updated;
    }

    /**
     * バッチ処理でリワードを承認
     */
    @Transactional
    public List<RewardDistribution> batchApproveRewards(List<Long> ids, UUID approver) {
        log.info("Batch approving {} rewards, approver: {}", ids.size(), approver);

        List<RewardDistribution> rewards = rewardDistributionRepository.findAllById(ids);

        for (RewardDistribution reward : rewards) {
            reward.approve(approver);
        }

        List<RewardDistribution> updated = rewardDistributionRepository.saveAll(rewards);
        log.info("Batch approved {} rewards", updated.size());
        return updated;
    }

    // ===== リワード作成ヘルパーメソッド =====

    /**
     * コンテンツ作成リワードを作成
     */
    @Transactional
    public RewardDistribution createContentReward(Long spaceId, UUID userId, BigDecimal amount,
            String contentId, String reason) {
        log.info("Creating content reward for userId: {}, spaceId: {}, amount: {}",
                userId, spaceId, amount);

        RewardDistribution reward = RewardDistribution.createContentReward(
                spaceId, userId, amount, contentId, reason);

        return createRewardDistribution(reward);
    }

    /**
     * 学習進捗リワードを作成
     */
    @Transactional
    public RewardDistribution createLearningReward(Long spaceId, UUID userId, BigDecimal amount,
            String progressId, BigDecimal progressScore) {
        log.info("Creating learning reward for userId: {}, spaceId: {}, amount: {}",
                userId, spaceId, amount);

        RewardDistribution reward = RewardDistribution.createLearningReward(
                spaceId, userId, amount, progressId, progressScore);

        return createRewardDistribution(reward);
    }

    /**
     * ガバナンス参加リワードを作成
     */
    @Transactional
    public RewardDistribution createGovernanceReward(Long spaceId, UUID userId, BigDecimal amount,
            String proposalId) {
        log.info("Creating governance reward for userId: {}, spaceId: {}, amount: {}",
                userId, spaceId, amount);

        RewardDistribution reward = RewardDistribution.createGovernanceReward(
                spaceId, userId, amount, proposalId);

        return createRewardDistribution(reward);
    }

    // ===== 統計・分析メソッド =====

    /**
     * ユーザーの総リワード獲得量を取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalRewardsByUser(UUID userId) {
        log.debug("Calculating total rewards for userId: {}", userId);
        List<RewardDistribution> allRewards = rewardDistributionRepository.findByUserId(userId);

        return allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.COMPLETED)
                .map(RewardDistribution::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * スペースの総リワード配布量を取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalRewardsBySpace(Long spaceId) {
        log.debug("Calculating total rewards for spaceId: {}", spaceId);
        List<RewardDistribution> allRewards = rewardDistributionRepository.findBySpaceId(spaceId);

        return allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.COMPLETED)
                .map(RewardDistribution::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * カテゴリ別リワード統計を取得
     */
    @Transactional(readOnly = true)
    public List<CategoryRewardStatistics> getRewardStatisticsByCategory(Long spaceId) {
        log.debug("Getting reward statistics by category for spaceId: {}", spaceId);

        List<RewardDistribution> allRewards = rewardDistributionRepository.findBySpaceId(spaceId);
        List<RewardDistribution> completedRewards = allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.COMPLETED)
                .toList();

        return completedRewards.stream()
                .collect(java.util.stream.Collectors.groupingBy(RewardDistribution::getCategory))
                .entrySet().stream()
                .map(entry -> {
                    RewardDistribution.RewardCategory category = entry.getKey();
                    List<RewardDistribution> rewards = entry.getValue();

                    BigDecimal totalAmount = rewards.stream()
                            .map(RewardDistribution::getFinalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Long count = (long) rewards.size();

                    BigDecimal averageAmount = count > 0
                            ? totalAmount.divide(BigDecimal.valueOf(count), 8, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return CategoryRewardStatistics.builder()
                            .category(category)
                            .totalAmount(totalAmount)
                            .count(count)
                            .averageAmount(averageAmount)
                            .build();
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .toList();
    }

    /**
     * 保留中のリワード数を取得
     */
    @Transactional(readOnly = true)
    public Long getPendingRewardsCount() {
        log.debug("Counting pending rewards");
        List<RewardDistribution> allRewards = rewardDistributionRepository.findAll();
        return allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.PENDING)
                .count();
    }

    /**
     * 処理待ちのリワード数を取得
     */
    @Transactional(readOnly = true)
    public Long getApprovedRewardsCount() {
        log.debug("Counting approved rewards");
        List<RewardDistribution> allRewards = rewardDistributionRepository.findAll();
        return allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.APPROVED)
                .count();
    }

    // ===== バリデーション・ヘルパーメソッド =====

    /**
     * リワード配布を取得（存在しない場合は例外をスロー）
     */
    private RewardDistribution getRewardDistribution(Long id) {
        return rewardDistributionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Reward distribution not found for ID: %s", id)));
    }

    /**
     * リワード配布のバリデーション
     */
    private void validateRewardDistribution(RewardDistribution reward) {
        if (reward.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Reward amount must be positive");
        }

        if (reward.getSpaceId() == null) {
            throw new IllegalArgumentException("Space ID is required");
        }

        if (reward.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (reward.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        if (reward.getTriggerType() == null) {
            throw new IllegalArgumentException("Trigger type is required");
        }
    }

    /**
     * ユーザーが期間内に受け取ったリワード総額を取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getUserRewardsInPeriod(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting user rewards for userId: {} in period {} to {}", userId, startDate, endDate);

        List<RewardDistribution> allRewards = rewardDistributionRepository.findByUserId(userId);

        return allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.COMPLETED)
                .filter(r -> r.getDistributionDate().isAfter(startDate) && r.getDistributionDate().isBefore(endDate))
                .map(RewardDistribution::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ===== 詳細統計メソッド =====

    /**
     * システム全体のリワード統計を取得
     */
    @Transactional(readOnly = true)
    public SystemRewardStatistics getSystemRewardStatistics() {
        log.debug("Getting system reward statistics");

        List<RewardDistribution> allRewards = rewardDistributionRepository.findAll();

        if (allRewards.isEmpty()) {
            return SystemRewardStatistics.builder()
                    .totalRewards(0L)
                    .completedRewards(0L)
                    .pendingRewards(0L)
                    .failedRewards(0L)
                    .totalAmount(BigDecimal.ZERO)
                    .averageRewardAmount(BigDecimal.ZERO)
                    .maxRewardAmount(BigDecimal.ZERO)
                    .uniqueRecipients(0L)
                    .build();
        }

        long totalRewards = allRewards.size();
        long completedRewards = allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.COMPLETED)
                .count();
        long pendingRewards = allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.PENDING)
                .count();
        long failedRewards = allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.FAILED)
                .count();

        BigDecimal totalAmount = allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.COMPLETED)
                .map(RewardDistribution::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageRewardAmount = completedRewards > 0
                ? totalAmount.divide(BigDecimal.valueOf(completedRewards), 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal maxRewardAmount = allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.COMPLETED)
                .map(RewardDistribution::getFinalAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        long uniqueRecipients = allRewards.stream()
                .filter(r -> r.getStatus() == RewardDistribution.DistributionStatus.COMPLETED)
                .map(RewardDistribution::getUserId)
                .distinct()
                .count();

        return SystemRewardStatistics.builder()
                .totalRewards(totalRewards)
                .completedRewards(completedRewards)
                .pendingRewards(pendingRewards)
                .failedRewards(failedRewards)
                .totalAmount(totalAmount)
                .averageRewardAmount(averageRewardAmount)
                .maxRewardAmount(maxRewardAmount)
                .uniqueRecipients(uniqueRecipients)
                .build();
    }

    // ===== 内部DTO クラス =====

    @lombok.Data
    @lombok.Builder
    public static class CategoryRewardStatistics {
        private RewardDistribution.RewardCategory category;
        private BigDecimal totalAmount;
        private Long count;
        private BigDecimal averageAmount;
    }

    @lombok.Data
    @lombok.Builder
    public static class SystemRewardStatistics {
        private Long totalRewards;
        private Long completedRewards;
        private Long pendingRewards;
        private Long failedRewards;
        private BigDecimal totalAmount;
        private BigDecimal averageRewardAmount;
        private BigDecimal maxRewardAmount;
        private Long uniqueRecipients;
    }
}
