package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderBook {
    List<OrderLevel> bids; // buy orders (price desc)
    List<OrderLevel> asks; // sell orders (price asc)
    long fetchedAtEpochMillis;

    @Value
    @Builder
    public static class OrderLevel {
        BigDecimal price;
        BigDecimal amount;
    }
}
