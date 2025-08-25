package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningContentRepository extends JpaRepository<LearningContent, Long> {

    /**
     * 学習空間IDでコンテンツを検索
     * 
     * @param spaceId 学習空間ID
     * @return コンテンツリスト
     */
    List<LearningContent> findBySpaceIdOrderByIdAsc(Long spaceId);

    /**
     * 学習空間IDとコンテンツタイプでコンテンツを検索
     * 
     * @param spaceId 学習空間ID
     * @param type    コンテンツタイプ
     * @return コンテンツリスト
     */
    List<LearningContent> findBySpaceIdAndTypeOrderByIdAsc(Long spaceId, LearningContent.ContentType type);

    /**
     * 学習空間IDとコンテンツIDでコンテンツを検索
     * 
     * @param spaceId 学習空間ID
     * @param id      コンテンツID
     * @return コンテンツ（Optional）
     */
    Optional<LearningContent> findBySpaceIdAndId(Long spaceId, Long id);

    /**
     * セクション情報も含めてコンテンツを取得
     * 
     * @param id コンテンツID
     * @return コンテンツ（Optional）
     */
    @Query("SELECT c FROM LearningContent c LEFT JOIN FETCH c.sections s LEFT JOIN FETCH s.materials WHERE c.id = :id")
    Optional<LearningContent> findByIdWithSectionsAndMaterials(@Param("id") Long id);

    /**
     * 学習空間のコンテンツ数を取得
     * 
     * @param spaceId 学習空間ID
     * @return コンテンツ数
     */
    long countBySpaceId(Long spaceId);

    /**
     * コンテンツタイプ別のコンテンツ数を取得
     * 
     * @param spaceId 学習空間ID
     * @param type    コンテンツタイプ
     * @return コンテンツ数
     */
    long countBySpaceIdAndType(Long spaceId, LearningContent.ContentType type);
}
