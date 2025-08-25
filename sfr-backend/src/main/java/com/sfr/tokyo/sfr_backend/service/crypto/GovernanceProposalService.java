package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.GovernanceProposal;
import com.sfr.tokyo.sfr_backend.repository.crypto.GovernanceProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
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
import java.util.UUID;

/**
 * ガバナンス提案サービス
 * ガバナンス提案の作成、管理、投票実行を担当
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GovernanceProposalService {

    private final GovernanceProposalRepository governanceProposalRepository;

    /**
     * ガバナンス提案を作成
     */
    @Transactional
    public GovernanceProposal createProposal(Long spaceId, UUID proposerId, String title, String description,
            GovernanceProposal.ProposalType proposalType,
            Integer minimumQuorum, int votingDurationHours,
            String parameters) {
        log.info("Creating governance proposal for space: {}, type: {}", spaceId, proposalType);

        GovernanceProposal proposal = GovernanceProposal.builder()
                .spaceId(spaceId)
                .proposerId(proposerId)
                .title(title)
                .description(description)
                .proposalType(proposalType)
                .minimumQuorum(minimumQuorum)
                .parameters(parameters)
                .status(GovernanceProposal.ProposalStatus.DRAFT)
                .votingStartDate(LocalDateTime.now().plusHours(1)) // 1時間後に投票開始
                .votingEndDate(LocalDateTime.now().plusHours(1 + votingDurationHours))
                .build();

        GovernanceProposal saved = governanceProposalRepository.save(proposal);
        log.info("Governance proposal created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * 緊急提案を作成
     */
    @Transactional
    public GovernanceProposal createEmergencyProposal(Long spaceId, UUID proposerId, String title, String description,
            String urgencyReason, String proposalData) {
        log.info("Creating emergency governance proposal for space: {}", spaceId);

        GovernanceProposal proposal = GovernanceProposal.builder()
                .spaceId(spaceId)
                .proposerId(proposerId)
                .title("[緊急] " + title)
                .description(description + "\n緊急理由: " + urgencyReason)
                .proposalType(GovernanceProposal.ProposalType.EMERGENCY_ACTION)
                .minimumQuorum(30) // 緊急提案は30で可決
                .parameters(proposalData)
                .status(GovernanceProposal.ProposalStatus.VOTING_ACTIVE)
                .votingStartDate(LocalDateTime.now()) // 即座に投票開始
                .votingEndDate(LocalDateTime.now().plusHours(24)) // 24時間で終了
                .build();

        GovernanceProposal saved = governanceProposalRepository.save(proposal);
        log.info("Emergency governance proposal created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * 提案を投票開始状態に変更
     */
    @Transactional
    public GovernanceProposal activateProposal(Long proposalId, UUID activatorId) {
        log.info("Activating governance proposal ID: {}", proposalId);

        GovernanceProposal proposal = getProposal(proposalId);

        if (proposal.getStatus() != GovernanceProposal.ProposalStatus.DRAFT) {
            throw new IllegalStateException("Only draft proposals can be activated");
        }

        proposal.setStatus(GovernanceProposal.ProposalStatus.VOTING_ACTIVE);
        // proposal.setActivatedBy(activatorId); // フィールドが存在しない
        // proposal.setActivatedAt(LocalDateTime.now()); // フィールドが存在しない

        GovernanceProposal updated = governanceProposalRepository.save(proposal);
        log.info("Governance proposal activated successfully");
        return updated;
    }

    /**
     * 提案をキャンセル
     */
    @Transactional
    public GovernanceProposal cancelProposal(Long proposalId, UUID cancellerId, String cancellationReason) {
        log.info("Cancelling governance proposal ID: {}", proposalId);

        GovernanceProposal proposal = getProposal(proposalId);

        if (proposal.getStatus() == GovernanceProposal.ProposalStatus.EXECUTED ||
                proposal.getStatus() == GovernanceProposal.ProposalStatus.REJECTED) {
            throw new IllegalStateException("Cannot cancel executed or failed proposals");
        }

        proposal.setStatus(GovernanceProposal.ProposalStatus.CANCELLED);
        proposal.setCancellationReason(cancellationReason);
        proposal.setCancelledBy(cancellerId);
        proposal.setCancelledAt(LocalDateTime.now());

        GovernanceProposal updated = governanceProposalRepository.save(proposal);
        log.info("Governance proposal cancelled successfully");
        return updated;
    }

    /**
     * 投票期間終了後の提案処理
     */
    @Transactional
    public GovernanceProposal finalizeProposal(Long proposalId) {
        log.info("Finalizing governance proposal ID: {}", proposalId);

        GovernanceProposal proposal = getProposal(proposalId);

        if (proposal.getStatus() != GovernanceProposal.ProposalStatus.VOTING_ACTIVE ||
                !proposal.isVotingEnded()) {
            throw new IllegalStateException("Proposal must be active and voting period must be ended");
        }

        // 投票結果の確定
        // boolean quorumReached = proposal.isQuorumReached(); // メソッドが存在しない
        // boolean approved = proposal.isApproved(); // メソッドが存在しない
        boolean quorumReached = proposal.getQuorumReached() != null && proposal.getQuorumReached();
        boolean approved = proposal.getVotingPowerFor().compareTo(proposal.getVotingPowerAgainst()) > 0;

        if (quorumReached && approved) {
            proposal.setStatus(GovernanceProposal.ProposalStatus.PASSED);
            log.info("Proposal passed - quorum: {}, approval: {}", quorumReached, approved);
        } else {
            proposal.setStatus(GovernanceProposal.ProposalStatus.REJECTED);
            log.info("Proposal rejected - quorum: {}, approval: {}", quorumReached, approved);
        }

        // proposal.setFinalizedAt(LocalDateTime.now()); // フィールドが存在しない

        GovernanceProposal updated = governanceProposalRepository.save(proposal);
        log.info("Governance proposal finalized with status: {}", updated.getStatus());
        return updated;
    }

    /**
     * 提案実行開始
     */
    @Transactional
    public GovernanceProposal startExecution(Long proposalId, UUID executorId) {
        log.info("Starting execution of governance proposal ID: {}", proposalId);

        GovernanceProposal proposal = getProposal(proposalId);

        if (proposal.getStatus() != GovernanceProposal.ProposalStatus.PASSED) {
            throw new IllegalStateException("Only passed proposals can be executed");
        }

        proposal.setStatus(GovernanceProposal.ProposalStatus.QUEUED);
        proposal.setExecutedBy(executorId);
        // proposal.setExecutionStartedAt(LocalDateTime.now()); // フィールドが存在しない

        GovernanceProposal updated = governanceProposalRepository.save(proposal);
        log.info("Proposal execution started");
        return updated;
    }

    /**
     * 提案実行完了
     */
    @Transactional
    public GovernanceProposal completeExecution(Long proposalId, String executionResults) {
        log.info("Completing execution of governance proposal ID: {}", proposalId);

        GovernanceProposal proposal = getProposal(proposalId);

        if (proposal.getStatus() != GovernanceProposal.ProposalStatus.QUEUED) {
            throw new IllegalStateException("Proposal must be in queued status");
        }

        proposal.setStatus(GovernanceProposal.ProposalStatus.EXECUTED);
        // proposal.setExecutionResults(executionResults); // フィールドが存在しない
        proposal.setExecutedAt(LocalDateTime.now());

        GovernanceProposal updated = governanceProposalRepository.save(proposal);
        log.info("Proposal execution completed");
        return updated;
    }

    /**
     * 提案実行失敗
     */
    @Transactional
    public GovernanceProposal markExecutionAsFailed(Long proposalId, String failureReason) {
        log.info("Marking execution as failed for governance proposal ID: {}", proposalId);

        GovernanceProposal proposal = getProposal(proposalId);

        if (proposal.getStatus() != GovernanceProposal.ProposalStatus.QUEUED) {
            throw new IllegalStateException("Proposal must be in queued status");
        }

        proposal.setStatus(GovernanceProposal.ProposalStatus.REJECTED);
        // proposal.setExecutionResults("実行失敗: " + failureReason); // フィールドが存在しない

        GovernanceProposal updated = governanceProposalRepository.save(proposal);
        log.info("Proposal execution marked as failed");
        return updated;
    }

    /**
     * 提案取得
     */
    @Transactional(readOnly = true)
    public GovernanceProposal getProposal(Long id) {
        return governanceProposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Governance proposal not found with ID: " + id));
    }

    /**
     * スペース別提案取得
     */
    @Transactional(readOnly = true)
    public Page<GovernanceProposal> getSpaceProposals(Long spaceId, int page, int size) {
        log.info("Getting governance proposals for space: {}, page: {}, size: {}", spaceId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        return governanceProposalRepository.findBySpaceId(spaceId, pageable);
    }

    /**
     * ステータス別提案取得
     */
    @Transactional(readOnly = true)
    public Page<GovernanceProposal> getProposalsByStatus(GovernanceProposal.ProposalStatus status, int page, int size) {
        log.info("Getting governance proposals by status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        // 全件取得してフィルタリング
        List<GovernanceProposal> filteredProposals = governanceProposalRepository.findAll().stream()
                .filter(p -> p.getStatus() == status)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        // 手動でページネーション
        int start = page * size;
        int end = Math.min(start + size, filteredProposals.size());
        List<GovernanceProposal> pageContent = start < filteredProposals.size() ? filteredProposals.subList(start, end)
                : List.of();

        return new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, filteredProposals.size());
    }

    /**
     * アクティブな投票中の提案取得
     */
    @Transactional(readOnly = true)
    public List<GovernanceProposal> getActiveProposals() {
        log.info("Getting active governance proposals");
        return governanceProposalRepository.findAll().stream()
                .filter(p -> p.getStatus() == GovernanceProposal.ProposalStatus.VOTING_ACTIVE &&
                        !p.isVotingEnded())
                .sorted((a, b) -> a.getVotingEndDate().compareTo(b.getVotingEndDate()))
                .toList();
    }

    /**
     * 投票期間終了済みの未処理提案取得
     */
    @Transactional(readOnly = true)
    public List<GovernanceProposal> getProposalsPendingFinalization() {
        log.info("Getting proposals pending finalization");
        return governanceProposalRepository.findAll().stream()
                .filter(p -> p.getStatus() == GovernanceProposal.ProposalStatus.VOTING_ACTIVE &&
                        p.isVotingEnded())
                // getFinalizedAt()メソッドが利用できないため、finalization判定をskip
                .sorted((a, b) -> a.getVotingEndDate().compareTo(b.getVotingEndDate()))
                .toList();
    }

    /**
     * 実行待ちの提案取得
     */
    @Transactional(readOnly = true)
    public List<GovernanceProposal> getProposalsPendingExecution() {
        log.info("Getting proposals pending execution");
        return governanceProposalRepository.findAll().stream()
                .filter(p -> p.getStatus() == GovernanceProposal.ProposalStatus.PASSED)
                .sorted((a, b) -> a.getVotingEndDate().compareTo(b.getVotingEndDate())) // getFinalizedAtの代わりにVotingEndDateを使用
                .toList();
    }

    /**
     * ガバナンス統計取得
     */
    @Transactional(readOnly = true)
    public GovernanceStatistics getGovernanceStatistics(Long spaceId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting governance statistics for space: {} from {} to {}", spaceId, startDate, endDate);

        List<GovernanceProposal> proposals = governanceProposalRepository.findBySpaceId(spaceId, Pageable.unpaged())
                .getContent().stream()
                .filter(p -> p.getCreatedAt().isAfter(startDate) && p.getCreatedAt().isBefore(endDate))
                .toList();

        if (proposals.isEmpty()) {
            return GovernanceStatistics.builder()
                    .totalProposals(0L)
                    .activeProposals(0L)
                    .passedProposals(0L)
                    .rejectedProposals(0L)
                    .executedProposals(0L)
                    .failedProposals(0L)
                    .averageQuorum(BigDecimal.ZERO)
                    .averageApprovalRate(BigDecimal.ZERO)
                    .participationRate(BigDecimal.ZERO)
                    .build();
        }

        long totalProposals = proposals.size();
        long activeProposals = proposals.stream()
                .mapToLong(p -> p.getStatus() == GovernanceProposal.ProposalStatus.VOTING_ACTIVE ? 1 : 0).sum();
        long passedProposals = proposals.stream()
                .mapToLong(p -> p.getStatus() == GovernanceProposal.ProposalStatus.PASSED ? 1 : 0).sum();
        long rejectedProposals = proposals.stream()
                .mapToLong(p -> p.getStatus() == GovernanceProposal.ProposalStatus.REJECTED ? 1 : 0).sum();
        long executedProposals = proposals.stream()
                .mapToLong(p -> p.getStatus() == GovernanceProposal.ProposalStatus.EXECUTED ? 1 : 0).sum();
        long failedProposals = proposals.stream()
                .mapToLong(p -> p.getStatus() == GovernanceProposal.ProposalStatus.REJECTED ? 1 : 0).sum();

        // 平均クォーラム計算
        BigDecimal averageQuorum = proposals.stream()
                .map(p -> BigDecimal.valueOf(p.getMinimumQuorum()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalProposals), 4, RoundingMode.HALF_UP);

        // 平均承認率計算（投票が終了したもののみ）
        List<GovernanceProposal> votedProposals = proposals.stream()
                .filter(p -> p.getTotalVotes() > 0)
                .toList();

        BigDecimal averageApprovalRate = BigDecimal.ZERO;
        if (!votedProposals.isEmpty()) {
            BigDecimal totalApprovalRate = votedProposals.stream()
                    // .map(GovernanceProposal::getApprovalRate) // メソッドが存在しない
                    .map(p -> p.getVotingPowerFor().divide(p.getTotalVotingPower().max(BigDecimal.ONE), 4,
                            java.math.RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageApprovalRate = totalApprovalRate.divide(
                    BigDecimal.valueOf(votedProposals.size()), 4, RoundingMode.HALF_UP);
        }

        // 参加率計算（総投票数／提案数）
        int totalVotes = proposals.stream()
                .mapToInt(GovernanceProposal::getTotalVotes)
                .sum();
        BigDecimal participationRate = totalProposals > 0
                ? BigDecimal.valueOf(totalVotes).divide(BigDecimal.valueOf(totalProposals), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return GovernanceStatistics.builder()
                .totalProposals(totalProposals)
                .activeProposals(activeProposals)
                .passedProposals(passedProposals)
                .rejectedProposals(rejectedProposals)
                .executedProposals(executedProposals)
                .failedProposals(failedProposals)
                .averageQuorum(averageQuorum)
                .averageApprovalRate(averageApprovalRate)
                .participationRate(participationRate)
                .build();
    }

    /**
     * 提案タイプ別統計取得
     */
    @Transactional(readOnly = true)
    public List<ProposalTypeStatistics> getProposalTypeStatistics(Long spaceId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("Getting proposal type statistics for space: {} from {} to {}", spaceId, startDate, endDate);

        List<GovernanceProposal> proposals = governanceProposalRepository.findBySpaceId(spaceId, Pageable.unpaged())
                .getContent().stream()
                .filter(p -> p.getCreatedAt().isAfter(startDate) && p.getCreatedAt().isBefore(endDate))
                .toList();

        return proposals.stream()
                .collect(java.util.stream.Collectors.groupingBy(GovernanceProposal::getProposalType))
                .entrySet().stream()
                .map(entry -> {
                    GovernanceProposal.ProposalType proposalType = entry.getKey();
                    List<GovernanceProposal> typeProposals = entry.getValue();

                    long passedCount = typeProposals.stream()
                            .mapToLong(p -> p.getStatus() == GovernanceProposal.ProposalStatus.PASSED ||
                                    p.getStatus() == GovernanceProposal.ProposalStatus.EXECUTED ? 1 : 0)
                            .sum();

                    double successRate = typeProposals.size() > 0 ? (double) passedCount / typeProposals.size() * 100
                            : 0.0;

                    return ProposalTypeStatistics.builder()
                            .proposalType(proposalType)
                            .count(typeProposals.size())
                            .passedCount(passedCount)
                            .successRate(BigDecimal.valueOf(successRate))
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getCount(), a.getCount()))
                .toList();
    }

    // === 内部DTOクラス ===

    @Data
    @Builder
    public static class GovernanceStatistics {
        private Long totalProposals;
        private Long activeProposals;
        private Long passedProposals;
        private Long rejectedProposals;
        private Long executedProposals;
        private Long failedProposals;
        private BigDecimal averageQuorum;
        private BigDecimal averageApprovalRate;
        private BigDecimal participationRate;
    }

    @Data
    @Builder
    public static class ProposalTypeStatistics {
        private GovernanceProposal.ProposalType proposalType;
        private Integer count;
        private Long passedCount;
        private BigDecimal successRate;
    }
}
