package com.sfr.tokyo.sfr_backend.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stripe決済関連DTO集合
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-09-11
 */
public class PaymentDto {

    /**
     * PaymentIntent作成リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePaymentIntentRequest {
        @NotNull
        @DecimalMin(value = "0.01", message = "金額は0.01以上である必要があります")
        private BigDecimal amountJpy;

        @NotBlank(message = "通貨コードが必要です")
        private String currency = "jpy";

        @NotBlank(message = "ユーザーIDが必要です")
        private String userId;

        private String description;
        
        private String metadata;
        
        // SFR換算用オプション
        private BigDecimal sfrEquivalent;
        private String exchangeRateSnapshot;
    }

    /**
     * PaymentIntent作成レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePaymentIntentResponse {
        @NotBlank
        private String paymentIntentId;

        @NotBlank
        private String clientSecret;

        @NotNull
        private BigDecimal amount;

        @NotBlank
        private String currency;

        @NotBlank
        private String status;

        // SFR換算情報
        private BigDecimal sfrEquivalent;
        private BigDecimal exchangeRate;
        private String exchangeRateTimestamp;
    }

    /**
     * 決済確認リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmPaymentRequest {
        @NotBlank(message = "PaymentIntent IDが必要です")
        private String paymentIntentId;

        @NotBlank(message = "ユーザーIDが必要です")
        private String userId;

        // SFR変換オプション
        private Boolean convertToSfr = false;
        private String sfrWalletAddress;
    }

    /**
     * 決済確認レスポンス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmPaymentResponse {
        @NotBlank
        private String paymentIntentId;

        @NotBlank
        private String status;

        private BigDecimal amountPaid;
        private String currency;

        // SFR変換結果
        private BigDecimal sfrAmountIssued;
        private String sfrTransactionId;
        private String conversionTimestamp;
    }

    /**
     * Webhook イベント処理結果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookEventResult {
        @NotBlank
        private String eventId;

        @NotBlank
        private String eventType;

        @NotBlank
        private String status; // processed, failed, ignored

        private String paymentIntentId;
        private String errorMessage;
        private String processingTimestamp;
    }
}
