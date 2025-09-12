package com.sfr.tokyo.sfr_backend.exchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradingPair {
    SFRT_JPY("SFRT/JPY"),
    SFRT_USDT("SFRT/USDT"),
    SFRT_BTC("SFRT/BTC");

    private final String symbol;
}
