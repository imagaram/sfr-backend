package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningQuizRepository extends JpaRepository<LearningQuiz, Long> {

    /**
     * 学習空間内のクイズ一覧取得
     */
    List<LearningQuiz> findBySpaceIdOrderByCreatedAtDesc(Long spaceId);

    /**
     * 学習空間内のクイズ数取得
     */
    Long countBySpaceId(Long spaceId);

    /**
     * タイトルでクイズ検索（部分一致）
     */
    @Query("SELECT lq FROM LearningQuiz lq " +
            "WHERE lq.spaceId = :spaceId " +
            "AND LOWER(lq.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
            "ORDER BY lq.createdAt DESC")
    List<LearningQuiz> findBySpaceIdAndTitleContainingIgnoreCase(@Param("spaceId") Long spaceId,
            @Param("title") String title);

    /**
     * 指定期間内に作成されたクイズ取得
     */
    @Query("SELECT lq FROM LearningQuiz lq " +
            "WHERE lq.spaceId = :spaceId " +
            "AND lq.createdAt >= :startDate " +
            "AND lq.createdAt <= :endDate " +
            "ORDER BY lq.createdAt DESC")
    List<LearningQuiz> findBySpaceIdAndCreatedAtBetween(@Param("spaceId") Long spaceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 学習空間とIDでクイズ取得（権限チェック用）
     */
    Optional<LearningQuiz> findByIdAndSpaceId(Long id, Long spaceId);

    /**
     * 問題数による統計取得
     */
    @Query("SELECT " +
            "COUNT(lq) as totalQuizzes, " +
            "AVG(CAST(JSON_LENGTH(lq.questions) AS DOUBLE)) as avgQuestions, " +
            "MIN(CAST(JSON_LENGTH(lq.questions) AS INTEGER)) as minQuestions, " +
            "MAX(CAST(JSON_LENGTH(lq.questions) AS INTEGER)) as maxQuestions " +
            "FROM LearningQuiz lq " +
            "WHERE lq.spaceId = :spaceId")
    Object[] getQuizStatistics(@Param("spaceId") Long spaceId);

    /**
     * 最近作成されたクイズ取得（上位N件）
     */
    @Query("SELECT lq FROM LearningQuiz lq " +
            "WHERE lq.spaceId = :spaceId " +
            "ORDER BY lq.createdAt DESC")
    List<LearningQuiz> findRecentQuizzesBySpaceId(@Param("spaceId") Long spaceId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * クイズが存在するかチェック
     */
    boolean existsBySpaceIdAndTitle(Long spaceId, String title);

    /**
     * 学習空間内のクイズタイトル一覧取得
     */
    @Query("SELECT lq.title FROM LearningQuiz lq " +
            "WHERE lq.spaceId = :spaceId " +
            "ORDER BY lq.title")
    List<String> findTitlesBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 複数の学習空間からクイズ取得
     */
    @Query("SELECT lq FROM LearningQuiz lq " +
            "WHERE lq.spaceId IN :spaceIds " +
            "ORDER BY lq.createdAt DESC")
    List<LearningQuiz> findBySpaceIdIn(@Param("spaceIds") List<Long> spaceIds);

    /**
     * ID範囲でクイズ取得
     */
    @Query("SELECT lq FROM LearningQuiz lq " +
            "WHERE lq.spaceId = :spaceId " +
            "AND lq.id BETWEEN :startId AND :endId " +
            "ORDER BY lq.id")
    List<LearningQuiz> findBySpaceIdAndIdBetween(@Param("spaceId") Long spaceId,
            @Param("startId") Long startId,
            @Param("endId") Long endId);
}
