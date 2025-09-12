package com.sfr.tokyo.sfr_backend.service.exchange;

/**
 * 売買区分列挙型
 * Phase 4: 取引所API統合対応
 */
public enum OrderSide {
    
    BUY("買い", "buy"),
    SELL("売り", "sell");
    
    private final String displayName;
    private final String apiValue;
    
    OrderSide(String displayName, String apiValue) {
        this.displayName = displayName;
        this.apiValue = apiValue;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getApiValue() {
        return apiValue;
    }
    
    /**
     * 逆の売買区分を取得
     */
    public OrderSide opposite() {
        return this == BUY ? SELL : BUY;
    }
}
