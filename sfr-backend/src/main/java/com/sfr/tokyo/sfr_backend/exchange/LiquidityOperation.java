package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LiquidityOperation {
    public enum OperationType { BUY, SELL, PROVIDE_LIQUIDITY, REMOVE_LIQUIDITY }

    OperationType type;
    String symbol; // e.g. SFRT/JPY
    BigDecimal amount;
    BigDecimal targetPrice; // optional
    String reason;
}
