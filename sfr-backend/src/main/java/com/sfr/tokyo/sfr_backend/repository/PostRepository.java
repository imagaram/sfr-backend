package com.sfr.tokyo.sfr_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.entity.PostEntity;

/**
 * 投稿情報を管理するリポジトリ
 * 推奨インデックス：character_id, created_at（複合インデックス）
 */
@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    /**
     * ユーザーIDに紐づく投稿を取得
     */
    List<PostEntity> findByUser_Id(UUID userId);

    /**
     * ユーザーIDに紐づく投稿をページング取得
     */
    Page<PostEntity> findByUser_Id(UUID userId, Pageable pageable);

    /**
     * タイトルまたは説明に特定のキーワードを含む投稿を検索
     */
    @Query("SELECT p FROM PostEntity p WHERE p.title LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<PostEntity> findByKeyword(@Param("keyword") String keyword);

    /**
     * 特定の期間内の投稿を取得
     */
    List<PostEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * ユーザーIDと期間で投稿を検索
     */
    List<PostEntity> findByUser_IdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    /**
     * 最新の投稿を指定件数取得（タイムライン表示用）
     */
    @Query("SELECT p FROM PostEntity p ORDER BY p.createdAt DESC")
    List<PostEntity> findLatestPosts(Pageable pageable);

    /**
     * ユーザーの投稿数を取得
     */
    @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * ファイルURLが存在する投稿を取得（添付ファイル付き投稿）
     */
    List<PostEntity> findByFileUrlIsNotNull();

    /**
     * ユーザーIDとタイトルで投稿を検索
     */
    List<PostEntity> findByUser_IdAndTitleContaining(UUID userId, String title);
}
