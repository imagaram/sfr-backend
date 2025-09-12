package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 複数取引所統合マネージャ (Phase4 基盤)。
 * 現時点では DummyExchange のみ登録し、将来 Bitbank / Binance / Coincheck 実装を差し替え可能。
 */
@Service
@RequiredArgsConstructor
public class MultiExchangeManager {

    private final Map<ExchangeType, ExchangeApiClient> exchanges = new EnumMap<>(ExchangeType.class);
    private final ExchangeSelectionStrategy selectionStrategy;

    public MultiExchangeManager(ExchangeSelectionStrategy selectionStrategy,
            // Spring Bean 名前でダミーを注入
            ExchangeApiClient dummyExchangeApiClient) {
        this.selectionStrategy = selectionStrategy;
        // 暫定: 全ての種類に同じダミー実装を割り当て（後で個別実装差し替え）
        exchanges.put(ExchangeType.BITBANK, dummyExchangeApiClient);
        exchanges.put(ExchangeType.BINANCE, dummyExchangeApiClient);
        exchanges.put(ExchangeType.COINCHECK, dummyExchangeApiClient);
    }

    public Map<ExchangeType, BigDecimal> getCurrentPrices(String symbolPair) {
        return exchanges.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().getCurrentPrice(symbolPair, baseCurrency(symbolPair))));
    }

    public LiquidityResult executeLiquidityOperation(LiquidityOperation op) {
        Map<ExchangeType, ExchangeMetrics> metrics = collectMetrics(op.getSymbol());
        ExchangeType chosen = selectionStrategy.selectBestExchange(metrics, op);
        ExchangeApiClient client = exchanges.get(chosen);

        OrderResult orderResult;
        switch (op.getType()) {
            case BUY -> orderResult = client.placeMarketOrder(op.getSymbol(), op.getAmount(), OrderSide.BUY);
            case SELL -> orderResult = client.placeMarketOrder(op.getSymbol(), op.getAmount(), OrderSide.SELL);
            default -> {
                return LiquidityResult.builder()
                        .success(false)
                        .executedOn(chosen)
                        .operationType(op.getType())
                        .detail("未対応のオペレーション種別: " + op.getType())
                        .build();
            }
        }

        return LiquidityResult.builder()
                .success(orderResult.isSuccess())
                .executedOn(chosen)
                .operationType(op.getType())
                .totalExecutedAmount(orderResult.getExecutedAmount())
                .avgPrice(orderResult.getAvgExecutionPrice())
                .detail(orderResult.getRawResponse())
                .build();
    }

    private Map<ExchangeType, ExchangeMetrics> collectMetrics(String symbolPair) {
        return exchanges.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            BigDecimal mid = e.getValue().getCurrentPrice(symbolPair, baseCurrency(symbolPair));
            return ExchangeMetrics.builder()
                    .exchange(e.getKey())
                    .bestBid(mid.subtract(new BigDecimal("0.5")))
                    .bestAsk(mid.add(new BigDecimal("0.5")))
                    .midPrice(mid)
                    .availableLiquidityBase(new BigDecimal("10000"))
                    .availableLiquidityQuote(new BigDecimal("1500000"))
                    .feeRate(new BigDecimal("0.001"))
                    .tradingAllowed(true)
                    .latencyMillis(50)
                    .build();
        }));
    }

    private String baseCurrency(String symbolPair) {
        int idx = symbolPair.indexOf('/');
        return idx > 0 ? symbolPair.substring(idx + 1) : "JPY";
    }
}
