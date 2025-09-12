package com.sfr.tokyo.sfr_backend.service.exchange;

import com.sfr.tokyo.sfr_backend.service.crypto.SfrtRewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * SFRT流動性管理・価格安定化サービス
 * Phase 4: 取引所API統合・グローバル展開準備
 * 
 * アルゴリズミック流動性管理による価格安定化を実現
 */
@Service
public class SfrtLiquidityService {
    
    private static final Logger logger = LoggerFactory.getLogger(SfrtLiquidityService.class);
    
    private static final BigDecimal PRICE_TOLERANCE = new BigDecimal("0.05"); // ±5%
    private static final BigDecimal MAX_SINGLE_OPERATION = new BigDecimal("1000000"); // 100万円相当
    private static final BigDecimal TARGET_PRICE_JPY = new BigDecimal("150.00"); // 1SFRT = 150円目標
    
    @Autowired
    @Qualifier("phase4MultiExchangeManager")
    private Phase4MultiExchangeManager multiExchangeManager;
    
    @Autowired
    private SfrtRewardService sfrtRewardService;
    
    @Autowired
    private SfrtRiskManagementService riskManagementService;
    
    private volatile boolean automaticTradingEnabled = true;
    
    /**
     * 流動性管理メイン処理（1分間隔）
     */
    @Scheduled(fixedRate = 60000)
    public void manageLiquidity() {
        if (!automaticTradingEnabled) {
            logger.debug("自動取引が一時停止中です");
            return;
        }
        
        logger.debug("流動性管理処理開始");
        
        try {
            // 1. 市場データ集約
            SfrtMarketData marketData = aggregateMarketData();
            
            if (marketData == null || !marketData.isValid()) {
                logger.warn("市場データが不正または取得できません");
                return;
            }
            
            // 2. 流動性戦略決定
            LiquidityDecision decision = makeLiquidityDecision(marketData);
            
            // 3. 戦略実行
            executeLiquidityDecision(decision);
            
            logger.debug("流動性管理処理完了: {}", decision);
            
        } catch (Exception e) {
            logger.error("流動性管理処理中エラー", e);
        }
    }
    
    /**
     * 市場データ集約
     */
    private SfrtMarketData aggregateMarketData() {
        try {
            // 全取引所の価格データ取得
            var prices = multiExchangeManager.getCurrentPrices("SFRT/JPY");
            
            if (prices.isEmpty()) {
                return null;
            }
            
            // 平均価格計算
            BigDecimal averagePrice = prices.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
            
            // 最低・最高価格
            BigDecimal minPrice = prices.values().stream()
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = prices.values().stream()
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            
            // 価格変動率計算
            BigDecimal priceVolatility = BigDecimal.ZERO;
            if (minPrice.compareTo(BigDecimal.ZERO) > 0) {
                priceVolatility = maxPrice.subtract(minPrice).divide(minPrice, 4, RoundingMode.HALF_UP);
            }
            
            return new SfrtMarketData(averagePrice, minPrice, maxPrice, priceVolatility, prices.size());
            
        } catch (Exception e) {
            logger.error("市場データ集約エラー", e);
            return null;
        }
    }
    
    /**
     * 流動性判断戦略
     */
    private LiquidityDecision makeLiquidityDecision(SfrtMarketData marketData) {
        BigDecimal currentPrice = marketData.getAveragePrice();
        BigDecimal priceDeviation = currentPrice.subtract(TARGET_PRICE_JPY).divide(TARGET_PRICE_JPY, 4, RoundingMode.HALF_UP);
        
        logger.debug("価格分析: current={}, target={}, deviation={}%", 
                    currentPrice, TARGET_PRICE_JPY, priceDeviation.multiply(BigDecimal.valueOf(100)));
        
        // 価格安定化が必要な場合
        if (priceDeviation.abs().compareTo(PRICE_TOLERANCE) > 0) {
            if (priceDeviation.compareTo(BigDecimal.ZERO) > 0) {
                // 価格高騰時：売却による安定化
                BigDecimal sellAmount = calculateSellAmount(priceDeviation);
                return new LiquidityDecision(LiquidityAction.STABILIZE_PRICE_SELL, sellAmount, 
                                           "価格高騰による安定化売却");
            } else {
                // 価格下落時：買い支えによる安定化
                BigDecimal buyAmount = calculateBuyAmount(priceDeviation.abs());
                return new LiquidityDecision(LiquidityAction.STABILIZE_PRICE_BUY, buyAmount,
                                           "価格下落による安定化買取");
            }
        }
        
        // 流動性提供が必要な場合
        if (marketData.getActiveExchanges() < 2) {
            return new LiquidityDecision(LiquidityAction.PROVIDE_LIQUIDITY, 
                                       new BigDecimal("500000"), "流動性提供");
        }
        
        // 通常状態：準備金維持
        return new LiquidityDecision(LiquidityAction.MAINTAIN_RESERVE, 
                                   BigDecimal.ZERO, "通常状態維持");
    }
    
    /**
     * 売却量計算
     */
    private BigDecimal calculateSellAmount(BigDecimal priceDeviation) {
        // 価格偏差に比例した売却量（最大100万円相当）
        BigDecimal baseAmount = new BigDecimal("200000"); // 基本20万円
        BigDecimal adjustedAmount = baseAmount.multiply(priceDeviation.multiply(BigDecimal.valueOf(10)));
        return adjustedAmount.min(MAX_SINGLE_OPERATION);
    }
    
    /**
     * 買取量計算
     */
    private BigDecimal calculateBuyAmount(BigDecimal priceDeviation) {
        // 価格偏差に比例した買取量（最大100万円相当）
        BigDecimal baseAmount = new BigDecimal("300000"); // 基本30万円
        BigDecimal adjustedAmount = baseAmount.multiply(priceDeviation.multiply(BigDecimal.valueOf(8)));
        return adjustedAmount.min(MAX_SINGLE_OPERATION);
    }
    
    /**
     * 流動性決定実行
     */
    private void executeLiquidityDecision(LiquidityDecision decision) {
        switch (decision.getAction()) {
            case STABILIZE_PRICE_SELL:
                executeStabilizationSell(decision);
                break;
            case STABILIZE_PRICE_BUY:
                executeStabilizationBuy(decision);
                break;
            case PROVIDE_LIQUIDITY:
                provideLiquidity(decision);
                break;
            case MAINTAIN_RESERVE:
                maintainReserve(decision);
                break;
        }
    }
    
    /**
     * 価格安定化売却実行
     */
    private void executeStabilizationSell(LiquidityDecision decision) {
        logger.info("価格安定化売却開始: amount={}", decision.getAmount());
        
        try {
            // 段階的売却プログラム実行
            CompletableFuture.runAsync(() -> executeSellProgram(decision.getAmount()));
            
        } catch (Exception e) {
            logger.error("安定化売却エラー", e);
        }
    }
    
    /**
     * 価格安定化買取実行
     */
    private void executeStabilizationBuy(LiquidityDecision decision) {
        logger.info("価格安定化買取開始: amount={}", decision.getAmount());
        
        try {
            // 段階的買取プログラム実行
            CompletableFuture.runAsync(() -> executeBuyProgram(decision.getAmount()));
            
        } catch (Exception e) {
            logger.error("安定化買取エラー", e);
        }
    }
    
    /**
     * 段階的売却プログラム
     */
    private void executeSellProgram(BigDecimal totalAmount) {
        BigDecimal remaining = totalAmount;
        int intervals = 10; // 10回に分けて実行
        
        logger.info("段階的売却開始: totalAmount={}, intervals={}", totalAmount, intervals);
        
        while (remaining.compareTo(BigDecimal.ZERO) > 0 && intervals > 0) {
            BigDecimal chunkSize = remaining.divide(BigDecimal.valueOf(intervals), 2, RoundingMode.HALF_UP);
            
            try {
                LiquidityOperation operation = LiquidityOperation.marketSell("SFRT/JPY", chunkSize, "価格安定化");
                LiquidityResult result = multiExchangeManager.executeLiquidityOperation(operation);
                
                if (result.isSuccess()) {
                    remaining = remaining.subtract(chunkSize);
                    intervals--;
                    
                    logger.info("売却実行成功: chunk={}, remaining={}", chunkSize, remaining);
                    
                    // 市場への影響を最小化するため待機
                    Thread.sleep(30000); // 30秒待機
                } else {
                    logger.error("売却実行失敗: {}", result.getErrorMessage());
                    break; // 失敗時は中断
                }
                
            } catch (Exception e) {
                logger.error("売却実行中例外", e);
                break;
            }
        }
        
        logger.info("段階的売却完了: executed={}", totalAmount.subtract(remaining));
    }
    
    /**
     * 段階的買取プログラム
     */
    private void executeBuyProgram(BigDecimal totalAmount) {
        BigDecimal remaining = totalAmount;
        int intervals = 8; // 8回に分けて実行
        
        logger.info("段階的買取開始: totalAmount={}, intervals={}", totalAmount, intervals);
        
        while (remaining.compareTo(BigDecimal.ZERO) > 0 && intervals > 0) {
            BigDecimal chunkSize = remaining.divide(BigDecimal.valueOf(intervals), 2, RoundingMode.HALF_UP);
            
            try {
                LiquidityOperation operation = LiquidityOperation.marketBuy("SFRT/JPY", chunkSize, "価格安定化");
                LiquidityResult result = multiExchangeManager.executeLiquidityOperation(operation);
                
                if (result.isSuccess()) {
                    remaining = remaining.subtract(chunkSize);
                    intervals--;
                    
                    logger.info("買取実行成功: chunk={}, remaining={}", chunkSize, remaining);
                    
                    // 市場への影響を最小化するため待機
                    Thread.sleep(45000); // 45秒待機
                } else {
                    logger.error("買取実行失敗: {}", result.getErrorMessage());
                    break; // 失敗時は中断
                }
                
            } catch (Exception e) {
                logger.error("買取実行中例外", e);
                break;
            }
        }
        
        logger.info("段階的買取完了: executed={}", totalAmount.subtract(remaining));
    }
    
    /**
     * 流動性提供
     */
    private void provideLiquidity(LiquidityDecision decision) {
        logger.info("流動性提供: {}", decision);
        // 実装省略 - 複数取引所への指値注文配置等
    }
    
    /**
     * 準備金維持
     */
    private void maintainReserve(LiquidityDecision decision) {
        logger.debug("準備金維持モード: {}", decision);
        // 実装省略 - 準備金比率チェック等
    }
    
    /**
     * 自動取引の一時停止
     */
    public void pauseAutomaticTrading() {
        this.automaticTradingEnabled = false;
        logger.warn("自動取引を一時停止しました");
    }
    
    /**
     * 自動取引の再開
     */
    public void resumeAutomaticTrading() {
        this.automaticTradingEnabled = true;
        logger.info("自動取引を再開しました");
    }
    
    /**
     * 自動取引状態取得
     */
    public boolean isAutomaticTradingEnabled() {
        return automaticTradingEnabled;
    }
}
