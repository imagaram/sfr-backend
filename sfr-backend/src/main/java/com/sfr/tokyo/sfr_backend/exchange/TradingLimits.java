package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TradingLimits {
    BigDecimal minOrderSize;
    BigDecimal maxOrderSize;
    BigDecimal minNotional;
    BigDecimal pricePrecision;
    BigDecimal sizePrecision;
}
