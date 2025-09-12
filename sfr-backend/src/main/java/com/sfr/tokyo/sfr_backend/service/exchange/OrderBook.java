package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;
import java.util.List;

/**
 * オーダーブック情報DTO
 * Phase 4: 取引所API統合対応
 */
public class OrderBook {
    
    private String symbol;
    private List<OrderBookEntry> bids; // 買い注文（高い順）
    private List<OrderBookEntry> asks; // 売り注文（安い順）
    private ExchangeType exchange;
    private long timestamp;
    
    public OrderBook() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public OrderBook(String symbol, List<OrderBookEntry> bids, List<OrderBookEntry> asks, ExchangeType exchange) {
        this();
        this.symbol = symbol;
        this.bids = bids;
        this.asks = asks;
        this.exchange = exchange;
    }
    
    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public List<OrderBookEntry> getBids() {
        return bids;
    }
    
    public void setBids(List<OrderBookEntry> bids) {
        this.bids = bids;
    }
    
    public List<OrderBookEntry> getAsks() {
        return asks;
    }
    
    public void setAsks(List<OrderBookEntry> asks) {
        this.asks = asks;
    }
    
    public ExchangeType getExchange() {
        return exchange;
    }
    
    public void setExchange(ExchangeType exchange) {
        this.exchange = exchange;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 最高買い価格を取得
     */
    public BigDecimal getBestBidPrice() {
        if (bids == null || bids.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return bids.get(0).getPrice();
    }
    
    /**
     * 最安売り価格を取得
     */
    public BigDecimal getBestAskPrice() {
        if (asks == null || asks.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return asks.get(0).getPrice();
    }
    
    /**
     * スプレッドを計算
     */
    public BigDecimal getSpread() {
        BigDecimal bestBid = getBestBidPrice();
        BigDecimal bestAsk = getBestAskPrice();
        
        if (bestBid.compareTo(BigDecimal.ZERO) > 0 && bestAsk.compareTo(BigDecimal.ZERO) > 0) {
            return bestAsk.subtract(bestBid);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 中値を計算
     */
    public BigDecimal getMidPrice() {
        BigDecimal bestBid = getBestBidPrice();
        BigDecimal bestAsk = getBestAskPrice();
        
        if (bestBid.compareTo(BigDecimal.ZERO) > 0 && bestAsk.compareTo(BigDecimal.ZERO) > 0) {
            return bestBid.add(bestAsk).divide(BigDecimal.valueOf(2));
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * オーダーブックエントリ（価格・数量ペア）
     */
    public static class OrderBookEntry {
        private BigDecimal price;
        private BigDecimal amount;
        
        public OrderBookEntry() {}
        
        public OrderBookEntry(BigDecimal price, BigDecimal amount) {
            this.price = price;
            this.amount = amount;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
        
        /**
         * 総額を計算
         */
        public BigDecimal getTotalValue() {
            return price.multiply(amount);
        }
        
        @Override
        public String toString() {
            return String.format("OrderBookEntry{price=%s, amount=%s}", price, amount);
        }
    }
    
    @Override
    public String toString() {
        return String.format("OrderBook{symbol='%s', bestBid=%s, bestAsk=%s, spread=%s, exchange=%s}",
                           symbol, getBestBidPrice(), getBestAskPrice(), getSpread(), exchange);
    }
}
