package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.BurnDecision;
import com.sfr.tokyo.sfr_backend.service.crypto.BurnDecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * BurnDecisionController - SFRバーン決定管理API
 * バーン決定の作成、承認、実行、統計機能を提供
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-21
 */
@RestController
@RequestMapping("/api/crypto/burn-decisions")
@RequiredArgsConstructor
@Slf4j
public class BurnDecisionController {

    private final BurnDecisionService burnDecisionService;

    // =============================================================================
    // バーン決定CRUD操作
    // =============================================================================

    /**
     * バーン決定作成
     * 
     * @param request        バーン決定作成リクエスト
     * @param authentication 認証情報
     * @return 作成されたバーン決定
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GOVERNANCE')")
    public ResponseEntity<BurnDecision> createBurnDecision(
            @Valid @RequestBody BurnDecisionCreateRequest request,
            Authentication authentication) {
        log.info("Creating burn decision: {}", request);

        BurnDecision burnDecision = burnDecisionService.createBurnDecision(
                request.getSpaceId(),
                request.getProposedBurnAmount(),
                request.getCirculatingSupplyBefore(),
                request.getDecisionType(),
                request.getTriggerReason(),
                request.getDecisionRationale());

        return ResponseEntity.ok(burnDecision);
    }

    /**
     * AI自動バーン決定作成
     * 
     * @param request AI自動バーン決定作成リクエスト
     * @return 作成されたバーン決定
     */
    @PostMapping("/ai-auto")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<BurnDecision> createAiBurnDecision(
            @Valid @RequestBody AiBurnDecisionCreateRequest request) {
        log.info("Creating AI burn decision: {}", request);

        BurnDecision burnDecision = burnDecisionService.createAiBurnDecision(
                request.getSpaceId(),
                request.getProposedBurnAmount(),
                request.getCirculatingSupplyBefore(),
                request.getTriggerReason(),
                request.getAiConfidenceScore(),
                request.getEconomicIndicators(),
                request.getDecisionRationale());

        return ResponseEntity.ok(burnDecision);
    }

    /**
     * バーン決定承認
     * 
     * @param id             バーン決定ID
     * @param request        承認リクエスト
     * @param authentication 認証情報
     * @return 承認されたバーン決定
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GOVERNANCE')")
    public ResponseEntity<BurnDecision> approveBurnDecision(
            @PathVariable Long id,
            @Valid @RequestBody BurnDecisionApproveRequest request,
            Authentication authentication) {
        log.info("Approving burn decision: {}", id);

        UUID approverId = UUID.fromString(authentication.getName());
        BurnDecision burnDecision = burnDecisionService.approveBurnDecision(
                id,
                approverId,
                request.getAdminNotes());

        return ResponseEntity.ok(burnDecision);
    }

    /**
     * バーン決定拒否
     * 
     * @param id             バーン決定ID
     * @param request        拒否リクエスト
     * @param authentication 認証情報
     * @return 拒否されたバーン決定
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GOVERNANCE')")
    public ResponseEntity<BurnDecision> rejectBurnDecision(
            @PathVariable Long id,
            @Valid @RequestBody BurnDecisionRejectRequest request,
            Authentication authentication) {
        log.info("Rejecting burn decision: {}", id);

        UUID rejectorId = UUID.fromString(authentication.getName());
        BurnDecision burnDecision = burnDecisionService.rejectBurnDecision(
                id,
                rejectorId,
                request.getRejectionReason());

        return ResponseEntity.ok(burnDecision);
    }

    /**
     * バーン実行開始
     * 
     * @param id             バーン決定ID
     * @param authentication 認証情報
     * @return 実行開始されたバーン決定
     */
    @PostMapping("/{id}/start-execution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BurnDecision> startBurnExecution(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("Starting burn execution: {}", id);

        UUID executorId = UUID.fromString(authentication.getName());
        BurnDecision burnDecision = burnDecisionService.startBurnExecution(id, executorId);

        return ResponseEntity.ok(burnDecision);
    }

    /**
     * バーン実行完了
     * 
     * @param id      バーン決定ID
     * @param request 実行完了リクエスト
     * @return 実行完了されたバーン決定
     */
    @PostMapping("/{id}/complete-execution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BurnDecision> completeBurnExecution(
            @PathVariable Long id,
            @Valid @RequestBody BurnExecutionCompleteRequest request) {
        log.info("Completing burn execution: {} with amount: {}", id, request.getActualBurnAmount());

        BurnDecision burnDecision = burnDecisionService.completeBurnExecution(
                id,
                request.getActualBurnAmount(),
                request.getCirculatingSupplyAfter(),
                request.getTransactionHash());

        return ResponseEntity.ok(burnDecision);
    }

    // =============================================================================
    // バーン決定検索・一覧
    // =============================================================================

    /**
     * バーン決定一覧取得
     * 
     * @param page    ページ番号
     * @param size    ページサイズ
     * @param spaceId スペースID（オプション）
     * @param status  ステータス（オプション）
     * @return バーン決定一覧
     */
    @GetMapping
    public ResponseEntity<Page<BurnDecision>> getBurnDecisions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long spaceId,
            @RequestParam(required = false) BurnDecision.BurnStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("decisionDate").descending());

        // 基本実装：全件取得のみ（フィルタリング機能は今後追加）
        Page<BurnDecision> decisions = burnDecisionService.findAllBurnDecisions(pageable);

        return ResponseEntity.ok(decisions);
    }

    /**
     * バーン決定詳細取得
     * 
     * @param id バーン決定ID
     * @return バーン決定詳細
     */
    @GetMapping("/{id}")
    public ResponseEntity<BurnDecision> getBurnDecision(@PathVariable Long id) {
        BurnDecision burnDecision = burnDecisionService.getBurnDecision(id);
        return ResponseEntity.ok(burnDecision);
    }

    /**
     * スペース別バーン決定履歴取得
     * 
     * @param spaceId スペースID
     * @param page    ページ番号
     * @param size    ページサイズ
     * @return スペース別バーン決定履歴
     */
    @GetMapping("/by-space/{spaceId}")
    public ResponseEntity<Page<BurnDecision>> getBurnDecisionsBySpace(
            @PathVariable Long spaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("decisionDate").descending());
        Page<BurnDecision> decisions = burnDecisionService.findBySpaceId(spaceId, pageable);

        return ResponseEntity.ok(decisions);
    }

    // =============================================================================
    // バーン決定統計・分析
    // =============================================================================

    /**
     * バーン決定統計取得
     * 
     * @param startDate 開始日時
     * @param endDate   終了日時
     * @return バーン決定統計
     */
    @GetMapping("/statistics")
    public ResponseEntity<BurnDecisionService.BurnDecisionStatistics> getBurnDecisionStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        BurnDecisionService.BurnDecisionStatistics statistics = burnDecisionService.getBurnDecisionStatistics(startDate,
                endDate);

        return ResponseEntity.ok(statistics);
    }

    /**
     * 決定タイプ別統計取得
     * 
     * @param startDate 開始日時
     * @param endDate   終了日時
     * @return 決定タイプ別統計
     */
    @GetMapping("/statistics/by-type")
    public ResponseEntity<List<BurnDecisionService.DecisionTypeStatistics>> getDecisionTypeStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        List<BurnDecisionService.DecisionTypeStatistics> statistics = burnDecisionService
                .getDecisionTypeStatistics(startDate, endDate);

        return ResponseEntity.ok(statistics);
    }

    /**
     * 高額バーン決定取得
     * 
     * @param threshold 閾値
     * @return 高額バーン決定一覧
     */
    @GetMapping("/high-value")
    public ResponseEntity<List<BurnDecision>> getHighValueBurnDecisions(
            @RequestParam(defaultValue = "1000000") BigDecimal threshold) {

        List<BurnDecision> decisions = burnDecisionService.getHighValueBurnDecisions(threshold);
        return ResponseEntity.ok(decisions);
    }

    // =============================================================================
    // DTOクラス
    // =============================================================================

    /**
     * バーン決定作成リクエスト
     */
    public static class BurnDecisionCreateRequest {
        private Long spaceId;
        private BigDecimal proposedBurnAmount;
        private BigDecimal circulatingSupplyBefore;
        private BurnDecision.DecisionType decisionType;
        private BurnDecision.TriggerReason triggerReason;
        private String decisionRationale;

        // Getters and setters
        public Long getSpaceId() {
            return spaceId;
        }

        public void setSpaceId(Long spaceId) {
            this.spaceId = spaceId;
        }

        public BigDecimal getProposedBurnAmount() {
            return proposedBurnAmount;
        }

        public void setProposedBurnAmount(BigDecimal proposedBurnAmount) {
            this.proposedBurnAmount = proposedBurnAmount;
        }

        public BigDecimal getCirculatingSupplyBefore() {
            return circulatingSupplyBefore;
        }

        public void setCirculatingSupplyBefore(BigDecimal circulatingSupplyBefore) {
            this.circulatingSupplyBefore = circulatingSupplyBefore;
        }

        public BurnDecision.DecisionType getDecisionType() {
            return decisionType;
        }

        public void setDecisionType(BurnDecision.DecisionType decisionType) {
            this.decisionType = decisionType;
        }

        public BurnDecision.TriggerReason getTriggerReason() {
            return triggerReason;
        }

        public void setTriggerReason(BurnDecision.TriggerReason triggerReason) {
            this.triggerReason = triggerReason;
        }

        public String getDecisionRationale() {
            return decisionRationale;
        }

        public void setDecisionRationale(String decisionRationale) {
            this.decisionRationale = decisionRationale;
        }
    }

    /**
     * AI自動バーン決定作成リクエスト
     */
    public static class AiBurnDecisionCreateRequest {
        private Long spaceId;
        private BigDecimal proposedBurnAmount;
        private BigDecimal circulatingSupplyBefore;
        private BurnDecision.TriggerReason triggerReason;
        private BigDecimal aiConfidenceScore;
        private String economicIndicators;
        private String decisionRationale;

        // Getters and setters
        public Long getSpaceId() {
            return spaceId;
        }

        public void setSpaceId(Long spaceId) {
            this.spaceId = spaceId;
        }

        public BigDecimal getProposedBurnAmount() {
            return proposedBurnAmount;
        }

        public void setProposedBurnAmount(BigDecimal proposedBurnAmount) {
            this.proposedBurnAmount = proposedBurnAmount;
        }

        public BigDecimal getCirculatingSupplyBefore() {
            return circulatingSupplyBefore;
        }

        public void setCirculatingSupplyBefore(BigDecimal circulatingSupplyBefore) {
            this.circulatingSupplyBefore = circulatingSupplyBefore;
        }

        public BurnDecision.TriggerReason getTriggerReason() {
            return triggerReason;
        }

        public void setTriggerReason(BurnDecision.TriggerReason triggerReason) {
            this.triggerReason = triggerReason;
        }

        public BigDecimal getAiConfidenceScore() {
            return aiConfidenceScore;
        }

        public void setAiConfidenceScore(BigDecimal aiConfidenceScore) {
            this.aiConfidenceScore = aiConfidenceScore;
        }

        public String getEconomicIndicators() {
            return economicIndicators;
        }

        public void setEconomicIndicators(String economicIndicators) {
            this.economicIndicators = economicIndicators;
        }

        public String getDecisionRationale() {
            return decisionRationale;
        }

        public void setDecisionRationale(String decisionRationale) {
            this.decisionRationale = decisionRationale;
        }
    }

    /**
     * バーン決定承認リクエスト
     */
    public static class BurnDecisionApproveRequest {
        private String adminNotes;

        public String getAdminNotes() {
            return adminNotes;
        }

        public void setAdminNotes(String adminNotes) {
            this.adminNotes = adminNotes;
        }
    }

    /**
     * バーン決定拒否リクエスト
     */
    public static class BurnDecisionRejectRequest {
        private String rejectionReason;

        public String getRejectionReason() {
            return rejectionReason;
        }

        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }
    }

    /**
     * バーン実行完了リクエスト
     */
    public static class BurnExecutionCompleteRequest {
        private BigDecimal actualBurnAmount;
        private BigDecimal circulatingSupplyAfter;
        private String transactionHash;

        public BigDecimal getActualBurnAmount() {
            return actualBurnAmount;
        }

        public void setActualBurnAmount(BigDecimal actualBurnAmount) {
            this.actualBurnAmount = actualBurnAmount;
        }

        public BigDecimal getCirculatingSupplyAfter() {
            return circulatingSupplyAfter;
        }

        public void setCirculatingSupplyAfter(BigDecimal circulatingSupplyAfter) {
            this.circulatingSupplyAfter = circulatingSupplyAfter;
        }

        public String getTransactionHash() {
            return transactionHash;
        }

        public void setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
        }
    }
}
