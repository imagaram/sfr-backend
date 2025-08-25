package com.sfr.tokyo.sfr_backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.entity.CouncilMember;
import com.sfr.tokyo.sfr_backend.entity.CouncilRole;

/**
 * 評議会メンバー情報を管理するリポジトリ
 * 推奨インデックス：user_id, role, term_start, term_end
 */
@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, UUID> {

    /**
     * ユーザーIDに基づく評議会メンバーの検索
     */
    List<CouncilMember> findByUserId(UUID userId);

    /**
     * 特定の役割を持つ評議会メンバーの検索
     */
    List<CouncilMember> findByRole(CouncilRole role);

    /**
     * 現在アクティブな評議会メンバー（任期中）の検索
     */
    @Query("SELECT cm FROM CouncilMember cm WHERE cm.termStart <= :currentDate AND (cm.termEnd IS NULL OR cm.termEnd >= :currentDate)")
    List<CouncilMember> findActiveMembers(@Param("currentDate") LocalDate currentDate);

    /**
     * 特定の役割を持つアクティブな評議会メンバーの検索
     */
    @Query("SELECT cm FROM CouncilMember cm WHERE cm.role = :role AND cm.termStart <= :currentDate AND (cm.termEnd IS NULL OR cm.termEnd >= :currentDate)")
    List<CouncilMember> findActiveMembersByRole(@Param("role") CouncilRole role,
            @Param("currentDate") LocalDate currentDate);

    /**
     * 特定の期間に任期がある評議会メンバーの検索
     */
    @Query("SELECT cm FROM CouncilMember cm WHERE " +
            "(cm.termStart <= :endDate AND (cm.termEnd IS NULL OR cm.termEnd >= :startDate))")
    List<CouncilMember> findMembersWithTermDuring(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * ユーザーIDと役割で評議会メンバーを検索
     */
    Optional<CouncilMember> findByUserIdAndRole(UUID userId, CouncilRole role);

    /**
     * 指定された日付より後に任期が終了する評議会メンバーの検索
     */
    List<CouncilMember> findByTermEndGreaterThanEqual(LocalDate date);

    /**
     * 指定された日付に任期中の評議会メンバーをページング取得
     */
    @Query("SELECT cm FROM CouncilMember cm WHERE cm.termStart <= :date AND (cm.termEnd IS NULL OR cm.termEnd >= :date)")
    Page<CouncilMember> findMembersActiveOnDate(@Param("date") LocalDate date, Pageable pageable);

    /**
     * 役割ごとの評議会メンバー数を集計
     */
    @Query("SELECT cm.role, COUNT(cm) FROM CouncilMember cm GROUP BY cm.role")
    List<Object[]> countMembersByRole();
}
