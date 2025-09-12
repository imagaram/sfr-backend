package com.sfr.tokyo.sfr_backend.controller.admin;

import com.sfr.tokyo.sfr_backend.entity.config.SfrPointConfig;
import com.sfr.tokyo.sfr_backend.service.config.SfrPointConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * SFRポイント管理者コントローラー
 * Phase 1 実装: SFRポイントシステム管理機能
 * 
 * エンドポイント:
 * - GET /api/v1/admin/sfr/config - 設定一覧取得
 * - PUT /api/v1/admin/sfr/config - 設定更新
 * - GET /api/v1/admin/sfr/stats - システム統計
 * - POST /api/v1/admin/sfr/config/init - 設定初期化
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/api/v1/admin/sfr")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class SfrAdminController {

    private final SfrPointConfigService configService;

    /**
     * SFRポイント設定一覧取得
     */
    @GetMapping("/config")
    public ResponseEntity<List<SfrPointConfig>> getConfigs(
            @RequestParam(defaultValue = "1") Long spaceId) {
        
        try {
            List<SfrPointConfig> configs = configService.getActiveConfigs(spaceId);
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            log.error("設定一覧取得失敗: spaceId={}", spaceId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * SFRポイント設定更新
     */
    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@Valid @RequestBody ConfigUpdateRequest request) {
        log.info("設定更新要求: key={}, value={}, spaceId={}", 
                request.getConfigKey(), request.getConfigValue(), request.getSpaceId());

        try {
            // 管理者認証チェック（後で実装）
            String updatedBy = request.getUpdatedBy() != null ? request.getUpdatedBy() : "admin";

            configService.updateConfigValue(
                request.getConfigKey(),
                request.getSpaceId(),
                request.getConfigValue(),
                updatedBy
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "設定を更新しました",
                "configKey", request.getConfigKey(),
                "newValue", request.getConfigValue()
            ));

        } catch (Exception e) {
            log.error("設定更新失敗: key={}", request.getConfigKey(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * SFRポイントシステム統計取得
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats(
            @RequestParam(defaultValue = "1") Long spaceId) {
        
        try {
            Map<String, Object> stats = Map.of(
                "systemEnabled", configService.isSfrPointSystemEnabled(spaceId),
                "exchangeRate", configService.getCurrentExchangeRate(spaceId),
                "feeRate", configService.getFeeRate(spaceId),
                "sfrtRewardRates", configService.getSfrtRewardRates(spaceId),
                "timestamp", System.currentTimeMillis()
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("システム統計取得失敗: spaceId={}", spaceId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 設定初期化（開発・テスト用）
     */
    @PostMapping("/config/init")
    public ResponseEntity<Map<String, Object>> initializeConfigs() {
        log.info("設定初期化要求");

        try {
            configService.initializeDefaultConfigs();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "設定の初期化が完了しました"
            ));
        } catch (Exception e) {
            log.error("設定初期化失敗", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * システム状態確認
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth(
            @RequestParam(defaultValue = "1") Long spaceId) {
        
        try {
            boolean isEnabled = configService.isSfrPointSystemEnabled(spaceId);
            BigDecimal exchangeRate = configService.getCurrentExchangeRate(spaceId);
            
            Map<String, Object> health = Map.of(
                "status", isEnabled ? "ACTIVE" : "INACTIVE",
                "exchangeRate", exchangeRate,
                "expectedRate", new BigDecimal("150.00"),
                "rateMatch", exchangeRate.compareTo(new BigDecimal("150.00")) == 0,
                "timestamp", System.currentTimeMillis()
            );

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("システム状態確認失敗: spaceId={}", spaceId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "ERROR",
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 推奨設定値一覧取得
     */
    @GetMapping("/config/defaults")
    public ResponseEntity<Map<String, String>> getDefaultConfigs() {
        Map<String, String> defaults = Map.of(
            SfrPointConfig.ConfigKeys.SFR_EXCHANGE_RATE, SfrPointConfig.DefaultValues.EXCHANGE_RATE,
            SfrPointConfig.ConfigKeys.SFR_FEE_RATE, SfrPointConfig.DefaultValues.FEE_RATE,
            SfrPointConfig.ConfigKeys.SFR_MIN_PURCHASE, SfrPointConfig.DefaultValues.MIN_PURCHASE,
            SfrPointConfig.ConfigKeys.SFR_MAX_PURCHASE, SfrPointConfig.DefaultValues.MAX_PURCHASE,
            SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_BUYER, SfrPointConfig.DefaultValues.SFRT_BUYER_RATE,
            SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_SELLER, SfrPointConfig.DefaultValues.SFRT_SELLER_RATE,
            SfrPointConfig.ConfigKeys.SFRT_REWARD_RATE_PLATFORM, SfrPointConfig.DefaultValues.SFRT_PLATFORM_RATE
        );

        return ResponseEntity.ok(defaults);
    }

    /**
     * 設定更新要求DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConfigUpdateRequest {
        
        @NotBlank(message = "設定キーは必須です")
        private String configKey;

        @NotBlank(message = "設定値は必須です")
        private String configValue;

        @NotNull(message = "スペースIDは必須です")
        @lombok.Builder.Default
        private Long spaceId = 1L;

        private String updatedBy;
    }
}
