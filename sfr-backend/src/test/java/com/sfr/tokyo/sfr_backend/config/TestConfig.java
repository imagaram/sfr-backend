package com.sfr.tokyo.sfr_backend.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * テスト専用設定クラス - Integration Test用Repository設定
 */
@TestConfiguration
@EnableJpaRepositories(basePackages = {
        "com.sfr.tokyo.sfr_backend.repository",
        "com.sfr.tokyo.sfr_backend.repository.crypto"
})
@EntityScan(basePackages = {
        "com.sfr.tokyo.sfr_backend.entity",
        "com.sfr.tokyo.sfr_backend.entity.crypto",
        "com.sfr.tokyo.sfr_backend.user"
})
@ComponentScan(basePackages = {
        "com.sfr.tokyo.sfr_backend.service",
        "com.sfr.tokyo.sfr_backend.service.crypto",
        "com.sfr.tokyo.sfr_backend.repository",
        "com.sfr.tokyo.sfr_backend.repository.crypto"
})
@EnableTransactionManagement
public class TestConfig {
    // Empty configuration class - annotations provide the configuration
}
