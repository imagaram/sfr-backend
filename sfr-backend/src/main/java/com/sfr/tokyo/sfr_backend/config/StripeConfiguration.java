package com.sfr.tokyo.sfr_backend.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe決済設定
 * 
 * @author SFR Development Team  
 * @version 1.0
 * @since 2025-09-11
 */
@Configuration
@Slf4j
public class StripeConfiguration {

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${stripe.environment:test}")
    private String environment;

    @PostConstruct
    public void init() {
        if (stripeApiKey.isEmpty()) {
            log.warn("Stripe API key is not configured. Stripe functionality will be disabled.");
            return;
        }

        Stripe.apiKey = stripeApiKey;
        log.info("Stripe configuration initialized for environment: {}", environment);
        
        // テスト環境でのAPI キー確認（先頭のみログ出力）
        if (environment.equals("test") && !stripeApiKey.isEmpty()) {
            String maskedKey = stripeApiKey.substring(0, 
                Math.min(stripeApiKey.length(), 12)) + "***";
            log.info("Using Stripe API key: {}", maskedKey);
        }
    }

    /**
     * Webhook署名検証用の秘密鍵を取得
     */
    public String getWebhookSecret() {
        return webhookSecret;
    }

    /**
     * Stripe APIキーが設定されているかチェック
     */
    public boolean isConfigured() {
        return !stripeApiKey.isEmpty();
    }

    /**
     * 本番環境かテスト環境かを判定
     */
    public boolean isProductionMode() {
        return "production".equals(environment);
    }
}
