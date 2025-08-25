package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習ランキングエンティティ
 * ユーザーの学習成果に基づく順位情報を管理
 */
@Entity
@Table(name = "learning_ranking", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "space_id",
        "ranking_type" }))
public class LearningRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "space_id")
    private Long spaceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ranking_type", nullable = false)
    private RankingType rankingType;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Column(name = "score", nullable = false, precision = 10, scale = 2)
    private BigDecimal score;

    @Column(name = "total_points")
    private Integer totalPoints;

    @Column(name = "badge_count")
    private Integer badgeCount;

    @Column(name = "content_completed")
    private Integer contentCompleted;

    @Column(name = "quiz_completed")
    private Integer quizCompleted;

    @Column(name = "streak_days")
    private Integer streakDays;

    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ランキングタイプ列挙型
     */
    public enum RankingType {
        POINTS("ポイントランキング"),
        BADGES("バッジランキング"),
        CONTENT_PROGRESS("コンテンツ進捗ランキング"),
        QUIZ_MASTERY("クイズ習熟度ランキング"),
        OVERALL("総合ランキング"),
        STREAK("連続学習ランキング"),
        WEEKLY("週間ランキング"),
        MONTHLY("月間ランキング");

        private final String displayName;

        RankingType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // デフォルトコンストラクタ
    public LearningRanking() {
    }

    // コンストラクタ（基本情報）
    public LearningRanking(UUID userId, RankingType rankingType, Integer rankPosition, BigDecimal score) {
        this.userId = userId;
        this.rankingType = rankingType;
        this.rankPosition = rankPosition;
        this.score = score;
        this.lastActivity = LocalDateTime.now();
    }

    // コンストラクタ（スペース指定）
    public LearningRanking(UUID userId, Long spaceId, RankingType rankingType,
            Integer rankPosition, BigDecimal score) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.rankingType = rankingType;
        this.rankPosition = rankPosition;
        this.score = score;
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * ランクアップ判定
     */
    public boolean hasRankedUp(Integer previousPosition) {
        return previousPosition != null && this.rankPosition < previousPosition;
    }

    /**
     * ランクダウン判定
     */
    public boolean hasRankedDown(Integer previousPosition) {
        return previousPosition != null && this.rankPosition > previousPosition;
    }

    /**
     * トップ10入り判定
     */
    public boolean isTopTen() {
        return rankPosition != null && rankPosition <= 10;
    }

    /**
     * トップ3入り判定
     */
    public boolean isTopThree() {
        return rankPosition != null && rankPosition <= 3;
    }

    /**
     * スペース特定ランキングかどうか
     */
    public boolean isSpaceSpecific() {
        return spaceId != null;
    }

    /**
     * ランク情報更新
     */
    public void updateRanking(Integer newPosition, BigDecimal newScore) {
        this.rankPosition = newPosition;
        this.score = newScore;
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * 統計情報更新
     */
    public void updateStats(Integer totalPoints, Integer badgeCount, Integer contentCompleted,
            Integer quizCompleted, Integer streakDays) {
        this.totalPoints = totalPoints;
        this.badgeCount = badgeCount;
        this.contentCompleted = contentCompleted;
        this.quizCompleted = quizCompleted;
        this.streakDays = streakDays;
        this.lastActivity = LocalDateTime.now();
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

    public RankingType getRankingType() {
        return rankingType;
    }

    public void setRankingType(RankingType rankingType) {
        this.rankingType = rankingType;
    }

    public Integer getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(Integer badgeCount) {
        this.badgeCount = badgeCount;
    }

    public Integer getContentCompleted() {
        return contentCompleted;
    }

    public void setContentCompleted(Integer contentCompleted) {
        this.contentCompleted = contentCompleted;
    }

    public Integer getQuizCompleted() {
        return quizCompleted;
    }

    public void setQuizCompleted(Integer quizCompleted) {
        this.quizCompleted = quizCompleted;
    }

    public Integer getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(Integer streakDays) {
        this.streakDays = streakDays;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
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
        return "LearningRanking{" +
                "id=" + id +
                ", userId=" + userId +
                ", spaceId=" + spaceId +
                ", rankingType=" + rankingType +
                ", rankPosition=" + rankPosition +
                ", score=" + score +
                ", totalPoints=" + totalPoints +
                ", badgeCount=" + badgeCount +
                ", lastActivity=" + lastActivity +
                '}';
    }
}
