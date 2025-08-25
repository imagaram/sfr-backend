package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * LearningComment Repository
 * 学習コメントデータアクセス層
 */
@Repository
public interface LearningCommentRepository extends JpaRepository<LearningComment, Long> {

        // ========== 基本検索メソッド ==========

        /**
         * トピック別コメント一覧取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        List<LearningComment> findByTopicIdOrderByCreatedAt(@Param("topicId") Long topicId);

        /**
         * トピック別コメント一覧取得（ページング）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        Page<LearningComment> findByTopicId(@Param("topicId") Long topicId, Pageable pageable);

        /**
         * 親コメント別返信一覧取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.parentCommentId = :parentId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        List<LearningComment> findRepliesByParentId(@Param("parentId") Long parentId);

        /**
         * ユーザー別コメント一覧取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.authorId = :authorId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        Page<LearningComment> findByAuthorId(@Param("authorId") UUID authorId, Pageable pageable);

        /**
         * ベストアンサーコメント取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.isBestAnswer = true AND c.deletedAt IS NULL")
        Optional<LearningComment> findBestAnswerByTopicId(@Param("topicId") Long topicId);

        /**
         * ソリューションコメント一覧取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.isSolution = true AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        List<LearningComment> findSolutionsByTopicId(@Param("topicId") Long topicId);

        /**
         * ピン留めコメント一覧取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.isPinned = true AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        List<LearningComment> findPinnedByTopicId(@Param("topicId") Long topicId);

        // ========== ステータス別検索 ==========

        /**
         * ステータス別コメント検索
         */
        @Query("SELECT c FROM LearningComment c WHERE c.commentStatus = :status AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        Page<LearningComment> findByStatus(@Param("status") String status, Pageable pageable);

        /**
         * モデレーション待ちコメント取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.requiresModeration = true AND c.moderationStatus = 'PENDING' AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        Page<LearningComment> findPendingModeration(Pageable pageable);

        /**
         * 承認済みコメント取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.commentStatus = 'APPROVED' AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        List<LearningComment> findApprovedByTopicId(@Param("topicId") Long topicId);

        // ========== 品質・人気度検索 ==========

        /**
         * 高品質コメント取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.qualityScore >= :minScore AND c.deletedAt IS NULL ORDER BY c.qualityScore DESC")
        List<LearningComment> findHighQualityByTopicId(@Param("topicId") Long topicId,
                        @Param("minScore") BigDecimal minScore);

        /**
         * 人気コメント取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.likeCount >= :minLikes AND c.deletedAt IS NULL ORDER BY c.likeCount DESC")
        List<LearningComment> findPopularByTopicId(@Param("topicId") Long topicId, @Param("minLikes") Integer minLikes);

        /**
         * トップレベルコメント取得（返信ではない）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.parentCommentId IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        List<LearningComment> findTopLevelByTopicId(@Param("topicId") Long topicId);

        /**
         * 最新コメント取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        List<LearningComment> findLatestByTopicId(@Param("topicId") Long topicId, Pageable pageable);

        // ========== 統計クエリ ==========

        /**
         * トピック別コメント数カウント
         */
        @Query("SELECT COUNT(c) FROM LearningComment c WHERE c.topicId = :topicId AND c.deletedAt IS NULL")
        Long countByTopicId(@Param("topicId") Long topicId);

        /**
         * ユーザー別コメント数カウント
         */
        @Query("SELECT COUNT(c) FROM LearningComment c WHERE c.authorId = :authorId AND c.deletedAt IS NULL")
        Long countByAuthorId(@Param("authorId") UUID authorId);

        /**
         * 期間別コメント数統計
         */
        @Query("SELECT DATE(c.createdAt) as date, COUNT(c) as count FROM LearningComment c WHERE c.createdAt >= :fromDate AND c.createdAt <= :toDate AND c.deletedAt IS NULL GROUP BY DATE(c.createdAt) ORDER BY date")
        List<Object[]> getCommentCountByDateRange(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

        /**
         * トピック別返信数カウント
         */
        @Query("SELECT c.parentCommentId, COUNT(c) FROM LearningComment c WHERE c.topicId = :topicId AND c.parentCommentId IS NOT NULL AND c.deletedAt IS NULL GROUP BY c.parentCommentId")
        List<Object[]> getReplyCountsByTopicId(@Param("topicId") Long topicId);

        // ========== 検索機能 ==========

        /**
         * コンテンツ検索
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        Page<LearningComment> searchByContent(@Param("topicId") Long topicId, @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 著者名検索
         */
        @Query("SELECT c FROM LearningComment c WHERE c.authorId = :authorId AND LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        Page<LearningComment> searchByAuthorAndContent(@Param("authorId") UUID authorId,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 複合検索
         */
        @Query("SELECT c FROM LearningComment c WHERE " +
                        "(:topicId IS NULL OR c.topicId = :topicId) AND " +
                        "(:authorId IS NULL OR c.authorId = :authorId) AND " +
                        "(:status IS NULL OR c.commentStatus = :status) AND " +
                        "(:keyword IS NULL OR LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        Page<LearningComment> searchComments(
                        @Param("topicId") Long topicId,
                        @Param("authorId") UUID authorId,
                        @Param("status") String status,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        // ========== 更新操作 ==========

        /**
         * いいね数更新
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.likeCount = c.likeCount + :increment, c.updatedAt = :now WHERE c.id = :commentId")
        int updateLikeCount(@Param("commentId") Long commentId, @Param("increment") Integer increment,
                        @Param("now") LocalDateTime now);

        /**
         * 品質スコア更新
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.qualityScore = :score, c.updatedAt = :now WHERE c.id = :commentId")
        int updateQualityScore(@Param("commentId") Long commentId, @Param("score") BigDecimal score,
                        @Param("now") LocalDateTime now);

        /**
         * ベストアンサー設定
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.isBestAnswer = :isBestAnswer, c.updatedAt = :now WHERE c.id = :commentId")
        int updateBestAnswer(@Param("commentId") Long commentId, @Param("isBestAnswer") Boolean isBestAnswer,
                        @Param("now") LocalDateTime now);

        /**
         * ピン留め設定
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.isPinned = :isPinned, c.updatedAt = :now WHERE c.id = :commentId")
        int updatePinned(@Param("commentId") Long commentId, @Param("isPinned") Boolean isPinned,
                        @Param("now") LocalDateTime now);

        /**
         * ソリューション設定
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.isSolution = :isSolution, c.updatedAt = :now WHERE c.id = :commentId")
        int updateSolution(@Param("commentId") Long commentId, @Param("isSolution") Boolean isSolution,
                        @Param("now") LocalDateTime now);

        /**
         * ステータス更新
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.commentStatus = :status, c.updatedAt = :now WHERE c.id = :commentId")
        int updateStatus(@Param("commentId") Long commentId, @Param("status") String status,
                        @Param("now") LocalDateTime now);

        /**
         * モデレーションステータス更新
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.moderationStatus = :status, c.moderatedAt = :now, c.moderatorId = :moderatorId, c.updatedAt = :now WHERE c.id = :commentId")
        int updateModerationStatus(@Param("commentId") Long commentId, @Param("status") String status,
                        @Param("moderatorId") UUID moderatorId, @Param("now") LocalDateTime now);

        /**
         * 論理削除
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.deletedAt = :now WHERE c.id = :commentId")
        int softDelete(@Param("commentId") Long commentId, @Param("now") LocalDateTime now);

        /**
         * 復旧
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.deletedAt = NULL, c.updatedAt = :now WHERE c.id = :commentId")
        int restore(@Param("commentId") Long commentId, @Param("now") LocalDateTime now);

        // ========== バッチ処理 ==========

        /**
         * トピック削除に伴うコメント一括削除
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.deletedAt = :now WHERE c.topicId = :topicId")
        int softDeleteByTopicId(@Param("topicId") Long topicId, @Param("now") LocalDateTime now);

        /**
         * 親コメント削除に伴う返信一括削除
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.deletedAt = :now WHERE c.parentCommentId = :parentId")
        int softDeleteRepliesByParentId(@Param("parentId") Long parentId, @Param("now") LocalDateTime now);

        /**
         * 古いコメントのアーカイブ
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.commentStatus = 'ARCHIVED', c.updatedAt = :now WHERE c.createdAt < :cutoffDate AND c.commentStatus = 'APPROVED'")
        int archiveOldComments(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("now") LocalDateTime now);

        // ========== 高度な検索 ==========

        /**
         * 関連コメント検索（同じ著者の他のコメント）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.authorId = :authorId AND c.topicId != :excludeTopicId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        List<LearningComment> findRelatedCommentsByAuthor(@Param("authorId") UUID authorId,
                        @Param("excludeTopicId") Long excludeTopicId, Pageable pageable);

        /**
         * 人気コメント（複合スコア順）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.deletedAt IS NULL ORDER BY (c.likeCount * 0.4 + c.qualityScore * 0.6) DESC")
        List<LearningComment> findPopularCommentsByScore(@Param("topicId") Long topicId, Pageable pageable);

        /**
         * アクティブな討論（最近の返信があるコメント）
         */
        @Query("SELECT DISTINCT p FROM LearningComment p WHERE p.topicId = :topicId AND p.parentCommentId IS NULL AND EXISTS (SELECT r FROM LearningComment r WHERE r.parentCommentId = p.id AND r.createdAt >= :sinceDate AND r.deletedAt IS NULL) AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
        List<LearningComment> findActiveDiscussions(@Param("topicId") Long topicId,
                        @Param("sinceDate") LocalDateTime sinceDate);

        /**
         * 解決済みコメント（ソリューションまたはベストアンサー）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND (c.isSolution = true OR c.isBestAnswer = true) AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        List<LearningComment> findSolvedComments(@Param("topicId") Long topicId);

        /**
         * 未解決コメント（返信が少ない質問コメント）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.parentCommentId IS NULL AND (SELECT COUNT(r) FROM LearningComment r WHERE r.parentCommentId = c.id AND r.deletedAt IS NULL) < :maxReplies AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
        List<LearningComment> findUnresolvedComments(@Param("topicId") Long topicId,
                        @Param("maxReplies") Integer maxReplies);

        /**
         * 最近のアクティビティ
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.updatedAt >= :sinceDate AND c.deletedAt IS NULL ORDER BY c.updatedAt DESC")
        List<LearningComment> findRecentActivity(@Param("topicId") Long topicId,
                        @Param("sinceDate") LocalDateTime sinceDate);

        // ========== Controller対応の追加メソッド ==========

        /**
         * ユーザー別コメント一覧（Page）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.authorId = :userId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        org.springframework.data.domain.Page<LearningComment> findByAuthorIdOrderByCreatedAtDesc(
                        @Param("userId") UUID userId, org.springframework.data.domain.Pageable pageable);

        /**
         * 人気コメント取得（Pageable版）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.deletedAt IS NULL ORDER BY c.likeCount DESC")
        List<LearningComment> findPopularByTopicIdWithPageable(@Param("topicId") Long topicId,
                        org.springframework.data.domain.Pageable pageable);

        /**
         * 高品質コメント取得（double版）
         */
        @Query("SELECT c FROM LearningComment c WHERE c.topicId = :topicId AND c.qualityScore >= :minScore AND c.deletedAt IS NULL ORDER BY c.qualityScore DESC")
        List<LearningComment> findHighQualityByTopicIdDouble(@Param("topicId") Long topicId,
                        @Param("minScore") double minScore);

        /**
         * モデレーション理由更新
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.moderationNotes = :reason, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
        int updateModerationReason(@Param("id") Long id, @Param("reason") String reason);

        /**
         * ユーザー別統計
         */
        @Query("SELECT c.authorId as authorId, COUNT(c) as commentCount, AVG(c.qualityScore) as avgQuality, " +
                        "SUM(c.likeCount) as totalLikes FROM LearningComment c WHERE c.deletedAt IS NULL " +
                        "GROUP BY c.authorId ORDER BY commentCount DESC")
        List<Object[]> getUserCommentStatistics();

        /**
         * 品質統計
         */
        @Query("SELECT AVG(c.qualityScore) as avgQuality, " +
                        "COUNT(CASE WHEN c.qualityScore >= 4.0 THEN 1 END) as highQuality, " +
                        "COUNT(CASE WHEN c.qualityScore < 2.0 THEN 1 END) as lowQuality, " +
                        "COUNT(c) as totalComments FROM LearningComment c WHERE c.deletedAt IS NULL")
        Object[] getCommentQualityStatistics();

        /**
         * ユーザーアクティビティ取得
         */
        @Query("SELECT c FROM LearningComment c WHERE c.authorId = :userId AND " +
                        "c.createdAt > :since AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
        List<LearningComment> findUserActivity(@Param("userId") UUID userId, @Param("since") LocalDateTime since,
                        org.springframework.data.domain.Pageable pageable);

        /**
         * 通知対象コメント取得
         */
        @Query("SELECT c FROM LearningComment c WHERE " +
                        "(c.topicId IN (SELECT t.id FROM LearningTopic t WHERE t.creatorId = :userId) OR " +
                        "c.parentCommentId IN (SELECT c2.id FROM LearningComment c2 WHERE c2.authorId = :userId)) AND "
                        +
                        "c.authorId != :userId AND c.createdAt > :since AND c.deletedAt IS NULL " +
                        "ORDER BY c.createdAt DESC")
        List<LearningComment> findNotificationComments(@Param("userId") UUID userId,
                        @Param("since") LocalDateTime since);

        /**
         * 孤立コメントのクリーンアップ
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.deletedAt = CURRENT_TIMESTAMP WHERE " +
                        "c.topicId NOT IN (SELECT t.id FROM LearningTopic t WHERE t.deletedAt IS NULL) AND " +
                        "c.deletedAt IS NULL")
        int cleanupOrphanedComments();

        /**
         * 品質スコア再計算
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.qualityScore = " +
                        "(c.likeCount * 2.0 + " +
                        "CASE WHEN c.isBestAnswer = true THEN 10.0 ELSE 0.0 END) / 10.0, " +
                        "c.updatedAt = CURRENT_TIMESTAMP WHERE c.deletedAt IS NULL")
        int recalculateQualityScores();

        /**
         * 非アクティブコメントの更新
         */
        @Modifying
        @Query("UPDATE LearningComment c SET c.commentStatus = 'INACTIVE', c.updatedAt = CURRENT_TIMESTAMP WHERE " +
                        "c.updatedAt < :cutoffDate AND c.commentStatus = 'ACTIVE' AND c.deletedAt IS NULL")
        int updateInactiveComments(@Param("cutoffDate") LocalDateTime cutoffDate);
}
