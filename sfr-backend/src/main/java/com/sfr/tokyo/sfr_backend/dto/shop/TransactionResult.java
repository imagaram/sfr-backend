package com.sfr.tokyo.sfr_backend.dto.shop;

/**
 * SFR決済処理結果クラス
 * Phase 2.2: SFR決済システム
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
public class TransactionResult {
    private final boolean success;
    private final String errorMessage;
    private final String transactionId;

    private TransactionResult(boolean success, String errorMessage, String transactionId) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.transactionId = transactionId;
    }

    public static TransactionResult success(String transactionId) {
        return new TransactionResult(true, null, transactionId);
    }

    public static TransactionResult failure(String errorMessage) {
        return new TransactionResult(false, errorMessage, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
