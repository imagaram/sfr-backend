package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExchangeMetrics {
    ExchangeType exchange;
    BigDecimal bestBid;
    BigDecimal bestAsk;
    BigDecimal midPrice;
    BigDecimal availableLiquidityBase; // base asset liquidity
    BigDecimal availableLiquidityQuote; // quote currency liquidity
    BigDecimal feeRate;
    boolean tradingAllowed;
    long latencyMillis;
}
