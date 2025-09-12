package com.sfr.tokyo.sfr_backend.repository.crypto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrtTransaction;

public interface SfrtTransactionRepository extends JpaRepository<SfrtTransaction, Long> {

	Page<SfrtTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

	SfrtTransaction findTopByUserIdOrderByCreatedAtDesc(String userId);

	@Query("select coalesce(sum(t.amount),0) from SfrtTransaction t where t.type in ('REWARD_BUYER','REWARD_SELLER','PLATFORM_RESERVE') and t.createdAt between :from and :to")
	BigDecimal getTotalRewardsInPeriod(LocalDateTime from, LocalDateTime to);

	@Query("select coalesce(sum(t.amount),0) from SfrtTransaction t where t.type in ('REWARD_BUYER','REWARD_SELLER','PLATFORM_RESERVE') and t.userId = :userId")
	BigDecimal getUserTotalRewards(String userId);

	@Query("select coalesce(sum(t.amount),0) from SfrtTransaction t where t.type = 'WITHDRAWAL' and t.userId = :userId")
	BigDecimal getUserTotalWithdrawals(String userId);

	@Query("select t.type, count(t) as cnt, coalesce(sum(t.amount),0) from SfrtTransaction t group by t.type")
	java.util.List<Object[]> getTransactionStatsByType();

	@Query("select date(t.createdAt), coalesce(sum(t.amount),0) from SfrtTransaction t where t.createdAt >= :from group by date(t.createdAt) order by date(t.createdAt)")
	java.util.List<Object[]> getDailyTransactionStats(LocalDateTime from);

	@Query("select t.userId, coalesce(sum(t.amount),0) as total from SfrtTransaction t where t.type in ('REWARD_BUYER','REWARD_SELLER') and t.createdAt between :from and :to group by t.userId order by total desc")
	java.util.List<Object[]> getTopRewardEarners(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
