package com.sfr.tokyo.sfr_backend.service.exchange;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * デフォルト取引所選択戦略
 * 品質スコアと流動性を重視した選択
 * Phase 4: 取引所API統合対応
 */
@Component
public class DefaultExchangeSelectionStrategy implements ExchangeSelectionStrategy {
    
    @Override
    public ExchangeType selectBestExchange(Map<ExchangeType, ExchangeMetrics> metrics, LiquidityOperation operation) {
        
        if (metrics.isEmpty()) {
            return null;
        }
        
        // 利用可能な取引所のみフィルタリング
        Map<ExchangeType, ExchangeMetrics> availableMetrics = metrics.entrySet().stream()
            .filter(entry -> entry.getValue().isAvailable() && entry.getValue().isTradingAvailable())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
        
        if (availableMetrics.isEmpty()) {
            return null;
        }
        
        // 単一取引所の場合はそれを選択
        if (availableMetrics.size() == 1) {
            return availableMetrics.keySet().iterator().next();
        }
        
        // 複数取引所から最適な選択
        return selectOptimalExchange(availableMetrics, operation);
    }
    
    /**
     * 最適取引所の選択ロジック
     */
    private ExchangeType selectOptimalExchange(Map<ExchangeType, ExchangeMetrics> availableMetrics, 
                                             LiquidityOperation operation) {
        
        ExchangeType bestExchange = null;
        BigDecimal bestScore = BigDecimal.ZERO;
        
        for (Map.Entry<ExchangeType, ExchangeMetrics> entry : availableMetrics.entrySet()) {
            ExchangeType exchangeType = entry.getKey();
            ExchangeMetrics metrics = entry.getValue();
            
            BigDecimal score = calculateExchangeScore(metrics, operation);
            
            if (score.compareTo(bestScore) > 0) {
                bestScore = score;
                bestExchange = exchangeType;
            }
        }
        
        return bestExchange;
    }
    
    /**
     * 取引所スコア計算
     */
    private BigDecimal calculateExchangeScore(ExchangeMetrics metrics, LiquidityOperation operation) {
        BigDecimal score = BigDecimal.ZERO;
        
        // 基本品質スコア（50%）
        score = score.add(metrics.calculateQualityScore().multiply(new BigDecimal("0.5")));
        
        // 流動性スコア（30%）
        if (metrics.getLiquidity() != null) {
            BigDecimal liquidityScore = metrics.getLiquidity()
                .divide(new BigDecimal("1000000")) // 100万円で1点
                .min(new BigDecimal("30"));
            score = score.add(liquidityScore);
        }
        
        // 価格優位性スコア（20%） - 買いの場合は安い取引所、売りの場合は高い取引所を優先
        if (operation.isBuyOperation()) {
            // 買い操作：価格が安いほど高スコア
            // (実装簡略化のため、ここでは基本スコアを使用)
            score = score.add(new BigDecimal("10"));
        } else {
            // 売り操作：価格が高いほど高スコア
            // (実装簡略化のため、ここでは基本スコアを使用)
            score = score.add(new BigDecimal("10"));
        }
        
        return score;
    }
}
