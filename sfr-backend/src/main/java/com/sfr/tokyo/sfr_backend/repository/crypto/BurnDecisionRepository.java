package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.BurnDecision;
import com.sfr.tokyo.sfr_backend.entity.crypto.BurnDecision.TriggerReason;
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
 * BurnDecisionRepository
 * SFR バーン決定のデータアクセス層
 * バーン決定履歴、統計、分析などを提供
 */
@Repository
public interface BurnDecisionRepository
                extends JpaRepository<BurnDecision, Long>, JpaSpecificationExecutor<BurnDecision> {

        // ===== 基本検索メソッド =====

        /**
         * 決定者IDでバーン決定を検索
         */
        List<BurnDecision> findByDecisionMakerId(UUID decisionMakerId);

        /**
         * 決定者IDでバーン決定をページネーション付きで検索
         */
        Page<BurnDecision> findByDecisionMakerId(UUID decisionMakerId, Pageable pageable);

        /**
         * スペースIDでバーン決定を検索
         */
        List<BurnDecision> findBySpaceId(Long spaceId);

        /**
         * スペースIDでバーン決定をページネーション付きで検索
         */
        Page<BurnDecision> findBySpaceId(Long spaceId, Pageable pageable);

        /**
         * 決定者IDとスペースIDでバーン決定を検索
         */
        List<BurnDecision> findByDecisionMakerIdAndSpaceId(UUID decisionMakerId, Long spaceId);

        /**
         * バーン理由で検索
         */
        List<BurnDecision> findByTriggerReason(TriggerReason triggerReason);

        /**
         * 複数の決定者IDでバーン決定を検索
         */
        List<BurnDecision> findByDecisionMakerIdIn(List<UUID> decisionMakerIds);

        // ===== 金額・日時ベース検索 =====

        /**
         * 指定金額以上のバーン決定を検索
         */
        @Query("SELECT bd FROM BurnDecision bd WHERE bd.actualBurnAmount >= :minAmount ORDER BY bd.actualBurnAmount DESC")
        List<BurnDecision> findByActualBurnAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount,
                        Pageable pageable);

        /**
         * 金額範囲でバーン決定を検索
         */
        @Query("SELECT bd FROM BurnDecision bd WHERE bd.actualBurnAmount BETWEEN :minAmount AND :maxAmount ORDER BY bd.actualBurnAmount DESC")
        Page<BurnDecision> findByActualBurnAmountBetween(@Param("minAmount") BigDecimal minAmount,
                        @Param("maxAmount") BigDecimal maxAmount,
                        Pageable pageable);

        /**
         * 決定日時範囲で検索
         */
        @Query("SELECT bd FROM BurnDecision bd WHERE bd.decisionDate BETWEEN :startDate AND :endDate ORDER BY bd.decisionDate DESC")
        Page<BurnDecision> findByDecisionDateBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * 指定日時以降のバーン決定を検索
         */
        @Query("SELECT bd FROM BurnDecision bd WHERE bd.decisionDate >= :since ORDER BY bd.decisionDate DESC")
        Page<BurnDecision> findByDecisionDateAfter(@Param("since") LocalDateTime since, Pageable pageable);

        // ===== ユーザー別統計メソッド =====

        /**
         * ユーザーの総バーン金額を計算
         */
        @Query("SELECT SUM(bd.actualBurnAmount) FROM BurnDecision bd WHERE bd.decisionMakerId = :decisionMakerId")
        BigDecimal getTotalBurnsByUser(@Param("decisionMakerId") UUID decisionMakerId);

        /**
         * ユーザーの平均バーン金額を計算
         */
        @Query("SELECT AVG(bd.actualBurnAmount) FROM BurnDecision bd WHERE bd.decisionMakerId = :decisionMakerId")
        BigDecimal getAverageBurnsByUser(@Param("decisionMakerId") UUID decisionMakerId);

        /**
         * ユーザーのバーン決定回数をカウント
         */
        @Query("SELECT COUNT(bd) FROM BurnDecision bd WHERE bd.decisionMakerId = :decisionMakerId")
        Long countBurnsByUser(@Param("decisionMakerId") UUID decisionMakerId);

        /**
         * ユーザーの特定期間のバーン合計を計算
         */
        @Query("SELECT SUM(bd.actualBurnAmount) FROM BurnDecision bd WHERE bd.decisionMakerId = :decisionMakerId " +
                        "AND bd.decisionDate BETWEEN :startDate AND :endDate")
        BigDecimal getUserBurnsByPeriod(@Param("decisionMakerId") UUID decisionMakerId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * ユーザーのバーン理由別統計を取得
         */
        @Query("SELECT bd.triggerReason, COUNT(bd), SUM(bd.actualBurnAmount), AVG(bd.actualBurnAmount) " +
                        "FROM BurnDecision bd WHERE bd.decisionMakerId = :decisionMakerId " +
                        "GROUP BY bd.triggerReason ORDER BY SUM(bd.actualBurnAmount) DESC")
        List<Object[]> getUserBurnReasonStatistics(@Param("decisionMakerId") UUID decisionMakerId);

        // ===== スペース別統計メソッド =====

        /**
         * スペースの総バーン金額を計算
         */
        @Query("SELECT SUM(bd.actualBurnAmount) FROM BurnDecision bd WHERE bd.spaceId = :spaceId")
        BigDecimal getTotalBurnsBySpace(@Param("spaceId") Long spaceId);

        /**
         * スペースの平均バーン金額を計算
         */
        @Query("SELECT AVG(bd.actualBurnAmount) FROM BurnDecision bd WHERE bd.spaceId = :spaceId")
        BigDecimal getAverageBurnsBySpace(@Param("spaceId") Long spaceId);

        /**
         * スペースのバーン決定回数をカウント
         */
        @Query("SELECT COUNT(bd) FROM BurnDecision bd WHERE bd.spaceId = :spaceId")
        Long countBurnsBySpace(@Param("spaceId") Long spaceId);

        /**
         * スペースの特定期間のバーン統計を取得
         */
        @Query("SELECT SUM(bd.actualBurnAmount), AVG(bd.actualBurnAmount), COUNT(bd) FROM BurnDecision bd " +
                        "WHERE bd.spaceId = :spaceId AND bd.decisionDate BETWEEN :startDate AND :endDate")
        Object[] getSpaceBurnStatsByPeriod(@Param("spaceId") Long spaceId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // ===== バーン理由別統計 =====

        /**
         * バーン理由別の統計情報を取得
         */
        @Query("SELECT bd.triggerReason, COUNT(bd), SUM(bd.actualBurnAmount), AVG(bd.actualBurnAmount) " +
                        "FROM BurnDecision bd GROUP BY bd.triggerReason ORDER BY SUM(bd.actualBurnAmount) DESC")
        List<Object[]> getBurnReasonStatistics();

        /**
         * 特定バーン理由の詳細統計を取得
         */
        @Query("SELECT COUNT(bd) as burnCount, " +
                        "SUM(bd.actualBurnAmount) as totalAmount, " +
                        "AVG(bd.actualBurnAmount) as averageAmount, " +
                        "MAX(bd.actualBurnAmount) as maxAmount, " +
                        "MIN(bd.actualBurnAmount) as minAmount " +
                        "FROM BurnDecision bd WHERE bd.triggerReason = :burnReason")
        Object[] getDetailedBurnReasonStatistics(@Param("burnReason") TriggerReason burnReason);

        // ===== 時間ベース集計 =====

        /**
         * 日別バーン統計を取得
         */
        @Query("SELECT DATE(bd.decisionDate) as date, COUNT(bd), SUM(bd.actualBurnAmount), AVG(bd.actualBurnAmount) " +
                        "FROM BurnDecision bd WHERE bd.decisionDate >= :since " +
                        "GROUP BY DATE(bd.decisionDate) ORDER BY DATE(bd.decisionDate) DESC")
        List<Object[]> getDailyBurnStatistics(@Param("since") LocalDateTime since);

        /**
         * 月別バーン統計を取得
         */
        @Query("SELECT YEAR(bd.decisionDate), MONTH(bd.decisionDate), COUNT(bd), SUM(bd.actualBurnAmount), AVG(bd.actualBurnAmount) "
                        +
                        "FROM BurnDecision bd WHERE bd.decisionDate >= :since " +
                        "GROUP BY YEAR(bd.decisionDate), MONTH(bd.decisionDate) " +
                        "ORDER BY YEAR(bd.decisionDate) DESC, MONTH(bd.decisionDate) DESC")
        List<Object[]> getMonthlyBurnStatistics(@Param("since") LocalDateTime since);

        /**
         * 時間別バーン統計を取得
         */
        @Query("SELECT HOUR(bd.decisionDate) as hour, COUNT(bd), SUM(bd.actualBurnAmount), AVG(bd.actualBurnAmount) " +
                        "FROM BurnDecision bd WHERE bd.decisionDate >= :since " +
                        "GROUP BY HOUR(bd.decisionDate) ORDER BY HOUR(bd.decisionDate)")
        List<Object[]> getHourlyBurnStatistics(@Param("since") LocalDateTime since);

        // ===== ランキング・トップユーザー =====

        /**
         * 総バーン金額でユーザーランキングを取得
         */
        @Query("SELECT bd.decisionMakerId, SUM(bd.actualBurnAmount) as totalBurn FROM BurnDecision bd " +
                        "GROUP BY bd.decisionMakerId ORDER BY totalBurn DESC")
        Page<Object[]> getUserBurnRanking(Pageable pageable);

        /**
         * 特定期間のバーン金額でユーザーランキングを取得
         */
        @Query("SELECT bd.decisionMakerId, SUM(bd.actualBurnAmount) as totalBurn FROM BurnDecision bd " +
                        "WHERE bd.decisionDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY bd.decisionMakerId ORDER BY totalBurn DESC")
        Page<Object[]> getUserBurnRankingByPeriod(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * スペース別バーンランキングを取得
         */
        @Query("SELECT bd.spaceId, SUM(bd.actualBurnAmount) as totalBurn FROM BurnDecision bd " +
                        "GROUP BY bd.spaceId ORDER BY totalBurn DESC")
        Page<Object[]> getSpaceBurnRanking(Pageable pageable);

        /**
         * バーン回数でユーザーランキングを取得
         */
        @Query("SELECT bd.decisionMakerId, COUNT(bd) as burnCount FROM BurnDecision bd " +
                        "GROUP BY bd.decisionMakerId ORDER BY burnCount DESC")
        Page<Object[]> getUserBurnCountRanking(Pageable pageable);

        // ===== 高度な分析クエリ =====

        /**
         * ユーザーのバーントレンドを取得
         */
        @Query("SELECT DATE(bd.decisionDate) as date, SUM(bd.actualBurnAmount) as dailyBurn " +
                        "FROM BurnDecision bd WHERE bd.decisionMakerId = :decisionMakerId " +
                        "AND bd.decisionDate >= :since " +
                        "GROUP BY DATE(bd.decisionDate) ORDER BY DATE(bd.decisionDate) ASC")
        List<Object[]> getUserBurnTrend(@Param("decisionMakerId") UUID decisionMakerId,
                        @Param("since") LocalDateTime since);

        /**
         * バーン理由別の期間比較統計を取得
         */
        @Query("SELECT bd.triggerReason, " +
                        "SUM(CASE WHEN bd.decisionDate BETWEEN :period1Start AND :period1End THEN bd.actualBurnAmount ELSE 0 END) as period1Total, "
                        +
                        "SUM(CASE WHEN bd.decisionDate BETWEEN :period2Start AND :period2End THEN bd.actualBurnAmount ELSE 0 END) as period2Total "
                        +
                        "FROM BurnDecision bd " +
                        "WHERE bd.decisionDate BETWEEN :period1Start AND :period2End " +
                        "GROUP BY bd.triggerReason")
        List<Object[]> getBurnReasonComparison(@Param("period1Start") LocalDateTime period1Start,
                        @Param("period1End") LocalDateTime period1End,
                        @Param("period2Start") LocalDateTime period2Start,
                        @Param("period2End") LocalDateTime period2End);

        /**
         * ユーザーのバーンパフォーマンス分析
         */
        @Query("SELECT bd.decisionMakerId, " +
                        "COUNT(bd) as totalBurns, " +
                        "SUM(bd.actualBurnAmount) as totalAmount, " +
                        "AVG(bd.actualBurnAmount) as avgAmount, " +
                        "MAX(bd.actualBurnAmount) as maxAmount, " +
                        "MIN(bd.actualBurnAmount) as minAmount " +
                        "FROM BurnDecision bd WHERE bd.decisionDate >= :since " +
                        "GROUP BY bd.decisionMakerId " +
                        "ORDER BY totalAmount DESC")
        Page<Object[]> getUserBurnPerformance(@Param("since") LocalDateTime since, Pageable pageable);

        // ===== 複合条件検索 =====

        /**
         * 複合条件でバーン決定を検索（ユーザー + 金額範囲 + 期間）
         */
        @Query("SELECT bd FROM BurnDecision bd WHERE " +
                        "bd.decisionMakerId = :decisionMakerId AND " +
                        "bd.actualBurnAmount BETWEEN :minAmount AND :maxAmount AND " +
                        "bd.decisionDate BETWEEN :startDate AND :endDate " +
                        "ORDER BY bd.decisionDate DESC")
        Page<BurnDecision> findByComplexConditions(@Param("decisionMakerId") UUID decisionMakerId,
                        @Param("minAmount") BigDecimal minAmount,
                        @Param("maxAmount") BigDecimal maxAmount,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * スペースとバーン理由での複合検索
         */
        @Query("SELECT bd FROM BurnDecision bd WHERE " +
                        "bd.spaceId = :spaceId AND " +
                        "bd.triggerReason = :burnReason AND " +
                        "bd.decisionDate >= :since " +
                        "ORDER BY bd.actualBurnAmount DESC")
        Page<BurnDecision> findBySpaceAndReasonAndDate(@Param("spaceId") Long spaceId,
                        @Param("burnReason") TriggerReason burnReason,
                        @Param("since") LocalDateTime since,
                        Pageable pageable);

        // ===== カスタムネイティブクエリ =====

        /**
         * バーンの詳細ランキング（パフォーマンス最適化）
         */
        @Query(value = """
                        SELECT bd.*,
                                 ROW_NUMBER() OVER (ORDER BY bd.actual_burn_amount DESC) as amount_rank,
                                 DENSE_RANK() OVER (ORDER BY bd.actual_burn_amount DESC) as amount_dense_rank
                        FROM burn_decisions bd
                         WHERE bd.decision_date >= :since
                         ORDER BY bd.actual_burn_amount DESC
                        """, nativeQuery = true)
        List<Object[]> getDetailedBurnRanking(@Param("since") LocalDateTime since, Pageable pageable);

        /**
         * バーン金額分布のヒストグラムデータを取得
         */
        @Query(value = """
                        SELECT
                                            CASE
                                                    WHEN actual_burn_amount = 0 THEN '0'
                                                    WHEN actual_burn_amount BETWEEN 0.01 AND 10 THEN '0.01-10'
                                                    WHEN actual_burn_amount BETWEEN 10.01 AND 100 THEN '10-100'
                                                    WHEN actual_burn_amount BETWEEN 100.01 AND 1000 THEN '100-1K'
                                                    WHEN actual_burn_amount BETWEEN 1000.01 AND 10000 THEN '1K-10K'
                                ELSE '10K+'
                            END as amount_range,
                            COUNT(*) as burn_count,
                                            SUM(actual_burn_amount) as total_amount,
                                            AVG(actual_burn_amount) as average_amount
                        FROM burn_decisions
                        GROUP BY amount_range
                                    ORDER BY MIN(actual_burn_amount)
                        """, nativeQuery = true)
        List<Object[]> getBurnAmountDistribution();

        /**
         * バーン決定効率分析
         */
        @Query(value = """
                        SELECT
                                            decision_maker_id,
                            COUNT(*) as total_burns,
                                            SUM(actual_burn_amount) as total_amount,
                                            AVG(actual_burn_amount) as avg_amount,
                                            MIN(actual_burn_amount) as min_amount,
                                            MAX(actual_burn_amount) as max_amount,
                                            STDDEV(actual_burn_amount) as amount_stddev,
                                            COUNT(DISTINCT trigger_reason) as burn_reasons_used
                        FROM burn_decisions
                                    WHERE decision_date >= :since
                                    GROUP BY decision_maker_id
                        HAVING COUNT(*) >= :minBurns
                        ORDER BY total_amount DESC
                        """, nativeQuery = true)
        List<Object[]> getUserBurnEfficiencyAnalysis(@Param("since") LocalDateTime since,
                        @Param("minBurns") int minBurns,
                        Pageable pageable);

        /**
         * バーン理由ごとの時系列分析
         */
        @Query(value = """
                        SELECT
                                            trigger_reason,
                                            DATE(decision_date) as burn_date,
                            COUNT(*) as daily_burns,
                                            SUM(actual_burn_amount) as daily_amount,
                                            AVG(actual_burn_amount) as avg_amount
                        FROM burn_decisions
                                    WHERE decision_date >= :since
                                    GROUP BY trigger_reason, DATE(decision_date)
                                    ORDER BY trigger_reason, burn_date
                        """, nativeQuery = true)
        List<Object[]> getBurnReasonTimeSeriesAnalysis(@Param("since") LocalDateTime since);
}
