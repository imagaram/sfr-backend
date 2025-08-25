package com.sfr.tokyo.sfr_backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.entity.Comment;

/**
 * コメント情報を管理するリポジトリ
 * 推奨インデックス：post_id（外部キー）
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 特定の投稿IDに紐づくすべてのコメントを取得
     */
    List<Comment> findByPost_Id(Long postId);

    /**
     * 特定の投稿IDに紐づくコメントをページング取得
     */
    Page<Comment> findByPost_Id(Long postId, Pageable pageable);

    /**
     * 特定のユーザーIDのコメントを取得
     */
    List<Comment> findByUser_Id(UUID userId);

    /**
     * 投稿IDとユーザーIDでコメントを検索
     */
    List<Comment> findByPost_IdAndUser_Id(Long postId, UUID userId);

    /**
     * コメント内容に特定のキーワードを含むコメントを検索
     */
    List<Comment> findByContentContaining(String keyword);

    /**
     * 特定の投稿に対するコメント数を取得
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    /**
     * 特定のユーザーのコメント数を取得
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * 投稿IDの配列に対応するコメントを一括取得（N+1問題対策）
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id IN :postIds ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdIn(@Param("postIds") List<Long> postIds);

    /**
     * 投稿ごとの最新のコメントを取得（サマリー表示用）
     */
    @Query(value = "SELECT c.* FROM comments c " +
            "INNER JOIN (SELECT post_id, MAX(created_at) as max_date FROM comments GROUP BY post_id) latest " +
            "ON c.post_id = latest.post_id AND c.created_at = latest.max_date", nativeQuery = true)
    List<Comment> findLatestCommentForEachPost();
}
