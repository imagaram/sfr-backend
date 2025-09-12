package com.serendipity.tokyo.sfrbackend.controller.sfrt;

import com.serendipity.tokyo.sfrbackend.service.sfrt.SfrtBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * SFRT統計情報APIコントローラー
 * - 供給量・分布統計
 * - 報酬配布状況
 * - システム状態監視
 */
@RestController
@RequestMapping("/api/v1/sfrt/stats")
@RequiredArgsConstructor
@Slf4j
public class SfrtStatsController {

    private final SfrtBalanceService sfrtBalanceService;

    /**
     * SFRT総供給量統計
     */
    @GetMapping("/supply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSfrtSupplyStats() {
        try {
            BigDecimal totalSupply = sfrtBalanceService.getTotalSfrtSupply();
            BigDecimal activeSupply = sfrtBalanceService.getActiveSfrtSupply();
            BigDecimal frozenSupply = sfrtBalanceService.getFrozenSfrtSupply();

            return ResponseEntity.ok(Map.of(
                "totalSupply", totalSupply,
                "activeSupply", activeSupply,
                "frozenSupply", frozenSupply,
                "circulationRate", totalSupply.compareTo(BigDecimal.ZERO) > 0 
                    ? activeSupply.divide(totalSupply, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO
            ));

        } catch (Exception e) {
            log.error("SFRT供給量統計取得エラー", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * SFRT分布統計
     */
    @GetMapping("/distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSfrtDistributionStats() {
        try {
            Map<String, Object> stats = sfrtBalanceService.getSfrtDistributionStats();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("SFRT分布統計取得エラー", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Space別SFRT統計
     */
    @GetMapping("/space/{spaceId}")
    @PreAuthorize("hasRole('SPACE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSpaceSfrtStats(
        @PathVariable Long spaceId
    ) {
        try {
            BigDecimal spaceSupply = sfrtBalanceService.getSpaceSfrtSupply(spaceId);
            Long activeUsers = sfrtBalanceService.getActiveUsersCount(spaceId);

            return ResponseEntity.ok(Map.of(
                "spaceId", spaceId,
                "totalSupply", spaceSupply,
                "activeUsers", activeUsers,
                "averageBalance", activeUsers > 0 
                    ? spaceSupply.divide(BigDecimal.valueOf(activeUsers), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO
            ));

        } catch (Exception e) {
            log.error("Space SFRT統計取得エラー: spaceId={}", spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ユーザー別SFRT状態（管理者用）
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserSfrtStats(
        @RequestParam String userId,
        @RequestParam Long spaceId
    ) {
        try {
            var balance = sfrtBalanceService.getOrCreateSfrtBalance(userId, spaceId);

            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "spaceId", spaceId,
                "balance", balance.getCurrentBalance(),
                "status", balance.getStatus(),
                "lastActivity", balance.getUpdatedAt(),
                "externalExchangeEnabled", balance.getExternalExchangeEnabled(),
                "createdAt", balance.getCreatedAt()
            ));

        } catch (Exception e) {
            log.error("ユーザーSFRT統計取得エラー: userId={}, spaceId={}", userId, spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * システム健全性チェック
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSfrtSystemHealth() {
        try {
            // システム状態チェック
            BigDecimal totalSupply = sfrtBalanceService.getTotalSfrtSupply();
            Long totalAccounts = sfrtBalanceService.getTotalAccountsCount();
            Long activeAccounts = sfrtBalanceService.getActiveAccountsCount();

            boolean isHealthy = true;
            StringBuilder issues = new StringBuilder();

            // 基本チェック
            if (totalSupply.compareTo(BigDecimal.ZERO) < 0) {
                isHealthy = false;
                issues.append("負の総供給量が検出されました; ");
            }

            // アクティブ率チェック
            if (totalAccounts > 0) {
                BigDecimal activeRate = BigDecimal.valueOf(activeAccounts)
                    .divide(BigDecimal.valueOf(totalAccounts), 4, RoundingMode.HALF_UP);
                
                if (activeRate.compareTo(new BigDecimal("0.1")) < 0) { // 10%未満
                    issues.append("アクティブ率が低下しています (").append(activeRate.multiply(new BigDecimal("100"))).append("%); ");
                }
            }

            return ResponseEntity.ok(Map.of(
                "healthy", isHealthy,
                "issues", issues.toString(),
                "totalSupply", totalSupply,
                "totalAccounts", totalAccounts,
                "activeAccounts", activeAccounts,
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("SFRTシステム健全性チェックエラー", e);
            return ResponseEntity.status(503).body(Map.of(
                "healthy", false,
                "issues", "システムチェック中にエラーが発生しました: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}
