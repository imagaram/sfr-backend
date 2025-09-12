package com.sfr.tokyo.sfr_backend.service.payment;

import com.sfr.tokyo.sfr_backend.config.StripeConfiguration;
import com.sfr.tokyo.sfr_backend.dto.payment.PaymentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe PaymentIntent管理サービス
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-09-11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentIntentService {

    private final StripeConfiguration stripeConfiguration;

    /**
     * PaymentIntentを作成
     */
    public PaymentDto.CreatePaymentIntentResponse createPaymentIntent(
            PaymentDto.CreatePaymentIntentRequest request) throws StripeException {
        
        if (!stripeConfiguration.isConfigured()) {
            throw new IllegalStateException("Stripe is not configured");
        }

        // 金額を円単位からセント単位に変換（Stripeは最小通貨単位を使用）
        long amountInCents = request.getAmountJpy().multiply(BigDecimal.valueOf(100)).longValue();

        // メタデータ設定
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", request.getUserId());
        if (request.getDescription() != null) {
            metadata.put("description", request.getDescription());
        }
        if (request.getSfrEquivalent() != null) {
            metadata.put("sfrEquivalent", request.getSfrEquivalent().toString());
        }

        // PaymentIntent作成パラメータ
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(request.getCurrency())
                .putMetadata("userId", request.getUserId())
                .putAllMetadata(metadata)
                .build();

        // Stripe API呼び出し
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        log.info("PaymentIntent created: {} for user: {} amount: {} {}", 
                paymentIntent.getId(), request.getUserId(), request.getAmountJpy(), request.getCurrency());

        // レスポンス作成
        return PaymentDto.CreatePaymentIntentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .amount(request.getAmountJpy())
                .currency(request.getCurrency())
                .status(paymentIntent.getStatus())
                .sfrEquivalent(request.getSfrEquivalent())
                .build();
    }

    /**
     * PaymentIntentの状態確認
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        if (!stripeConfiguration.isConfigured()) {
            throw new IllegalStateException("Stripe is not configured");
        }

        return PaymentIntent.retrieve(paymentIntentId);
    }

    /**
     * PaymentIntentをキャンセル
     */
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        if (!stripeConfiguration.isConfigured()) {
            throw new IllegalStateException("Stripe is not configured");
        }

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.cancel();
    }

    /**
     * 決済確認処理
     */
    public PaymentDto.ConfirmPaymentResponse confirmPayment(
            PaymentDto.ConfirmPaymentRequest request) throws StripeException {
        
        PaymentIntent paymentIntent = retrievePaymentIntent(request.getPaymentIntentId());
        
        // ユーザー検証
        String metadataUserId = paymentIntent.getMetadata().get("userId");
        if (!request.getUserId().equals(metadataUserId)) {
            throw new IllegalArgumentException("PaymentIntent does not belong to user");
        }

        log.info("Payment confirmed: {} for user: {} status: {}", 
                paymentIntent.getId(), request.getUserId(), paymentIntent.getStatus());

        // レスポンス作成
        BigDecimal amountPaid = BigDecimal.valueOf(paymentIntent.getAmount()).divide(BigDecimal.valueOf(100));
        
        return PaymentDto.ConfirmPaymentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .status(paymentIntent.getStatus())
                .amountPaid(amountPaid)
                .currency(paymentIntent.getCurrency())
                .build();
    }
}
