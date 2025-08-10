package com.sfr.tokyo.sfr_backend.entity;

import jakarta.persistence.Entity;
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

// 作品投稿情報を扱うためのエンティティクラス
@Data // Lombokのアノテーションで、getter, setterなどを自動生成
@Builder // Lombokのアノテーションで、ビルダークラスを自動生成
@NoArgsConstructor // Lombokのアノテーションで、引数なしコンストラクタを自動生成
@AllArgsConstructor // Lombokのアノテーションで、全引数コンストラクタを自動生成
@Entity // このクラスがJPAエンティティであることを示す
@Table(name = "posts") // データベースの'posts'テーブルに対応付け
public class Post {

    @Id // 主キーであることを示す
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDがデータベースによって自動生成されることを示す
    private Long id;

    private String title; // 作品のタイトル

    private String description; // 作品の説明

    private String fileUrl; // アップロードされたファイルのURL

    // 作品を投稿したユーザー
    @ManyToOne
    @JoinColumn(name = "user_id") // 'posts'テーブルの'user_id'カラムと'users'テーブルを紐付ける
    private User user;
}
