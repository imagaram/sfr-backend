package com.sfr.tokyo.sfr_backend.exchange.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.sfr.tokyo.sfr_backend.exchange.*;

/**
 * Phase4 初期導入用のダミー取引所クライアント。
 * 実取引所API統合前にロジックを疎結合に検証するためのスタブ。
 */
@Component("dummyExchangeApiClient")
public class DummyExchangeApiClient implements ExchangeApiClient {

    @Override
    public BigDecimal getCurrentPrice(String symbol, String baseCurrency) {
        return randomPrice();
    }

    @Override
    public OrderResult placeLimitOrder(String symbol, BigDecimal amount, BigDecimal price, OrderSide side) {
        return OrderResult.success("LIM-" + System.currentTimeMillis(), amount, price, "dummy");
    }

    @Override
    public OrderResult placeMarketOrder(String symbol, BigDecimal amount, OrderSide side) {
        BigDecimal execPrice = randomPrice();
        return OrderResult.success("MKT-" + System.currentTimeMillis(), amount, execPrice, "dummy");
    }

    @Override
    public ExchangeBalance getBalance(String assetSymbol) {
        return ExchangeBalance.builder()
                .asset(assetSymbol)
                .free(new BigDecimal("100000"))
                .locked(BigDecimal.ZERO)
                .build();
    }

    @Override
    public List<ExchangeTrade> getTradeHistory(String symbol, LocalDateTime from) {
        return Collections.emptyList();
    }

    @Override
    public OrderBook getOrderBook(String symbol, int depth) {
        return OrderBook.builder()
                .bids(Collections.emptyList())
                .asks(Collections.emptyList())
                .fetchedAtEpochMillis(System.currentTimeMillis())
                .build();
    }

    @Override
    public TradingLimits getTradingLimits() {
        return TradingLimits.builder()
                .minOrderSize(new BigDecimal("0.01"))
                .maxOrderSize(new BigDecimal("10000"))
                .minNotional(new BigDecimal("100"))
                .pricePrecision(new BigDecimal("0.01"))
                .sizePrecision(new BigDecimal("0.0001"))
                .build();
    }

    @Override
    public ComplianceStatus getComplianceStatus() {
        return ComplianceStatus.builder()
                .apiHealthy(true)
                .tradingAllowed(true)
                .withdrawalAllowed(true)
                .jurisdictionNote("DUMMY")
                .lastCheckedIso(LocalDateTime.now().toString())
                .build();
    }

    private BigDecimal randomPrice() {
        double base = 120.0 + ThreadLocalRandom.current().nextDouble(20.0); // 120-140
        return new BigDecimal(String.format("%.2f", base));
    }
}
