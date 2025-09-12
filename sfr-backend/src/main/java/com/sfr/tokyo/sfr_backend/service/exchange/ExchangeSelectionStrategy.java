package com.sfr.tokyo.sfr_backend.service.exchange;

import java.util.Map;

/**
 * 取引所選択戦略インターフェース
 * Phase 4: 取引所API統合対応
 */
public interface ExchangeSelectionStrategy {
    
    /**
     * 最適な取引所を選択
     * @param metrics 取引所メトリクス
     * @param operation 実行する操作
     * @return 選択された取引所タイプ
     */
    ExchangeType selectBestExchange(Map<ExchangeType, ExchangeMetrics> metrics, LiquidityOperation operation);
}


