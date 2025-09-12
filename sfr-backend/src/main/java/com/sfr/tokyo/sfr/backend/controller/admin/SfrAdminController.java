package com.sfr.tokyo.sfr.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/sfr")
@Tag(name = "SFR Admin", description = "SFR管理画面API - ロードマップ Phase 1.3")
public class SfrAdminController {
    
    /**
     * SFR設定取得
     */
    @GetMapping("/config")
    @Operation(summary = "SFR設定取得", description = "現在のSFR設定を取得します")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "設定取得成功"),
        @ApiResponse(responseCode = "403", description = "管理者権限が必要"),
        @ApiResponse(responseCode = "500", description = "システムエラー")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SfrAdminConfig> getSfrConfig(
            @Parameter(description = "スペースID", required = true)
            @RequestParam(defaultValue = "1") Long spaceId) {
        
        SfrAdminConfig config = new SfrAdminConfig();
        config.setIsEnabled(true); // TODO: configService.isSfrEnabled(spaceId);
        config.setFixedRate(new BigDecimal("150.00")); // 固定レート
        config.setMaxPurchasePerUser(new BigDecimal("100000")); // TODO: configから取得
        config.setDailyPurchaseLimit(new BigDecimal("50000")); // TODO: configから取得
        config.setPurchaseStartDate(LocalDateTime.now().toString());
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * SFRシステム有効化/無効化
     */
    @PostMapping("/toggle")
    @Operation(summary = "SFRシステム切り替え", description = "SFRシステムの有効/無効を切り替えます")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SfrToggleResponse> toggleSfrSystem(
            @Valid @RequestBody SfrToggleRequest request) {
        
        try {
            // SFRシステムの有効化/無効化
            // TODO: configService.setSfrEnabled(request.getSpaceId(), request.isEnabled());
            
            String message = request.isEnabled() ? 
                "SFRシステムを有効化しました" : 
                "SFRシステムを無効化しました";
                
            return ResponseEntity.ok(SfrToggleResponse.success(
                request.isEnabled(), message));
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                SfrToggleResponse.failure("設定の更新に失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * SFR設定更新
     */
    @PutMapping("/config")
    @Operation(summary = "SFR設定更新", description = "SFR設定を更新します")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SfrConfigUpdateResponse> updateSfrConfig(
            @Valid @RequestBody SfrAdminConfigRequest request) {
        
        try {
            // 各設定の更新
            // TODO: configService.setSfrEnabled(request.getSpaceId(), request.isEnabled());
            // TODO: その他の設定値の更新実装
            
            return ResponseEntity.ok(SfrConfigUpdateResponse.success(
                "SFR設定を更新しました"));
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                SfrConfigUpdateResponse.failure("設定の更新に失敗しました: " + e.getMessage()));
        }
    }
    
    // DTOクラス（ロードマップ Phase 1.3 の仕様）
    public static class SfrAdminConfig {
        private Boolean isEnabled;
        private BigDecimal fixedRate;
        private BigDecimal maxPurchasePerUser;
        private BigDecimal dailyPurchaseLimit;
        private String purchaseStartDate;
        
        // Getters and Setters
        public Boolean getIsEnabled() {
            return isEnabled;
        }
        
        public void setIsEnabled(Boolean isEnabled) {
            this.isEnabled = isEnabled;
        }
        
        public BigDecimal getFixedRate() {
            return fixedRate;
        }
        
        public void setFixedRate(BigDecimal fixedRate) {
            this.fixedRate = fixedRate;
        }
        
        public BigDecimal getMaxPurchasePerUser() {
            return maxPurchasePerUser;
        }
        
        public void setMaxPurchasePerUser(BigDecimal maxPurchasePerUser) {
            this.maxPurchasePerUser = maxPurchasePerUser;
        }
        
        public BigDecimal getDailyPurchaseLimit() {
            return dailyPurchaseLimit;
        }
        
        public void setDailyPurchaseLimit(BigDecimal dailyPurchaseLimit) {
            this.dailyPurchaseLimit = dailyPurchaseLimit;
        }
        
        public String getPurchaseStartDate() {
            return purchaseStartDate;
        }
        
        public void setPurchaseStartDate(String purchaseStartDate) {
            this.purchaseStartDate = purchaseStartDate;
        }
    }
    
    public static class SfrToggleRequest {
        @NotNull(message = "スペースIDは必須です")
        private Long spaceId;
        
        @NotNull(message = "有効/無効フラグは必須です")
        private Boolean enabled;
        
        // Getters and Setters
        public Long getSpaceId() {
            return spaceId;
        }
        
        public void setSpaceId(Long spaceId) {
            this.spaceId = spaceId;
        }
        
        public Boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class SfrToggleResponse {
        private boolean success;
        private String message;
        private Boolean isEnabled;
        
        private SfrToggleResponse(boolean success, String message, Boolean isEnabled) {
            this.success = success;
            this.message = message;
            this.isEnabled = isEnabled;
        }
        
        public static SfrToggleResponse success(Boolean isEnabled, String message) {
            return new SfrToggleResponse(true, message, isEnabled);
        }
        
        public static SfrToggleResponse failure(String errorMessage) {
            return new SfrToggleResponse(false, errorMessage, null);
        }
        
        // Getters
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Boolean getIsEnabled() {
            return isEnabled;
        }
    }
    
    public static class SfrAdminConfigRequest {
        @NotNull(message = "スペースIDは必須です")
        private Long spaceId;
        
        @NotNull(message = "有効/無効フラグは必須です")
        private Boolean enabled;
        
        @DecimalMin(value = "150.00", message = "固定レートは150以上である必要があります")
        private BigDecimal fixedRate;
        
        @DecimalMin(value = "0", message = "最大購入額は0以上である必要があります")
        private BigDecimal maxPurchasePerUser;
        
        @DecimalMin(value = "0", message = "日次購入限度額は0以上である必要があります")
        private BigDecimal dailyPurchaseLimit;
        
        private String purchaseStartDate;
        
        // Getters and Setters
        public Long getSpaceId() {
            return spaceId;
        }
        
        public void setSpaceId(Long spaceId) {
            this.spaceId = spaceId;
        }
        
        public Boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
        
        public BigDecimal getFixedRate() {
            return fixedRate;
        }
        
        public void setFixedRate(BigDecimal fixedRate) {
            this.fixedRate = fixedRate;
        }
        
        public BigDecimal getMaxPurchasePerUser() {
            return maxPurchasePerUser;
        }
        
        public void setMaxPurchasePerUser(BigDecimal maxPurchasePerUser) {
            this.maxPurchasePerUser = maxPurchasePerUser;
        }
        
        public BigDecimal getDailyPurchaseLimit() {
            return dailyPurchaseLimit;
        }
        
        public void setDailyPurchaseLimit(BigDecimal dailyPurchaseLimit) {
            this.dailyPurchaseLimit = dailyPurchaseLimit;
        }
        
        public String getPurchaseStartDate() {
            return purchaseStartDate;
        }
        
        public void setPurchaseStartDate(String purchaseStartDate) {
            this.purchaseStartDate = purchaseStartDate;
        }
    }
    
    public static class SfrConfigUpdateResponse {
        private boolean success;
        private String message;
        
        private SfrConfigUpdateResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static SfrConfigUpdateResponse success(String message) {
            return new SfrConfigUpdateResponse(true, message);
        }
        
        public static SfrConfigUpdateResponse failure(String errorMessage) {
            return new SfrConfigUpdateResponse(false, errorMessage);
        }
        
        // Getters
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
