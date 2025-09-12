package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.service.crypto.SfrIntegrationService;
import com.sfr.tokyo.sfr_backend.service.crypto.SfrIntegrationService.BalanceConsistencyResult;
import com.sfr.tokyo.sfr_backend.service.crypto.SfrIntegrationService.SystemSyncSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * SFR統合管理コントローラー
 * Phase 1.4: 既存システム統合
 * 
 * UserBalance（既存暗号資産システム）とSfrPoint（新ポイントシステム）の
 * 統合管理機能を提供
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/api/v1/sfr/integration")
@RequiredArgsConstructor
@Slf4j
public class SfrIntegrationController {

    private final SfrIntegrationService integrationService;

    /**
     * 現在ユーザーの残高整合性チェック
     * 
     * @param principal 認証済みユーザー情報
     * @return 整合性チェック結果
     */
    @GetMapping("/balance-consistency")
    public ResponseEntity<BalanceConsistencyResult> checkUserBalanceConsistency(Principal principal) {
        String userId = principal.getName();
        log.info("残高整合性チェック要求: userId={}", userId);
        
        BalanceConsistencyResult result = integrationService.checkBalanceConsistency(userId);
        
        log.info("残高整合性チェック結果: userId={}, consistent={}, discrepancy={}", 
                userId, result.isConsistent(), result.getDiscrepancy());
        
        return ResponseEntity.ok(result);
    }

    /**
     * 現在ユーザーの残高同期修復
     * SfrPointの残高を正として、UserBalanceを同期修復
     * 
     * @param principal 認証済みユーザー情報
     * @return 修復結果
     */
    @PostMapping("/repair-balance")
    public ResponseEntity<Map<String, Object>> repairUserBalance(Principal principal) {
        String userId = principal.getName();
        log.info("残高同期修復要求: userId={}", userId);
        
        try {
            // 修復前の状態を記録
            BalanceConsistencyResult beforeState = integrationService.checkBalanceConsistency(userId);
            
            // 残高修復実行
            integrationService.repairBalanceConsistency(userId);
            
            // 修復後の状態を確認
            BalanceConsistencyResult afterState = integrationService.checkBalanceConsistency(userId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "残高同期修復が完了しました",
                "beforeState", beforeState,
                "afterState", afterState
            );
            
            log.info("残高同期修復完了: userId={}, 修復前整合性={}, 修復後整合性={}", 
                    userId, beforeState.isConsistent(), afterState.isConsistent());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("残高同期修復エラー: userId={}", userId, e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "残高同期修復に失敗しました: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * システム全体の同期状況取得（管理者専用）
     * 
     * @return システム同期サマリー
     */
    @GetMapping("/system-sync-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSyncSummary> getSystemSyncSummary() {
        log.info("システム同期状況取得要求");
        
        SystemSyncSummary summary = integrationService.getSystemSyncSummary();
        
        log.info("システム同期状況: SfrPointユーザー数={}, UserBalanceユーザー数={}, 不整合数={}", 
                summary.getTotalSfrPointUsers(), summary.getTotalUserBalanceUsers(), summary.getInconsistentUsers());
        
        return ResponseEntity.ok(summary);
    }

    /**
     * 既存転送システムとSfrPointの手動同期（管理者専用）
     * 既存のUserBalance転送操作をSfrPointにも反映させる
     * 
     * @param request 転送同期要求
     * @return 同期結果
     */
    @PostMapping("/sync-legacy-transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncLegacyTransfer(
            @RequestBody LegacyTransferSyncRequest request) {
        log.info("既存転送同期要求: from={}, to={}, amount={}", 
                request.getFromUserId(), request.getToUserId(), request.getAmount());
        
        try {
            integrationService.syncLegacyTransferToSfrPoint(
                request.getFromUserId(),
                request.getToUserId(),
                request.getAmount(),
                request.getTransactionType(),
                request.getDescription()
            );
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "既存転送の同期が完了しました"
            );
            
            log.info("既存転送同期完了: from={}, to={}, amount={}", 
                    request.getFromUserId(), request.getToUserId(), request.getAmount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("既存転送同期エラー", e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "転送同期に失敗しました: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Phase 1.4統合システムのヘルスチェック
     * 
     * @return システム状態
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            SystemSyncSummary summary = integrationService.getSystemSyncSummary();
            
            Map<String, Object> health = Map.of(
                "status", "healthy",
                "phase", "1.4",
                "description", "既存システム統合",
                "sfrPointUsers", summary.getTotalSfrPointUsers(),
                "userBalanceUsers", summary.getTotalUserBalanceUsers(),
                "lastCheck", summary.getLastSyncCheck()
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("統合システムヘルスチェックエラー", e);
            Map<String, Object> health = Map.of(
                "status", "error",
                "error", e.getMessage()
            );
            return ResponseEntity.status(500).body(health);
        }
    }

    // ===== DTO Classes =====

    /**
     * 既存転送同期要求DTO
     */
    @lombok.Data
    public static class LegacyTransferSyncRequest {
        private String fromUserId;
        private String toUserId;
        private java.math.BigDecimal amount;
        private String transactionType;
        private String description;
    }
}
