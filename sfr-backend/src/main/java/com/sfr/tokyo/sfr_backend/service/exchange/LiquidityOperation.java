package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 流動性操作DTO
 * Phase 4: 取引所API統合対応
 */
public class LiquidityOperation {
    
    private LiquidityOperationType type;
    private String symbol;
    private BigDecimal amount;
    private BigDecimal price;
    private String reason;
    private LocalDateTime timestamp;
    private int priority; // 1-10 (10が最優先)
    
    public LiquidityOperation() {
        this.timestamp = LocalDateTime.now();
        this.priority = 5; // デフォルト優先度
    }
    
    public LiquidityOperation(LiquidityOperationType type, String symbol, BigDecimal amount) {
        this();
        this.type = type;
        this.symbol = symbol;
        this.amount = amount;
    }
    
    // 静的ファクトリーメソッド
    public static LiquidityOperation marketSell(String symbol, BigDecimal amount, String reason) {
        LiquidityOperation op = new LiquidityOperation(LiquidityOperationType.MARKET_SELL, symbol, amount);
        op.reason = reason;
        return op;
    }
    
    public static LiquidityOperation marketBuy(String symbol, BigDecimal amount, String reason) {
        LiquidityOperation op = new LiquidityOperation(LiquidityOperationType.MARKET_BUY, symbol, amount);
        op.reason = reason;
        return op;
    }
    
    public static LiquidityOperation limitSell(String symbol, BigDecimal amount, BigDecimal price, String reason) {
        LiquidityOperation op = new LiquidityOperation(LiquidityOperationType.LIMIT_SELL, symbol, amount);
        op.price = price;
        op.reason = reason;
        return op;
    }
    
    public static LiquidityOperation limitBuy(String symbol, BigDecimal amount, BigDecimal price, String reason) {
        LiquidityOperation op = new LiquidityOperation(LiquidityOperationType.LIMIT_BUY, symbol, amount);
        op.price = price;
        op.reason = reason;
        return op;
    }
    
    // Getters and Setters
    public LiquidityOperationType getType() {
        return type;
    }
    
    public void setType(LiquidityOperationType type) {
        this.type = type;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
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
        this.priority = Math.max(1, Math.min(10, priority)); // 1-10の範囲に制限
    }
    
    /**
     * 市場注文かどうか判定
     */
    public boolean isMarketOrder() {
        return type == LiquidityOperationType.MARKET_BUY || type == LiquidityOperationType.MARKET_SELL;
    }
    
    /**
     * 指値注文かどうか判定
     */
    public boolean isLimitOrder() {
        return type == LiquidityOperationType.LIMIT_BUY || type == LiquidityOperationType.LIMIT_SELL;
    }
    
    /**
     * 買い操作かどうか判定
     */
    public boolean isBuyOperation() {
        return type == LiquidityOperationType.MARKET_BUY || type == LiquidityOperationType.LIMIT_BUY;
    }
    
    /**
     * 売り操作かどうか判定
     */
    public boolean isSellOperation() {
        return type == LiquidityOperationType.MARKET_SELL || type == LiquidityOperationType.LIMIT_SELL;
    }
    
    /**
     * 操作の価値を計算（市場注文の場合は概算）
     */
    public BigDecimal getEstimatedValue(BigDecimal marketPrice) {
        BigDecimal operationPrice = isLimitOrder() ? price : marketPrice;
        return amount.multiply(operationPrice);
    }
    
    @Override
    public String toString() {
        return String.format("LiquidityOperation{type=%s, symbol='%s', amount=%s, price=%s, " +
                           "reason='%s', priority=%d, timestamp=%s}",
                           type, symbol, amount, price, reason, priority, timestamp);
    }
    
    /**
     * 流動性操作タイプ列挙型
     */
    public enum LiquidityOperationType {
        MARKET_BUY("成行買い", "market_buy"),
        MARKET_SELL("成行売り", "market_sell"),
        LIMIT_BUY("指値買い", "limit_buy"),
        LIMIT_SELL("指値売り", "limit_sell");
        
        private final String displayName;
        private final String code;
        
        LiquidityOperationType(String displayName, String code) {
            this.displayName = displayName;
            this.code = code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public boolean isMarket() {
            return this == MARKET_BUY || this == MARKET_SELL;
        }
        
        public boolean isLimit() {
            return this == LIMIT_BUY || this == LIMIT_SELL;
        }
        
        public boolean isBuy() {
            return this == MARKET_BUY || this == LIMIT_BUY;
        }
        
        public boolean isSell() {
            return this == MARKET_SELL || this == LIMIT_SELL;
        }
    }
}
