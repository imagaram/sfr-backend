package com.sfr.tokyo.sfr_backend.service.exchange.impl;

import com.sfr.tokyo.sfr_backend.service.exchange.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * モック取引所実装
 * Phase 4: 取引所API統合・テスト用
 * 
 * 実際の取引所APIを模擬した動作を提供
 */
@Component
public class MockExchangeApiClient implements ExchangeApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(MockExchangeApiClient.class);
    
    private final AtomicLong orderIdCounter = new AtomicLong(1000);
    private final AtomicLong tradeIdCounter = new AtomicLong(5000);
    
    // モック価格データ（実際の取引所では外部APIから取得）
    private BigDecimal mockPrice = new BigDecimal("148.50"); // 1SFRT = 148.50円
    private final Random priceRandom = new Random();
    
    // モック残高データ
    private final Map<String, ExchangeBalance> mockBalances = new HashMap<>();
    
    // モック注文履歴
    private final List<ExchangeTrade> mockTrades = new ArrayList<>();
    
    public MockExchangeApiClient() {
        initializeMockData();
    }
    
    /**
     * モックデータ初期化
     */
    private void initializeMockData() {
        // SFRT残高初期化
        mockBalances.put("SFRT", new ExchangeBalance("SFRT", 
            new BigDecimal("1000000"), // 利用可能: 100万SFRT
            new BigDecimal("50000"),   // ロック: 5万SFRT
            ExchangeType.MOCK_EXCHANGE));
        
        // JPY残高初期化
        mockBalances.put("JPY", new ExchangeBalance("JPY",
            new BigDecimal("50000000"), // 利用可能: 5000万円
            new BigDecimal("1000000"),  // ロック: 100万円
            ExchangeType.MOCK_EXCHANGE));
    }
    
    @Override
    public ExchangeType getExchangeType() {
        return ExchangeType.MOCK_EXCHANGE;
    }
    
    @Override
    public boolean isAvailable() {
        return true; // モック取引所は常に利用可能
    }
    
    @Override
    public BigDecimal getCurrentPrice(String symbol, String baseCurrency) {
        if ("SFRT/JPY".equals(symbol) || ("SFRT".equals(symbol) && "JPY".equals(baseCurrency))) {
            // 価格変動シミュレーション（±2%範囲）
            double variation = (priceRandom.nextDouble() - 0.5) * 0.04; // ±2%
            BigDecimal variationAmount = mockPrice.multiply(BigDecimal.valueOf(variation));
            mockPrice = mockPrice.add(variationAmount);
            
            // 価格範囲制限（100円〜200円）
            if (mockPrice.compareTo(new BigDecimal("100")) < 0) {
                mockPrice = new BigDecimal("100");
            } else if (mockPrice.compareTo(new BigDecimal("200")) > 0) {
                mockPrice = new BigDecimal("200");
            }
            
            return mockPrice.setScale(2, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }
    
    @Override
    public OrderResult placeSellOrder(String symbol, BigDecimal amount, BigDecimal price) {
        return placeLimitOrder(symbol, amount, price, OrderSide.SELL);
    }
    
    @Override
    public OrderResult placeBuyOrder(String symbol, BigDecimal amount, BigDecimal price) {
        return placeLimitOrder(symbol, amount, price, OrderSide.BUY);
    }
    
    @Override
    public ExchangeBalance getBalance(String symbol) {
        return mockBalances.get(symbol);
    }
    
    @Override
    public List<ExchangeTrade> getTradeHistory(String symbol, LocalDateTime from) {
        return mockTrades.stream()
            .filter(trade -> trade.getSymbol().equals(symbol))
            .filter(trade -> trade.getTimestamp().isAfter(from))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    @Override
    public OrderResult placeMarketOrder(String symbol, BigDecimal amount, OrderSide side) {
        logger.info("モック成行注文: symbol={}, amount={}, side={}", symbol, amount, side);
        
        try {
            // 残高チェック
            if (!checkBalance(symbol, amount, side)) {
                return OrderResult.failure("残高不足", ExchangeType.MOCK_EXCHANGE);
            }
            
            // 現在価格で即座に約定
            BigDecimal executionPrice = getCurrentPrice(symbol, "JPY");
            String orderId = "MOCK_" + orderIdCounter.getAndIncrement();
            
            // 取引実行シミュレーション
            simulateTradeExecution(symbol, amount, executionPrice, side, orderId);
            
            OrderResult result = OrderResult.success(orderId, symbol, side, amount, executionPrice, ExchangeType.MOCK_EXCHANGE);
            result.setExecutedAmount(amount);
            result.setExecutedPrice(executionPrice);
            result.setStatus(OrderStatus.FILLED);
            
            logger.info("モック成行注文約定: {}", result);
            return result;
            
        } catch (Exception e) {
            logger.error("モック成行注文エラー", e);
            return OrderResult.failure("注文処理エラー: " + e.getMessage(), ExchangeType.MOCK_EXCHANGE);
        }
    }
    
    @Override
    public OrderResult placeLimitOrder(String symbol, BigDecimal amount, BigDecimal price, OrderSide side) {
        logger.info("モック指値注文: symbol={}, amount={}, price={}, side={}", symbol, amount, price, side);
        
        try {
            // 残高チェック
            if (!checkBalance(symbol, amount, side)) {
                return OrderResult.failure("残高不足", ExchangeType.MOCK_EXCHANGE);
            }
            
            String orderId = "MOCK_" + orderIdCounter.getAndIncrement();
            BigDecimal currentPrice = getCurrentPrice(symbol, "JPY");
            
            // 指値注文の約定判定（簡略化）
            boolean canExecute = (side == OrderSide.BUY && price.compareTo(currentPrice) >= 0) ||
                               (side == OrderSide.SELL && price.compareTo(currentPrice) <= 0);
            
            if (canExecute) {
                // 即座に約定
                simulateTradeExecution(symbol, amount, price, side, orderId);
                
                OrderResult result = OrderResult.success(orderId, symbol, side, amount, price, ExchangeType.MOCK_EXCHANGE);
                result.setExecutedAmount(amount);
                result.setExecutedPrice(price);
                result.setStatus(OrderStatus.FILLED);
                
                logger.info("モック指値注文約定: {}", result);
                return result;
            } else {
                // 注文待機状態
                OrderResult result = OrderResult.success(orderId, symbol, side, amount, price, ExchangeType.MOCK_EXCHANGE);
                result.setStatus(OrderStatus.PENDING);
                
                logger.info("モック指値注文待機: {}", result);
                return result;
            }
            
        } catch (Exception e) {
            logger.error("モック指値注文エラー", e);
            return OrderResult.failure("注文処理エラー: " + e.getMessage(), ExchangeType.MOCK_EXCHANGE);
        }
    }
    
    @Override
    public List<OrderBook> getOrderBook(String symbol, int depth) {
        // モックオーダーブック生成
        List<OrderBook.OrderBookEntry> bids = new ArrayList<>();
        List<OrderBook.OrderBookEntry> asks = new ArrayList<>();
        
        BigDecimal currentPrice = getCurrentPrice(symbol, "JPY");
        
        // 買い注文（現在価格から下向き）
        for (int i = 0; i < depth; i++) {
            BigDecimal price = currentPrice.subtract(BigDecimal.valueOf(i * 0.5));
            BigDecimal amount = new BigDecimal(String.valueOf(1000 + priceRandom.nextInt(5000)));
            bids.add(new OrderBook.OrderBookEntry(price, amount));
        }
        
        // 売り注文（現在価格から上向き）
        for (int i = 1; i <= depth; i++) {
            BigDecimal price = currentPrice.add(BigDecimal.valueOf(i * 0.5));
            BigDecimal amount = new BigDecimal(String.valueOf(1000 + priceRandom.nextInt(5000)));
            asks.add(new OrderBook.OrderBookEntry(price, amount));
        }
        
        OrderBook orderBook = new OrderBook(symbol, bids, asks, ExchangeType.MOCK_EXCHANGE);
        return Collections.singletonList(orderBook);
    }
    
    @Override
    public TradingLimits getTradingLimits() {
        return new TradingLimits(ExchangeType.MOCK_EXCHANGE);
    }
    
    @Override
    public ComplianceStatus getComplianceStatus() {
        return new ComplianceStatus(ExchangeType.MOCK_EXCHANGE);
    }
    
    @Override
    public boolean cancelOrder(String orderId) {
        logger.info("モック注文キャンセル: orderId={}", orderId);
        return true; // モックでは常に成功
    }
    
    @Override
    public OrderStatus getOrderStatus(String orderId) {
        // モックでは適当な状態を返す
        return OrderStatus.FILLED;
    }
    
    /**
     * 残高チェック
     */
    private boolean checkBalance(String symbol, BigDecimal amount, OrderSide side) {
        if (side == OrderSide.SELL) {
            // 売り注文：SFRT残高チェック
            ExchangeBalance sfrtBalance = mockBalances.get("SFRT");
            return sfrtBalance != null && sfrtBalance.isAvailable(amount);
        } else {
            // 買い注文：JPY残高チェック
            BigDecimal currentPrice = getCurrentPrice(symbol, "JPY");
            BigDecimal requiredJpy = amount.multiply(currentPrice);
            ExchangeBalance jpyBalance = mockBalances.get("JPY");
            return jpyBalance != null && jpyBalance.isAvailable(requiredJpy);
        }
    }
    
    /**
     * 取引実行シミュレーション
     */
    private void simulateTradeExecution(String symbol, BigDecimal amount, BigDecimal price, 
                                      OrderSide side, String orderId) {
        
        // 残高更新
        updateBalances(symbol, amount, price, side);
        
        // 取引履歴追加
        String tradeId = "TRADE_" + tradeIdCounter.getAndIncrement();
        ExchangeTrade trade = new ExchangeTrade(tradeId, orderId, symbol, side, amount, price, ExchangeType.MOCK_EXCHANGE);
        trade.setFee(calculateFee(amount, price));
        trade.setFeeCurrency("JPY");
        mockTrades.add(trade);
        
        logger.debug("モック取引実行: {}", trade);
    }
    
    /**
     * 残高更新
     */
    private void updateBalances(String symbol, BigDecimal amount, BigDecimal price, OrderSide side) {
        ExchangeBalance sfrtBalance = mockBalances.get("SFRT");
        ExchangeBalance jpyBalance = mockBalances.get("JPY");
        
        if (side == OrderSide.SELL) {
            // SFRT売却：SFRT減少、JPY増加
            BigDecimal newSfrtAvailable = sfrtBalance.getAvailable().subtract(amount);
            sfrtBalance.setAvailable(newSfrtAvailable);
            
            BigDecimal jpyReceived = amount.multiply(price);
            BigDecimal newJpyAvailable = jpyBalance.getAvailable().add(jpyReceived);
            jpyBalance.setAvailable(newJpyAvailable);
        } else {
            // SFRT購入：JPY減少、SFRT増加
            BigDecimal jpyRequired = amount.multiply(price);
            BigDecimal newJpyAvailable = jpyBalance.getAvailable().subtract(jpyRequired);
            jpyBalance.setAvailable(newJpyAvailable);
            
            BigDecimal newSfrtAvailable = sfrtBalance.getAvailable().add(amount);
            sfrtBalance.setAvailable(newSfrtAvailable);
        }
    }
    
    /**
     * 手数料計算
     */
    private BigDecimal calculateFee(BigDecimal amount, BigDecimal price) {
        BigDecimal totalValue = amount.multiply(price);
        return totalValue.multiply(new BigDecimal("0.001")); // 0.1%手数料
    }
}
