package com.sfr.tokyo.sfr_backend.repository;

import com.sfr.tokyo.sfr_backend.entity.CharacterStatus;
import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * キャラクター情報を管理するリポジトリ
 * 推奨インデックス：user_id, status（複合インデックス）
 */
@Repository
public interface CharacterRepository extends JpaRepository<CharacterLifecycle, Long> {

    /**
     * 名前とユーザーIDでキャラクターを検索
     */
    CharacterLifecycle findByNameAndUser_Id(String name, UUID userId);

    /**
     * ユーザーIDに紐づくすべてのキャラクターを取得
     */
    List<CharacterLifecycle> findByUser_Id(UUID userId);

    /**
     * ユーザーIDとキャラクターIDでキャラクターを検索
     */
    Optional<CharacterLifecycle> findByIdAndUser_Id(Long characterId, UUID userId);

    /**
     * ステータスでキャラクターを検索
     */
    List<CharacterLifecycle> findByStatus(CharacterStatus status);

    /**
     * ユーザーIDとステータスでキャラクターを検索
     * インデックス最適化のターゲット
     */
    List<CharacterLifecycle> findByUser_IdAndStatus(UUID userId, CharacterStatus status);

    /**
     * ライフスパンポイントが指定値以下のキャラクターを検索
     */
    List<CharacterLifecycle> findByLifespanPointsLessThanEqual(Integer points);

    /**
     * キャラクター名の部分一致検索
     */
    List<CharacterLifecycle> findByNameContaining(String name);

    /**
     * ページング対応のキャラクター一覧取得（ユーザーIDでフィルタリング）
     */
    Page<CharacterLifecycle> findByUser_Id(UUID userId, Pageable pageable);

    /**
     * ユーザーごとのキャラクター数を取得
     */
    @Query("SELECT COUNT(c) FROM CharacterLifecycle c WHERE c.user.id = :userId")
    long countByUser_Id(@Param("userId") UUID userId);

    /**
     * ステータス別のキャラクター数を取得
     */
    @Query("SELECT c.status, COUNT(c) FROM CharacterLifecycle c GROUP BY c.status")
    List<Object[]> countByStatusGrouped();
}
