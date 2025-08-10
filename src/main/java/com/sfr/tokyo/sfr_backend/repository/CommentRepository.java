package com.sfr.tokyo.sfr_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.entity.Comment;

// Commentエンティティのデータベース操作を管理するリポジトリ
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 特定の投稿IDに紐づくすべてのコメントを取得する
    List<Comment> findByPostId(Long postId);
}
