package com.sfr.tokyo.sfr_backend.service.exchange;

import java.time.LocalDateTime;

/**
 * 流動性操作結果DTO
 * Phase 4: 取引所API統合対応
 */
public class LiquidityResult {
    
    private boolean success;
    private OrderResult orderResult;
    private LiquidityOperation operation;
    private String errorMessage;
    private LocalDateTime timestamp;
    private ExchangeType executedExchange;
    private long executionTimeMs;
    
    public LiquidityResult() {
        this.timestamp = LocalDateTime.now();
    }
    
    public LiquidityResult(boolean success, LiquidityOperation operation) {
        this();
        this.success = success;
        this.operation = operation;
    }
    
    // 静的ファクトリーメソッド
    public static LiquidityResult success(OrderResult orderResult, LiquidityOperation operation) {
        LiquidityResult result = new LiquidityResult(true, operation);
        result.orderResult = orderResult;
        result.executedExchange = orderResult.getExchange();
        return result;
    }
    
    public static LiquidityResult failure(String errorMessage, LiquidityOperation operation) {
        LiquidityResult result = new LiquidityResult(false, operation);
        result.errorMessage = errorMessage;
        return result;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public OrderResult getOrderResult() {
        return orderResult;
    }
    
    public void setOrderResult(OrderResult orderResult) {
        this.orderResult = orderResult;
    }
    
    public LiquidityOperation getOperation() {
        return operation;
    }
    
    public void setOperation(LiquidityOperation operation) {
        this.operation = operation;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public ExchangeType getExecutedExchange() {
        return executedExchange;
    }
    
    public void setExecutedExchange(ExchangeType executedExchange) {
        this.executedExchange = executedExchange;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    /**
     * 成功かつ約定済みかどうか判定
     */
    public boolean isExecuted() {
        return success && orderResult != null && orderResult.isExecuted();
    }
    
    /**
     * 完全約定かどうか判定
     */
    public boolean isFullyExecuted() {
        return success && orderResult != null && orderResult.isFullyExecuted();
    }
    
    /**
     * 部分約定かどうか判定
     */
    public boolean isPartiallyExecuted() {
        return success && orderResult != null && orderResult.isPartiallyExecuted();
    }
    
    @Override
    public String toString() {
        return String.format("LiquidityResult{success=%s, executedExchange=%s, operation=%s, " +
                           "orderResult=%s, errorMessage='%s', executionTimeMs=%d}",
                           success, executedExchange, operation, orderResult, errorMessage, executionTimeMs);
    }
}
