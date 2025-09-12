package com.sfr.tokyo.sfr_backend.service.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 取引所API統合インターフェース
 * 複数の外部取引所との統一的なAPI連携を提供
 * 
 * Phase 4: 取引所API統合・グローバル展開準備
 */
public interface ExchangeApiClient {
    
    /**
     * 取引所タイプを取得
     */
    ExchangeType getExchangeType();
    
    /**
     * 取引所の有効性チェック
     */
    boolean isAvailable();
    
    // ========== 基本取引機能 ==========
    
    /**
     * 現在価格を取得
     * @param symbol 取引ペア（例: "SFRT/JPY"）
     * @param baseCurrency 基準通貨
     * @return 現在価格
     */
    BigDecimal getCurrentPrice(String symbol, String baseCurrency);
    
    /**
     * 売り注文を発注
     * @param symbol 取引ペア
     * @param amount 数量
     * @param price 指定価格
     * @return 注文結果
     */
    OrderResult placeSellOrder(String symbol, BigDecimal amount, BigDecimal price);
    
    /**
     * 買い注文を発注
     * @param symbol 取引ペア
     * @param amount 数量
     * @param price 指定価格
     * @return 注文結果
     */
    OrderResult placeBuyOrder(String symbol, BigDecimal amount, BigDecimal price);
    
    // ========== 残高・履歴管理 ==========
    
    /**
     * 残高を取得
     * @param symbol 通貨シンボル
     * @return 残高情報
     */
    ExchangeBalance getBalance(String symbol);
    
    /**
     * 取引履歴を取得
     * @param symbol 取引ペア
     * @param from 開始日時
     * @return 取引履歴リスト
     */
    List<ExchangeTrade> getTradeHistory(String symbol, LocalDateTime from);
    
    // ========== 高度な取引機能 ==========
    
    /**
     * 成行注文を発注
     * @param symbol 取引ペア
     * @param amount 数量
     * @param side 売買区分
     * @return 注文結果
     */
    OrderResult placeMarketOrder(String symbol, BigDecimal amount, OrderSide side);
    
    /**
     * 指値注文を発注
     * @param symbol 取引ペア
     * @param amount 数量
     * @param price 指定価格
     * @param side 売買区分
     * @return 注文結果
     */
    OrderResult placeLimitOrder(String symbol, BigDecimal amount, BigDecimal price, OrderSide side);
    
    /**
     * オーダーブックを取得
     * @param symbol 取引ペア
     * @param depth 取得深度
     * @return オーダーブック
     */
    List<OrderBook> getOrderBook(String symbol, int depth);
    
    // ========== リスク管理 ==========
    
    /**
     * 取引制限情報を取得
     * @return 取引制限
     */
    TradingLimits getTradingLimits();
    
    /**
     * コンプライアンス状況を取得
     * @return コンプライアンス状況
     */
    ComplianceStatus getComplianceStatus();
    
    /**
     * 注文をキャンセル
     * @param orderId 注文ID
     * @return キャンセル結果
     */
    boolean cancelOrder(String orderId);
    
    /**
     * 注文状況を確認
     * @param orderId 注文ID
     * @return 注文状況
     */
    OrderStatus getOrderStatus(String orderId);
}
