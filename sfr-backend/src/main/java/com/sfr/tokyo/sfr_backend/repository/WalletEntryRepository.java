package com.sfr.tokyo.sfr_backend.repository;

import com.sfr.tokyo.sfr_backend.entity.TransactionType;
import com.sfr.tokyo.sfr_backend.entity.WalletEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 財布エントリー情報を管理するリポジトリ
 * 推奨インデックス：wallet_id, timestamp（複合インデックス）
 */
@Repository
public interface WalletEntryRepository extends JpaRepository<WalletEntry, Long> {

    /**
     * チームIDに紐づくすべての財布エントリーを取得
     */
    List<WalletEntry> findByTeamId(Long teamId);

    /**
     * 特定の期間内のチームIDに紐づく財布エントリーを取得
     */
    List<WalletEntry> findByTeamIdAndTimestampBetween(Long teamId, LocalDateTime start, LocalDateTime end);

    /**
     * チームIDと取引タイプで財布エントリーを検索
     */
    List<WalletEntry> findByTeamIdAndTransactionType(Long teamId, TransactionType transactionType);

    /**
     * 特定の期間内のチームIDと取引タイプで財布エントリーを検索
     */
    List<WalletEntry> findByTeamIdAndTransactionTypeAndTimestampBetween(
            Long teamId, TransactionType transactionType, LocalDateTime start, LocalDateTime end);

    /**
     * チームIDに紐づく財布エントリーをページング取得
     */
    Page<WalletEntry> findByTeamId(Long teamId, Pageable pageable);

    /**
     * 金額が指定値以上の財布エントリーを検索
     */
    List<WalletEntry> findByAmountGreaterThanEqual(Double amount);

    /**
     * チームIDごとの総収入と総支出を取得（サマリー計算）
     */
    @Query("SELECT we.teamId, " +
            "SUM(CASE WHEN we.transactionType = com.sfr.tokyo.sfr_backend.entity.TransactionType.INCOME THEN we.amount ELSE 0 END) as totalIncome, "
            +
            "SUM(CASE WHEN we.transactionType = com.sfr.tokyo.sfr_backend.entity.TransactionType.EXPENSE THEN we.amount ELSE 0 END) as totalExpense "
            +
            "FROM WalletEntry we GROUP BY we.teamId")
    List<Object[]> getTeamFinancialSummary();

    /**
     * 特定のチームの残高を取得
     */
    @Query("SELECT SUM(CASE WHEN we.transactionType = com.sfr.tokyo.sfr_backend.entity.TransactionType.INCOME THEN we.amount "
            +
            "ELSE -we.amount END) FROM WalletEntry we WHERE we.teamId = :teamId")
    Double getTeamBalance(@Param("teamId") Long teamId);

    /**
     * 特定の月のチームの収支サマリーを取得
     */
    @Query("SELECT we.transactionType, SUM(we.amount) FROM WalletEntry we " +
            "WHERE we.teamId = :teamId AND YEAR(we.timestamp) = :year AND MONTH(we.timestamp) = :month " +
            "GROUP BY we.transactionType")
    List<Object[]> getMonthlyTeamFinancialSummary(
            @Param("teamId") Long teamId, @Param("year") int year, @Param("month") int month);
}
