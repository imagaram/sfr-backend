package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningBadge;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningBadge.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * バッジマスターリポジトリ
 */
@Repository
public interface LearningBadgeRepository extends JpaRepository<LearningBadge, Long> {

    /**
     * アクティブなバッジ一覧を取得
     */
    List<LearningBadge> findByIsActiveTrue();

    /**
     * 指定されたタイプのアクティブバッジを取得
     */
    List<LearningBadge> findByBadgeTypeAndIsActiveTrue(BadgeType badgeType);

    /**
     * 指定されたスペースのアクティブバッジを取得
     */
    List<LearningBadge> findBySpaceIdAndIsActiveTrue(UUID spaceId);

    /**
     * グローバルバッジのみを取得（スペース特定なし）
     */
    List<LearningBadge> findBySpaceIdIsNullAndIsActiveTrue();

    /**
     * 指定されたスペースまたはグローバルのアクティブバッジを取得
     */
    @Query("SELECT lb FROM LearningBadge lb WHERE (lb.spaceId = :spaceId OR lb.spaceId IS NULL) AND lb.isActive = true")
    List<LearningBadge> findAvailableBadgesForSpace(@Param("spaceId") UUID spaceId);

    /**
     * 条件値範囲でバッジを検索（達成可能バッジの検索用）
     */
    @Query("SELECT lb FROM LearningBadge lb WHERE lb.isActive = true AND lb.requiredValue <= :currentValue AND (lb.spaceId = :spaceId OR lb.spaceId IS NULL)")
    List<LearningBadge> findAchievableBadges(@Param("currentValue") Integer currentValue,
            @Param("spaceId") UUID spaceId);

    /**
     * 指定されたタイプと条件値でバッジを検索
     */
    @Query("SELECT lb FROM LearningBadge lb WHERE lb.badgeType = :badgeType AND lb.requiredValue = :conditionValue AND lb.isActive = true AND (lb.spaceId = :spaceId OR lb.spaceId IS NULL)")
    List<LearningBadge> findByTypeAndCondition(@Param("badgeType") BadgeType badgeType,
            @Param("conditionValue") Integer conditionValue,
            @Param("spaceId") UUID spaceId);

    /**
     * バッジ名で検索（部分一致）
     */
    @Query("SELECT lb FROM LearningBadge lb WHERE lb.name LIKE %:name% AND lb.isActive = true")
    List<LearningBadge> findByNameContaining(@Param("name") String name);

    /**
     * 順序でソートされたバッジ一覧を取得
     */
    List<LearningBadge> findByIsActiveTrueOrderById();

    /**
     * 指定されたスペースの順序ソート済みバッジを取得
     */
    List<LearningBadge> findBySpaceIdAndIsActiveTrueOrderById(UUID spaceId);
}
