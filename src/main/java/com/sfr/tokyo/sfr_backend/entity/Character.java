package com.sfr.tokyo.sfr_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
@Table(name = "characters") // テーブル名を "characters" に指定
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // IDの型をLongに統一

    private String name;
    
    // プロフィールが長くなる可能性を考慮して長さを設定
    @Column(length = 1000)
    private String profile;
    
    private String imageUrl; // キャラクター画像のURL

    // ユーザーとの多対一のリレーションシップ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 外部キーのカラム名を指定
    private User user;
}
