package com.sfr.tokyo.sfr_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.entity.Post;

// PostRepositoryはPostエンティティを扱うためのJPAリポジトリ
@Repository // このインターフェースがリポジトリであることを示す
public interface PostRepository extends JpaRepository<Post, Long> {

    // user_idで投稿を検索するメソッド
    // Spring Data JPAがこのメソッド名から自動的にクエリを生成する
    List<Post> findByUserId(Long userId);

}
