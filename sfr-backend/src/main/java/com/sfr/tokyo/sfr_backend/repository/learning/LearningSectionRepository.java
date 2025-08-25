package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningSectionRepository extends JpaRepository<LearningSection, Long> {

    /**
     * コンテンツIDでセクションを表示順序でソートして検索
     * 
     * @param contentId コンテンツID
     * @return セクションリスト
     */
    List<LearningSection> findByContentIdOrderByDisplayOrderAsc(Long contentId);

    /**
     * コンテンツIDとセクションIDでセクションを検索
     * 
     * @param contentId コンテンツID
     * @param id        セクションID
     * @return セクション（Optional）
     */
    Optional<LearningSection> findByContentIdAndId(Long contentId, Long id);

    /**
     * マテリアル情報も含めてセクションを取得
     * 
     * @param id セクションID
     * @return セクション（Optional）
     */
    @Query("SELECT s FROM LearningSection s LEFT JOIN FETCH s.materials WHERE s.id = :id")
    Optional<LearningSection> findByIdWithMaterials(@Param("id") Long id);

    /**
     * コンテンツのセクション数を取得
     * 
     * @param contentId コンテンツID
     * @return セクション数
     */
    long countByContentId(Long contentId);

    /**
     * 指定した表示順序以降のセクションを取得
     * 
     * @param contentId    コンテンツID
     * @param displayOrder 表示順序
     * @return セクションリスト
     */
    List<LearningSection> findByContentIdAndDisplayOrderGreaterThanEqualOrderByDisplayOrderAsc(Long contentId,
            Integer displayOrder);
}
