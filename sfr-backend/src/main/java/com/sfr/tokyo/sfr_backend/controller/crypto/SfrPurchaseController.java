package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPurchaseTransaction;
import com.sfr.tokyo.sfr_backend.service.crypto.SfrPurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * SFRポイント購入コントローラー
 * Phase 1 実装: SFRポイント購入機能
 * 
 * エンドポイント:
 * - POST /api/v1/crypto/sfr/purchase - SFR購入要求
 * - POST /api/v1/crypto/sfr/complete - 購入完了処理
 * - GET /api/v1/crypto/sfr/history - 購入履歴
 * - GET /api/v1/crypto/sfr/stats - 購入統計
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/api/v1/crypto/sfr")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class SfrPurchaseController {

    private final SfrPurchaseService sfrPurchaseService;

    /**
     * SFRポイント購入要求
     */
    @PostMapping("/purchase")
    public ResponseEntity<SfrPurchaseResponse> createPurchaseRequest(@Valid @RequestBody SfrPurchaseRequest request) {
        log.info("SFR購入要求受信: userId={}, yenAmount={}", request.getUserId(), request.getYenAmount());

        try {
            SfrPurchaseTransaction transaction = sfrPurchaseService.createPurchaseRequest(
                request.getUserId(),
                request.getYenAmount(),
                request.getSpaceId()
            );

            SfrPurchaseResponse response = SfrPurchaseResponse.builder()
                    .transactionId(transaction.getId())
                    .sfrAmount(transaction.getSfrAmount())
                    .yenAmount(transaction.getYenAmount())
                    .exchangeRate(transaction.getExchangeRate())
                    .stripePaymentIntentId(transaction.getStripePaymentIntentId())
                    .status(transaction.getStatus().name())
                    .build();

            log.info("SFR購入要求作成成功: transactionId={}", transaction.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("SFR購入要求作成失敗: userId={}", request.getUserId(), e);
            return ResponseEntity.badRequest().body(
                SfrPurchaseResponse.builder()
                    .error(e.getMessage())
                    .build()
            );
        }
    }

    /**
     * SFRポイント購入完了処理
     */
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completePurchase(@Valid @RequestBody SfrPurchaseCompleteRequest request) {
        log.info("SFR購入完了処理: transactionId={}, paymentIntentId={}", 
                request.getTransactionId(), request.getStripePaymentIntentId());

        try {
            sfrPurchaseService.completePurchase(
                request.getTransactionId(),
                request.getStripePaymentIntentId()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "SFRポイント購入が完了しました"
            ));

        } catch (Exception e) {
            log.error("SFR購入完了処理失敗: transactionId={}", request.getTransactionId(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * ユーザーの購入履歴取得
     */
    @GetMapping("/history")
    public ResponseEntity<List<SfrPurchaseTransaction>> getPurchaseHistory(
            @RequestParam @NotBlank String userId,
            @RequestParam(defaultValue = "1") Long spaceId) {
        
        try {
            List<SfrPurchaseTransaction> history = sfrPurchaseService.getUserPurchaseHistory(userId, spaceId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("購入履歴取得失敗: userId={}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザーの購入統計取得
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPurchaseStats(
            @RequestParam @NotBlank String userId,
            @RequestParam(defaultValue = "1") Long spaceId) {
        
        try {
            Map<String, Object> stats = sfrPurchaseService.getPurchaseStats(userId, spaceId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("購入統計取得失敗: userId={}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * SFR購入要求DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SfrPurchaseRequest {
        
        @NotBlank(message = "ユーザーIDは必須です")
        private String userId;

        @NotNull(message = "購入金額は必須です")
        @DecimalMin(value = "100.0", message = "最小購入金額は100円です")
        private BigDecimal yenAmount;

        @NotNull(message = "スペースIDは必須です")
        @lombok.Builder.Default
        private Long spaceId = 1L;
    }

    /**
     * SFR購入完了要求DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SfrPurchaseCompleteRequest {
        
        @NotNull(message = "取引IDは必須です")
        private Long transactionId;

        @NotBlank(message = "PaymentIntent IDは必須です")
        private String stripePaymentIntentId;
    }

    /**
     * SFR購入レスポンスDTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SfrPurchaseResponse {
        
        private Long transactionId;
        private BigDecimal sfrAmount;
        private BigDecimal yenAmount;
        private BigDecimal exchangeRate;
        private String stripePaymentIntentId;
        private String status;
        private String error;
    }
}
