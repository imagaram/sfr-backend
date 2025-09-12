package com.sfr.tokyo.sfr_backend.exchange;

import java.util.Map;

public interface ExchangeSelectionStrategy {
    ExchangeType selectBestExchange(Map<ExchangeType, ExchangeMetrics> metrics, LiquidityOperation op);
}
