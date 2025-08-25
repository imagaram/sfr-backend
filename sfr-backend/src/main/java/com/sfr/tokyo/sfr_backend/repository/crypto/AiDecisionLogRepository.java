package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.AiDecisionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AiDecisionLogRepository
 * SFR AI決定ログのデータアクセス層
 */
@Repository
public interface AiDecisionLogRepository
        extends JpaRepository<AiDecisionLog, Long>, JpaSpecificationExecutor<AiDecisionLog> {

    // ===== 基本検索メソッド =====
    // 実行者ベースの検索（旧: userId）
    List<AiDecisionLog> findByExecutedBy(UUID executedBy);

    Page<AiDecisionLog> findByExecutedBy(UUID executedBy, Pageable pageable);

    List<AiDecisionLog> findBySpaceId(Long spaceId);

    Page<AiDecisionLog> findBySpaceId(Long spaceId, Pageable pageable);

    List<AiDecisionLog> findByDecisionType(AiDecisionLog.DecisionType decisionType);

    List<AiDecisionLog> findByModelVersion(String modelVersion);

    // ===== 決定結果ベース検索 =====
    @Query("SELECT adl FROM AiDecisionLog adl WHERE adl.impactScore >= :minImpact ORDER BY adl.impactScore DESC")
    List<AiDecisionLog> findByImpactScoreGreaterThanEqual(@Param("minImpact") BigDecimal minImpact,
            Pageable pageable);

    @Query("SELECT adl FROM AiDecisionLog adl WHERE adl.confidenceScore >= :minConfidence ORDER BY adl.confidenceScore DESC")
    List<AiDecisionLog> findByConfidenceScoreGreaterThanEqual(@Param("minConfidence") BigDecimal minConfidence,
            Pageable pageable);

    // ===== 日時ベース検索 =====
    @Query("SELECT adl FROM AiDecisionLog adl WHERE adl.decisionDate BETWEEN :startDate AND :endDate ORDER BY adl.decisionDate DESC")
    Page<AiDecisionLog> findByDecisionDateBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT adl FROM AiDecisionLog adl WHERE adl.decisionDate >= :since ORDER BY adl.decisionDate DESC")
    Page<AiDecisionLog> findByDecisionDateAfter(@Param("since") LocalDateTime since, Pageable pageable);

    // ===== 統計メソッド =====
    @Query("SELECT COUNT(adl) FROM AiDecisionLog adl WHERE adl.executedBy = :userId")
    Long countDecisionsByExecutor(@Param("userId") UUID userId);

    @Query("SELECT COUNT(adl) FROM AiDecisionLog adl WHERE adl.spaceId = :spaceId")
    Long countDecisionsBySpace(@Param("spaceId") Long spaceId);

    @Query("SELECT adl.decisionType, COUNT(adl), AVG(adl.confidenceScore) FROM AiDecisionLog adl GROUP BY adl.decisionType ORDER BY COUNT(adl) DESC")
    List<Object[]> getDecisionTypeStatistics();

    @Query("SELECT AVG(adl.confidenceScore) FROM AiDecisionLog adl WHERE adl.decisionType = :decisionType")
    BigDecimal getAverageConfidenceByType(@Param("decisionType") AiDecisionLog.DecisionType decisionType);

    @Query("SELECT AVG(adl.impactScore) FROM AiDecisionLog adl WHERE adl.decisionType = :decisionType")
    BigDecimal getAverageImpactScoreByType(@Param("decisionType") AiDecisionLog.DecisionType decisionType);

    // ===== 複合条件検索 =====
    @Query("SELECT adl FROM AiDecisionLog adl WHERE adl.spaceId = :spaceId AND adl.decisionType = :decisionType AND adl.decisionDate >= :since ORDER BY adl.decisionDate DESC")
    Page<AiDecisionLog> findBySpaceAndTypeAndDate(@Param("spaceId") Long spaceId,
            @Param("decisionType") AiDecisionLog.DecisionType decisionType,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    // ===== AI パフォーマンス分析 =====
    @Query("SELECT adl.modelVersion, COUNT(adl), AVG(adl.confidenceScore), AVG(adl.impactScore) FROM AiDecisionLog adl GROUP BY adl.modelVersion ORDER BY AVG(adl.confidenceScore) DESC")
    List<Object[]> getModelPerformanceStatistics();

    @Query("SELECT DATE(adl.decisionDate) as date, COUNT(adl), AVG(adl.confidenceScore) FROM AiDecisionLog adl WHERE adl.decisionDate >= :since GROUP BY DATE(adl.decisionDate) ORDER BY DATE(adl.decisionDate) DESC")
    List<Object[]> getDailyAiPerformance(@Param("since") LocalDateTime since);
}
