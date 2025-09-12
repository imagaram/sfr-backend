package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 取引所取引履歴DTO
 * Phase 4: 取引所API統合対応
 */
public class ExchangeTrade {
    
    private String tradeId;
    private String orderId;
    private String symbol;
    private OrderSide side;
    private BigDecimal amount;
    private BigDecimal price;
    private BigDecimal fee;
    private String feeCurrency;
    private LocalDateTime timestamp;
    private ExchangeType exchange;
    private String rawData; // デバッグ用原始データ
    
    public ExchangeTrade() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ExchangeTrade(String tradeId, String orderId, String symbol, OrderSide side,
                        BigDecimal amount, BigDecimal price, ExchangeType exchange) {
        this();
        this.tradeId = tradeId;
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.amount = amount;
        this.price = price;
        this.exchange = exchange;
    }
    
    // Getters and Setters
    public String getTradeId() {
        return tradeId;
    }
    
    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public OrderSide getSide() {
        return side;
    }
    
    public void setSide(OrderSide side) {
        this.side = side;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getFee() {
        return fee;
    }
    
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
    
    public String getFeeCurrency() {
        return feeCurrency;
    }
    
    public void setFeeCurrency(String feeCurrency) {
        this.feeCurrency = feeCurrency;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public ExchangeType getExchange() {
        return exchange;
    }
    
    public void setExchange(ExchangeType exchange) {
        this.exchange = exchange;
    }
    
    public String getRawData() {
        return rawData;
    }
    
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
    
    /**
     * 取引総額を計算
     */
    public BigDecimal getTotalValue() {
        return amount.multiply(price);
    }
    
    /**
     * 手数料込みの実質取引額を計算
     */
    public BigDecimal getNetValue() {
        BigDecimal totalValue = getTotalValue();
        if (fee != null) {
            return side == OrderSide.BUY ? totalValue.add(fee) : totalValue.subtract(fee);
        }
        return totalValue;
    }
    
    @Override
    public String toString() {
        return String.format("ExchangeTrade{tradeId='%s', orderId='%s', symbol='%s', side=%s, " +
                           "amount=%s, price=%s, fee=%s, timestamp=%s, exchange=%s}",
                           tradeId, orderId, symbol, side, amount, price, fee, timestamp, exchange);
    }
}
