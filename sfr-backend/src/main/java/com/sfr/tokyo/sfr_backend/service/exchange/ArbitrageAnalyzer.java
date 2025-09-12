package com.sfr.tokyo.sfr_backend.service.exchange;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 裁定機会分析サービス
 * Phase 4: 取引所API統合対応
 */
@Component
public class ArbitrageAnalyzer {
    
    private static final BigDecimal MIN_PROFIT_THRESHOLD = new BigDecimal("0.01"); // 最小利益1%
    private static final BigDecimal MAX_SPREAD_THRESHOLD = new BigDecimal("0.10"); // 最大スプレッド10%
    
    /**
     * 裁定機会を分析
     * @param prices 取引所別価格マップ
     * @return 裁定機会（なければnull）
     */
    public ArbitrageOpportunity findOpportunity(Map<ExchangeType, BigDecimal> prices) {
        
        if (prices.size() < 2) {
            return null;
        }
        
        ExchangeType buyExchange = null;
        ExchangeType sellExchange = null;
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        
        // 最低価格と最高価格の取引所を特定
        for (Map.Entry<ExchangeType, BigDecimal> entry : prices.entrySet()) {
            BigDecimal price = entry.getValue();
            
            if (minPrice == null || price.compareTo(minPrice) < 0) {
                minPrice = price;
                buyExchange = entry.getKey();
            }
            
            if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                maxPrice = price;
                sellExchange = entry.getKey();
            }
        }
        
        // 同一取引所の場合は機会なし
        if (buyExchange == sellExchange || minPrice == null || maxPrice == null) {
            return null;
        }
        
        // 利益率計算
        BigDecimal spread = maxPrice.subtract(minPrice);
        BigDecimal profitRate = spread.divide(minPrice, 4, java.math.RoundingMode.HALF_UP);
        
        // 利益率が閾値を超える場合のみ機会とする
        if (profitRate.compareTo(MIN_PROFIT_THRESHOLD) >= 0 && 
            profitRate.compareTo(MAX_SPREAD_THRESHOLD) <= 0) {
            
            return new ArbitrageOpportunity(
                "SFRT/JPY",
                buyExchange,
                sellExchange,
                minPrice,
                maxPrice,
                calculateOptimalAmount(spread, minPrice),
                profitRate
            );
        }
        
        return null;
    }
    
    /**
     * 最適取引量を計算
     */
    private BigDecimal calculateOptimalAmount(BigDecimal spread, BigDecimal price) {
        // リスクを考慮して保守的な取引量を設定
        // 実際の実装では流動性、残高、リスク許容度を考慮
        return new BigDecimal("100000"); // 固定で10万円分
    }
    
    /**
     * 裁定機会DTO
     */
    public static class ArbitrageOpportunity {
        private String symbol;
        private ExchangeType buyExchange;
        private ExchangeType sellExchange;
        private BigDecimal buyPrice;
        private BigDecimal sellPrice;
        private BigDecimal amount;
        private BigDecimal profitRate;
        
        public ArbitrageOpportunity(String symbol, ExchangeType buyExchange, ExchangeType sellExchange,
                                  BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal amount, BigDecimal profitRate) {
            this.symbol = symbol;
            this.buyExchange = buyExchange;
            this.sellExchange = sellExchange;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.amount = amount;
            this.profitRate = profitRate;
        }
        
        public boolean isProfitable() {
            return profitRate.compareTo(BigDecimal.ZERO) > 0;
        }
        
        public BigDecimal getExpectedProfit() {
            return sellPrice.subtract(buyPrice).multiply(amount);
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public ExchangeType getBuyExchange() { return buyExchange; }
        public ExchangeType getSellExchange() { return sellExchange; }
        public BigDecimal getBuyPrice() { return buyPrice; }
        public BigDecimal getSellPrice() { return sellPrice; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getProfitRate() { return profitRate; }
        
        @Override
        public String toString() {
            return String.format("ArbitrageOpportunity{symbol='%s', buy=%s@%s, sell=%s@%s, " +
                               "amount=%s, profitRate=%s%%, expectedProfit=%s}",
                               symbol, buyExchange, buyPrice, sellExchange, sellPrice,
                               amount, profitRate.multiply(new BigDecimal("100")), getExpectedProfit());
        }
    }
}
