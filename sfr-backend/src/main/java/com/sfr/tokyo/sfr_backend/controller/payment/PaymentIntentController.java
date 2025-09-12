package com.sfr.tokyo.sfr_backend.controller.payment;

import com.sfr.tokyo.sfr_backend.dto.payment.PaymentDto;
import com.sfr.tokyo.sfr_backend.service.payment.PaymentIntentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe決済API
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-09-11
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment API", description = "Stripe決済管理API")
public class PaymentIntentController {

    private final PaymentIntentService paymentIntentService;

    /**
     * PaymentIntent作成
     */
    @PostMapping("/payment-intents")
    @Operation(summary = "PaymentIntent作成", description = "Stripe決済のためのPaymentIntentを作成")
    public ResponseEntity<PaymentDto.CreatePaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentDto.CreatePaymentIntentRequest request) {
        
        try {
            PaymentDto.CreatePaymentIntentResponse response = 
                    paymentIntentService.createPaymentIntent(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (StripeException e) {
            log.error("Stripe error creating PaymentIntent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            
        } catch (IllegalStateException e) {
            log.error("Stripe not configured: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            
        } catch (Exception e) {
            log.error("Unexpected error creating PaymentIntent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PaymentIntent状態確認
     */
    @GetMapping("/payment-intents/{paymentIntentId}")
    @Operation(summary = "PaymentIntent確認", description = "PaymentIntentの現在状態を取得")
    public ResponseEntity<?> getPaymentIntent(@PathVariable String paymentIntentId) {
        
        try {
            var paymentIntent = paymentIntentService.retrievePaymentIntent(paymentIntentId);
            
            // 基本情報のみレスポンス
            var response = PaymentDto.CreatePaymentIntentResponse.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .status(paymentIntent.getStatus())
                    .amount(java.math.BigDecimal.valueOf(paymentIntent.getAmount())
                            .divide(java.math.BigDecimal.valueOf(100)))
                    .currency(paymentIntent.getCurrency())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (StripeException e) {
            log.error("Stripe error retrieving PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            
        } catch (Exception e) {
            log.error("Error retrieving PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PaymentIntentキャンセル
     */
    @PostMapping("/payment-intents/{paymentIntentId}/cancel")
    @Operation(summary = "PaymentIntentキャンセル", description = "PaymentIntentをキャンセル")
    public ResponseEntity<?> cancelPaymentIntent(@PathVariable String paymentIntentId) {
        
        try {
            var paymentIntent = paymentIntentService.cancelPaymentIntent(paymentIntentId);
            
            log.info("PaymentIntent cancelled: {}", paymentIntentId);
            
            return ResponseEntity.ok().body(java.util.Map.of(
                    "paymentIntentId", paymentIntent.getId(),
                    "status", paymentIntent.getStatus()
            ));
            
        } catch (StripeException e) {
            log.error("Stripe error cancelling PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            
        } catch (Exception e) {
            log.error("Error cancelling PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 決済確認
     */
    @PostMapping("/confirm")
    @Operation(summary = "決済確認", description = "PaymentIntentの決済完了を確認")
    public ResponseEntity<PaymentDto.ConfirmPaymentResponse> confirmPayment(
            @Valid @RequestBody PaymentDto.ConfirmPaymentRequest request) {
        
        try {
            PaymentDto.ConfirmPaymentResponse response = 
                    paymentIntentService.confirmPayment(request);
            
            return ResponseEntity.ok(response);
            
        } catch (StripeException e) {
            log.error("Stripe error confirming payment {}: {}", request.getPaymentIntentId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            
        } catch (IllegalArgumentException e) {
            log.error("Payment confirmation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            
        } catch (Exception e) {
            log.error("Unexpected error confirming payment {}: {}", request.getPaymentIntentId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
