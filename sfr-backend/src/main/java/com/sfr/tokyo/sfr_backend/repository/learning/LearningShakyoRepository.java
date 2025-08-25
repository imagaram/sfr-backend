package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningShakyo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 学習写経リポジトリ - LearningShakyo Repository
 */
@Repository
public interface LearningShakyoRepository extends JpaRepository<LearningShakyo, Long> {

    // 基本検索メソッド

    /**
     * ユーザーIDで写経一覧を取得
     */
    List<LearningShakyo> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * ユーザーIDとスペースIDで写経一覧を取得
     */
    List<LearningShakyo> findByUserIdAndSpaceIdOrderByCreatedAtDesc(UUID userId, Long spaceId);

    /**
     * ユーザーIDと写経タイプで写経一覧を取得
     */
    List<LearningShakyo> findByUserIdAndShakyoTypeOrderByCreatedAtDesc(UUID userId,
            LearningShakyo.ShakyoType shakyoType);

    /**
     * ユーザーIDと写経ステータスで写経一覧を取得
     */
    List<LearningShakyo> findByUserIdAndShakyoStatusOrderByUpdatedAtDesc(UUID userId,
            LearningShakyo.ShakyoStatus shakyoStatus);

    /**
     * ユーザーIDと難易度レベルで写経一覧を取得
     */
    List<LearningShakyo> findByUserIdAndDifficultyLevelOrderByCreatedAtDesc(UUID userId,
            LearningShakyo.DifficultyLevel difficultyLevel);

    /**
     * プログラミング言語で写経一覧を取得
     */
    List<LearningShakyo> findByProgrammingLanguageOrderByCreatedAtDesc(String programmingLanguage);

    /**
     * ユーザーの進行中の写経を取得
     */
    List<LearningShakyo> findByUserIdAndShakyoStatusInOrderByLastTypedAtDesc(
            UUID userId, List<LearningShakyo.ShakyoStatus> statuses);

    /**
     * ユーザーの完了した写経を取得
     */
    List<LearningShakyo> findByUserIdAndShakyoStatusAndCompletedAtBetweenOrderByCompletedAtDesc(
            UUID userId, LearningShakyo.ShakyoStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // 統計・分析メソッド

    /**
     * ユーザーの写経総数を取得
     */
    @Query("SELECT COUNT(ls) FROM LearningShakyo ls WHERE ls.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの完了した写経数を取得
     */
    @Query("SELECT COUNT(ls) FROM LearningShakyo ls WHERE ls.userId = :userId AND ls.shakyoStatus = :status")
    long countByUserIdAndShakyoStatus(@Param("userId") UUID userId,
            @Param("status") LearningShakyo.ShakyoStatus status);

    /**
     * ユーザーの写経タイプ別完了数を取得
     */
    @Query("SELECT ls.shakyoType, COUNT(ls) FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED' " +
            "GROUP BY ls.shakyoType")
    List<Object[]> countCompletedByUserIdAndShakyoType(@Param("userId") UUID userId);

    /**
     * ユーザーの難易度別完了数を取得
     */
    @Query("SELECT ls.difficultyLevel, COUNT(ls) FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED' " +
            "GROUP BY ls.difficultyLevel")
    List<Object[]> countCompletedByUserIdAndDifficultyLevel(@Param("userId") UUID userId);

    /**
     * ユーザーの平均正確率を取得
     */
    @Query("SELECT AVG(ls.accuracyRate) FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED'")
    Optional<BigDecimal> findAverageAccuracyRateByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの平均入力速度を取得
     */
    @Query("SELECT AVG(ls.typingSpeedCpm) FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED'")
    Optional<BigDecimal> findAverageTypingSpeedByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの総学習時間を取得
     */
    @Query("SELECT SUM(ls.totalTypingTimeSeconds) FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED'")
    Optional<Long> findTotalTypingTimeByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの最高スコアを取得
     */
    @Query("SELECT MAX(ls.score) FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED'")
    Optional<BigDecimal> findMaxScoreByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの平均スコアを取得
     */
    @Query("SELECT AVG(ls.score) FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED'")
    Optional<BigDecimal> findAverageScoreByUserId(@Param("userId") UUID userId);

    // 検索・フィルタリングメソッド

    /**
     * タイトルで写経を検索（部分一致）
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.userId = :userId AND ls.title LIKE %:title% " +
            "ORDER BY ls.createdAt DESC")
    List<LearningShakyo> findByUserIdAndTitleContaining(@Param("userId") UUID userId, @Param("title") String title);

    /**
     * スコア範囲で写経を検索
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.userId = :userId AND ls.score BETWEEN :minScore AND :maxScore " +
            "ORDER BY ls.score DESC")
    List<LearningShakyo> findByUserIdAndScoreBetween(@Param("userId") UUID userId,
            @Param("minScore") BigDecimal minScore,
            @Param("maxScore") BigDecimal maxScore);

    /**
     * 正確率範囲で写経を検索
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.userId = :userId AND ls.accuracyRate BETWEEN :minAccuracy AND :maxAccuracy "
            +
            "ORDER BY ls.accuracyRate DESC")
    List<LearningShakyo> findByUserIdAndAccuracyRateBetween(@Param("userId") UUID userId,
            @Param("minAccuracy") BigDecimal minAccuracy,
            @Param("maxAccuracy") BigDecimal maxAccuracy);

    /**
     * 入力速度範囲で写経を検索
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.userId = :userId AND ls.typingSpeedCpm BETWEEN :minSpeed AND :maxSpeed "
            +
            "ORDER BY ls.typingSpeedCpm DESC")
    List<LearningShakyo> findByUserIdAndTypingSpeedBetween(@Param("userId") UUID userId,
            @Param("minSpeed") Integer minSpeed,
            @Param("maxSpeed") Integer maxSpeed);

    /**
     * 複合条件での写経検索（ページネーション対応）
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.userId = :userId " +
            "AND (:shakyoType IS NULL OR ls.shakyoType = :shakyoType) " +
            "AND (:shakyoStatus IS NULL OR ls.shakyoStatus = :shakyoStatus) " +
            "AND (:difficultyLevel IS NULL OR ls.difficultyLevel = :difficultyLevel) " +
            "AND (:programmingLanguage IS NULL OR ls.programmingLanguage = :programmingLanguage) " +
            "ORDER BY ls.createdAt DESC")
    Page<LearningShakyo> findByUserIdWithFilters(@Param("userId") UUID userId,
            @Param("shakyoType") LearningShakyo.ShakyoType shakyoType,
            @Param("shakyoStatus") LearningShakyo.ShakyoStatus shakyoStatus,
            @Param("difficultyLevel") LearningShakyo.DifficultyLevel difficultyLevel,
            @Param("programmingLanguage") String programmingLanguage,
            Pageable pageable);

    // ランキング・比較メソッド

    /**
     * 写経タイプ別のトップスコア一覧を取得
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.shakyoType = :shakyoType AND ls.shakyoStatus = 'COMPLETED' " +
            "ORDER BY ls.score DESC")
    Page<LearningShakyo> findTopScoresByShakyoType(@Param("shakyoType") LearningShakyo.ShakyoType shakyoType,
            Pageable pageable);

    /**
     * 難易度別のトップスコア一覧を取得
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.difficultyLevel = :difficultyLevel AND ls.shakyoStatus = 'COMPLETED' "
            +
            "ORDER BY ls.score DESC")
    Page<LearningShakyo> findTopScoresByDifficultyLevel(
            @Param("difficultyLevel") LearningShakyo.DifficultyLevel difficultyLevel,
            Pageable pageable);

    /**
     * プログラミング言語別のトップスコア一覧を取得
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.programmingLanguage = :programmingLanguage AND ls.shakyoStatus = 'COMPLETED' "
            +
            "ORDER BY ls.score DESC")
    Page<LearningShakyo> findTopScoresByProgrammingLanguage(@Param("programmingLanguage") String programmingLanguage,
            Pageable pageable);

    /**
     * 最高正確率の写経一覧を取得
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.shakyoStatus = 'COMPLETED' " +
            "ORDER BY ls.accuracyRate DESC, ls.typingSpeedCpm DESC")
    Page<LearningShakyo> findTopAccuracyRates(Pageable pageable);

    /**
     * 最高入力速度の写経一覧を取得
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.shakyoStatus = 'COMPLETED' " +
            "ORDER BY ls.typingSpeedCpm DESC, ls.accuracyRate DESC")
    Page<LearningShakyo> findTopTypingSpeeds(Pageable pageable);

    // 時系列分析メソッド

    /**
     * ユーザーの日別完了数を取得
     */
    @Query("SELECT FUNCTION('DATE', ls.completedAt), COUNT(ls) FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED' " +
            "AND ls.completedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', ls.completedAt) " +
            "ORDER BY FUNCTION('DATE', ls.completedAt)")
    List<Object[]> findDailyCompletionsByUserId(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * ユーザーの月別進捗統計を取得
     */
    @Query("SELECT FUNCTION('YEAR', ls.completedAt), FUNCTION('MONTH', ls.completedAt), " +
            "COUNT(ls), AVG(ls.accuracyRate), AVG(ls.typingSpeedCpm), AVG(ls.score) " +
            "FROM LearningShakyo ls " +
            "WHERE ls.userId = :userId AND ls.shakyoStatus = 'COMPLETED' " +
            "AND ls.completedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', ls.completedAt), FUNCTION('MONTH', ls.completedAt) " +
            "ORDER BY FUNCTION('YEAR', ls.completedAt), FUNCTION('MONTH', ls.completedAt)")
    List<Object[]> findMonthlyProgressByUserId(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 最近の写経活動を取得
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.userId = :userId " +
            "AND ls.lastTypedAt >= :sinceDate " +
            "ORDER BY ls.lastTypedAt DESC")
    List<LearningShakyo> findRecentActivitiesByUserId(@Param("userId") UUID userId,
            @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 長時間未活動の写経を取得
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.userId = :userId " +
            "AND ls.shakyoStatus IN ('IN_PROGRESS', 'PAUSED') " +
            "AND ls.lastTypedAt < :beforeDate " +
            "ORDER BY ls.lastTypedAt ASC")
    List<LearningShakyo> findInactiveShakyo(@Param("userId") UUID userId,
            @Param("beforeDate") LocalDateTime beforeDate);

    // スペース別統計メソッド

    /**
     * スペース別の写経統計を取得
     */
    @Query("SELECT ls.spaceId, COUNT(ls), AVG(ls.accuracyRate), AVG(ls.typingSpeedCpm), AVG(ls.score) " +
            "FROM LearningShakyo ls " +
            "WHERE ls.spaceId IS NOT NULL AND ls.shakyoStatus = 'COMPLETED' " +
            "GROUP BY ls.spaceId")
    List<Object[]> findStatisticsBySpace();

    /**
     * スペース内のユーザーランキングを取得
     */
    @Query("SELECT ls FROM LearningShakyo ls WHERE ls.spaceId = :spaceId AND ls.shakyoStatus = 'COMPLETED' " +
            "ORDER BY ls.score DESC, ls.accuracyRate DESC, ls.typingSpeedCpm DESC")
    Page<LearningShakyo> findRankingBySpace(@Param("spaceId") Long spaceId, Pageable pageable);
}
