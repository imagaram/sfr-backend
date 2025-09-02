package com.sfr.tokyo.sfr_backend.council.repository;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 評議員制度パラメーターリポジトリ
 */
@Repository
public interface CouncilParameterRepository extends JpaRepository<CouncilParameter, Long> {

    /**
     * パラメーターキーによる検索
     */
    Optional<CouncilParameter> findByParamKey(String paramKey);

    /**
     * パラメーターキーの存在確認
     */
    boolean existsByParamKey(String paramKey);

    /**
     * 値の型による検索
     */
    List<CouncilParameter> findByValueType(CouncilParameter.ValueType valueType);

    /**
     * パラメーターキーの部分一致検索
     */
    @Query("SELECT cp FROM CouncilParameter cp WHERE cp.paramKey LIKE %:keyPattern%")
    List<CouncilParameter> findByParamKeyContaining(@Param("keyPattern") String keyPattern);

    /**
     * 説明文による検索
     */
    @Query("SELECT cp FROM CouncilParameter cp WHERE cp.description LIKE %:description%")
    List<CouncilParameter> findByDescriptionContaining(@Param("description") String description);

    /**
     * パラメーターキーのプレフィックス検索
     */
    @Query("SELECT cp FROM CouncilParameter cp WHERE cp.paramKey LIKE :prefix%")
    List<CouncilParameter> findByParamKeyStartingWith(@Param("prefix") String prefix);

    /**
     * 更新日時の範囲検索
     */
    @Query("SELECT cp FROM CouncilParameter cp WHERE cp.updatedAt >= :from AND cp.updatedAt <= :to")
    List<CouncilParameter> findByUpdatedAtBetween(@Param("from") java.time.Instant from, @Param("to") java.time.Instant to);
}
