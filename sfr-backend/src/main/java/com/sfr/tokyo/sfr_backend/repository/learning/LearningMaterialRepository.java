package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {

    /**
     * セクションIDでマテリアルを表示順序でソートして検索
     * 
     * @param sectionId セクションID
     * @return マテリアルリスト
     */
    List<LearningMaterial> findBySectionIdOrderByDisplayOrderAsc(Long sectionId);

    /**
     * セクションIDとマテリアルIDでマテリアルを検索
     * 
     * @param sectionId セクションID
     * @param id        マテリアルID
     * @return マテリアル（Optional）
     */
    Optional<LearningMaterial> findBySectionIdAndId(Long sectionId, Long id);

    /**
     * セクションのマテリアル数を取得
     * 
     * @param sectionId セクションID
     * @return マテリアル数
     */
    long countBySectionId(Long sectionId);

    /**
     * 指定した表示順序以降のマテリアルを取得
     * 
     * @param sectionId    セクションID
     * @param displayOrder 表示順序
     * @return マテリアルリスト
     */
    List<LearningMaterial> findBySectionIdAndDisplayOrderGreaterThanEqualOrderByDisplayOrderAsc(Long sectionId,
            Integer displayOrder);
}
