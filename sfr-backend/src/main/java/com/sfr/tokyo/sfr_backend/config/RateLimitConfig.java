package com.sfr.tokyo.sfr_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sfr.tokyo.sfr_backend.interceptor.RateLimitInterceptor;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;

/**
 * レート制限設定クラス
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitService rateLimitService;

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor(rateLimitService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/status");
    }
}
