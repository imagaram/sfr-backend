package com.sfr.tokyo.sfr_backend.dto;

import com.sfr.tokyo.sfr_backend.entity.TransactionType;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class WalletEntryDTO {
    private Long id;

    @NotNull(message = "teamIdは必須です")
    private Long teamId;

    @NotNull(message = "金額は必須です")
    @Positive(message = "金額は正の値である必要があります")
    private Double amount;

    @NotNull(message = "説明は必須です")
    @Size(min = 1, max = 255, message = "説明は1〜255文字で入力してください")
    private String description;

    @NotNull(message = "取引タイプは必須です")
    private TransactionType transactionType;

    private LocalDateTime timestamp;

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
