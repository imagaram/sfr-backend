package com.sfr.tokyo.sfr_backend.repository.config;

import com.sfr.tokyo.sfr_backend.entity.config.SfrPointConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SFRポイント設定リポジトリ
 * Phase 1 実装: SFRポイントシステム設定管理
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Repository
public interface SfrPointConfigRepository extends JpaRepository<SfrPointConfig, Long> {

    /**
     * アクティブな設定をキーとスペースIDで検索
     */
    Optional<SfrPointConfig> findByConfigKeyAndSpaceIdAndIsActiveTrue(String configKey, Long spaceId);

    /**
     * スペース固有のアクティブな設定一覧を取得
     */
    List<SfrPointConfig> findBySpaceIdAndIsActiveTrueOrderByConfigKey(Long spaceId);

    /**
     * 設定キーでアクティブな設定一覧を取得
     */
    List<SfrPointConfig> findByConfigKeyAndIsActiveTrueOrderBySpaceId(String configKey);

    /**
     * 設定の存在確認
     */
    boolean existsByConfigKeyAndSpaceIdAndIsActiveTrue(String configKey, Long spaceId);

    /**
     * 設定の無効化
     */
    @Query("UPDATE SfrPointConfig s SET s.isActive = false, s.updatedBy = :updatedBy WHERE s.configKey = :configKey AND s.spaceId = :spaceId")
    int deactivateConfig(@Param("configKey") String configKey, @Param("spaceId") Long spaceId, @Param("updatedBy") String updatedBy);

    /**
     * バージョン指定での設定更新
     */
    @Query("UPDATE SfrPointConfig s SET s.configValue = :configValue, s.updatedBy = :updatedBy WHERE s.id = :id AND s.version = :version")
    int updateConfigWithVersion(@Param("id") Long id, @Param("configValue") String configValue, 
                               @Param("updatedBy") String updatedBy, @Param("version") Long version);
}
