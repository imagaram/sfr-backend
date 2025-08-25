package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ユーザーバッジ獲得記録エンティティ
 * ユーザーが獲得したバッジの履歴を管理
 */
@Entity
@Table(name = "learning_user_badge", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "badge_id" }))
public class LearningUserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private LearningBadge badge;

    @Column(name = "earned_value")
    private Integer earnedValue;

    @Column(name = "earned_reason", length = 500)
    private String earnedReason;

    @CreationTimestamp
    @Column(name = "earned_at", nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    // デフォルトコンストラクタ
    public LearningUserBadge() {
    }

    // コンストラクタ
    public LearningUserBadge(UUID userId, LearningBadge badge) {
        this.userId = userId;
        this.badge = badge;
    }

    // コンストラクタ（詳細情報付き）
    public LearningUserBadge(UUID userId, LearningBadge badge, Integer earnedValue, String earnedReason) {
        this.userId = userId;
        this.badge = badge;
        this.earnedValue = earnedValue;
        this.earnedReason = earnedReason;
    }

    /**
     * バッジ獲得が最近かどうかをチェック（7日以内）
     */
    public boolean isRecentlyEarned() {
        return earnedAt != null &&
                earnedAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    /**
     * バッジ獲得の詳細情報があるかどうかをチェック
     */
    public boolean hasEarnedDetails() {
        return earnedValue != null || earnedReason != null;
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

    public LearningBadge getBadge() {
        return badge;
    }

    public void setBadge(LearningBadge badge) {
        this.badge = badge;
    }

    public Integer getEarnedValue() {
        return earnedValue;
    }

    public void setEarnedValue(Integer earnedValue) {
        this.earnedValue = earnedValue;
    }

    public String getEarnedReason() {
        return earnedReason;
    }

    public void setEarnedReason(String earnedReason) {
        this.earnedReason = earnedReason;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }

    @Override
    public String toString() {
        return "LearningUserBadge{" +
                "id=" + id +
                ", userId=" + userId +
                ", badge=" + (badge != null ? badge.getName() : null) +
                ", earnedValue=" + earnedValue +
                ", earnedAt=" + earnedAt +
                '}';
    }
}
