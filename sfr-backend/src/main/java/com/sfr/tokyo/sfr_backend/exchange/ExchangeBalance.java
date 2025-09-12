package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExchangeBalance {
    String asset;
    BigDecimal free;
    BigDecimal locked;

    public BigDecimal getTotal() { return free.add(locked); }
}
