package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningBadge.BadgeType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習バッジDTO
 */
public class LearningBadgeDto {

    private Long id;

    @NotBlank(message = "バッジ名は必須です")
    @Size(min = 1, max = 100, message = "バッジ名は1文字以上100文字以下である必要があります")
    private String name;

    @Size(max = 1000, message = "説明は1000文字以下である必要があります")
    private String description;

    @Size(max = 500, message = "アイコンURLは500文字以下である必要があります")
    private String iconUrl;

    @NotNull(message = "バッジタイプは必須です")
    private BadgeType badgeType;

    @Min(value = 0, message = "必要値は0以上である必要があります")
    private Integer requiredValue;

    private UUID spaceId;

    private Boolean isActive = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public LearningBadgeDto() {
    }

    // コンストラクタ
    public LearningBadgeDto(String name, String description, BadgeType badgeType) {
        this.name = name;
        this.description = description;
        this.badgeType = badgeType;
    }

    // コンストラクタ（完全指定）
    public LearningBadgeDto(Long id, String name, String description, String iconUrl,
            BadgeType badgeType, Integer requiredValue, UUID spaceId,
            Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.badgeType = badgeType;
        this.requiredValue = requiredValue;
        this.spaceId = spaceId;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 学習空間固有のバッジかどうかをチェック
     */
    public boolean isSpaceSpecific() {
        return spaceId != null;
    }

    /**
     * バッジが有効かどうかをチェック
     */
    public boolean isActiveBadge() {
        return Boolean.TRUE.equals(isActive);
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public BadgeType getBadgeType() {
        return badgeType;
    }

    public void setBadgeType(BadgeType badgeType) {
        this.badgeType = badgeType;
    }

    public Integer getRequiredValue() {
        return requiredValue;
    }

    public void setRequiredValue(Integer requiredValue) {
        this.requiredValue = requiredValue;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(UUID spaceId) {
        this.spaceId = spaceId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
        return "LearningBadgeDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", badgeType=" + badgeType +
                ", requiredValue=" + requiredValue +
                ", spaceId=" + spaceId +
                ", isActive=" + isActive +
                '}';
    }
}
