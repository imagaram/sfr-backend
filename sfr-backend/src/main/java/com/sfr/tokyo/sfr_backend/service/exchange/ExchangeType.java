package com.sfr.tokyo.sfr_backend.service.exchange;

/**
 * 取引所タイプ列挙型
 * Phase 4: 取引所API統合対応
 */
public enum ExchangeType {
    
    // 日本国内取引所
    BITBANK("Bitbank", "JP", "bitbank"),
    COINCHECK("Coincheck", "JP", "coincheck"),
    BITFLYER("bitFlyer", "JP", "bitflyer"),
    GMO_COIN("GMOコイン", "JP", "gmocoin"),
    
    // 国際取引所
    BINANCE("Binance", "GLOBAL", "binance"),
    BYBIT("Bybit", "GLOBAL", "bybit"),
    OKEX("OKEx", "GLOBAL", "okex"),
    HUOBI("Huobi", "GLOBAL", "huobi"),
    
    // DEX（分散型取引所）- 将来拡張用
    UNISWAP("Uniswap", "DEX", "uniswap"),
    SUSHISWAP("SushiSwap", "DEX", "sushiswap"),
    
    // テスト・モック用
    MOCK_EXCHANGE("Mock Exchange", "TEST", "mock");
    
    private final String displayName;
    private final String region;
    private final String apiPrefix;
    
    ExchangeType(String displayName, String region, String apiPrefix) {
        this.displayName = displayName;
        this.region = region;
        this.apiPrefix = apiPrefix;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getRegion() {
        return region;
    }
    
    public String getApiPrefix() {
        return apiPrefix;
    }
    
    /**
     * 日本国内取引所かどうか判定
     */
    public boolean isJapanese() {
        return "JP".equals(region);
    }
    
    /**
     * グローバル取引所かどうか判定
     */
    public boolean isGlobal() {
        return "GLOBAL".equals(region);
    }
    
    /**
     * DEX（分散型取引所）かどうか判定
     */
    public boolean isDex() {
        return "DEX".equals(region);
    }
    
    /**
     * テスト用取引所かどうか判定
     */
    public boolean isTest() {
        return "TEST".equals(region);
    }
}
