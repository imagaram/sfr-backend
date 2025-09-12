package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 取引所残高情報DTO
 * Phase 4: 取引所API統合対応
 */
public class ExchangeBalance {
    
    private String symbol;
    private BigDecimal available;
    private BigDecimal locked;
    private BigDecimal total;
    private ExchangeType exchange;
    private LocalDateTime lastUpdated;
    
    public ExchangeBalance() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    public ExchangeBalance(String symbol, BigDecimal available, BigDecimal locked, ExchangeType exchange) {
        this();
        this.symbol = symbol;
        this.available = available;
        this.locked = locked;
        this.total = available.add(locked);
        this.exchange = exchange;
    }
    
    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public BigDecimal getAvailable() {
        return available;
    }
    
    public void setAvailable(BigDecimal available) {
        this.available = available;
        updateTotal();
    }
    
    public BigDecimal getLocked() {
        return locked;
    }
    
    public void setLocked(BigDecimal locked) {
        this.locked = locked;
        updateTotal();
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public ExchangeType getExchange() {
        return exchange;
    }
    
    public void setExchange(ExchangeType exchange) {
        this.exchange = exchange;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * 合計残高を再計算
     */
    private void updateTotal() {
        if (available != null && locked != null) {
            this.total = available.add(locked);
        }
    }
    
    /**
     * 指定金額が利用可能かチェック
     */
    public boolean isAvailable(BigDecimal amount) {
        return available != null && available.compareTo(amount) >= 0;
    }
    
    /**
     * 残高が空かどうか判定
     */
    public boolean isEmpty() {
        return total == null || total.compareTo(BigDecimal.ZERO) == 0;
    }
    
    @Override
    public String toString() {
        return String.format("ExchangeBalance{symbol='%s', available=%s, locked=%s, total=%s, exchange=%s}",
                           symbol, available, locked, total, exchange);
    }
}
