package com.sfr.tokyo.sfr.backend.controller;

import com.sfr.tokyo.sfr.backend.entity.SfrPurchaseTransaction;
import com.sfr.tokyo.sfr.backend.service.SfrPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sfr")
@Tag(name = "SFR Purchase", description = "SFR購入システムAPI")
public class SfrPurchaseController {
    
    @Autowired
    private SfrPurchaseService sfrPurchaseService;
    
    /**
     * SFR購入処理
     * ロードマップ Phase 1.2 の実装
     */
    @PostMapping("/purchase")
    @Operation(summary = "SFR購入", description = "法定通貨でSFRを購入します（1 SFR = 150円固定レート）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "購入成功"),
        @ApiResponse(responseCode = "400", description = "リクエストパラメータエラー"),
        @ApiResponse(responseCode = "403", description = "購入限度額超過"),
        @ApiResponse(responseCode = "500", description = "システムエラー")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> purchaseSfr(
            @Parameter(description = "ユーザーID", required = true)
            @RequestParam String userId,
            
            @Parameter(description = "スペースID", required = true) 
            @RequestParam Long spaceId,
            
            @Parameter(description = "購入金額（円）", required = true)
            @Valid @RequestBody SfrPurchaseRequest request) {
        
        SfrPurchaseService.SfrPurchaseResult result = sfrPurchaseService.purchaseSfr(
            userId, spaceId, request.getJpyAmount());
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(SfrPurchaseResponse.success(
                result.getTransactionId(),
                result.getSfrAmount(),
                result.getExchangeRate()
            ));
        } else {
            return ResponseEntity.badRequest().body(
                SfrPurchaseResponse.failure(result.getErrorMessage())
            );
        }
    }
    
    /**
     * ユーザーの購入履歴取得
     */
    @GetMapping("/purchase/history")
    @Operation(summary = "購入履歴取得", description = "ユーザーのSFR購入履歴を取得します")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SfrPurchaseTransaction>> getPurchaseHistory(
            @Parameter(description = "ユーザーID", required = true)
            @RequestParam String userId) {
        
        List<SfrPurchaseTransaction> history = sfrPurchaseService.getUserPurchaseHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * 購入限度額情報取得
     */
    @GetMapping("/purchase/limits")
    @Operation(summary = "購入限度額取得", description = "ユーザーの購入限度額と使用状況を取得します")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SfrPurchaseService.PurchaseLimitInfo> getPurchaseLimits(
            @Parameter(description = "ユーザーID", required = true)
            @RequestParam String userId,
            
            @Parameter(description = "スペースID", required = true)
            @RequestParam Long spaceId) {
        
        SfrPurchaseService.PurchaseLimitInfo limitInfo = 
            sfrPurchaseService.getPurchaseLimitInfo(userId, spaceId);
        return ResponseEntity.ok(limitInfo);
    }
    
    // DTOクラス
    public static class SfrPurchaseRequest {
        @NotNull(message = "購入金額は必須です")
        @DecimalMin(value = "150", message = "最小購入額は150円です")
        private BigDecimal jpyAmount;
        
        private String paymentMethodId; // Stripe Payment Method ID（将来実装）
        private String memo;
        
        // Getters and Setters
        public BigDecimal getJpyAmount() {
            return jpyAmount;
        }
        
        public void setJpyAmount(BigDecimal jpyAmount) {
            this.jpyAmount = jpyAmount;
        }
        
        public String getPaymentMethodId() {
            return paymentMethodId;
        }
        
        public void setPaymentMethodId(String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
        }
        
        public String getMemo() {
            return memo;
        }
        
        public void setMemo(String memo) {
            this.memo = memo;
        }
    }
    
    public static class SfrPurchaseResponse {
        private boolean success;
        private String message;
        private SfrPurchaseData data;
        
        private SfrPurchaseResponse(boolean success, String message, SfrPurchaseData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public static SfrPurchaseResponse success(Long transactionId, BigDecimal sfrAmount, BigDecimal exchangeRate) {
            SfrPurchaseData data = new SfrPurchaseData();
            data.transactionId = transactionId;
            data.sfrAmount = sfrAmount;
            data.exchangeRate = exchangeRate;
            data.jpyAmount = sfrAmount.multiply(exchangeRate);
            
            return new SfrPurchaseResponse(true, "SFR購入が完了しました", data);
        }
        
        public static SfrPurchaseResponse failure(String errorMessage) {
            return new SfrPurchaseResponse(false, errorMessage, null);
        }
        
        // Getters
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public SfrPurchaseData getData() {
            return data;
        }
        
        public static class SfrPurchaseData {
            private Long transactionId;
            private BigDecimal sfrAmount;
            private BigDecimal jpyAmount;
            private BigDecimal exchangeRate;
            
            // Getters
            public Long getTransactionId() {
                return transactionId;
            }
            
            public BigDecimal getSfrAmount() {
                return sfrAmount;
            }
            
            public BigDecimal getJpyAmount() {
                return jpyAmount;
            }
            
            public BigDecimal getExchangeRate() {
                return exchangeRate;
            }
        }
    }
}
