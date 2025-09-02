package com.sfr.tokyo.sfr_backend.council.repository;

import com.sfr.tokyo.sfr_backend.entity.council.ManifestoDocument;
import com.sfr.tokyo.sfr_backend.entity.council.ManifestoSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ManifestoSectionリポジトリ
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
 */
@Repository
public interface ManifestoSectionRepository extends JpaRepository<ManifestoSection, Long> {

    /**
     * Manifestoドキュメント別セクション一覧を表示順で取得
     */
    List<ManifestoSection> findByManifestoDocumentOrderByDisplayOrderAsc(ManifestoDocument manifestoDocument);

    /**
     * Manifestoドキュメント別セクション一覧をページング形式で取得
     */
    Page<ManifestoSection> findByManifestoDocumentOrderByDisplayOrderAsc(ManifestoDocument manifestoDocument, Pageable pageable);

    /**
     * セクションID別検索
     */
    List<ManifestoSection> findBySectionIdOrderByManifestoDocument_VersionDesc(String sectionId);

    /**
     * カテゴリ別セクション一覧を取得
     */
    List<ManifestoSection> findByCategoryOrderByDisplayOrderAsc(String category);

    /**
     * 特定Manifestoドキュメントの特定セクションを取得
     */
    Optional<ManifestoSection> findByManifestoDocumentAndSectionId(ManifestoDocument manifestoDocument, String sectionId);

    /**
     * タグを含むセクション検索
     */
    @Query("SELECT ms FROM ManifestoSection ms WHERE ms.tags LIKE %:tag% ORDER BY ms.displayOrder ASC")
    List<ManifestoSection> findByTagsContaining(@Param("tag") String tag);

    /**
     * 指定期間内に更新されたセクション一覧を取得
     */
    @Query("SELECT ms FROM ManifestoSection ms WHERE ms.lastModified BETWEEN :startDate AND :endDate ORDER BY ms.lastModified DESC")
    List<ManifestoSection> findByLastModifiedBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Manifestoドキュメント別セクション数を取得
     */
    Long countByManifestoDocument(ManifestoDocument manifestoDocument);

    /**
     * カテゴリ別セクション数を取得
     */
    Long countByCategory(String category);
}
