package com.sfr.tokyo.sfr_backend.dto;

public class WalletBalanceDTO {
    private Long walletId;
    private Double totalBalance;

    // Getter/Setter
    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(Double totalBalance) {
        this.totalBalance = totalBalance;
    }
}
