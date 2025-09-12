package com.sfr.tokyo.sfr_backend.service.exchange;

/**
 * 注文状況列挙型
 * Phase 4: 取引所API統合対応
 */
public enum OrderStatus {
    
    PENDING("待機中", "pending"),
    SUBMITTED("発注済み", "submitted"),
    PARTIALLY_FILLED("部分約定", "partially_filled"),
    FILLED("約定済み", "filled"),
    CANCELLED("キャンセル済み", "cancelled"),
    REJECTED("拒否", "rejected"),
    FAILED("失敗", "failed"),
    EXPIRED("期限切れ", "expired");
    
    private final String displayName;
    private final String apiValue;
    
    OrderStatus(String displayName, String apiValue) {
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
     * 完了状態かどうか判定
     */
    public boolean isComplete() {
        return this == FILLED || this == CANCELLED || this == REJECTED || 
               this == FAILED || this == EXPIRED;
    }
    
    /**
     * 成功状態かどうか判定
     */
    public boolean isSuccess() {
        return this == FILLED || this == PARTIALLY_FILLED;
    }
    
    /**
     * 失敗状態かどうか判定
     */
    public boolean isFailure() {
        return this == REJECTED || this == FAILED;
    }
    
    /**
     * アクティブ状態かどうか判定
     */
    public boolean isActive() {
        return this == PENDING || this == SUBMITTED || this == PARTIALLY_FILLED;
    }
}
