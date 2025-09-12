package com.sfr.tokyo.sfr_backend.repository.space;

import com.sfr.tokyo.sfr_backend.entity.space.Space;
import com.sfr.tokyo.sfr_backend.entity.space.Space.SpaceMode;
import com.sfr.tokyo.sfr_backend.entity.space.Space.SpaceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * スペースリポジトリ
 * 
 * @author SFR Development Team
 * @version 2.0
 * @since 2025-09-11
 */
@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {

    /**
     * 名前でスペースを検索
     */
    Optional<Space> findByName(String name);

    /**
     * 人気スペース取得（指定モード）
     * 
     * @param mode スペースモード
     * @param pageable ページング情報
     * @return 人気スペースリスト
     */
    @Query("SELECT s FROM Space s WHERE s.mode = :mode AND s.status = 'ACTIVE' ORDER BY s.memberCount DESC, s.updatedAt DESC")
    List<Space> findPopularSpacesByMode(@Param("mode") Space.SpaceMode mode, Pageable pageable);

    /**
     * ステータス別カウント
     * 
     * @param status スペースステータス
     * @return カウント数
     */
    Long countByStatus(Space.SpaceStatus status);

    /**
     * 名前の存在チェック
     */
    boolean existsByName(String name);

    /**
     * オーナーIDでスペースを検索
     */
    List<Space> findByOwnerId(String ownerId);

    /**
     * モードでスペースを検索
     */
    Page<Space> findByMode(SpaceMode mode, Pageable pageable);

    /**
     * ステータスでスペースを検索
     */
    Page<Space> findByStatus(SpaceStatus status, Pageable pageable);

    /**
     * 公開スペースを検索
     */
    Page<Space> findByIsPublicTrue(Pageable pageable);

    /**
     * モードとステータスでスペースを検索
     */
    Page<Space> findByModeAndStatus(SpaceMode mode, SpaceStatus status, Pageable pageable);

    /**
     * 公開かつアクティブなスペースを検索
     */
    Page<Space> findByIsPublicTrueAndStatus(SpaceStatus status, Pageable pageable);

    /**
     * スペース名でLIKE検索
     */
    @Query("SELECT s FROM Space s WHERE s.name LIKE %:keyword% OR s.description LIKE %:keyword%")
    Page<Space> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 人気スペース（メンバー数順）
     */
    @Query("SELECT s FROM Space s WHERE s.status = 'ACTIVE' AND s.isPublic = true ORDER BY s.memberCount DESC")
    List<Space> findPopularSpaces(Pageable pageable);

    /**
     * 推奨スペース（ユーザーの興味に基づく - 今後実装）
     */
    @Query("SELECT s FROM Space s WHERE s.status = 'ACTIVE' AND s.isPublic = true ORDER BY s.createdAt DESC")
    List<Space> findRecommendedSpaces(@Param("userId") String userId, Pageable pageable);

    /**
     * 統計情報取得
     */
    @Query("SELECT s.mode, COUNT(s), AVG(s.memberCount) FROM Space s WHERE s.status = 'ACTIVE' GROUP BY s.mode")
    List<Object[]> getSpaceStatistics();

    /**
     * 特定オーナーのスペース統計
     */
    @Query("SELECT COUNT(s), SUM(s.memberCount) FROM Space s WHERE s.ownerId = :ownerId AND s.status = 'ACTIVE'")
    Object[] getOwnerSpaceStatistics(@Param("ownerId") String ownerId);

    /**
     * モード別のアクティブスペース数
     */
    long countByModeAndStatus(SpaceMode mode, SpaceStatus status);

    /**
     * 満員近いスペース検索（90%以上）
     */
    @Query("SELECT s FROM Space s WHERE s.memberCount >= s.maxMembers * 0.9 AND s.status = 'ACTIVE'")
    List<Space> findNearlyFullSpaces();
}
