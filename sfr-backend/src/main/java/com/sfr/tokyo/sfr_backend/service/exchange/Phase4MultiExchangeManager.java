package com.sfr.tokyo.sfr_backend.service.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * マルチ取引所管理サービス
 * Phase 4: 取引所API統合・グローバル展開準備
 * 
 * 複数の取引所を統合管理し、最適な取引実行を提供
 */
@Service("phase4MultiExchangeManager")
public class Phase4MultiExchangeManager {
    
    private static final Logger logger = LoggerFactory.getLogger(Phase4MultiExchangeManager.class);
    
    private final Map<ExchangeType, ExchangeApiClient> exchanges;
    private final ExchangeSelectionStrategy selectionStrategy;
    private final ArbitrageAnalyzer arbitrageAnalyzer;
    
    public Phase4MultiExchangeManager(List<ExchangeApiClient> exchangeClients,
                               ExchangeSelectionStrategy selectionStrategy,
                               ArbitrageAnalyzer arbitrageAnalyzer) {
        this.exchanges = exchangeClients.stream()
            .collect(Collectors.toMap(
                ExchangeApiClient::getExchangeType,
                client -> client
            ));
        this.selectionStrategy = selectionStrategy;
        this.arbitrageAnalyzer = arbitrageAnalyzer;
        
        logger.info("MultiExchangeManager initialized with {} exchanges: {}", 
                   exchanges.size(), exchanges.keySet());
    }
    
    /**
     * 最適取引所での流動性操作実行
     */
    public LiquidityResult executeLiquidityOperation(LiquidityOperation operation) {
        logger.info("流動性操作開始: {}", operation);
        
        try {
            // 1. 全取引所のメトリクス調査
            Map<ExchangeType, ExchangeMetrics> metrics = analyzeAllExchangeMetrics(operation);
            
            // 2. 最適取引所選択
            ExchangeType bestExchange = selectionStrategy.selectBestExchange(metrics, operation);
            
            if (bestExchange == null) {
                return LiquidityResult.failure("適切な取引所が見つかりません", operation);
            }
            
            // 3. 取引実行・結果監視
            return executeWithMonitoring(exchanges.get(bestExchange), operation);
            
        } catch (Exception e) {
            logger.error("流動性操作失敗: {}", operation, e);
            return LiquidityResult.failure("流動性操作中にエラーが発生: " + e.getMessage(), operation);
        }
    }
    
    /**
     * 全取引所のメトリクス分析
     */
    private Map<ExchangeType, ExchangeMetrics> analyzeAllExchangeMetrics(LiquidityOperation operation) {
        Map<ExchangeType, CompletableFuture<ExchangeMetrics>> futures = new HashMap<>();
        
        // 並列でメトリクス取得
        exchanges.entrySet().forEach(entry -> {
            futures.put(entry.getKey(), 
                CompletableFuture.supplyAsync(() -> 
                    analyzeExchangeMetrics(entry.getValue(), operation)));
        });
        
        // 結果収集
        Map<ExchangeType, ExchangeMetrics> metrics = new HashMap<>();
        futures.entrySet().forEach(entry -> {
            try {
                metrics.put(entry.getKey(), entry.getValue().get());
            } catch (Exception e) {
                logger.warn("取引所メトリクス取得失敗: {}", entry.getKey(), e);
                metrics.put(entry.getKey(), ExchangeMetrics.unavailable(entry.getKey()));
            }
        });
        
        return metrics;
    }
    
    /**
     * 個別取引所のメトリクス分析
     */
    private ExchangeMetrics analyzeExchangeMetrics(ExchangeApiClient client, LiquidityOperation operation) {
        try {
            if (!client.isAvailable()) {
                return ExchangeMetrics.unavailable(client.getExchangeType());
            }
            
            // 価格・流動性・手数料等の分析
            BigDecimal currentPrice = client.getCurrentPrice(operation.getSymbol(), "JPY");
            List<OrderBook> orderBook = client.getOrderBook(operation.getSymbol(), 10);
            TradingLimits limits = client.getTradingLimits();
            ComplianceStatus compliance = client.getComplianceStatus();
            
            return ExchangeMetrics.builder()
                .exchangeType(client.getExchangeType())
                .available(true)
                .currentPrice(currentPrice)
                .liquidity(calculateLiquidity(orderBook))
                .tradingLimits(limits)
                .complianceStatus(compliance)
                .lastUpdated(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            logger.warn("メトリクス分析エラー: {}", client.getExchangeType(), e);
            return ExchangeMetrics.unavailable(client.getExchangeType());
        }
    }
    
    /**
     * 流動性計算
     */
    private BigDecimal calculateLiquidity(List<OrderBook> orderBooks) {
        if (orderBooks == null || orderBooks.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return orderBooks.stream()
            .map(book -> {
                BigDecimal bidLiquidity = book.getBids().stream()
                    .map(OrderBook.OrderBookEntry::getTotalValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                BigDecimal askLiquidity = book.getAsks().stream()
                    .map(OrderBook.OrderBookEntry::getTotalValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                return bidLiquidity.add(askLiquidity);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 監視付き取引実行
     */
    private LiquidityResult executeWithMonitoring(ExchangeApiClient client, LiquidityOperation operation) {
        logger.info("取引実行開始: {} on {}", operation, client.getExchangeType());
        
        try {
            OrderResult result;
            
            switch (operation.getType()) {
                case MARKET_SELL:
                    result = client.placeMarketOrder(operation.getSymbol(), operation.getAmount(), OrderSide.SELL);
                    break;
                case MARKET_BUY:
                    result = client.placeMarketOrder(operation.getSymbol(), operation.getAmount(), OrderSide.BUY);
                    break;
                case LIMIT_SELL:
                    result = client.placeLimitOrder(operation.getSymbol(), operation.getAmount(), 
                                                  operation.getPrice(), OrderSide.SELL);
                    break;
                case LIMIT_BUY:
                    result = client.placeLimitOrder(operation.getSymbol(), operation.getAmount(), 
                                                  operation.getPrice(), OrderSide.BUY);
                    break;
                default:
                    return LiquidityResult.failure("未対応の操作タイプ: " + operation.getType(), operation);
            }
            
            if (result.isSuccess()) {
                logger.info("取引実行成功: {}", result);
                return LiquidityResult.success(result, operation);
            } else {
                logger.warn("取引実行失敗: {}", result);
                return LiquidityResult.failure(result.getErrorMessage(), operation);
            }
            
        } catch (Exception e) {
            logger.error("取引実行中エラー: {}", operation, e);
            return LiquidityResult.failure("取引実行エラー: " + e.getMessage(), operation);
        }
    }
    
    /**
     * 価格裁定・流動性最適化（30秒間隔）
     */
    @Scheduled(fixedRate = 30000)
    public void performArbitrageCheck() {
        logger.debug("裁定機会チェック開始");
        
        try {
            Map<ExchangeType, BigDecimal> prices = getCurrentPrices("SFRT/JPY");
            
            if (prices.size() < 2) {
                logger.debug("価格データ不足 (取引所数: {})", prices.size());
                return;
            }
            
            ArbitrageAnalyzer.ArbitrageOpportunity opportunity = arbitrageAnalyzer.findOpportunity(prices);
            
            if (opportunity.isProfitable()) {
                logger.info("裁定機会発見: {}", opportunity);
                executeArbitrage(opportunity);
            } else {
                logger.debug("裁定機会なし");
            }
            
        } catch (Exception e) {
            logger.error("裁定チェック中エラー", e);
        }
    }
    
    /**
     * 全取引所の現在価格を取得
     */
    public Map<ExchangeType, BigDecimal> getCurrentPrices(String symbol) {
        Map<ExchangeType, CompletableFuture<BigDecimal>> futures = new HashMap<>();
        
        exchanges.entrySet().forEach(entry -> {
            futures.put(entry.getKey(),
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return entry.getValue().getCurrentPrice(symbol, "JPY");
                    } catch (Exception e) {
                        logger.warn("価格取得失敗: {}", entry.getKey(), e);
                        return BigDecimal.ZERO;
                    }
                }));
        });
        
        Map<ExchangeType, BigDecimal> prices = new HashMap<>();
        futures.entrySet().forEach(entry -> {
            try {
                BigDecimal price = entry.getValue().get();
                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    prices.put(entry.getKey(), price);
                }
            } catch (Exception e) {
                logger.warn("価格取得タイムアウト: {}", entry.getKey(), e);
            }
        });
        
        return prices;
    }
    
    /**
     * 裁定取引実行
     */
    private void executeArbitrage(ArbitrageAnalyzer.ArbitrageOpportunity opportunity) {
        logger.info("裁定取引実行: {}", opportunity);
        
        try {
            // 安い取引所で買い
            ExchangeApiClient buyExchange = exchanges.get(opportunity.getBuyExchange());
            OrderResult buyResult = buyExchange.placeMarketOrder(
                opportunity.getSymbol(), 
                opportunity.getAmount(), 
                OrderSide.BUY
            );
            
            if (buyResult.isSuccess()) {
                // 高い取引所で売り
                ExchangeApiClient sellExchange = exchanges.get(opportunity.getSellExchange());
                OrderResult sellResult = sellExchange.placeMarketOrder(
                    opportunity.getSymbol(),
                    opportunity.getAmount(),
                    OrderSide.SELL
                );
                
                if (sellResult.isSuccess()) {
                    logger.info("裁定取引完了: buy={}, sell={}", buyResult, sellResult);
                } else {
                    logger.error("裁定売り注文失敗: {}", sellResult);
                    // ロールバック処理が必要な場合はここに追加
                }
            } else {
                logger.error("裁定買い注文失敗: {}", buyResult);
            }
            
        } catch (Exception e) {
            logger.error("裁定取引実行エラー", e);
        }
    }
    
    /**
     * 利用可能な取引所リストを取得
     */
    public List<ExchangeType> getAvailableExchanges() {
        return exchanges.entrySet().stream()
            .filter(entry -> entry.getValue().isAvailable())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * 特定取引所のクライアントを取得
     */
    public ExchangeApiClient getExchangeClient(ExchangeType exchangeType) {
        return exchanges.get(exchangeType);
    }
}
