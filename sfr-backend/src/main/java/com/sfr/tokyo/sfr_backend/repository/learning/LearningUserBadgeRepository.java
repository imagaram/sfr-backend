package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningUserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ユーザーバッジ獲得記録リポジトリ
 */
@Repository
public interface LearningUserBadgeRepository extends JpaRepository<LearningUserBadge, Long> {

    /**
     * ユーザーが獲得した全バッジを取得
     */
    List<LearningUserBadge> findByUserId(UUID userId);

    /**
     * ユーザーが特定のバッジを獲得しているかチェック
     */
    Optional<LearningUserBadge> findByUserIdAndBadge_Id(UUID userId, Long badgeId);

    /**
     * ユーザーが特定のバッジを獲得しているかの存在チェック
     */
    boolean existsByUserIdAndBadge_Id(UUID userId, Long badgeId);

    /**
     * ユーザーの最近獲得したバッジを取得（日付降順）
     */
    List<LearningUserBadge> findByUserIdOrderByEarnedAtDesc(UUID userId);

    /**
     * ユーザーの指定期間内に獲得したバッジを取得
     */
    @Query("SELECT lub FROM LearningUserBadge lub WHERE lub.userId = :userId AND lub.earnedAt >= :since ORDER BY lub.earnedAt DESC")
    List<LearningUserBadge> findRecentBadges(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    /**
     * ユーザーの獲得バッジ数をカウント
     */
    long countByUserId(UUID userId);

    /**
     * 指定されたバッジを獲得したユーザー一覧を取得
     */
    List<LearningUserBadge> findByBadge_Id(Long badgeId);

    /**
     * 指定されたバッジを獲得したユーザー数をカウント
     */
    long countByBadge_Id(Long badgeId);

    /**
     * バッジ情報を含む獲得記録を取得（JOIN FETCH）
     */
    @Query("SELECT lub FROM LearningUserBadge lub JOIN FETCH lub.badge WHERE lub.userId = :userId ORDER BY lub.earnedAt DESC")
    List<LearningUserBadge> findByUserIdWithBadge(@Param("userId") UUID userId);

    /**
     * 最近獲得したバッジ情報を含む記録を取得
     */
    @Query("SELECT lub FROM LearningUserBadge lub JOIN FETCH lub.badge WHERE lub.userId = :userId AND lub.earnedAt >= :since ORDER BY lub.earnedAt DESC")
    List<LearningUserBadge> findRecentBadgesWithDetails(@Param("userId") UUID userId,
            @Param("since") LocalDateTime since);

    /**
     * ユーザーの指定期間内獲得バッジ数をカウント
     */
    @Query("SELECT COUNT(lub) FROM LearningUserBadge lub WHERE lub.userId = :userId AND lub.earnedAt BETWEEN :startDate AND :endDate")
    long countBadgesByPeriod(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 特定のバッジタイプを獲得したユーザーバッジを取得
     */
    @Query("SELECT lub FROM LearningUserBadge lub JOIN lub.badge b WHERE lub.userId = :userId AND b.badgeType = :badgeType ORDER BY lub.earnedAt DESC")
    List<LearningUserBadge> findByUserIdAndBadgeType(@Param("userId") UUID userId,
            @Param("badgeType") String badgeType);

    /**
     * スペース関連バッジの獲得記録を取得
     */
    @Query("SELECT lub FROM LearningUserBadge lub JOIN lub.badge b WHERE lub.userId = :userId AND b.spaceId = :spaceId ORDER BY lub.earnedAt DESC")
    List<LearningUserBadge> findByUserIdAndSpaceId(@Param("userId") UUID userId, @Param("spaceId") UUID spaceId);

    /**
     * 全ユーザーの最近獲得バッジ活動を取得（管理者用）
     */
    @Query("SELECT lub FROM LearningUserBadge lub JOIN FETCH lub.badge WHERE lub.earnedAt >= :since ORDER BY lub.earnedAt DESC")
    List<LearningUserBadge> findRecentBadgeActivity(@Param("since") LocalDateTime since);

    /**
     * バッジ獲得の上位ユーザーを取得
     */
    @Query("SELECT lub.userId, COUNT(lub) as badgeCount FROM LearningUserBadge lub GROUP BY lub.userId ORDER BY badgeCount DESC")
    List<Object[]> findTopBadgeEarners();

    /**
     * ユーザーが最後に獲得したバッジを取得
     */
    Optional<LearningUserBadge> findFirstByUserIdOrderByEarnedAtDesc(UUID userId);
}
