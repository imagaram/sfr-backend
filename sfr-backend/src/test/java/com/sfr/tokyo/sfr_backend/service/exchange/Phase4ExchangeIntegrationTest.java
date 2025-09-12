package com.sfr.tokyo.sfr_backend.service.exchange;

import com.sfr.tokyo.sfr_backend.service.exchange.impl.MockExchangeApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4: 取引所API統合・グローバル展開準備テスト
 * 
 * Phase4MultiExchangeManager、SfrtLiquidityService、ArbitrageAnalyzer の統合テスト
 */
@ExtendWith(MockitoExtension.class)
class Phase4ExchangeIntegrationTest {
    
    private Phase4MultiExchangeManager multiExchangeManager;
    private ArbitrageAnalyzer arbitrageAnalyzer;
    private DefaultExchangeSelectionStrategy exchangeSelectionStrategy;
    private MockExchangeApiClient mockExchangeClient;
    
    @BeforeEach
    void setUp() {
        // テスト用コンポーネント初期化
        mockExchangeClient = new MockExchangeApiClient();
        arbitrageAnalyzer = new ArbitrageAnalyzer();
        exchangeSelectionStrategy = new DefaultExchangeSelectionStrategy();
        
        multiExchangeManager = new Phase4MultiExchangeManager(
            Arrays.asList(mockExchangeClient),
            exchangeSelectionStrategy,
            arbitrageAnalyzer
        );
    }
    
    @Test
    void testExchangeApiClientBasicOperations() {
        // 基本的なAPI操作テスト
        
        // 1. 取引所タイプ確認
        assertEquals(ExchangeType.MOCK_EXCHANGE, mockExchangeClient.getExchangeType());
        assertTrue(mockExchangeClient.isAvailable());
        
        // 2. 価格取得
        BigDecimal price = mockExchangeClient.getCurrentPrice("SFRT/JPY", "JPY");
        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
        System.out.println("現在価格: " + price + " JPY");
        
        // 3. 残高確認
        ExchangeBalance sfrtBalance = mockExchangeClient.getBalance("SFRT");
        assertNotNull(sfrtBalance);
        assertTrue(sfrtBalance.getTotal().compareTo(BigDecimal.ZERO) > 0);
        System.out.println("SFRT残高: " + sfrtBalance);
        
        // 4. 取引制限確認
        TradingLimits limits = mockExchangeClient.getTradingLimits();
        assertNotNull(limits);
        assertTrue(limits.getMinOrderAmount().compareTo(BigDecimal.ZERO) > 0);
        
        // 5. コンプライアンス状況確認
        ComplianceStatus compliance = mockExchangeClient.getComplianceStatus();
        assertNotNull(compliance);
        assertTrue(compliance.isFullyEnabled());
    }
    
    @Test
    void testMarketOrderExecution() {
        // 成行注文実行テスト
        
        BigDecimal sellAmount = new BigDecimal("1000"); // 1000 SFRT
        
        // 売り注文実行
        OrderResult sellResult = mockExchangeClient.placeMarketOrder("SFRT/JPY", sellAmount, OrderSide.SELL);
        
        assertNotNull(sellResult);
        assertTrue(sellResult.isSuccess());
        assertEquals(OrderStatus.FILLED, sellResult.getStatus());
        assertEquals(sellAmount, sellResult.getExecutedAmount());
        assertNotNull(sellResult.getExecutedPrice());
        
        System.out.println("売り注文結果: " + sellResult);
        
        // 買い注文実行
        BigDecimal buyAmount = new BigDecimal("500"); // 500 SFRT
        OrderResult buyResult = mockExchangeClient.placeMarketOrder("SFRT/JPY", buyAmount, OrderSide.BUY);
        
        assertNotNull(buyResult);
        assertTrue(buyResult.isSuccess());
        assertEquals(OrderStatus.FILLED, buyResult.getStatus());
        
        System.out.println("買い注文結果: " + buyResult);
    }
    
    @Test
    void testLimitOrderExecution() {
        // 指値注文実行テスト
        
        BigDecimal currentPrice = mockExchangeClient.getCurrentPrice("SFRT/JPY", "JPY");
        BigDecimal sellPrice = currentPrice.add(new BigDecimal("5")); // 現在価格+5円
        
        // 高い価格での売り注文（即座に約定しない）
        OrderResult sellResult = mockExchangeClient.placeLimitOrder("SFRT/JPY", 
            new BigDecimal("100"), sellPrice, OrderSide.SELL);
        
        assertNotNull(sellResult);
        assertTrue(sellResult.isSuccess());
        // 価格が高いため待機状態になる可能性
        
        // 現在価格での買い注文（即座に約定）
        OrderResult buyResult = mockExchangeClient.placeLimitOrder("SFRT/JPY",
            new BigDecimal("100"), currentPrice, OrderSide.BUY);
        
        assertNotNull(buyResult);
        assertTrue(buyResult.isSuccess());
        
        System.out.println("指値売り注文: " + sellResult);
        System.out.println("指値買い注文: " + buyResult);
    }
    
    @Test
    void testMultiExchangeManager() {
        // マルチ取引所管理テスト
        
        // 1. 利用可能取引所確認
        var availableExchanges = multiExchangeManager.getAvailableExchanges();
        assertFalse(availableExchanges.isEmpty());
        assertTrue(availableExchanges.contains(ExchangeType.MOCK_EXCHANGE));
        
        // 2. 価格取得
        Map<ExchangeType, BigDecimal> prices = multiExchangeManager.getCurrentPrices("SFRT/JPY");
        assertFalse(prices.isEmpty());
        assertTrue(prices.containsKey(ExchangeType.MOCK_EXCHANGE));
        
        System.out.println("取引所別価格: " + prices);
        
        // 3. 流動性操作実行
        LiquidityOperation operation = LiquidityOperation.marketSell("SFRT/JPY", 
            new BigDecimal("500"), "テスト売却");
        
        LiquidityResult result = multiExchangeManager.executeLiquidityOperation(operation);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(ExchangeType.MOCK_EXCHANGE, result.getExecutedExchange());
        
        System.out.println("流動性操作結果: " + result);
    }
    
    @Test
    void testArbitrageAnalyzer() {
        // 裁定機会分析テスト
        
        // テスト用価格差データ作成
        Map<ExchangeType, BigDecimal> prices = Map.of(
            ExchangeType.MOCK_EXCHANGE, new BigDecimal("150.00"),
            ExchangeType.BITBANK, new BigDecimal("152.00"),     // 2円高い
            ExchangeType.COINCHECK, new BigDecimal("148.50")    // 1.5円安い
        );
        
        ArbitrageAnalyzer.ArbitrageOpportunity opportunity = arbitrageAnalyzer.findOpportunity(prices);
        
        if (opportunity != null && opportunity.isProfitable()) {
            assertTrue(opportunity.getProfitRate().compareTo(BigDecimal.ZERO) > 0);
            assertNotNull(opportunity.getBuyExchange());
            assertNotNull(opportunity.getSellExchange());
            
            System.out.println("裁定機会発見: " + opportunity);
            System.out.println("予想利益: " + opportunity.getExpectedProfit() + " JPY");
        } else {
            System.out.println("裁定機会なし（価格差が閾値以下）");
        }
    }
    
    @Test
    void testExchangeSelectionStrategy() {
        // 取引所選択戦略テスト
        
        // テスト用メトリクス作成
        ComplianceStatus enabledCompliance = new ComplianceStatus(ExchangeType.MOCK_EXCHANGE);
        enabledCompliance.setTradingEnabled(true);
        
        ComplianceStatus disabledCompliance = new ComplianceStatus(ExchangeType.BITBANK);
        disabledCompliance.setTradingEnabled(false);
        
        ExchangeMetrics metrics1 = ExchangeMetrics.builder()
            .exchangeType(ExchangeType.MOCK_EXCHANGE)
            .available(true)
            .currentPrice(new BigDecimal("150.00"))
            .liquidity(new BigDecimal("5000000"))
            .complianceStatus(enabledCompliance)
            .build();
        
        ExchangeMetrics metrics2 = ExchangeMetrics.builder()
            .exchangeType(ExchangeType.BITBANK)
            .available(false) // 利用不可
            .currentPrice(new BigDecimal("151.00"))
            .liquidity(new BigDecimal("3000000"))
            .complianceStatus(disabledCompliance)
            .build();
        
        Map<ExchangeType, ExchangeMetrics> metricsMap = Map.of(
            ExchangeType.MOCK_EXCHANGE, metrics1,
            ExchangeType.BITBANK, metrics2
        );
        
        LiquidityOperation operation = LiquidityOperation.marketBuy("SFRT/JPY", 
            new BigDecimal("1000"), "テスト購入");
        
        ExchangeType selectedExchange = exchangeSelectionStrategy.selectBestExchange(metricsMap, operation);
        
        // 利用可能な取引所が選択されるはず
        assertEquals(ExchangeType.MOCK_EXCHANGE, selectedExchange);
        
        System.out.println("選択された取引所: " + selectedExchange);
    }
    
    @Test
    void testOrderBookAnalysis() {
        // オーダーブック分析テスト
        
        var orderBooks = mockExchangeClient.getOrderBook("SFRT/JPY", 5);
        assertFalse(orderBooks.isEmpty());
        
        OrderBook orderBook = orderBooks.get(0);
        assertNotNull(orderBook);
        assertFalse(orderBook.getBids().isEmpty());
        assertFalse(orderBook.getAsks().isEmpty());
        
        BigDecimal bestBid = orderBook.getBestBidPrice();
        BigDecimal bestAsk = orderBook.getBestAskPrice();
        BigDecimal spread = orderBook.getSpread();
        BigDecimal midPrice = orderBook.getMidPrice();
        
        assertTrue(bestBid.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(bestAsk.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(bestAsk.compareTo(bestBid) >= 0);
        
        System.out.println("オーダーブック分析:");
        System.out.println("  最高買い価格: " + bestBid);
        System.out.println("  最安売り価格: " + bestAsk);
        System.out.println("  スプレッド: " + spread);
        System.out.println("  中値: " + midPrice);
    }
    
    @Test
    void testErrorHandling() {
        // エラーハンドリングテスト
        
        // 残高不足での注文
        BigDecimal hugeAmount = new BigDecimal("10000000"); // 1000万SFRT（残高超過）
        
        OrderResult result = mockExchangeClient.placeMarketOrder("SFRT/JPY", hugeAmount, OrderSide.SELL);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        
        System.out.println("エラーハンドリング結果: " + result.getErrorMessage());
    }
}
