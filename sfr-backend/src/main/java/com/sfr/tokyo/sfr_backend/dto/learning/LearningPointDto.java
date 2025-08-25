package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningPoint.PointType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習ポイントDTO
 */
public class LearningPointDto {

    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    private Long spaceId;

    @NotNull(message = "ポイント数は必須です")
    @Min(value = 0, message = "ポイント数は0以上である必要があります")
    private Integer points;

    @NotNull(message = "ポイントタイプは必須です")
    private PointType pointType;

    private LocalDateTime lastEarnedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public LearningPointDto() {
    }

    // コンストラクタ
    public LearningPointDto(UUID userId, PointType pointType, Integer points) {
        this.userId = userId;
        this.pointType = pointType;
        this.points = points;
    }

    // コンストラクタ（学習空間指定）
    public LearningPointDto(UUID userId, Long spaceId, PointType pointType, Integer points) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.pointType = pointType;
        this.points = points;
    }

    // コンストラクタ（完全指定）
    public LearningPointDto(Long id, UUID userId, Long spaceId, Integer points,
            PointType pointType, LocalDateTime lastEarnedAt,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.spaceId = spaceId;
        this.points = points;
        this.pointType = pointType;
        this.lastEarnedAt = lastEarnedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 全体ポイントかどうかをチェック
     */
    public boolean isGlobalPoints() {
        return spaceId == null;
    }

    /**
     * 学習空間固有のポイントかどうかをチェック
     */
    public boolean isSpaceSpecificPoints() {
        return spaceId != null;
    }

    /**
     * 最近ポイントを獲得したかどうかをチェック（24時間以内）
     */
    public boolean isRecentlyEarned() {
        return lastEarnedAt != null &&
                lastEarnedAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public PointType getPointType() {
        return pointType;
    }

    public void setPointType(PointType pointType) {
        this.pointType = pointType;
    }

    public LocalDateTime getLastEarnedAt() {
        return lastEarnedAt;
    }

    public void setLastEarnedAt(LocalDateTime lastEarnedAt) {
        this.lastEarnedAt = lastEarnedAt;
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

    @Override
    public String toString() {
        return "LearningPointDto{" +
                "id=" + id +
                ", userId=" + userId +
                ", spaceId=" + spaceId +
                ", points=" + points +
                ", pointType=" + pointType +
                ", lastEarnedAt=" + lastEarnedAt +
                '}';
    }
}
