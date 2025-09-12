package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExchangeTrade {
    String tradeId;
    String symbol;
    OrderSide side;
    BigDecimal price;
    BigDecimal amount;
    BigDecimal fee;
    LocalDateTime executedAt;
}
