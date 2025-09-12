package com.sfr.tokyo.sfr_backend.repository.crypto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrtBalance;

public interface SfrtBalanceRepository extends JpaRepository<SfrtBalance, Long> {

	Optional<SfrtBalance> findByUserId(String userId);

	@Query("select coalesce(sum(b.balance),0) from SfrtBalance b")
	BigDecimal getTotalSfrtSupply();

	@Query("select coalesce(sum(b.balance),0) from SfrtBalance b where b.userId <> :platformUserId")
	BigDecimal getCirculatingSfrtSupply(String platformUserId);

	@Query("select count(b) from SfrtBalance b where b.balance > 0")
	Long getActiveHolderCount();

	@Query("select avg(b.balance) from SfrtBalance b where b.balance > 0")
	BigDecimal getAverageSfrtBalance();

	// ダミー: 分布データ（バケット境界をシンプルに返却）
	@Query("select '0-99' as bucket, count(b) from SfrtBalance b where b.balance < 100 group by bucket")
	List<Object[]> getSfrtBalanceDistribution();

	@Query("select b from SfrtBalance b where b.balance >= :minBalance")
	List<SfrtBalance> findUsersWithMinimumBalance(BigDecimal minBalance);

	@Query("select b from SfrtBalance b where b.updatedAt < :threshold")
	List<SfrtBalance> findInactiveUsers(LocalDateTime threshold);
}
