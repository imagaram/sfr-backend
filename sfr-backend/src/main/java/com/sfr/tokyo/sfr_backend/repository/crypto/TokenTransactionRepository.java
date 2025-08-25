package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.TokenTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * TokenTransactionRepository
 * SFR トークン取引のデータアクセス層
 */
@Repository
public interface TokenTransactionRepository
        extends JpaRepository<TokenTransaction, Long>, JpaSpecificationExecutor<TokenTransaction> {

    // ===== 基本検索メソッド =====
    List<TokenTransaction> findByFromUserId(UUID fromUserId);

    Page<TokenTransaction> findByFromUserId(UUID fromUserId, Pageable pageable);

    List<TokenTransaction> findByToUserId(UUID toUserId);

    Page<TokenTransaction> findByToUserId(UUID toUserId, Pageable pageable);

    List<TokenTransaction> findBySpaceId(Long spaceId);

    Page<TokenTransaction> findBySpaceId(Long spaceId, Pageable pageable);

    List<TokenTransaction> findByTransactionType(String transactionType);

    List<TokenTransaction> findByStatus(String status);

    // ===== ユーザー関連検索 =====
    @Query("SELECT tt FROM TokenTransaction tt WHERE tt.fromUserId = :userId OR tt.toUserId = :userId ORDER BY tt.transactionTimestamp DESC")
    Page<TokenTransaction> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    // ===== 金額・日時ベース検索 =====
    @Query("SELECT tt FROM TokenTransaction tt WHERE tt.amount >= :minAmount ORDER BY tt.amount DESC")
    List<TokenTransaction> findByAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount, Pageable pageable);

    @Query("SELECT tt FROM TokenTransaction tt WHERE tt.amount BETWEEN :minAmount AND :maxAmount ORDER BY tt.amount DESC")
    Page<TokenTransaction> findByAmountBetween(@Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    @Query("SELECT tt FROM TokenTransaction tt WHERE tt.transactionTimestamp BETWEEN :startDate AND :endDate ORDER BY tt.transactionTimestamp DESC")
    Page<TokenTransaction> findByTimestampBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ===== 統計メソッド =====
    @Query("SELECT SUM(tt.amount) FROM TokenTransaction tt WHERE tt.fromUserId = :userId AND tt.status = 'COMPLETED'")
    BigDecimal getTotalSentByUser(@Param("userId") UUID userId);

    @Query("SELECT SUM(tt.amount) FROM TokenTransaction tt WHERE tt.toUserId = :userId AND tt.status = 'COMPLETED'")
    BigDecimal getTotalReceivedByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(tt) FROM TokenTransaction tt WHERE tt.fromUserId = :userId OR tt.toUserId = :userId")
    Long countTransactionsByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(tt) FROM TokenTransaction tt WHERE tt.spaceId = :spaceId")
    Long countTransactionsBySpace(@Param("spaceId") Long spaceId);

    @Query("SELECT tt.transactionType, COUNT(tt), SUM(tt.amount) FROM TokenTransaction tt GROUP BY tt.transactionType ORDER BY SUM(tt.amount) DESC")
    List<Object[]> getTransactionTypeStatistics();

    // ===== 複合条件検索 =====
    @Query("SELECT tt FROM TokenTransaction tt WHERE tt.spaceId = :spaceId AND tt.transactionType = :transactionType AND tt.status = :status ORDER BY tt.transactionTimestamp DESC")
    Page<TokenTransaction> findBySpaceAndTypeAndStatus(@Param("spaceId") Long spaceId,
            @Param("transactionType") String transactionType,
            @Param("status") String status,
            Pageable pageable);
}
