package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 取引所メトリクスDTO
 * Phase 4: 取引所API統合対応
 */
public class ExchangeMetrics {
    
    private ExchangeType exchangeType;
    private boolean available;
    private BigDecimal currentPrice;
    private BigDecimal liquidity;
    private BigDecimal spread;
    private BigDecimal volume24h;
    private TradingLimits tradingLimits;
    private ComplianceStatus complianceStatus;
    private LocalDateTime lastUpdated;
    private BigDecimal responseTimeMs;
    private BigDecimal feeRate;
    
    public ExchangeMetrics() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    public ExchangeMetrics(ExchangeType exchangeType) {
        this();
        this.exchangeType = exchangeType;
    }
    
    // 静的ファクトリーメソッド
    public static ExchangeMetrics unavailable(ExchangeType exchangeType) {
        ExchangeMetrics metrics = new ExchangeMetrics(exchangeType);
        metrics.available = false;
        metrics.currentPrice = BigDecimal.ZERO;
        metrics.liquidity = BigDecimal.ZERO;
        return metrics;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public ExchangeType getExchangeType() {
        return exchangeType;
    }
    
    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public BigDecimal getLiquidity() {
        return liquidity;
    }
    
    public void setLiquidity(BigDecimal liquidity) {
        this.liquidity = liquidity;
    }
    
    public BigDecimal getSpread() {
        return spread;
    }
    
    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }
    
    public BigDecimal getVolume24h() {
        return volume24h;
    }
    
    public void setVolume24h(BigDecimal volume24h) {
        this.volume24h = volume24h;
    }
    
    public TradingLimits getTradingLimits() {
        return tradingLimits;
    }
    
    public void setTradingLimits(TradingLimits tradingLimits) {
        this.tradingLimits = tradingLimits;
    }
    
    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }
    
    public void setComplianceStatus(ComplianceStatus complianceStatus) {
        this.complianceStatus = complianceStatus;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public BigDecimal getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(BigDecimal responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public BigDecimal getFeeRate() {
        return feeRate;
    }
    
    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }
    
    /**
     * 取引可能かどうか判定
     */
    public boolean isTradingAvailable() {
        return available && 
               complianceStatus != null && complianceStatus.isTradingEnabled() &&
               currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 品質スコアを計算（0-100）
     */
    public BigDecimal calculateQualityScore() {
        if (!available) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal score = new BigDecimal("50"); // ベーススコア
        
        // 流動性スコア（30点満点）
        if (liquidity != null && liquidity.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal liquidityScore = liquidity.divide(new BigDecimal("10000000")) // 1000万円で満点
                .min(new BigDecimal("30"));
            score = score.add(liquidityScore);
        }
        
        // レスポンス時間スコア（20点満点）
        if (responseTimeMs != null) {
            if (responseTimeMs.compareTo(new BigDecimal("100")) <= 0) {
                score = score.add(new BigDecimal("20"));
            } else if (responseTimeMs.compareTo(new BigDecimal("500")) <= 0) {
                score = score.add(new BigDecimal("10"));
            }
        }
        
        return score.min(new BigDecimal("100"));
    }
    
    @Override
    public String toString() {
        return String.format("ExchangeMetrics{exchange=%s, available=%s, price=%s, " +
                           "liquidity=%s, spread=%s, qualityScore=%s}",
                           exchangeType, available, currentPrice, liquidity, spread, 
                           calculateQualityScore());
    }
    
    /**
     * ビルダークラス
     */
    public static class Builder {
        private ExchangeMetrics metrics = new ExchangeMetrics();
        
        public Builder exchangeType(ExchangeType exchangeType) {
            metrics.exchangeType = exchangeType;
            return this;
        }
        
        public Builder available(boolean available) {
            metrics.available = available;
            return this;
        }
        
        public Builder currentPrice(BigDecimal currentPrice) {
            metrics.currentPrice = currentPrice;
            return this;
        }
        
        public Builder liquidity(BigDecimal liquidity) {
            metrics.liquidity = liquidity;
            return this;
        }
        
        public Builder spread(BigDecimal spread) {
            metrics.spread = spread;
            return this;
        }
        
        public Builder volume24h(BigDecimal volume24h) {
            metrics.volume24h = volume24h;
            return this;
        }
        
        public Builder tradingLimits(TradingLimits tradingLimits) {
            metrics.tradingLimits = tradingLimits;
            return this;
        }
        
        public Builder complianceStatus(ComplianceStatus complianceStatus) {
            metrics.complianceStatus = complianceStatus;
            return this;
        }
        
        public Builder lastUpdated(LocalDateTime lastUpdated) {
            metrics.lastUpdated = lastUpdated;
            return this;
        }
        
        public Builder responseTimeMs(BigDecimal responseTimeMs) {
            metrics.responseTimeMs = responseTimeMs;
            return this;
        }
        
        public Builder feeRate(BigDecimal feeRate) {
            metrics.feeRate = feeRate;
            return this;
        }
        
        public ExchangeMetrics build() {
            return metrics;
        }
    }
}
