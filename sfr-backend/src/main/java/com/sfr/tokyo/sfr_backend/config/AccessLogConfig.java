package com.sfr.tokyo.sfr_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sfr.tokyo.sfr_backend.interceptor.AccessLogInterceptor;

/**
 * アクセスログ設定クラス
 */
@Configuration
public class AccessLogConfig implements WebMvcConfigurer {

    @Bean
    public AccessLogInterceptor accessLogInterceptor() {
        return new AccessLogInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLogInterceptor())
                .addPathPatterns("/**");
    }
}
