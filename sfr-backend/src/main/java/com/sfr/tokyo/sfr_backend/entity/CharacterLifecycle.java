package com.sfr.tokyo.sfr_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.sfr.tokyo.sfr_backend.entity.CharacterStatus;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// キャラクター情報を表現するエンティティクラス
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "character_lifecycle") // テーブル名を "character_lifecycle" に指定
public class CharacterLifecycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // IDの型をLongに統一

    private String name;

    // プロフィールが長くなる可能性を考慮して長さを設定
    @Column(length = 1000)
    private String profile;

    private String imageUrl; // キャラクター画像のURL

    // キャラクターの寿命ポイント（初期値365）
    @Column(name = "lifespan_points")
    @Builder.Default
    private Integer lifespanPoints = 365;

    // キャラクターのステータス（初期値ACTIVE）
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private CharacterStatus status = CharacterStatus.ACTIVE;

    // 作成日時
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 更新日時
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ユーザーとの多対一のリレーションシップ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 外部キーのカラム名を指定
    private User user;

    /**
     * エンティティ保存前に実行される処理
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;

        // 初期値の設定
        if (lifespanPoints == null) {
            lifespanPoints = 365;
        }
        if (status == null) {
            status = CharacterStatus.ACTIVE;
        }
    }
}