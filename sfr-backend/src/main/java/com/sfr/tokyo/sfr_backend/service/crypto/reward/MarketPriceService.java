package com.sfr.tokyo.sfr_backend.service.crypto.reward;

import com.sfr.tokyo.sfr_backend.entity.crypto.reward.MarketPriceHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市場価格サービス
 * M係数（市場状況係数）の管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MarketPriceService {

    // TODO: MarketPriceHistoryRepositoryを作成後に追加
    // private final MarketPriceHistoryRepository marketPriceHistoryRepository;

    private static final BigDecimal DEFAULT_TARGET_PRICE = new BigDecimal("150.00");
    private static final BigDecimal DEFAULT_CURRENT_PRICE = new BigDecimal("145.50");

    /**
     * 現在のM係数を取得
     *
     * @return M係数（市場状況係数）
     */
    public BigDecimal getCurrentMarketFactor() {
        BigDecimal currentPrice = getCurrentPrice();
        BigDecimal targetPrice = getTargetPrice();
        
        BigDecimal marketFactor = MarketPriceHistory.calculateMarketFactor(currentPrice, targetPrice);
        
        log.debug("M係数取得: currentPrice={}, targetPrice={}, marketFactor={}", 
                currentPrice, targetPrice, marketFactor);
        
        return marketFactor;
    }

    /**
     * 現在のSFR/JPY価格を取得
     *
     * @return 現在価格
     */
    public BigDecimal getCurrentPrice() {
        // TODO: データベースから最新価格を取得
        // 現在は固定値を返す
        return DEFAULT_CURRENT_PRICE;
    }

    /**
     * 目標価格を取得
     *
     * @return 目標価格
     */
    public BigDecimal getTargetPrice() {
        // TODO: 設定値から取得
        return DEFAULT_TARGET_PRICE;
    }

    /**
     * 市場価格を更新
     *
     * @param priceJpy SFR/JPY価格
     * @param priceSource 価格取得元
     * @param volume24h 24時間取引量
     * @param marketCap 時価総額
     * @return 作成された市場価格履歴
     */
    @Transactional
    public MarketPriceHistory updateMarketPrice(
            BigDecimal priceJpy,
            String priceSource,
            BigDecimal volume24h,
            BigDecimal marketCap) {
        
        log.info("市場価格更新: priceJpy={}, priceSource={}, volume24h={}, marketCap={}", 
                priceJpy, priceSource, volume24h, marketCap);

        MarketPriceHistory priceHistory = MarketPriceHistory.builder()
                .priceJpy(priceJpy)
                .priceSource(priceSource)
                .volume24h(volume24h)
                .marketCap(marketCap)
                .targetPrice(getTargetPrice())
                .priceTimestamp(LocalDateTime.now())
                .build();

        // M係数を自動計算
        priceHistory.calculateAndSetMarketFactor();

        // TODO: リポジトリで保存
        // return marketPriceHistoryRepository.save(priceHistory);

        return priceHistory;
    }

    /**
     * 指定期間の価格履歴を取得
     *
     * @param fromDate 開始日時
     * @param toDate 終了日時
     * @return 価格履歴のリスト
     */
    public java.util.List<MarketPriceHistory> getPriceHistory(LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("価格履歴取得: fromDate={}, toDate={}", fromDate, toDate);
        
        // TODO: リポジトリから履歴を取得
        // return marketPriceHistoryRepository.findByPriceTimestampBetweenOrderByPriceTimestampDesc(fromDate, toDate);
        
        return java.util.Collections.emptyList();
    }

    /**
     * 価格トレンド分析
     *
     * @param period 分析期間（日数）
     * @return 価格トレンド情報
     */
    public PriceTrendAnalysis analyzePriceTrend(int period) {
        log.debug("価格トレンド分析: period={} days", period);
        
        // LocalDateTime endDate = LocalDateTime.now();
        // LocalDateTime startDate = endDate.minusDays(period);
        
        // TODO: 実際のデータから分析
        // List<MarketPriceHistory> priceHistory = getPriceHistory(startDate, endDate);
        
        // 仮の分析結果
        BigDecimal currentPrice = getCurrentPrice();
        BigDecimal periodStartPrice = new BigDecimal("140.00");
        BigDecimal changeRate = currentPrice.subtract(periodStartPrice)
                .divide(periodStartPrice, 6, java.math.RoundingMode.HALF_UP);
        
        MarketPriceHistory.PriceTrend trend;
        if (changeRate.compareTo(new BigDecimal("0.02")) > 0) {
            trend = MarketPriceHistory.PriceTrend.UP;
        } else if (changeRate.compareTo(new BigDecimal("-0.02")) < 0) {
            trend = MarketPriceHistory.PriceTrend.DOWN;
        } else {
            trend = MarketPriceHistory.PriceTrend.STABLE;
        }
        
        return PriceTrendAnalysis.builder()
                .period(period)
                .startPrice(periodStartPrice)
                .endPrice(currentPrice)
                .changeRate(changeRate)
                .trend(trend)
                .averageVolume(new BigDecimal("50000"))
                .volatility(new BigDecimal("0.05"))
                .build();
    }

    /**
     * 価格に基づくM係数の詳細計算
     *
     * @param price 価格
     * @return M係数の詳細情報
     */
    public MarketFactorDetails calculateMarketFactorDetails(BigDecimal price) {
        BigDecimal targetPrice = getTargetPrice();
        BigDecimal marketFactor = MarketPriceHistory.calculateMarketFactor(price, targetPrice);
        
        BigDecimal ratio = price.divide(targetPrice, 4, java.math.RoundingMode.HALF_UP);
        
        String priceRange;
        String recommendation;
        
        if (ratio.compareTo(new BigDecimal("0.8")) < 0) {
            priceRange = "低価格帯（目標の80%未満）";
            recommendation = "報酬増額により参加促進";
        } else if (ratio.compareTo(new BigDecimal("1.0")) < 0) {
            priceRange = "適正価格帯（目標の80%-100%）";
            recommendation = "通常報酬での維持";
        } else if (ratio.compareTo(new BigDecimal("1.13")) < 0) {
            priceRange = "高価格帯（目標の100%-113%）";
            recommendation = "報酬抑制により供給調整";
        } else {
            priceRange = "過熱価格帯（目標の113%以上）";
            recommendation = "報酬大幅抑制により冷却";
        }
        
        return MarketFactorDetails.builder()
                .currentPrice(price)
                .targetPrice(targetPrice)
                .priceRatio(ratio)
                .marketFactor(marketFactor)
                .priceRange(priceRange)
                .recommendation(recommendation)
                .build();
    }

    /**
     * 価格トレンド分析結果クラス
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PriceTrendAnalysis {
        private int period;
        private BigDecimal startPrice;
        private BigDecimal endPrice;
        private BigDecimal changeRate;
        private MarketPriceHistory.PriceTrend trend;
        private BigDecimal averageVolume;
        private BigDecimal volatility;
    }

    /**
     * M係数詳細情報クラス
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketFactorDetails {
        private BigDecimal currentPrice;
        private BigDecimal targetPrice;
        private BigDecimal priceRatio;
        private BigDecimal marketFactor;
        private String priceRange;
        private String recommendation;
    }
}
