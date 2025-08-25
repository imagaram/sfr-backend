package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningRanking.RankingType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習ランキングDTO
 */
public class LearningRankingDto {

    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    private Long spaceId;

    @NotNull(message = "ランキングタイプは必須です")
    private RankingType rankingType;

    @NotNull(message = "ランク順位は必須です")
    @Min(value = 1, message = "ランク順位は1以上である必要があります")
    private Integer rankPosition;

    @NotNull(message = "スコアは必須です")
    @DecimalMin(value = "0.0", message = "スコアは0以上である必要があります")
    private BigDecimal score;

    @Min(value = 0, message = "総ポイントは0以上である必要があります")
    private Integer totalPoints;

    @Min(value = 0, message = "バッジ数は0以上である必要があります")
    private Integer badgeCount;

    @Min(value = 0, message = "完了コンテンツ数は0以上である必要があります")
    private Integer contentCompleted;

    @Min(value = 0, message = "完了クイズ数は0以上である必要があります")
    private Integer quizCompleted;

    @Min(value = 0, message = "連続日数は0以上である必要があります")
    private Integer streakDays;

    private LocalDateTime lastActivity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ユーザー情報（結合表示用）
    private String userName;
    private String userFirstName;
    private String userLastName;

    // ランク変動情報
    private Integer previousRank;
    private Integer rankChange;

    // デフォルトコンストラクタ
    public LearningRankingDto() {
    }

    // コンストラクタ（基本情報）
    public LearningRankingDto(UUID userId, RankingType rankingType, Integer rankPosition, BigDecimal score) {
        this.userId = userId;
        this.rankingType = rankingType;
        this.rankPosition = rankPosition;
        this.score = score;
    }

    // コンストラクタ（詳細情報付き）
    public LearningRankingDto(UUID userId, Long spaceId, RankingType rankingType,
            Integer rankPosition, BigDecimal score, Integer totalPoints,
            Integer badgeCount, String userName) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.rankingType = rankingType;
        this.rankPosition = rankPosition;
        this.score = score;
        this.totalPoints = totalPoints;
        this.badgeCount = badgeCount;
        this.userName = userName;
    }

    /**
     * ランクアップしたかどうかを判定
     */
    public boolean hasRankedUp() {
        return previousRank != null && rankPosition < previousRank;
    }

    /**
     * ランクダウンしたかどうかを判定
     */
    public boolean hasRankedDown() {
        return previousRank != null && rankPosition > previousRank;
    }

    /**
     * ランク変動計算
     */
    public void calculateRankChange() {
        if (previousRank != null) {
            this.rankChange = previousRank - rankPosition;
        }
    }

    /**
     * トップ10入りかどうかを判定
     */
    public boolean isTopTen() {
        return rankPosition != null && rankPosition <= 10;
    }

    /**
     * トップ3入りかどうかを判定
     */
    public boolean isTopThree() {
        return rankPosition != null && rankPosition <= 3;
    }

    /**
     * スペース特定ランキングかどうかを判定
     */
    public boolean isSpaceSpecific() {
        return spaceId != null;
    }

    /**
     * ユーザー表示名を取得
     */
    public String getDisplayName() {
        if (userName != null)
            return userName;
        if (userFirstName != null && userLastName != null) {
            return userFirstName + " " + userLastName;
        }
        if (userFirstName != null)
            return userFirstName;
        return "ユーザー" + userId.toString().substring(0, 8);
    }

    /**
     * ランク変動メッセージを取得
     */
    public String getRankChangeMessage() {
        if (rankChange == null || rankChange == 0)
            return "変動なし";
        if (rankChange > 0)
            return "↑" + rankChange + "位上昇";
        return "↓" + Math.abs(rankChange) + "位下降";
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public Integer getPreviousRank() {
        return previousRank;
    }

    public void setPreviousRank(Integer previousRank) {
        this.previousRank = previousRank;
        calculateRankChange();
    }

    public Integer getRankChange() {
        return rankChange;
    }

    public void setRankChange(Integer rankChange) {
        this.rankChange = rankChange;
    }

    @Override
    public String toString() {
        return "LearningRankingDto{" +
                "id=" + id +
                ", userId=" + userId +
                ", rankingType=" + rankingType +
                ", rankPosition=" + rankPosition +
                ", score=" + score +
                ", userName='" + userName + '\'' +
                ", rankChange=" + rankChange +
                '}';
    }
}
