package com.sfr.tokyo.sfr_backend.controller.payment;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe Webhook処理コントローラー
 * Stripe決済イベントの署名検証と処理を行う
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-09-12
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/stripe/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook.endpoint.secret:}")
    private String webhookSecret;

    @Value("${stripe.api.secret.key:}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isEmpty()) {
            Stripe.apiKey = stripeSecretKey;
            log.info("Stripe API key configured");
        } else {
            log.warn("Stripe API key not configured");
        }
    }

    /**
     * Stripe Webhook処理エンドポイント
     * 
     * @param payload Webhookペイロード
     * @param sigHeader Stripe署名ヘッダー
     * @return 処理結果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Stripe webhook received, payload length: {}", payload.length());
        
        Event event;
        
        try {
            // 署名検証
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Webhook signature verified successfully");
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid signature", 400));
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Webhook processing failed", 400));
        }

        // イベント処理
        try {
            processWebhookEvent(event);
            
            Map<String, Object> response = new HashMap<>();
            response.put("received", true);
            response.put("eventType", event.getType());
            response.put("eventId", event.getId());
            
            log.info("Webhook processed successfully: eventType={}, eventId={}", 
                    event.getType(), event.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Event processing failed", 500));
        }
    }

    /**
     * Webhookイベント処理
     * 
     * @param event Stripeイベント
     */
    private void processWebhookEvent(Event event) {
        log.info("Processing webhook event: type={}, id={}", event.getType(), event.getId());
        
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            case "payment_intent.canceled":
                handlePaymentIntentCanceled(event);
                break;
            case "charge.dispute.created":
                handleChargeDisputeCreated(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
                break;
        }
    }

    /**
     * 決済成功処理
     * 
     * @param event Stripeイベント
     */
    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);
        
        if (paymentIntent != null) {
            log.info("Payment succeeded: id={}, amount={}, currency={}", 
                    paymentIntent.getId(), 
                    paymentIntent.getAmount(), 
                    paymentIntent.getCurrency());
            
            // TODO: 決済成功時の業務処理
            // - 注文ステータス更新
            // - 商品配送準備
            // - SFR報酬付与
            // - 通知送信
        }
    }

    /**
     * 決済失敗処理
     * 
     * @param event Stripeイベント
     */
    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);
        
        if (paymentIntent != null) {
            log.warn("Payment failed: id={}, amount={}, currency={}, lastPaymentError={}", 
                    paymentIntent.getId(), 
                    paymentIntent.getAmount(), 
                    paymentIntent.getCurrency(),
                    paymentIntent.getLastPaymentError() != null ? 
                            paymentIntent.getLastPaymentError().getMessage() : "Unknown");
            
            // TODO: 決済失敗時の業務処理
            // - 注文ステータス更新
            // - ユーザー通知
            // - リトライ処理
        }
    }

    /**
     * 決済キャンセル処理
     * 
     * @param event Stripeイベント
     */
    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);
        
        if (paymentIntent != null) {
            log.info("Payment canceled: id={}, amount={}, currency={}", 
                    paymentIntent.getId(), 
                    paymentIntent.getAmount(), 
                    paymentIntent.getCurrency());
            
            // TODO: 決済キャンセル時の業務処理
            // - 注文ステータス更新
            // - 在庫復旧
            // - ユーザー通知
        }
    }

    /**
     * チャージバック作成処理
     * 
     * @param event Stripeイベント
     */
    private void handleChargeDisputeCreated(Event event) {
        log.warn("Charge dispute created: eventId={}", event.getId());
        
        // TODO: チャージバック対応処理
        // - 管理者通知
        // - 証拠書類準備
        // - 顧客対応
    }

    /**
     * エラーレスポンス作成
     * 
     * @param message エラーメッセージ
     * @param code エラーコード
     * @return エラーレスポンス
     */
    private Map<String, Object> createErrorResponse(String message, int code) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("code", code);
        response.put("received", false);
        return response;
    }

    /**
     * Webhook設定状態確認エンドポイント
     * 
     * @return 設定状態
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWebhookStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("webhookSecretConfigured", webhookSecret != null && !webhookSecret.isEmpty());
        status.put("stripeApiKeyConfigured", stripeSecretKey != null && !stripeSecretKey.isEmpty());
        status.put("endpointPath", "/api/payment/stripe/webhook");
        
        return ResponseEntity.ok(status);
    }
}
