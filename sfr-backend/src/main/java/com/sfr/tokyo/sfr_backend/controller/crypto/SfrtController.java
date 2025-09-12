package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrtBalance;
import com.sfr.tokyo.sfr_backend.entity.crypto.SfrtTransaction;
import com.sfr.tokyo.sfr_backend.service.crypto.SfrtBalanceService;
import com.sfr.tokyo.sfr_backend.service.crypto.SfrtManagementService;
import com.sfr.tokyo.sfr_backend.service.crypto.SfrtRewardService;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrtTransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SFRTコントローラー
 * Phase 3.4: SFRT API統合・フロントエンド対応
 * 
 * 主要機能:
 * - SFRT残高・取引履歴API
 * - 報酬シミュレーション・統計API
 * - 管理者向け手動報酬API
 * - リアルタイムメトリクス・価格指標API
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/api/v1/sfrt")
@CrossOrigin(origins = "*")
public class SfrtController {

    @Autowired
    private SfrtBalanceService sfrtBalanceService;

    @Autowired
    private SfrtManagementService sfrtManagementService;

    @Autowired
    private SfrtRewardService sfrtRewardService;

    @Autowired
    private SfrtTransactionRepository sfrtTransactionRepository;

    // ===== ユーザー向けAPI =====

    /**
     * ユーザーのSFRT残高取得
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<SfrtBalanceResponse> getSfrtBalance(@PathVariable String userId) {
        try {
            SfrtBalance balance = sfrtBalanceService.getSfrtBalance(userId);
            
            SfrtBalanceResponse response = SfrtBalanceResponse.builder()
                .userId(balance.getUserId())
                .balance(balance.getBalance())
                .totalEarned(balance.getTotalEarned())
                .totalWithdrawn(balance.getTotalWithdrawn())
                .profitAmount(balance.getProfitAmount())
                .lastUpdated(balance.getUpdatedAt())
                .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザーのSFRT取引履歴取得
     */
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<Page<SfrtTransaction>> getTransactionHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SfrtTransaction> transactions = sfrtTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * SFRT報酬シミュレーション
     */
    @GetMapping("/simulate/rewards")
    public ResponseEntity<SfrtRewardService.SfrtRewardSimulation> simulateRewards(
            @RequestParam BigDecimal sfrAmount) {
        try {
            SfrtRewardService.SfrtRewardSimulation simulation = sfrtRewardService.simulateRewards(sfrAmount);
            return ResponseEntity.ok(simulation);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザーの報酬統計
     */
    @GetMapping("/stats/rewards/{userId}")
    public ResponseEntity<UserRewardStats> getUserRewardStats(@PathVariable String userId) {
        try {
            BigDecimal totalRewards = sfrtTransactionRepository.getUserTotalRewards(userId);
            BigDecimal totalWithdrawals = sfrtTransactionRepository.getUserTotalWithdrawals(userId);
            SfrtTransaction latestTransaction = sfrtTransactionRepository.findTopByUserIdOrderByCreatedAtDesc(userId);

            UserRewardStats stats = UserRewardStats.builder()
                .userId(userId)
                .totalRewards(totalRewards)
                .totalWithdrawals(totalWithdrawals)
                .netRewards(totalRewards.subtract(totalWithdrawals))
                .latestTransaction(latestTransaction)
                .build();

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== 統計・分析API =====

    /**
     * SFRT総供給量・流通量情報
     */
    @GetMapping("/supply")
    public ResponseEntity<SfrtManagementService.SfrtSupplyInfo> getSfrtSupplyInfo() {
        try {
            SfrtManagementService.SfrtSupplyInfo supplyInfo = sfrtManagementService.getSfrtSupplyInfo();
            return ResponseEntity.ok(supplyInfo);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * SFRT価格指標・メトリクス
     */
    @GetMapping("/metrics/price")
    public ResponseEntity<SfrtManagementService.SfrtPriceMetrics> getPriceMetrics() {
        try {
            SfrtManagementService.SfrtPriceMetrics priceMetrics = sfrtManagementService.calculatePriceMetrics();
            return ResponseEntity.ok(priceMetrics);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * SFRT保有者分析
     */
    @GetMapping("/analytics/holders")
    public ResponseEntity<SfrtManagementService.SfrtHolderAnalysis> getHolderAnalysis() {
        try {
            SfrtManagementService.SfrtHolderAnalysis analysis = sfrtManagementService.analyzeSfrtHolders();
            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * プラットフォーム経済指標
     */
    @GetMapping("/metrics/economic")
    public ResponseEntity<SfrtManagementService.PlatformEconomicMetrics> getEconomicMetrics() {
        try {
            SfrtManagementService.PlatformEconomicMetrics metrics = sfrtManagementService.calculateEconomicMetrics();
            return ResponseEntity.ok(metrics);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 取引統計（種別別）
     */
    @GetMapping("/stats/transactions")
    public ResponseEntity<List<Object[]>> getTransactionStats() {
        try {
            List<Object[]> stats = sfrtTransactionRepository.getTransactionStatsByType();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 日別取引統計
     */
    @GetMapping("/stats/daily")
    public ResponseEntity<List<Object[]>> getDailyStats(@RequestParam(defaultValue = "30") int days) {
        try {
            LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
            List<Object[]> stats = sfrtTransactionRepository.getDailyTransactionStats(fromDate);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== 管理者向けAPI =====

    /**
     * 手動SFRT報酬配布（管理者用）
     */
    @PostMapping("/admin/distribute-reward")
    public ResponseEntity<SfrtRewardService.ManualRewardResult> distributeManualReward(
            @RequestBody ManualRewardRequest request) {
        try {
            // TODO: 管理者権限チェックの実装が必要
            
            SfrtRewardService.ManualRewardResult result = sfrtRewardService.distributeManualReward(
                request.getUserId(),
                request.getRewardAmount(),
                request.getReason(),
                request.getAdminUserId()
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            SfrtRewardService.ManualRewardResult errorResult = SfrtRewardService.ManualRewardResult.failure(e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * プラットフォームSFRT残高取得（管理者用）
     */
    @GetMapping("/admin/platform-balance")
    public ResponseEntity<SfrtBalance> getPlatformBalance() {
        try {
            SfrtBalance platformBalance = sfrtBalanceService.getPlatformSfrtBalance();
            return ResponseEntity.ok(platformBalance);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 上位報酬獲得者一覧（管理者用）
     */
    @GetMapping("/admin/top-earners")
    public ResponseEntity<List<Object[]>> getTopEarners(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            LocalDateTime endDate = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, limit);
            
            List<Object[]> topEarners = sfrtTransactionRepository.getTopRewardEarners(startDate, endDate, pageable);
            return ResponseEntity.ok(topEarners);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== DTOクラス定義 =====

    @lombok.Data
    @lombok.Builder
    public static class SfrtBalanceResponse {
        private String userId;
        private BigDecimal balance;
        private BigDecimal totalEarned;
        private BigDecimal totalWithdrawn;
        private BigDecimal profitAmount;
        private LocalDateTime lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserRewardStats {
        private String userId;
        private BigDecimal totalRewards;
        private BigDecimal totalWithdrawals;
        private BigDecimal netRewards;
        private SfrtTransaction latestTransaction;
    }

    @lombok.Data
    public static class ManualRewardRequest {
        private String userId;
        private BigDecimal rewardAmount;
        private String reason;
        private String adminUserId;
    }
}
