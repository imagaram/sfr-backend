package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation.SimulationType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation.SimulationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 学習シミュレーション機能のリポジトリ
 * 
 * シミュレーションデータのCRUD操作と複雑なクエリを提供します。
 */
@Repository
public interface LearningSimulationRepository extends JpaRepository<LearningSimulation, Long> {

    /**
     * ユーザーIDでシミュレーションを検索
     */
    List<LearningSimulation> findByUserId(UUID userId);

    /**
     * ユーザーIDとページネーションでシミュレーションを検索
     */
    Page<LearningSimulation> findByUserId(UUID userId, Pageable pageable);

    /**
     * ユーザーIDとシミュレーション種類でシミュレーションを検索
     */
    List<LearningSimulation> findByUserIdAndSimulationType(UUID userId, SimulationType simulationType);

    /**
     * ユーザーIDとステータスでシミュレーションを検索
     */
    List<LearningSimulation> findByUserIdAndStatus(UUID userId, SimulationStatus status);

    /**
     * ユーザーIDとシミュレーション種類とステータスでシミュレーションを検索
     */
    List<LearningSimulation> findByUserIdAndSimulationTypeAndStatus(
            UUID userId, SimulationType simulationType, SimulationStatus status);

    /**
     * シミュレーション種類でシミュレーションを検索
     */
    Page<LearningSimulation> findBySimulationType(SimulationType simulationType, Pageable pageable);

    /**
     * ステータスでシミュレーションを検索
     */
    Page<LearningSimulation> findByStatus(SimulationStatus status, Pageable pageable);

    /**
     * 難易度レベルでシミュレーションを検索
     */
    Page<LearningSimulation> findByDifficultyLevel(Integer difficultyLevel, Pageable pageable);

    /**
     * ユーザーIDと難易度レベルでシミュレーションを検索
     */
    List<LearningSimulation> findByUserIdAndDifficultyLevel(UUID userId, Integer difficultyLevel);

    /**
     * 特定の期間に作成されたシミュレーションを検索
     */
    @Query("SELECT s FROM LearningSimulation s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<LearningSimulation> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 特定の期間に完了されたシミュレーションを検索
     */
    @Query("SELECT s FROM LearningSimulation s WHERE s.completedAt BETWEEN :startDate AND :endDate")
    List<LearningSimulation> findByCompletedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * ユーザーの完了済みシミュレーション数を取得
     */
    @Query("SELECT COUNT(s) FROM LearningSimulation s WHERE s.userId = :userId AND s.status = 'COMPLETED'")
    Long countCompletedSimulationsByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの特定のシミュレーション種類の完了済み数を取得
     */
    @Query("SELECT COUNT(s) FROM LearningSimulation s WHERE s.userId = :userId AND s.simulationType = :type AND s.status = 'COMPLETED'")
    Long countCompletedSimulationsByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") SimulationType type);

    /**
     * ユーザーの平均スコアを取得
     */
    @Query("SELECT AVG(s.score) FROM LearningSimulation s WHERE s.userId = :userId AND s.status = 'COMPLETED' AND s.score IS NOT NULL")
    Double findAverageScoreByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの特定のシミュレーション種類の平均スコアを取得
     */
    @Query("SELECT AVG(s.score) FROM LearningSimulation s WHERE s.userId = :userId AND s.simulationType = :type AND s.status = 'COMPLETED' AND s.score IS NOT NULL")
    Double findAverageScoreByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") SimulationType type);

    /**
     * ユーザーの最高スコアを取得
     */
    @Query("SELECT MAX(s.score) FROM LearningSimulation s WHERE s.userId = :userId AND s.status = 'COMPLETED' AND s.score IS NOT NULL")
    Optional<Double> findMaxScoreByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの総実行時間を取得（分）
     */
    @Query("SELECT SUM(s.actualDurationMinutes) FROM LearningSimulation s WHERE s.userId = :userId AND s.actualDurationMinutes IS NOT NULL")
    Optional<Long> findTotalDurationByUserId(@Param("userId") UUID userId);

    /**
     * 実行中のシミュレーションを検索
     */
    @Query("SELECT s FROM LearningSimulation s WHERE s.status = 'IN_PROGRESS' AND s.lastAccessedAt < :cutoffTime")
    List<LearningSimulation> findStaleInProgressSimulations(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 最大試行回数に達したシミュレーションを検索
     */
    @Query("SELECT s FROM LearningSimulation s WHERE s.userId = :userId AND s.attemptsCount >= s.maxAttempts AND s.maxAttempts IS NOT NULL")
    List<LearningSimulation> findMaxAttemptsReachedByUserId(@Param("userId") UUID userId);

    /**
     * 特定の進行率以上のシミュレーションを検索
     */
    @Query("SELECT s FROM LearningSimulation s WHERE s.userId = :userId AND s.progressPercentage >= :minProgress")
    List<LearningSimulation> findByUserIdAndProgressPercentageGreaterThanEqual(
            @Param("userId") UUID userId,
            @Param("minProgress") Double minProgress);

    /**
     * ユーザーの最新のシミュレーションを指定件数取得
     */
    List<LearningSimulation> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * ユーザーの最近アクセスしたシミュレーションを指定件数取得
     */
    List<LearningSimulation> findTop10ByUserIdOrderByLastAccessedAtDesc(UUID userId);

    /**
     * 特定の期間にアクセスされたシミュレーションを検索
     */
    @Query("SELECT s FROM LearningSimulation s WHERE s.lastAccessedAt BETWEEN :startDate AND :endDate")
    List<LearningSimulation> findByLastAccessedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * シミュレーション種類別の統計情報を取得
     */
    @Query("SELECT s.simulationType, COUNT(s), AVG(s.score), MAX(s.score), AVG(s.actualDurationMinutes) " +
            "FROM LearningSimulation s WHERE s.status = 'COMPLETED' GROUP BY s.simulationType")
    List<Object[]> getSimulationStatisticsByType();

    /**
     * ユーザー別のシミュレーション種類別統計情報を取得
     */
    @Query("SELECT s.simulationType, COUNT(s), AVG(s.score), MAX(s.score), AVG(s.actualDurationMinutes) " +
            "FROM LearningSimulation s WHERE s.userId = :userId AND s.status = 'COMPLETED' GROUP BY s.simulationType")
    List<Object[]> getSimulationStatisticsByUserIdAndType(@Param("userId") UUID userId);

    /**
     * 日別の完了済みシミュレーション数を取得
     */
    @Query("SELECT DATE(s.completedAt), COUNT(s) FROM LearningSimulation s " +
            "WHERE s.status = 'COMPLETED' AND s.completedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(s.completedAt) ORDER BY DATE(s.completedAt)")
    List<Object[]> getDailyCompletionStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * ユーザーの日別の完了済みシミュレーション数を取得
     */
    @Query("SELECT DATE(s.completedAt), COUNT(s) FROM LearningSimulation s " +
            "WHERE s.userId = :userId AND s.status = 'COMPLETED' AND s.completedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(s.completedAt) ORDER BY DATE(s.completedAt)")
    List<Object[]> getDailyCompletionStatsByUserId(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * タイトルの部分一致検索
     */
    Page<LearningSimulation> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * ユーザーIDとタイトルの部分一致検索
     */
    Page<LearningSimulation> findByUserIdAndTitleContainingIgnoreCase(
            UUID userId, String title, Pageable pageable);

    /**
     * 説明の部分一致検索
     */
    Page<LearningSimulation> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * 複数条件での検索（動的クエリ用）
     */
    @Query("SELECT s FROM LearningSimulation s WHERE " +
            "(:userId IS NULL OR s.userId = :userId) AND " +
            "(:simulationType IS NULL OR s.simulationType = :simulationType) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:difficultyLevel IS NULL OR s.difficultyLevel = :difficultyLevel) AND " +
            "(:minScore IS NULL OR s.score >= :minScore) AND " +
            "(:maxScore IS NULL OR s.score <= :maxScore)")
    Page<LearningSimulation> findByMultipleCriteria(
            @Param("userId") UUID userId,
            @Param("simulationType") SimulationType simulationType,
            @Param("status") SimulationStatus status,
            @Param("difficultyLevel") Integer difficultyLevel,
            @Param("minScore") Double minScore,
            @Param("maxScore") Double maxScore,
            Pageable pageable);

    /**
     * 特定のユーザーが特定のシミュレーション種類で最後に完了したシミュレーションを取得
     */
    Optional<LearningSimulation> findTopByUserIdAndSimulationTypeAndStatusOrderByCompletedAtDesc(
            UUID userId, SimulationType simulationType, SimulationStatus status);

    /**
     * 制限時間を超過したシミュレーションを検索
     */
    @Query("SELECT s FROM LearningSimulation s WHERE s.estimatedDurationMinutes IS NOT NULL " +
            "AND s.actualDurationMinutes IS NOT NULL AND s.actualDurationMinutes > s.estimatedDurationMinutes")
    List<LearningSimulation> findOverTimeSimulations();

    /**
     * ユーザーの制限時間を超過したシミュレーションを検索
     */
    @Query("SELECT s FROM LearningSimulation s WHERE s.userId = :userId " +
            "AND s.estimatedDurationMinutes IS NOT NULL AND s.actualDurationMinutes IS NOT NULL " +
            "AND s.actualDurationMinutes > s.estimatedDurationMinutes")
    List<LearningSimulation> findOverTimeSimulationsByUserId(@Param("userId") UUID userId);
}
