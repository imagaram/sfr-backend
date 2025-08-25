package com.sfr.tokyo.sfr_backend.dto.learning;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class LearningProgressDto {

    private Long id;

    @NotNull(message = "コンテンツIDは必須です")
    private Long contentId;

    @NotNull(message = "進捗パーセンテージは必須です")
    @DecimalMin(value = "0.0", message = "進捗は0%以上である必要があります")
    @DecimalMax(value = "100.0", message = "進捗は100%以下である必要があります")
    private BigDecimal progressPercent;

    private UUID userId;
    private String contentTitle;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public LearningProgressDto() {
    }

    // コンストラクタ（作成用）
    public LearningProgressDto(Long contentId, BigDecimal progressPercent) {
        this.contentId = contentId;
        this.progressPercent = progressPercent;
    }

    // コンストラクタ（レスポンス用）
    public LearningProgressDto(Long id, Long contentId, BigDecimal progressPercent,
            UUID userId, String contentTitle, LocalDateTime completedAt,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.contentId = contentId;
        this.progressPercent = progressPercent;
        this.userId = userId;
        this.contentTitle = contentTitle;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 完了判定メソッド
    public boolean isCompleted() {
        return progressPercent != null && progressPercent.compareTo(BigDecimal.valueOf(100)) == 0;
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public BigDecimal getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(BigDecimal progressPercent) {
        this.progressPercent = progressPercent;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
