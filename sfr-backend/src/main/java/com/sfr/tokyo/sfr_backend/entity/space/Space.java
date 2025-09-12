package com.sfr.tokyo.sfr_backend.entity.space;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 統合スペースエンティティ
 * 学習・交流・エンタメが融合したスペースを管理
 * 
 * 旧 learning_space テーブルの後継として設計
 * SCHOOL/SALON/FANCLUB の3モードを統合サポート
 * 
 * @author SFR Development Team
 * @version 2.0
 * @since 2025-09-11
 */
@Entity
@Table(name = "space", indexes = {
    @Index(name = "idx_space_mode", columnList = "mode"),
    @Index(name = "idx_space_status", columnList = "status"),
    @Index(name = "idx_space_owner", columnList = "owner_id"),
    @Index(name = "idx_space_public", columnList = "is_public"),
    @Index(name = "idx_space_created", columnList = "created_at"),
    @Index(name = "idx_space_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * スペース名 (必須)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * スペースの説明
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * スペースモード
     * SCHOOL: 学習重視モード
     * SALON: 交流重視モード  
     * FANCLUB: エンタメ重視モード
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceMode mode;

    /**
     * スペースステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SpaceStatus status = SpaceStatus.ACTIVE;

    /**
     * 公開/非公開設定
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    /**
     * 最大メンバー数
     */
    @Column(name = "max_members", nullable = false)
    @Builder.Default
    private Integer maxMembers = 1000;

    /**
     * 現在のメンバー数
     */
    @Column(name = "member_count", nullable = false)
    @Builder.Default
    private Integer memberCount = 0;

    /**
     * スペース所有者ID
     */
    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * スペースモード列挙型
     */
    public enum SpaceMode {
        /** 学習重視モード - 構造化された学習コンテンツ */
        SCHOOL,
        /** 交流重視モード - コミュニティディスカッション */
        SALON, 
        /** エンタメ重視モード - キャラクター・ゲーム要素 */
        FANCLUB
    }

    /**
     * スペースステータス列挙型
     */
    public enum SpaceStatus {
        /** アクティブ - 通常運用中 */
        ACTIVE,
        /** 非アクティブ - 一時停止 */
        INACTIVE,
        /** 保留中 - 審査・設定中 */
        PENDING
    }

    /**
     * メンバー数を増加
     */
    public void incrementMemberCount() {
        if (this.memberCount < this.maxMembers) {
            this.memberCount++;
        } else {
            throw new IllegalStateException("Maximum member count exceeded");
        }
    }

    /**
     * メンバー数を減少
     */
    public void decrementMemberCount() {
        if (this.memberCount > 0) {
            this.memberCount--;
        }
    }

    /**
     * 満員かどうかを判定
     */
    public boolean isFull() {
        return this.memberCount >= this.maxMembers;
    }

    /**
     * アクティブかどうかを判定
     */
    public boolean isActive() {
        return this.status == SpaceStatus.ACTIVE;
    }
}
