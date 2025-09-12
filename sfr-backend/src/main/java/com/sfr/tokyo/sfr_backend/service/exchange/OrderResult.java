package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 取引所注文結果DTO
 * Phase 4: 取引所API統合対応
 */
public class OrderResult {
    
    private boolean success;
    private String orderId;
    private String symbol;
    private OrderSide side;
    private BigDecimal amount;
    private BigDecimal price;
    private BigDecimal executedAmount;
    private BigDecimal executedPrice;
    private OrderStatus status;
    private LocalDateTime timestamp;
    private String errorMessage;
    private ExchangeType exchange;
    
    // コンストラクタ
    public OrderResult() {
        this.timestamp = LocalDateTime.now();
    }
    
    public OrderResult(boolean success, String orderId) {
        this();
        this.success = success;
        this.orderId = orderId;
    }
    
    // 成功結果作成用静的メソッド
    public static OrderResult success(String orderId, String symbol, OrderSide side, 
                                    BigDecimal amount, BigDecimal price, ExchangeType exchange) {
        OrderResult result = new OrderResult(true, orderId);
        result.symbol = symbol;
        result.side = side;
        result.amount = amount;
        result.price = price;
        result.exchange = exchange;
        result.status = OrderStatus.PENDING;
        return result;
    }
    
    // 失敗結果作成用静的メソッド
    public static OrderResult failure(String errorMessage, ExchangeType exchange) {
        OrderResult result = new OrderResult(false, null);
        result.errorMessage = errorMessage;
        result.exchange = exchange;
        result.status = OrderStatus.FAILED;
        return result;
    }
    
    // 部分約定結果作成用
    public static OrderResult partialExecution(String orderId, String symbol, OrderSide side,
                                             BigDecimal totalAmount, BigDecimal executedAmount,
                                             BigDecimal executedPrice, ExchangeType exchange) {
        OrderResult result = success(orderId, symbol, side, totalAmount, executedPrice, exchange);
        result.executedAmount = executedAmount;
        result.executedPrice = executedPrice;
        result.status = OrderStatus.PARTIALLY_FILLED;
        return result;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
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
    
    public BigDecimal getExecutedAmount() {
        return executedAmount;
    }
    
    public void setExecutedAmount(BigDecimal executedAmount) {
        this.executedAmount = executedAmount;
    }
    
    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }
    
    public void setExecutedPrice(BigDecimal executedPrice) {
        this.executedPrice = executedPrice;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public ExchangeType getExchange() {
        return exchange;
    }
    
    public void setExchange(ExchangeType exchange) {
        this.exchange = exchange;
    }
    
    /**
     * 約定済みかどうか判定
     */
    public boolean isExecuted() {
        return success && (status == OrderStatus.FILLED || status == OrderStatus.PARTIALLY_FILLED);
    }
    
    /**
     * 完全約定かどうか判定
     */
    public boolean isFullyExecuted() {
        return success && status == OrderStatus.FILLED;
    }
    
    /**
     * 部分約定かどうか判定
     */
    public boolean isPartiallyExecuted() {
        return success && status == OrderStatus.PARTIALLY_FILLED;
    }
    
    @Override
    public String toString() {
        return String.format("OrderResult{success=%s, orderId='%s', symbol='%s', side=%s, " +
                           "amount=%s, price=%s, executedAmount=%s, status=%s, exchange=%s}",
                           success, orderId, symbol, side, amount, price, executedAmount, status, exchange);
    }
}
