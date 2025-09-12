package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;

/**
 * 取引制限情報DTO
 * Phase 4: 取引所API統合対応
 */
public class TradingLimits {
    
    private BigDecimal minOrderAmount;
    private BigDecimal maxOrderAmount;
    private BigDecimal dailyTradingLimit;
    private BigDecimal monthlyTradingLimit;
    private BigDecimal dailyWithdrawalLimit;
    private BigDecimal monthlyWithdrawalLimit;
    private int maxActiveOrders;
    private ExchangeType exchange;
    
    public TradingLimits() {}
    
    public TradingLimits(ExchangeType exchange) {
        this.exchange = exchange;
        setDefaultLimits();
    }
    
    /**
     * デフォルト制限値を設定
     */
    private void setDefaultLimits() {
        this.minOrderAmount = new BigDecimal("100"); // 最小100円
        this.maxOrderAmount = new BigDecimal("10000000"); // 最大1000万円
        this.dailyTradingLimit = new BigDecimal("50000000"); // 日次5000万円
        this.monthlyTradingLimit = new BigDecimal("1000000000"); // 月次10億円
        this.dailyWithdrawalLimit = new BigDecimal("10000000"); // 日次出金1000万円
        this.monthlyWithdrawalLimit = new BigDecimal("300000000"); // 月次出金3億円
        this.maxActiveOrders = 100; // 最大同時注文数
    }
    
    // Getters and Setters
    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }
    
    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }
    
    public BigDecimal getMaxOrderAmount() {
        return maxOrderAmount;
    }
    
    public void setMaxOrderAmount(BigDecimal maxOrderAmount) {
        this.maxOrderAmount = maxOrderAmount;
    }
    
    public BigDecimal getDailyTradingLimit() {
        return dailyTradingLimit;
    }
    
    public void setDailyTradingLimit(BigDecimal dailyTradingLimit) {
        this.dailyTradingLimit = dailyTradingLimit;
    }
    
    public BigDecimal getMonthlyTradingLimit() {
        return monthlyTradingLimit;
    }
    
    public void setMonthlyTradingLimit(BigDecimal monthlyTradingLimit) {
        this.monthlyTradingLimit = monthlyTradingLimit;
    }
    
    public BigDecimal getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }
    
    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) {
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }
    
    public BigDecimal getMonthlyWithdrawalLimit() {
        return monthlyWithdrawalLimit;
    }
    
    public void setMonthlyWithdrawalLimit(BigDecimal monthlyWithdrawalLimit) {
        this.monthlyWithdrawalLimit = monthlyWithdrawalLimit;
    }
    
    public int getMaxActiveOrders() {
        return maxActiveOrders;
    }
    
    public void setMaxActiveOrders(int maxActiveOrders) {
        this.maxActiveOrders = maxActiveOrders;
    }
    
    public ExchangeType getExchange() {
        return exchange;
    }
    
    public void setExchange(ExchangeType exchange) {
        this.exchange = exchange;
    }
    
    /**
     * 注文金額が制限内かチェック
     */
    public boolean isOrderAmountValid(BigDecimal amount) {
        return amount.compareTo(minOrderAmount) >= 0 && amount.compareTo(maxOrderAmount) <= 0;
    }
    
    /**
     * 日次取引制限チェック
     */
    public boolean isDailyTradingLimitExceeded(BigDecimal currentDailyVolume, BigDecimal newTradeAmount) {
        return currentDailyVolume.add(newTradeAmount).compareTo(dailyTradingLimit) > 0;
    }
    
    /**
     * 月次取引制限チェック
     */
    public boolean isMonthlyTradingLimitExceeded(BigDecimal currentMonthlyVolume, BigDecimal newTradeAmount) {
        return currentMonthlyVolume.add(newTradeAmount).compareTo(monthlyTradingLimit) > 0;
    }
    
    @Override
    public String toString() {
        return String.format("TradingLimits{minOrder=%s, maxOrder=%s, dailyLimit=%s, " +
                           "monthlyLimit=%s, maxActiveOrders=%d, exchange=%s}",
                           minOrderAmount, maxOrderAmount, dailyTradingLimit, 
                           monthlyTradingLimit, maxActiveOrders, exchange);
    }
}
