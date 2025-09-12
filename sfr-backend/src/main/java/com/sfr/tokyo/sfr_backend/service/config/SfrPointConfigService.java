package com.sfr.tokyo.sfr_backend.service.config;

import com.sfr.tokyo.sfr_backend.entity.config.SfrPointConfig;
import com.sfr.tokyo.sfr_backend.repository.config.SfrPointConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * SFRポイント設定管理サービス
 * Phase 1 実装: システム設定の初期化と管理
 * 
 * 主要機能:
 * - デフォルト設定の初期化
 * - 設定値の取得・更新
 * - 設定の妥当性検証
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SfrPointConfigService {

    private final SfrPointConfigRepository configRepository;

    /**
     * アプリケーション起動時のデフォルト設定初期化
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultConfigs() {
        log.info("SFRポイント設定の初期化を開始します");

        Long spaceId = 1L; // デフォルトスペース
        
        // デフォルト設定のリスト
        Map<String, String> defaultConfigs = Map.of(
            SfrPointConfig.ConfigKeys.SFR_EXCHANGE_RATE, SfrPointConfig.DefaultValues.EXCHANGE_RATE,
            SfrPointConfig.ConfigKeys.SFR_FEE_RATE, SfrPointConfig.DefaultValues.FEE_RATE,
            SfrPointConfig.ConfigKeys.SFR_MIN_PURCHASE, SfrPointConfig.DefaultValues.MIN_PURCHASE,
            SfrPointConfig.ConfigKeys.SFR_MAX_PURCHASE, SfrPointConfig.DefaultValues.MAX_PURCHASE,
            SfrPointConfig.ConfigKeys.SFR_DAILY_LIMIT, SfrPointConfig.DefaultValues.DAILY_LIMIT,
            SfrPointConfig.ConfigKeys.SFR_MONTHLY_LIMIT, SfrPointConfig.DefaultValues.MONTHLY_LIMIT,
            SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_BUYER, SfrPointConfig.DefaultValues.SFRT_BUYER_RATE,
            SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_SELLER, SfrPointConfig.DefaultValues.SFRT_SELLER_RATE,
            SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_PLATFORM, SfrPointConfig.DefaultValues.SFRT_PLATFORM_RATE,
            SfrPointConfig.ConfigKeys.SFR_POINT_ENABLED, SfrPointConfig.DefaultValues.POINT_ENABLED
        );

        int initializedCount = 0;
        for (Map.Entry<String, String> entry : defaultConfigs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!configRepository.existsByConfigKeyAndSpaceIdAndIsActiveTrue(key, spaceId)) {
                SfrPointConfig config = SfrPointConfig.builder()
                        .spaceId(spaceId)
                        .configKey(key)
                        .configValue(value)
                        .description(getConfigDescription(key))
                        .updatedBy("system")
                        .build();

                configRepository.save(config);
                initializedCount++;
                log.debug("設定を初期化しました: {} = {}", key, value);
            }
        }

        log.info("SFRポイント設定の初期化完了: {}件の設定を初期化", initializedCount);
    }

    /**
     * 設定値取得（BigDecimal型）
     */
    @Transactional(readOnly = true)
    public BigDecimal getConfigValueAsBigDecimal(String configKey, Long spaceId) {
        return configRepository.findByConfigKeyAndSpaceIdAndIsActiveTrue(configKey, spaceId)
                .map(SfrPointConfig::getValueAsBigDecimal)
                .orElseThrow(() -> new IllegalArgumentException("設定が見つかりません: " + configKey));
    }

    /**
     * 設定値取得（Boolean型）
     */
    @Transactional(readOnly = true)
    public Boolean getConfigValueAsBoolean(String configKey, Long spaceId) {
        return configRepository.findByConfigKeyAndSpaceIdAndIsActiveTrue(configKey, spaceId)
                .map(SfrPointConfig::getValueAsBoolean)
                .orElseThrow(() -> new IllegalArgumentException("設定が見つかりません: " + configKey));
    }

    /**
     * 設定値更新
     */
    public void updateConfigValue(String configKey, Long spaceId, String newValue, String updatedBy) {
        log.info("設定値更新: {} = {} (spaceId={}, updatedBy={})", configKey, newValue, spaceId, updatedBy);

        // 既存設定を無効化
        configRepository.deactivateConfig(configKey, spaceId, updatedBy);

        // 新しい設定を作成
        SfrPointConfig newConfig = SfrPointConfig.builder()
                .spaceId(spaceId)
                .configKey(configKey)
                .configValue(newValue)
                .description(getConfigDescription(configKey))
                .updatedBy(updatedBy)
                .build();

        configRepository.save(newConfig);
        log.info("設定値更新完了: {}", configKey);
    }

    /**
     * アクティブな設定一覧取得
     */
    @Transactional(readOnly = true)
    public List<SfrPointConfig> getActiveConfigs(Long spaceId) {
        return configRepository.findBySpaceIdAndIsActiveTrueOrderByConfigKey(spaceId);
    }

    /**
     * SFRポイントシステムの有効性チェック
     */
    @Transactional(readOnly = true)
    public boolean isSfrPointSystemEnabled(Long spaceId) {
        try {
            return getConfigValueAsBoolean(SfrPointConfig.ConfigKeys.SFR_POINT_ENABLED, spaceId);
        } catch (Exception e) {
            log.warn("SFRポイントシステム有効性チェックエラー: spaceId={}", spaceId, e);
            return false;
        }
    }

    /**
     * 現在の為替レート取得（1SFR = 150円固定）
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentExchangeRate(Long spaceId) {
        return getConfigValueAsBigDecimal(SfrPointConfig.ConfigKeys.SFR_EXCHANGE_RATE, spaceId);
    }

    /**
     * 手数料率取得
     */
    @Transactional(readOnly = true)
    public BigDecimal getFeeRate(Long spaceId) {
        return getConfigValueAsBigDecimal(SfrPointConfig.ConfigKeys.SFR_FEE_RATE, spaceId);
    }

    /**
     * SFRT報酬率取得
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getSfrtRewardRates(Long spaceId) {
        return Map.of(
            "buyer", getConfigValueAsBigDecimal(SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_BUYER, spaceId),
            "seller", getConfigValueAsBigDecimal(SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_SELLER, spaceId),
            "platform", getConfigValueAsBigDecimal(SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_PLATFORM, spaceId)
        );
    }

    /**
     * 設定キーの説明を返す
     */
    private String getConfigDescription(String configKey) {
        return switch (configKey) {
            case SfrPointConfig.ConfigKeys.SFR_EXCHANGE_RATE -> "SFR円換算レート（1SFR = n円）";
            case SfrPointConfig.ConfigKeys.SFR_FEE_RATE -> "SFR取引手数料率（小数）";
            case SfrPointConfig.ConfigKeys.SFR_MIN_PURCHASE -> "SFR最小購入額（円）";
            case SfrPointConfig.ConfigKeys.SFR_MAX_PURCHASE -> "SFR最大購入額（円）";
            case SfrPointConfig.ConfigKeys.SFR_DAILY_LIMIT -> "SFR日次購入限度額（円）";
            case SfrPointConfig.ConfigKeys.SFR_MONTHLY_LIMIT -> "SFR月次購入限度額（円）";
            case SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_BUYER -> "SFRT購入者報酬率（小数）";
            case SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_SELLER -> "SFRT販売者報酬率（小数）";
            case SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_PLATFORM -> "SFRTプラットフォーム報酬率（小数）";
            case SfrPointConfig.ConfigKeys.SFR_POINT_ENABLED -> "SFRポイントシステム有効フラグ";
            case SfrPointConfig.ConfigKeys.SFR_STRIPE_ENABLED -> "Stripe決済有効フラグ";
            default -> "SFRポイントシステム設定";
        };
    }
}
