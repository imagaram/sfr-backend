package com.sfr.tokyo.sfr_backend.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase4: 取引所API 統合基盤インタフェース
 * 複数取引所（国内/海外）を統一的に扱うための抽象化。
 */
public interface ExchangeApiClient {

    // 基本価格取得
    BigDecimal getCurrentPrice(String symbol, String baseCurrency);

    // 指値注文
    OrderResult placeLimitOrder(String symbol, BigDecimal amount, BigDecimal price, OrderSide side);

    // 成行注文
    OrderResult placeMarketOrder(String symbol, BigDecimal amount, OrderSide side);

    // 残高
    ExchangeBalance getBalance(String assetSymbol);

    // 約定履歴
    List<ExchangeTrade> getTradeHistory(String symbol, LocalDateTime from);

    // オーダーブック
    OrderBook getOrderBook(String symbol, int depth);

    // 取引制限
    TradingLimits getTradingLimits();

    // コンプライアンス/稼働状態
    ComplianceStatus getComplianceStatus();
}
