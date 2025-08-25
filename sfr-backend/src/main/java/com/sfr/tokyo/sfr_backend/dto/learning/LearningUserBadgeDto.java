package com.sfr.tokyo.sfr_backend.dto.learning;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ユーザーバッジ獲得記録DTO
 */
public class LearningUserBadgeDto {

    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @NotNull(message = "バッジIDは必須です")
    private Long badgeId;

    private String badgeName;

    private String badgeDescription;

    private String badgeIconUrl;

    @Min(value = 0, message = "獲得値は0以上である必要があります")
    private Integer earnedValue;

    @Size(max = 500, message = "獲得理由は500文字以下である必要があります")
    private String earnedReason;

    private LocalDateTime earnedAt;

    // デフォルトコンストラクタ
    public LearningUserBadgeDto() {
    }

    // コンストラクタ
    public LearningUserBadgeDto(UUID userId, Long badgeId) {
        this.userId = userId;
        this.badgeId = badgeId;
    }

    // コンストラクタ（詳細情報付き）
    public LearningUserBadgeDto(UUID userId, Long badgeId, Integer earnedValue, String earnedReason) {
        this.userId = userId;
        this.badgeId = badgeId;
        this.earnedValue = earnedValue;
        this.earnedReason = earnedReason;
    }

    // コンストラクタ（完全指定）
    public LearningUserBadgeDto(Long id, UUID userId, Long badgeId, String badgeName,
            String badgeDescription, String badgeIconUrl,
            Integer earnedValue, String earnedReason, LocalDateTime earnedAt) {
        this.id = id;
        this.userId = userId;
        this.badgeId = badgeId;
        this.badgeName = badgeName;
        this.badgeDescription = badgeDescription;
        this.badgeIconUrl = badgeIconUrl;
        this.earnedValue = earnedValue;
        this.earnedReason = earnedReason;
        this.earnedAt = earnedAt;
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

    /**
     * バッジ情報が完全に含まれているかどうかをチェック
     */
    public boolean hasBadgeDetails() {
        return badgeName != null;
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

    public Long getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Long badgeId) {
        this.badgeId = badgeId;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public String getBadgeDescription() {
        return badgeDescription;
    }

    public void setBadgeDescription(String badgeDescription) {
        this.badgeDescription = badgeDescription;
    }

    public String getBadgeIconUrl() {
        return badgeIconUrl;
    }

    public void setBadgeIconUrl(String badgeIconUrl) {
        this.badgeIconUrl = badgeIconUrl;
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
        return "LearningUserBadgeDto{" +
                "id=" + id +
                ", userId=" + userId +
                ", badgeId=" + badgeId +
                ", badgeName='" + badgeName + '\'' +
                ", earnedValue=" + earnedValue +
                ", earnedAt=" + earnedAt +
                '}';
    }
}
