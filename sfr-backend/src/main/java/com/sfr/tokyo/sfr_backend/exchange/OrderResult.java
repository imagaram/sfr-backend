package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderResult {
    boolean success;
    String orderId;
    BigDecimal executedAmount;
    BigDecimal avgExecutionPrice;
    String rawResponse;
    String error;

    public static OrderResult success(String orderId, BigDecimal executedAmount, BigDecimal avgPrice, String raw) {
        return OrderResult.builder()
                .success(true)
                .orderId(orderId)
                .executedAmount(executedAmount)
                .avgExecutionPrice(avgPrice)
                .rawResponse(raw)
                .build();
    }

    public static OrderResult failure(String error, String raw) {
        return OrderResult.builder()
                .success(false)
                .error(error)
                .rawResponse(raw)
                .build();
    }
}
