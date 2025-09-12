package com.sfr.tokyo.sfr_backend.controller.crypto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sfr.tokyo.sfr_backend.exchange.*;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sfrt/liquidity")
@RequiredArgsConstructor
public class SfrtLiquidityController {

    private final MultiExchangeManager multiExchangeManager;

    @GetMapping("/prices")
    public ResponseEntity<Map<ExchangeType, BigDecimal>> getCurrentPrices() {
        return ResponseEntity.ok(multiExchangeManager.getCurrentPrices("SFRT/JPY"));
    }

    @PostMapping("/operate")
    public ResponseEntity<LiquidityResult> operate(@RequestBody LiquidityOperationRequest req) {
        LiquidityOperation op = LiquidityOperation.builder()
                .type(req.getType())
                .symbol(req.getSymbol())
                .amount(req.getAmount())
                .targetPrice(req.getTargetPrice())
                .reason(req.getReason())
                .build();
        return ResponseEntity.ok(multiExchangeManager.executeLiquidityOperation(op));
    }

    @Data
    public static class LiquidityOperationRequest {
        private LiquidityOperation.OperationType type;
        private String symbol = "SFRT/JPY";
        private BigDecimal amount;
        private BigDecimal targetPrice;
        private String reason;
        private Instant requestedAt = Instant.now();
    }
}
