package com.sfr.tokyo.sfr_backend.service.exchange;

import java.time.LocalDateTime;

/**
 * コンプライアンス状況DTO
 * Phase 4: 取引所API統合対応
 */
public class ComplianceStatus {
    
    private boolean kycVerified;
    private boolean amlCleared;
    private boolean tradingEnabled;
    private boolean withdrawalEnabled;
    private ComplianceLevel level;
    private String restrictions;
    private LocalDateTime lastVerification;
    private LocalDateTime nextReview;
    private ExchangeType exchange;
    
    public ComplianceStatus() {
        this.lastVerification = LocalDateTime.now();
    }
    
    public ComplianceStatus(ExchangeType exchange) {
        this();
        this.exchange = exchange;
        setDefaultStatus();
    }
    
    /**
     * デフォルトコンプライアンス状況を設定
     */
    private void setDefaultStatus() {
        this.kycVerified = true;
        this.amlCleared = true;
        this.tradingEnabled = true;
        this.withdrawalEnabled = true;
        this.level = ComplianceLevel.STANDARD;
        this.restrictions = null;
        this.nextReview = LocalDateTime.now().plusMonths(6);
    }
    
    // Getters and Setters
    public boolean isKycVerified() {
        return kycVerified;
    }
    
    public void setKycVerified(boolean kycVerified) {
        this.kycVerified = kycVerified;
    }
    
    public boolean isAmlCleared() {
        return amlCleared;
    }
    
    public void setAmlCleared(boolean amlCleared) {
        this.amlCleared = amlCleared;
    }
    
    public boolean isTradingEnabled() {
        return tradingEnabled;
    }
    
    public void setTradingEnabled(boolean tradingEnabled) {
        this.tradingEnabled = tradingEnabled;
    }
    
    public boolean isWithdrawalEnabled() {
        return withdrawalEnabled;
    }
    
    public void setWithdrawalEnabled(boolean withdrawalEnabled) {
        this.withdrawalEnabled = withdrawalEnabled;
    }
    
    public ComplianceLevel getLevel() {
        return level;
    }
    
    public void setLevel(ComplianceLevel level) {
        this.level = level;
    }
    
    public String getRestrictions() {
        return restrictions;
    }
    
    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }
    
    public LocalDateTime getLastVerification() {
        return lastVerification;
    }
    
    public void setLastVerification(LocalDateTime lastVerification) {
        this.lastVerification = lastVerification;
    }
    
    public LocalDateTime getNextReview() {
        return nextReview;
    }
    
    public void setNextReview(LocalDateTime nextReview) {
        this.nextReview = nextReview;
    }
    
    public ExchangeType getExchange() {
        return exchange;
    }
    
    public void setExchange(ExchangeType exchange) {
        this.exchange = exchange;
    }
    
    /**
     * 完全に利用可能かチェック
     */
    public boolean isFullyEnabled() {
        return kycVerified && amlCleared && tradingEnabled && withdrawalEnabled;
    }
    
    /**
     * 制限があるかチェック
     */
    public boolean hasRestrictions() {
        return restrictions != null && !restrictions.trim().isEmpty();
    }
    
    /**
     * レビューが必要かチェック
     */
    public boolean isReviewRequired() {
        return nextReview != null && LocalDateTime.now().isAfter(nextReview);
    }
    
    /**
     * コンプライアンスレベル列挙型
     */
    public enum ComplianceLevel {
        BASIC("基本", "basic"),
        STANDARD("標準", "standard"),
        PREMIUM("プレミアム", "premium"),
        INSTITUTIONAL("機関投資家", "institutional"),
        RESTRICTED("制限付き", "restricted"),
        SUSPENDED("停止中", "suspended");
        
        private final String displayName;
        private final String code;
        
        ComplianceLevel(String displayName, String code) {
            this.displayName = displayName;
            this.code = code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public boolean isActive() {
            return this != SUSPENDED;
        }
        
        public boolean isRestricted() {
            return this == RESTRICTED || this == SUSPENDED;
        }
    }
    
    @Override
    public String toString() {
        return String.format("ComplianceStatus{kyc=%s, aml=%s, trading=%s, withdrawal=%s, " +
                           "level=%s, restrictions='%s', exchange=%s}",
                           kycVerified, amlCleared, tradingEnabled, withdrawalEnabled,
                           level, restrictions, exchange);
    }
}
