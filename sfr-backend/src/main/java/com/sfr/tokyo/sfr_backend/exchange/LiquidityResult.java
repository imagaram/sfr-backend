package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LiquidityResult {
    boolean success;
    ExchangeType executedOn;
    LiquidityOperation.OperationType operationType;
    BigDecimal totalExecutedAmount;
    BigDecimal avgPrice;
    String detail;
}
