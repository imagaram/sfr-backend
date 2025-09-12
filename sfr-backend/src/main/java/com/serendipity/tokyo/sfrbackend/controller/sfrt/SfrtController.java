package com.serendipity.tokyo.sfrbackend.controller.sfrt;

import com.serendipity.tokyo.sfrbackend.entity.sfrt.SfrtBalance;
import com.serendipity.tokyo.sfrbackend.entity.sfrt.SfrtTransaction;
import com.serendipity.tokyo.sfrbackend.service.sfrt.SfrtBalanceService;
// TODO: 今後のリワード機能で使用予定
// import com.serendipity.tokyo.sfrbackend.service.sfrt.SfrtRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * SFRT（暗号資産）管理APIコントローラー
 * - SFRT残高・履歴表示
 * - 報酬配布管理
 * - 転送・交換機能
 */
@RestController
@RequestMapping("/api/v1/sfrt")
@RequiredArgsConstructor
@Slf4j
public class SfrtController {

    private final SfrtBalanceService sfrtBalanceService;
    // TODO: 今後のリワード配布機能で使用予定
    // private final SfrtRewardService sfrtRewardService;

    /**
     * ユーザーのSFRT残高を取得
     */
    @GetMapping("/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SfrtBalance> getSfrtBalance(
        @RequestParam String userId,
        @RequestParam Long spaceId
    ) {
        try {
            SfrtBalance balance = sfrtBalanceService.getOrCreateSfrtBalance(userId, spaceId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            log.error("SFRT残高取得エラー: userId={}, spaceId={}", userId, spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * SFRT取引履歴を取得
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<SfrtTransaction>> getSfrtTransactions(
        @RequestParam String userId,
        @RequestParam Long spaceId,
        Pageable pageable
    ) {
        try {
            // TODO: SfrtTransactionService実装後に更新
            // Page<SfrtTransaction> transactions = sfrtTransactionService.getUserTransactions(userId, spaceId, pageable);
            // return ResponseEntity.ok(transactions);
            return ResponseEntity.status(501).build(); // Not Implemented
        } catch (Exception e) {
            log.error("SFRT取引履歴取得エラー: userId={}, spaceId={}", userId, spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * SFRT転送（ユーザー間）
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> transferSfrt(
        @RequestBody TransferSfrtRequest request
    ) {
        try {
            // 転送元から減少
            SfrtTransaction deductTransaction = sfrtBalanceService.deductSfrtBalance(
                request.getFromUserId(),
                request.getSpaceId(),
                request.getAmount(),
                SfrtTransaction.SfrtTransactionType.TRANSFER_OUT,
                "SFRT転送: " + request.getToUserId() + "へ"
            );

            // 転送先に増加
            SfrtTransaction addTransaction = sfrtBalanceService.addSfrtBalance(
                request.getToUserId(),
                request.getSpaceId(),
                request.getAmount(),
                SfrtTransaction.SfrtTransactionType.TRANSFER_IN,
                "SFRT受信: " + request.getFromUserId() + "から"
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "SFRT転送が完了しました",
                "transferOutId", deductTransaction.getId(),
                "transferInId", addTransaction.getId(),
                "amount", request.getAmount()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("SFRT転送エラー: {}", request, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "転送処理中にエラーが発生しました"
            ));
        }
    }

    /**
     * SFRT手動報酬配布（管理者用）
     */
    @PostMapping("/admin/distribute-reward")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> distributeReward(
        @RequestBody DistributeRewardRequest request
    ) {
        try {
            SfrtTransaction transaction = sfrtBalanceService.addSfrtBalance(
                request.getUserId(),
                request.getSpaceId(),
                request.getAmount(),
                request.getTransactionType(),
                request.getDescription()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "報酬配布が完了しました",
                "transactionId", transaction.getId(),
                "amount", request.getAmount()
            ));

        } catch (Exception e) {
            log.error("報酬配布エラー: {}", request, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "報酬配布中にエラーが発生しました"
            ));
        }
    }

    /**
     * SFRTステータス更新（管理者用）
     */
    @PutMapping("/admin/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateSfrtStatus(
        @RequestParam String userId,
        @RequestParam Long spaceId,
        @RequestParam SfrtBalance.SfrtStatus status
    ) {
        try {
            if (status == SfrtBalance.SfrtStatus.FROZEN) {
                sfrtBalanceService.freezeSfrtBalance(userId, spaceId);
            } else if (status == SfrtBalance.SfrtStatus.ACTIVE) {
                sfrtBalanceService.unfreezeSfrtBalance(userId, spaceId);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "SFRT状態を更新しました",
                "userId", userId,
                "spaceId", spaceId,
                "status", status
            ));

        } catch (Exception e) {
            log.error("SFRT状態更新エラー: userId={}, spaceId={}, status={}", userId, spaceId, status, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "状態更新中にエラーが発生しました"
            ));
        }
    }

    /**
     * 外部取引所連携設定
     */
    @PutMapping("/exchange-settings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> updateExchangeSettings(
        @RequestParam String userId,
        @RequestParam Long spaceId,
        @RequestParam Boolean enabled
    ) {
        try {
            sfrtBalanceService.setExternalExchangeEnabled(userId, spaceId, enabled);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "外部取引所連携設定を更新しました",
                "enabled", enabled
            ));

        } catch (Exception e) {
            log.error("外部取引所設定更新エラー: userId={}, spaceId={}, enabled={}", userId, spaceId, enabled, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "設定更新中にエラーが発生しました"
            ));
        }
    }

    /**
     * SFRT転送リクエスト
     */
    public static class TransferSfrtRequest {
        private String fromUserId;
        private String toUserId;
        private Long spaceId;
        private BigDecimal amount;

        // Getters and Setters
        public String getFromUserId() { return fromUserId; }
        public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
        
        public String getToUserId() { return toUserId; }
        public void setToUserId(String toUserId) { this.toUserId = toUserId; }
        
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    /**
     * 報酬配布リクエスト
     */
    public static class DistributeRewardRequest {
        private String userId;
        private Long spaceId;
        private BigDecimal amount;
        private SfrtTransaction.SfrtTransactionType transactionType;
        private String description;

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public SfrtTransaction.SfrtTransactionType getTransactionType() { return transactionType; }
        public void setTransactionType(SfrtTransaction.SfrtTransactionType transactionType) { this.transactionType = transactionType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
