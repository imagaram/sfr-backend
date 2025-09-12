package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 流動性判断DTO
 * Phase 4: 取引所API統合対応
 */
public class LiquidityDecision {
    
    private LiquidityAction action;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime timestamp;
    private int priority;
    
    public LiquidityDecision() {
        this.timestamp = LocalDateTime.now();
        this.priority = 5;
    }
    
    public LiquidityDecision(LiquidityAction action, BigDecimal amount, String reason) {
        this();
        this.action = action;
        this.amount = amount;
        this.reason = reason;
    }
    
    // Getters and Setters
    public LiquidityAction getAction() {
        return action;
    }
    
    public void setAction(LiquidityAction action) {
        this.action = action;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    @Override
    public String toString() {
        return String.format("LiquidityDecision{action=%s, amount=%s, reason='%s', priority=%d}",
                           action, amount, reason, priority);
    }
}

/**
 * 流動性アクション列挙型
 */
enum LiquidityAction {
    STABILIZE_PRICE_SELL("価格安定化売却"),
    STABILIZE_PRICE_BUY("価格安定化買取"),
    PROVIDE_LIQUIDITY("流動性提供"),
    MAINTAIN_RESERVE("準備金維持");
    
    private final String displayName;
    
    LiquidityAction(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
