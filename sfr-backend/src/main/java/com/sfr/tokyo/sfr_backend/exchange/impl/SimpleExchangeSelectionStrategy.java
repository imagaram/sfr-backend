package com.sfr.tokyo.sfr_backend.exchange.impl;

import java.util.Comparator;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sfr.tokyo.sfr_backend.exchange.*;

/**
 * シンプルな選択戦略: 売りは最高bid, 買いは最安ask, 取引不可は排除。
 */
@Component
public class SimpleExchangeSelectionStrategy implements ExchangeSelectionStrategy {

    @Override
    public ExchangeType selectBestExchange(Map<ExchangeType, ExchangeMetrics> metrics, LiquidityOperation op) {
        return metrics.values().stream()
                .filter(ExchangeMetrics::isTradingAllowed)
                .min(Comparator.comparing(m -> score(m, op)))
                .map(ExchangeMetrics::getExchange)
                .orElseThrow(() -> new IllegalStateException("利用可能な取引所がありません"));
    }

    private double score(ExchangeMetrics m, LiquidityOperation op) {
        // 単純スコア: スプレッド + 手数料 + レイテンシ
        double spread = m.getBestAsk().subtract(m.getBestBid()).doubleValue();
        double fee = m.getFeeRate().doubleValue();
        double latency = m.getLatencyMillis() / 1000.0;
        return spread + fee + latency;
    }
}
