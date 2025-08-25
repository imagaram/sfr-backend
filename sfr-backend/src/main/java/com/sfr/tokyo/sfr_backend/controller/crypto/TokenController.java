package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.dto.crypto.TokenDto;
import com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TokenController - SFRトークン管理API
 * トークンの残高照会、転送、発行、徴収機能を提供
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-20
 */
@RestController
@RequestMapping("/api/crypto/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final UserBalanceService userBalanceService;
    private final BalanceHistoryService balanceHistoryService;

    // =============================================================================
    // 残高関連エンドポイント
    // =============================================================================

    /**
     * 現在ユーザーの残高取得
     * 
     * @param authentication 認証情報
     * @return 残高情報
     */
    @GetMapping("/balance")
    public ResponseEntity<TokenDto.BalanceResponse> getBalance(Authentication authentication) {
        String userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Long spaceId = 1L; // デフォルトスペースID
        log.info("Getting balance for user: {} in space: {}", userId, spaceId);

        try {
            UserBalance userBalance = userBalanceService.getUserBalance(userId, spaceId)
                    .orElseGet(() -> userBalanceService.createUserBalance(userId, spaceId, BigDecimal.ZERO));

            TokenDto.BalanceResponse response = TokenDto.BalanceResponse.builder()
                    .userId(userId)
                    .balance(userBalance.getCurrentBalance())
                    .lastUpdated(userBalance.getUpdatedAt())
                    .displayBalance(formatBalance(userBalance.getCurrentBalance()))
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting balance for user: {} in space: {}", userId, spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 指定ユーザーの残高取得（ADMIN専用）
     * 
     * @param userId ユーザーID
     * @return 残高情報
     */
    @GetMapping("/balance/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TokenDto.BalanceResponse> getUserBalance(@PathVariable String userId) {
        Long spaceId = 1L; // デフォルトスペースID
        log.info("Admin getting balance for user: {} in space: {}", userId, spaceId);

        try {
            UserBalance userBalance = userBalanceService.getUserBalance(userId, spaceId)
                    .orElseGet(() -> userBalanceService.createUserBalance(userId, spaceId, BigDecimal.ZERO));

            TokenDto.BalanceResponse response = TokenDto.BalanceResponse.builder()
                    .userId(userId)
                    .balance(userBalance.getCurrentBalance())
                    .lastUpdated(userBalance.getUpdatedAt())
                    .displayBalance(formatBalance(userBalance.getCurrentBalance()))
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting balance for user: {} in space: {}", userId, spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // =============================================================================
    // 転送関連エンドポイント
    // =============================================================================

    /**
     * トークン転送
     * 
     * @param request        転送リクエスト
     * @param authentication 認証情報
     * @return 転送結果
     */
    @PostMapping("/transfer")
    public ResponseEntity<TokenDto.TransferResponse> transferTokens(
            @Valid @RequestBody TokenDto.TransferRequest request,
            Authentication authentication) {
        String senderId = resolveUserId(authentication);
        if (senderId == null) {
            return ResponseEntity.status(401).build();
        }
        Long spaceId = 1L; // デフォルトスペースID
        log.info("Processing token transfer from {} to {} amount: {} in space: {}",
                senderId, request.getRecipientId(), request.getAmount(), spaceId);

        try {
            // 送金者残高チェック
            UserBalance senderBalance = userBalanceService.getUserBalance(senderId, spaceId)
                    .orElseThrow(() -> new RuntimeException("Sender balance not found"));

            if (senderBalance.getCurrentBalance().compareTo(request.getAmount()) < 0) {
                return ResponseEntity.badRequest().body(
                        TokenDto.TransferResponse.builder()
                                .status("INSUFFICIENT_BALANCE")
                                .build());
            }

            // 受取人の残高取得または作成
            UserBalance recipientBalance = userBalanceService.getUserBalance(request.getRecipientId(), spaceId)
                    .orElseGet(() -> userBalanceService.createUserBalance(request.getRecipientId(), spaceId,
                            BigDecimal.ZERO));

            // 転送実行
            String transactionId = UUID.randomUUID().toString();

            // 送金者残高更新
            BigDecimal newSenderBalance = senderBalance.getCurrentBalance().subtract(request.getAmount());
            userBalanceService.updateUserBalance(senderId, spaceId, newSenderBalance);

            // 受取人残高更新
            BigDecimal newRecipientBalance = recipientBalance.getCurrentBalance().add(request.getAmount());
            userBalanceService.updateUserBalance(request.getRecipientId(), spaceId, newRecipientBalance);

            // 履歴記録（送金者）
            balanceHistoryService.recordTransfer(senderId, senderBalance.getCurrentBalance(),
                    request.getAmount(), true, transactionId);

            // 履歴記録（受取人）
            balanceHistoryService.recordTransfer(request.getRecipientId(),
                    recipientBalance.getCurrentBalance(),
                    request.getAmount(), false, transactionId);

            TokenDto.TransferResponse response = TokenDto.TransferResponse.builder()
                    .transactionId(transactionId)
                    .senderId(senderId)
                    .recipientId(request.getRecipientId())
                    .amount(request.getAmount())
                    .senderBalanceAfter(newSenderBalance)
                    .recipientBalanceAfter(newRecipientBalance)
                    .processedAt(LocalDateTime.now())
                    .message(request.getMessage())
                    .status("SUCCESS")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing token transfer", e);
            return ResponseEntity.internalServerError().body(
                    TokenDto.TransferResponse.builder()
                            .status("FAILED")
                            .build());
        }
    }

    // =============================================================================
    // 管理者専用エンドポイント
    // =============================================================================

    /**
     * トークン発行（ADMIN専用）
     * 
     * @param request 発行リクエスト
     * @return 発行結果
     */
    @PostMapping("/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TokenDto.IssueResponse> issueTokens(
            @Valid @RequestBody TokenDto.IssueRequest request) {
        Long spaceId = 1L; // デフォルトスペースID
        log.info("Admin issuing tokens to user: {} amount: {} in space: {}", request.getUserId(), request.getAmount(),
                spaceId);

        try {
            // ユーザー残高取得または作成
            UserBalance userBalance = userBalanceService.getUserBalance(request.getUserId(), spaceId)
                    .orElseGet(
                            () -> userBalanceService.createUserBalance(request.getUserId(), spaceId, BigDecimal.ZERO));

            // 残高更新
            BigDecimal oldBalance = userBalance.getCurrentBalance();
            BigDecimal newBalance = oldBalance.add(request.getAmount());
            userBalanceService.updateUserBalance(request.getUserId(), spaceId, newBalance);

            // 履歴記録
            String transactionId = UUID.randomUUID().toString();
            balanceHistoryService.recordEarning(request.getUserId(), oldBalance,
                    request.getAmount(), transactionId, request.getReason());

            TokenDto.IssueResponse response = TokenDto.IssueResponse.builder()
                    .transactionId(transactionId)
                    .userId(request.getUserId())
                    .issuedAmount(request.getAmount())
                    .balanceAfter(newBalance)
                    .issuedAt(LocalDateTime.now())
                    .reason(request.getReason())
                    .status("SUCCESS")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error issuing tokens", e);
            return ResponseEntity.internalServerError().body(
                    TokenDto.IssueResponse.builder()
                            .status("FAILED")
                            .build());
        }
    }

    /**
     * トークン徴収（ADMIN専用）
     * 
     * @param request 徴収リクエスト
     * @return 徴収結果
     */
    @PostMapping("/collect")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TokenDto.CollectResponse> collectTokens(
            @Valid @RequestBody TokenDto.CollectRequest request) {
        Long spaceId = 1L; // デフォルトスペースID
        log.info("Admin collecting tokens from user: {} amount: {} in space: {}", request.getUserId(),
                request.getAmount(), spaceId);

        try {
            UserBalance userBalance = userBalanceService.getUserBalance(request.getUserId(), spaceId)
                    .orElseThrow(() -> new RuntimeException("User balance not found"));

            // 残高不足チェック（強制徴収でない場合）
            if (!request.getForceCollection() && userBalance.getCurrentBalance().compareTo(request.getAmount()) < 0) {
                return ResponseEntity.badRequest().body(
                        TokenDto.CollectResponse.builder()
                                .status("INSUFFICIENT_BALANCE")
                                .build());
            }

            // 残高更新
            BigDecimal oldBalance = userBalance.getCurrentBalance();
            BigDecimal newBalance = oldBalance.subtract(request.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                newBalance = BigDecimal.ZERO; // 残高をマイナスにしない
            }
            userBalanceService.updateUserBalance(request.getUserId(), spaceId, newBalance);

            // 履歴記録
            String transactionId = UUID.randomUUID().toString();
            balanceHistoryService.recordCollection(request.getUserId(), oldBalance,
                    request.getAmount(), transactionId, request.getReason());

            TokenDto.CollectResponse response = TokenDto.CollectResponse.builder()
                    .transactionId(transactionId)
                    .userId(request.getUserId())
                    .collectedAmount(request.getAmount())
                    .balanceAfter(newBalance)
                    .collectedAt(LocalDateTime.now())
                    .reason(request.getReason())
                    .status("SUCCESS")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error collecting tokens", e);
            return ResponseEntity.internalServerError().body(
                    TokenDto.CollectResponse.builder()
                            .status("FAILED")
                            .build());
        }
    }

    // =============================================================================
    // 履歴・統計エンドポイント
    // =============================================================================

    /**
     * 取引履歴取得
     * 
     * @param request        履歴取得リクエスト
     * @param authentication 認証情報
     * @return 取引履歴
     */
    @GetMapping("/history")
    public ResponseEntity<Page<TokenDto.TransactionHistoryItem>> getTransactionHistory(
            @Valid TokenDto.TransactionHistoryRequest request,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        log.info("Getting transaction history for user: {}", userId);

        try {
            Page<BalanceHistory> historyPage = balanceHistoryService.getUserBalanceHistory(
                    userId, request.getPage(), request.getSize());

            Page<TokenDto.TransactionHistoryItem> response = historyPage.map(this::convertToHistoryItem);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting transaction history for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 統計情報取得
     * 
     * @param request        統計リクエスト
     * @param authentication 認証情報
     * @return 統計情報
     */
    @GetMapping("/statistics")
    public ResponseEntity<TokenDto.StatisticsResponse> getStatistics(
            @Valid TokenDto.StatisticsRequest request,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        log.info("Getting statistics for user: {}", userId);

        try {
            LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate()
                    : LocalDateTime.now().minusMonths(1);
            LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate() : LocalDateTime.now();

            BalanceHistoryService.BalanceStatistics stats = balanceHistoryService.getBalanceStatistics(
                    userId, startDate, endDate);

            BigDecimal currentBalance = balanceHistoryService.calculateCurrentBalance(userId);

            TokenDto.StatisticsResponse response = TokenDto.StatisticsResponse.builder()
                    .totalTransactions(stats.getTotalTransactions())
                    .totalEarnings(stats.getTotalEarnings())
                    .totalSpendings(stats.getTotalSpendings())
                    .totalCollections(stats.getTotalCollections())
                    .netChange(stats.getNetChange())
                    .currentBalance(currentBalance)
                    .periodStart(startDate)
                    .periodEnd(endDate)
                    .averageTransactionAmount(stats.getAverageTransactionAmount())
                    .maxIncrease(stats.getMaxIncrease())
                    .maxDecrease(stats.getMaxDecrease())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting statistics for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // =============================================================================
    // ヘルパーメソッド
    // =============================================================================

    /**
     * 残高を表示用にフォーマット
     */
    private String formatBalance(BigDecimal balance) {
        return String.format("%.8f SFR", balance);
    }

    /**
     * BalanceHistoryを履歴アイテムに変換
     */
    private TokenDto.TransactionHistoryItem convertToHistoryItem(BalanceHistory history) {
        return TokenDto.TransactionHistoryItem.builder()
                .historyId(history.getHistoryId())
                .transactionType(history.getTransactionType().name())
                .amount(history.getAmount())
                .balanceBefore(history.getBalanceBefore())
                .balanceAfter(history.getBalanceAfter())
                .createdAt(history.getCreatedAt())
                .reason(history.getReason())
                .referenceId(history.getReferenceId())
                .displayType(history.getTransactionType().getDisplayName())
                .build();
    }

    private String resolveUserId(Authentication authentication) {
        if (authentication != null) {
            return authentication.getName();
        }
        Authentication ctxAuth = SecurityContextHolder.getContext().getAuthentication();
        return ctxAuth != null ? ctxAuth.getName() : null;
    }
}
