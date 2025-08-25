package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.BurnDecision;
import com.sfr.tokyo.sfr_backend.repository.crypto.BurnDecisionRepository;
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
 * バーン判定サービス
 * トークンバーン（燃焼）の判定と管理を担当
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BurnDecisionService {

    private final BurnDecisionRepository burnDecisionRepository;

    /**
     * バーン判定を作成
     */
    @Transactional
    public BurnDecision createBurnDecision(Long spaceId, BigDecimal proposedBurnAmount,
            BigDecimal circulatingSupplyBefore,
            BurnDecision.DecisionType decisionType,
            BurnDecision.TriggerReason triggerReason,
            String decisionRationale) {
        log.info("Creating burn decision for space: {}, proposed burn: {}", spaceId, proposedBurnAmount);

        // バーン率計算
        BigDecimal burnRate = proposedBurnAmount.divide(circulatingSupplyBefore, 6, RoundingMode.HALF_UP);

        BurnDecision decision = BurnDecision.builder()
                .spaceId(spaceId)
                .proposedBurnAmount(proposedBurnAmount)
                .circulatingSupplyBefore(circulatingSupplyBefore)
                .burnRateProposed(burnRate)
                .decisionType(decisionType)
                .triggerReason(triggerReason)
                .decisionRationale(decisionRationale)
                .status(BurnDecision.BurnStatus.PROPOSED)
                .decisionDate(LocalDateTime.now())
                .build();

        BurnDecision saved = burnDecisionRepository.save(decision);
        log.info("Burn decision created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * AIによる自動バーン判定作成
     */
    @Transactional
    public BurnDecision createAiBurnDecision(Long spaceId, BigDecimal proposedBurnAmount,
            BigDecimal circulatingSupplyBefore,
            BurnDecision.TriggerReason triggerReason,
            BigDecimal aiConfidenceScore,
            String economicIndicators,
            String decisionRationale) {
        log.info("Creating AI burn decision for space: {}", spaceId);

        BigDecimal burnRate = proposedBurnAmount.divide(circulatingSupplyBefore, 6, RoundingMode.HALF_UP);

        BurnDecision decision = BurnDecision.builder()
                .spaceId(spaceId)
                .proposedBurnAmount(proposedBurnAmount)
                .circulatingSupplyBefore(circulatingSupplyBefore)
                .burnRateProposed(burnRate)
                .decisionType(BurnDecision.DecisionType.AI_AUTOMATIC)
                .triggerReason(triggerReason)
                .aiConfidenceScore(aiConfidenceScore)
                .economicIndicators(economicIndicators)
                .decisionRationale(decisionRationale)
                .status(BurnDecision.BurnStatus.APPROVED)
                .decisionDate(LocalDateTime.now())
                .build();

        BurnDecision saved = burnDecisionRepository.save(decision);
        log.info("AI burn decision created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * バーン判定を承認
     */
    @Transactional
    public BurnDecision approveBurnDecision(Long decisionId, UUID adminId, String adminNotes) {
        log.info("Approving burn decision ID: {} by admin: {}", decisionId, adminId);

        BurnDecision decision = getBurnDecision(decisionId);

        if (decision.getStatus() != BurnDecision.BurnStatus.PROPOSED &&
                decision.getStatus() != BurnDecision.BurnStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Burn decision cannot be approved in current status: " + decision.getStatus());
        }

        decision.setStatus(BurnDecision.BurnStatus.APPROVED);
        decision.setApprovedBy(adminId);
        decision.setApprovedAt(LocalDateTime.now());
        decision.setAdminNotes(adminNotes);

        BurnDecision updated = burnDecisionRepository.save(decision);
        log.info("Burn decision approved successfully");
        return updated;
    }

    /**
     * バーン判定を拒否
     */
    @Transactional
    public BurnDecision rejectBurnDecision(Long decisionId, UUID adminId, String rejectionReason) {
        log.info("Rejecting burn decision ID: {} by admin: {}", decisionId, adminId);

        BurnDecision decision = getBurnDecision(decisionId);

        if (decision.getStatus() != BurnDecision.BurnStatus.PROPOSED &&
                decision.getStatus() != BurnDecision.BurnStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Burn decision cannot be rejected in current status: " + decision.getStatus());
        }

        decision.setStatus(BurnDecision.BurnStatus.REJECTED);
        decision.setApprovedBy(adminId);
        decision.setApprovedAt(LocalDateTime.now());
        decision.setAdminNotes(rejectionReason);

        BurnDecision updated = burnDecisionRepository.save(decision);
        log.info("Burn decision rejected successfully");
        return updated;
    }

    /**
     * バーン実行開始
     */
    @Transactional
    public BurnDecision startBurnExecution(Long decisionId, UUID executorId) {
        log.info("Starting burn execution for decision ID: {}", decisionId);

        BurnDecision decision = getBurnDecision(decisionId);

        if (decision.getStatus() != BurnDecision.BurnStatus.APPROVED &&
                decision.getStatus() != BurnDecision.BurnStatus.SCHEDULED) {
            throw new IllegalStateException("Burn decision must be approved before execution");
        }

        decision.setStatus(BurnDecision.BurnStatus.EXECUTING);
        decision.setExecutedBy(executorId);

        BurnDecision updated = burnDecisionRepository.save(decision);
        log.info("Burn execution started");
        return updated;
    }

    /**
     * バーン実行完了
     */
    @Transactional
    public BurnDecision completeBurnExecution(Long decisionId, BigDecimal actualBurnAmount,
            BigDecimal circulatingSupplyAfter, String transactionHash) {
        log.info("Completing burn execution for decision ID: {}", decisionId);

        BurnDecision decision = getBurnDecision(decisionId);

        if (decision.getStatus() != BurnDecision.BurnStatus.EXECUTING) {
            throw new IllegalStateException("Burn must be in executing status to complete");
        }

        BigDecimal actualBurnRate = actualBurnAmount.divide(
                decision.getCirculatingSupplyBefore(), 6, RoundingMode.HALF_UP);

        decision.setStatus(BurnDecision.BurnStatus.COMPLETED);
        decision.setActualBurnAmount(actualBurnAmount);
        decision.setCirculatingSupplyAfter(circulatingSupplyAfter);
        decision.setBurnRateActual(actualBurnRate);
        decision.setTransactionHash(transactionHash);
        decision.setActualExecutionDate(LocalDateTime.now());

        BurnDecision updated = burnDecisionRepository.save(decision);
        log.info("Burn execution completed successfully");
        return updated;
    }

    /**
     * バーン実行失敗
     */
    @Transactional
    public BurnDecision markBurnAsFailed(Long decisionId, String failureReason) {
        log.info("Marking burn as failed for decision ID: {}", decisionId);

        BurnDecision decision = getBurnDecision(decisionId);

        if (decision.getStatus() != BurnDecision.BurnStatus.EXECUTING) {
            throw new IllegalStateException("Only executing burns can be marked as failed");
        }

        decision.setStatus(BurnDecision.BurnStatus.FAILED);
        decision.setRollbackReason(failureReason);

        BurnDecision updated = burnDecisionRepository.save(decision);
        log.info("Burn marked as failed");
        return updated;
    }

    /**
     * バーン判定取得
     */
    @Transactional(readOnly = true)
    public BurnDecision getBurnDecision(Long id) {
        return burnDecisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Burn decision not found with ID: " + id));
    }

    /**
     * スペース別バーン判定履歴取得
     */
    @Transactional(readOnly = true)
    public Page<BurnDecision> getSpaceBurnDecisions(Long spaceId, int page, int size) {
        log.info("Getting burn decisions for space: {}, page: {}, size: {}", spaceId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("decisionDate").descending());
        return burnDecisionRepository.findBySpaceId(spaceId, pageable);
    }

    /**
     * ステータス別バーン判定取得（デフォルト実装）
     */
    @Transactional(readOnly = true)
    public Page<BurnDecision> getBurnDecisionsByStatus(BurnDecision.BurnStatus status, int page, int size) {
        log.info("Getting burn decisions by status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("decisionDate").descending());
        // 全件取得してフィルタリング（パフォーマンスは劣るが動作する）
        List<BurnDecision> filteredDecisions = burnDecisionRepository.findAll().stream()
                .filter(d -> d.getStatus() == status)
                .sorted((a, b) -> b.getDecisionDate().compareTo(a.getDecisionDate()))
                .toList();

        // 手動でページネーション
        int start = page * size;
        int end = Math.min(start + size, filteredDecisions.size());
        List<BurnDecision> pageContent = start < filteredDecisions.size() ? filteredDecisions.subList(start, end)
                : List.of();

        return new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, filteredDecisions.size());
    }

    /**
     * 承認待ちバーン判定取得
     */
    @Transactional(readOnly = true)
    public List<BurnDecision> getPendingBurnDecisions() {
        log.info("Getting pending burn decisions");
        // 全件取得後、Java側でフィルタリング
        return burnDecisionRepository.findAll().stream()
                .filter(d -> d.getStatus() == BurnDecision.BurnStatus.PROPOSED ||
                        d.getStatus() == BurnDecision.BurnStatus.UNDER_REVIEW)
                .sorted((a, b) -> b.getDecisionDate().compareTo(a.getDecisionDate()))
                .toList();
    }

    /**
     * 投票中のバーン判定取得
     */
    @Transactional(readOnly = true)
    public List<BurnDecision> getVotingBurnDecisions() {
        log.info("Getting voting burn decisions");
        return burnDecisionRepository.findAll().stream()
                .filter(d -> d.getStatus() == BurnDecision.BurnStatus.VOTING &&
                        d.getVotingEndDate() != null &&
                        d.getVotingEndDate().isAfter(LocalDateTime.now()))
                .sorted((a, b) -> a.getVotingEndDate().compareTo(b.getVotingEndDate()))
                .toList();
    }

    /**
     * 期間内のバーン判定統計取得
     */
    @Transactional(readOnly = true)
    public BurnDecisionStatistics getBurnDecisionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting burn decision statistics from {} to {}", startDate, endDate);

        // 代替として既存のリポジトリメソッドを使用
        List<BurnDecision> decisions = burnDecisionRepository.findByDecisionDateBetween(startDate, endDate,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        if (decisions.isEmpty()) {
            return BurnDecisionStatistics.builder()
                    .totalDecisions(0L)
                    .approvedDecisions(0L)
                    .rejectedDecisions(0L)
                    .completedDecisions(0L)
                    .failedDecisions(0L)
                    .totalProposedBurnAmount(BigDecimal.ZERO)
                    .totalActualBurnAmount(BigDecimal.ZERO)
                    .approvalRate(BigDecimal.ZERO)
                    .completionRate(BigDecimal.ZERO)
                    .averageProposedAmount(BigDecimal.ZERO)
                    .averageActualAmount(BigDecimal.ZERO)
                    .build();
        }

        // 統計計算
        long totalDecisions = decisions.size();
        long approvedDecisions = decisions.stream()
                .mapToLong(d -> (d.getStatus() == BurnDecision.BurnStatus.APPROVED ||
                        d.getStatus() == BurnDecision.BurnStatus.SCHEDULED ||
                        d.getStatus() == BurnDecision.BurnStatus.EXECUTING ||
                        d.getStatus() == BurnDecision.BurnStatus.COMPLETED) ? 1 : 0)
                .sum();
        long rejectedDecisions = decisions.stream()
                .mapToLong(d -> d.getStatus() == BurnDecision.BurnStatus.REJECTED ? 1 : 0)
                .sum();
        long completedDecisions = decisions.stream()
                .mapToLong(d -> d.getStatus() == BurnDecision.BurnStatus.COMPLETED ? 1 : 0)
                .sum();
        long failedDecisions = decisions.stream()
                .mapToLong(d -> d.getStatus() == BurnDecision.BurnStatus.FAILED ? 1 : 0)
                .sum();

        BigDecimal totalProposedBurnAmount = decisions.stream()
                .map(BurnDecision::getProposedBurnAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActualBurnAmount = decisions.stream()
                .filter(d -> d.getActualBurnAmount() != null)
                .map(BurnDecision::getActualBurnAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 承認率計算
        BigDecimal approvalRate = totalDecisions > 0 ? BigDecimal.valueOf(approvedDecisions)
                .divide(BigDecimal.valueOf(totalDecisions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        // 完了率計算
        BigDecimal completionRate = approvedDecisions > 0 ? BigDecimal.valueOf(completedDecisions)
                .divide(BigDecimal.valueOf(approvedDecisions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        // 平均金額計算
        BigDecimal averageProposedAmount = totalDecisions > 0
                ? totalProposedBurnAmount.divide(BigDecimal.valueOf(totalDecisions), 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal averageActualAmount = completedDecisions > 0
                ? totalActualBurnAmount.divide(BigDecimal.valueOf(completedDecisions), 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return BurnDecisionStatistics.builder()
                .totalDecisions(totalDecisions)
                .approvedDecisions(approvedDecisions)
                .rejectedDecisions(rejectedDecisions)
                .completedDecisions(completedDecisions)
                .failedDecisions(failedDecisions)
                .totalProposedBurnAmount(totalProposedBurnAmount)
                .totalActualBurnAmount(totalActualBurnAmount)
                .approvalRate(approvalRate)
                .completionRate(completionRate)
                .averageProposedAmount(averageProposedAmount)
                .averageActualAmount(averageActualAmount)
                .build();
    }

    /**
     * デシジョンタイプ別統計取得
     */
    @Transactional(readOnly = true)
    public List<DecisionTypeStatistics> getDecisionTypeStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting decision type statistics from {} to {}", startDate, endDate);

        // 既存のリポジトリメソッドを使用
        List<BurnDecision> decisions = burnDecisionRepository.findByDecisionDateBetween(startDate, endDate,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        return decisions.stream()
                .collect(java.util.stream.Collectors.groupingBy(BurnDecision::getDecisionType))
                .entrySet().stream()
                .map(entry -> {
                    BurnDecision.DecisionType decisionType = entry.getKey();
                    List<BurnDecision> typeDecisions = entry.getValue();

                    BigDecimal totalAmount = typeDecisions.stream()
                            .map(BurnDecision::getProposedBurnAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long completedCount = typeDecisions.stream()
                            .mapToLong(d -> d.getStatus() == BurnDecision.BurnStatus.COMPLETED ? 1 : 0)
                            .sum();

                    return DecisionTypeStatistics.builder()
                            .decisionType(decisionType)
                            .count(typeDecisions.size())
                            .totalAmount(totalAmount)
                            .completedCount(completedCount)
                            .averageAmount(
                                    typeDecisions.size() > 0
                                            ? totalAmount.divide(BigDecimal.valueOf(typeDecisions.size()), 8,
                                                    RoundingMode.HALF_UP)
                                            : BigDecimal.ZERO)
                            .build();
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .toList();
    }

    /**
     * 高額バーン判定取得
     */
    @Transactional(readOnly = true)
    public List<BurnDecision> getHighValueBurnDecisions(BigDecimal threshold) {
        log.info("Getting high value burn decisions above threshold: {}", threshold);
        // 代替として既存のリポジトリメソッドを使用
        return burnDecisionRepository.findByActualBurnAmountGreaterThanEqual(threshold,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("actualBurnAmount").descending()));
    }

    /**
     * 全バーン決定をページネーション付きで取得
     */
    public Page<BurnDecision> findAllBurnDecisions(Pageable pageable) {
        log.info("Getting all burn decisions with pagination");
        return burnDecisionRepository.findAll(pageable);
    }

    /**
     * スペース別バーン決定をページネーション付きで取得
     */
    public Page<BurnDecision> findBySpaceId(Long spaceId, Pageable pageable) {
        log.info("Getting burn decisions for space: {}", spaceId);
        return burnDecisionRepository.findBySpaceId(spaceId, pageable);
    }

    // === 内部DTOクラス ===

    @Data
    @Builder
    public static class BurnDecisionStatistics {
        private Long totalDecisions;
        private Long approvedDecisions;
        private Long rejectedDecisions;
        private Long completedDecisions;
        private Long failedDecisions;
        private BigDecimal totalProposedBurnAmount;
        private BigDecimal totalActualBurnAmount;
        private BigDecimal approvalRate;
        private BigDecimal completionRate;
        private BigDecimal averageProposedAmount;
        private BigDecimal averageActualAmount;
    }

    @Data
    @Builder
    public static class DecisionTypeStatistics {
        private BurnDecision.DecisionType decisionType;
        private Integer count;
        private BigDecimal totalAmount;
        private Long completedCount;
        private BigDecimal averageAmount;
    }
}
