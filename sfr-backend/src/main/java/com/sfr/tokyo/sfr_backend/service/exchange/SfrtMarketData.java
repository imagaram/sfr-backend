package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;

/**
 * SFRT市場データDTO
 * Phase 4: 取引所API統合対応
 */
public class SfrtMarketData {
    
    private BigDecimal averagePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal priceVolatility;
    private int activeExchanges;
    private long timestamp;
    
    public SfrtMarketData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public SfrtMarketData(BigDecimal averagePrice, BigDecimal minPrice, BigDecimal maxPrice, 
                         BigDecimal priceVolatility, int activeExchanges) {
        this();
        this.averagePrice = averagePrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.priceVolatility = priceVolatility;
        this.activeExchanges = activeExchanges;
    }
    
    /**
     * 市場データが有効かチェック
     */
    public boolean isValid() {
        return averagePrice != null && 
               averagePrice.compareTo(BigDecimal.ZERO) > 0 &&
               activeExchanges > 0;
    }
    
    // Getters and Setters
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
    
    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }
    
    public BigDecimal getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }
    
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public BigDecimal getPriceVolatility() {
        return priceVolatility;
    }
    
    public void setPriceVolatility(BigDecimal priceVolatility) {
        this.priceVolatility = priceVolatility;
    }
    
    public int getActiveExchanges() {
        return activeExchanges;
    }
    
    public void setActiveExchanges(int activeExchanges) {
        this.activeExchanges = activeExchanges;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 価格スプレッドを計算
     */
    public BigDecimal getPriceSpread() {
        if (maxPrice != null && minPrice != null) {
            return maxPrice.subtract(minPrice);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 高い流動性かどうか判定
     */
    public boolean hasHighLiquidity() {
        return activeExchanges >= 3 && 
               priceVolatility != null && 
               priceVolatility.compareTo(new BigDecimal("0.02")) <= 0; // 2%以下の変動
    }
    
    @Override
    public String toString() {
        return String.format("SfrtMarketData{avgPrice=%s, volatility=%s%%, activeExchanges=%d, spread=%s}",
                           averagePrice, 
                           priceVolatility != null ? priceVolatility.multiply(new BigDecimal("100")) : "N/A",
                           activeExchanges, 
                           getPriceSpread());
    }
}
