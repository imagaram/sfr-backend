package com.sfr.tokyo.sfr_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sfr.tokyo.sfr_backend.interceptor.SecurityHeadersInterceptor;

/**
 * セキュリティヘッダー設定クラス
 * Content Security Policy (CSP), HSTS, X-Frame-Options等のセキュリティヘッダーを設定
 */
@Configuration
public class SecurityHeadersConfig implements WebMvcConfigurer {

    @Bean
    public SecurityHeadersInterceptor securityHeadersInterceptor() {
        return new SecurityHeadersInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityHeadersInterceptor());
    }
}
