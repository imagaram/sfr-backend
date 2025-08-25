package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習ポイントエンティティ
 * ユーザーの学習活動によって獲得されるポイントを管理
 */
@Entity
@Table(name = "learning_point", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "space_id",
        "point_type" }))
public class LearningPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "points", nullable = false)
    private Integer points = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false)
    private PointType pointType;

    @Column(name = "last_earned_at")
    private LocalDateTime lastEarnedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ポイントタイプ列挙型
     */
    public enum PointType {
        TOTAL("総合ポイント"),
        QUIZ("クイズポイント"),
        CONTENT("学習コンテンツポイント"),
        PARTICIPATION("参加ポイント"),
        STREAK("連続学習ポイント"),
        BONUS("ボーナスポイント");

        private final String displayName;

        PointType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // デフォルトコンストラクタ
    public LearningPoint() {
    }

    // コンストラクタ
    public LearningPoint(UUID userId, PointType pointType) {
        this.userId = userId;
        this.pointType = pointType;
    }

    // コンストラクタ（学習空間指定）
    public LearningPoint(UUID userId, Long spaceId, PointType pointType) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.pointType = pointType;
    }

    // コンストラクタ（完全指定）
    public LearningPoint(UUID userId, Long spaceId, PointType pointType, Integer points) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.pointType = pointType;
        this.points = points;
    }

    /**
     * ポイントを追加
     */
    public void addPoints(Integer pointsToAdd) {
        if (pointsToAdd != null && pointsToAdd > 0) {
            this.points = (this.points == null ? 0 : this.points) + pointsToAdd;
            this.lastEarnedAt = LocalDateTime.now();
        }
    }

    /**
     * ポイントを減算（0未満にはならない）
     */
    public void subtractPoints(Integer pointsToSubtract) {
        if (pointsToSubtract != null && pointsToSubtract > 0) {
            this.points = Math.max(0, (this.points == null ? 0 : this.points) - pointsToSubtract);
        }
    }

    /**
     * ポイントを設定（直接設定）
     */
    public void setPointsDirectly(Integer newPoints) {
        this.points = newPoints == null ? 0 : Math.max(0, newPoints);
        this.lastEarnedAt = LocalDateTime.now();
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
        return "LearningPoint{" +
                "id=" + id +
                ", userId=" + userId +
                ", spaceId=" + spaceId +
                ", points=" + points +
                ", pointType=" + pointType +
                ", lastEarnedAt=" + lastEarnedAt +
                '}';
    }
}
