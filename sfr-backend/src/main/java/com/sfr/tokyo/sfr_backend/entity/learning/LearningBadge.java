package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習バッジエンティティ
 * ユーザーが獲得できる各種バッジの定義を管理
 */
@Entity
@Table(name = "learning_badge")
public class LearningBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false)
    private BadgeType badgeType;

    @Column(name = "required_value")
    private Integer requiredValue;

    @Column(name = "space_id", columnDefinition = "CHAR(36)")
    private UUID spaceId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * バッジタイプ列挙型
     */
    public enum BadgeType {
        PROGRESS("学習進捗"),
        QUIZ("クイズ達成"),
        CONTENT("コンテンツ完了"),
        POINTS("ポイント獲得"),
        STREAK("連続学習"),
        PARTICIPATION("参加"),
        SPECIAL("特別バッジ");

        private final String displayName;

        BadgeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // デフォルトコンストラクタ
    public LearningBadge() {
    }

    // コンストラクタ
    public LearningBadge(String name, String description, BadgeType badgeType) {
        this.name = name;
        this.description = description;
        this.badgeType = badgeType;
    }

    // コンストラクタ（詳細設定付き）
    public LearningBadge(String name, String description, BadgeType badgeType,
            Integer requiredValue, UUID spaceId, String iconUrl) {
        this.name = name;
        this.description = description;
        this.badgeType = badgeType;
        this.requiredValue = requiredValue;
        this.spaceId = spaceId;
        this.iconUrl = iconUrl;
    }

    /**
     * バッジの達成条件をチェック
     */
    public boolean isEligible(Integer userValue) {
        return requiredValue == null || (userValue != null && userValue >= requiredValue);
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
        return "LearningBadge{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", badgeType=" + badgeType +
                ", requiredValue=" + requiredValue +
                ", spaceId=" + spaceId +
                ", isActive=" + isActive +
                '}';
    }
}
