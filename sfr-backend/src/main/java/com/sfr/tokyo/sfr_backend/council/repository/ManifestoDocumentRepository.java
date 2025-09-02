package com.sfr.tokyo.sfr_backend.council.repository;

import com.sfr.tokyo.sfr_backend.entity.council.ManifestoDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ManifestoDocumentリポジトリ
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
 */
@Repository
public interface ManifestoDocumentRepository extends JpaRepository<ManifestoDocument, Long> {

    /**
     * 現在アクティブなManifesto文書を取得
     */
    Optional<ManifestoDocument> findByIsActiveTrue();

    /**
     * バージョン別Manifesto文書を取得
     */
    Optional<ManifestoDocument> findByVersion(String version);

    /**
     * 公開フェーズ別Manifesto文書一覧を取得
     */
    List<ManifestoDocument> findByPhaseOrderByUpdatedAtDesc(String phase);

    /**
     * 指定期間内に更新されたManifesto文書一覧を取得
     */
    @Query("SELECT md FROM ManifestoDocument md WHERE md.updatedAt BETWEEN :startDate AND :endDate ORDER BY md.updatedAt DESC")
    List<ManifestoDocument> findByUpdatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * バージョン番号でソートしたManifesto文書一覧を取得
     * H2データベース用のクエリ（MySQL用のUNSIGNEDは使用しない）
     */
    @Query("SELECT md FROM ManifestoDocument md ORDER BY md.version DESC")
    List<ManifestoDocument> findAllOrderByVersionDesc();

    /**
     * 最新バージョンのManifesto文書を取得
     */
    @Query("SELECT md FROM ManifestoDocument md WHERE md.version = (" +
           "SELECT MAX(md2.version) FROM ManifestoDocument md2)")
    Optional<ManifestoDocument> findLatestVersion();

    /**
     * 作成者別Manifesto文書数を取得
     */
    @Query("SELECT COUNT(md) FROM ManifestoDocument md WHERE md.authors LIKE %:author%")
    Long countByAuthorsContaining(@Param("author") String author);

    /**
     * アクティブなManifestoドキュメントを最新バージョン順で取得（外部API用）
     */
    List<ManifestoDocument> findByIsActiveTrueOrderByVersionDesc();

    /**
     * 利用可能なバージョン一覧を取得（外部API用）
     */
    @Query("SELECT DISTINCT md.version FROM ManifestoDocument md ORDER BY md.version DESC")
    List<String> findDistinctVersions();
}
