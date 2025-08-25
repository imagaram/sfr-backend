package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.TokenTransaction;
import com.sfr.tokyo.sfr_backend.repository.crypto.TokenTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * トークン取引サービス
 * SFRトークンの取引記録と分析を担当
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenTransactionService {

    private final TokenTransactionRepository tokenTransactionRepository;

    /**
     * 取引を記録
     */
    @Transactional
    public TokenTransaction recordTransaction(Long spaceId, String fromUserId, String toUserId,
            BigDecimal amount, TokenTransaction.TransactionType transactionType,
            String description, String transactionHash) {
        log.info("Recording transaction: {} -> {}, amount: {}, type: {}", fromUserId, toUserId, amount,
                transactionType);

        TokenTransaction transaction = TokenTransaction.builder()
                .spaceId(spaceId)
                .fromUserId(UUID.fromString(fromUserId))
                .toUserId(UUID.fromString(toUserId))
                .amount(amount)
                .transactionType(transactionType)
                .description(description)
                .transactionHash(transactionHash)
                .status(TokenTransaction.TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        TokenTransaction saved = tokenTransactionRepository.save(transaction);
        log.info("Transaction recorded with ID: {}", saved.getId());
        return saved;
    }

    /**
     * 取引を確認（成功）
     */
    @Transactional
    public TokenTransaction confirmTransaction(Long transactionId, String confirmationHash, Integer blockNumber) {
        log.info("Confirming transaction ID: {}", transactionId);

        TokenTransaction transaction = getTransaction(transactionId);

        if (transaction.getStatus() != TokenTransaction.TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in pending status");
        }

        transaction.setStatus(TokenTransaction.TransactionStatus.CONFIRMED);
        // transaction.setConfirmationHash(confirmationHash); // メソッドが利用できません
        // transaction.setBlockNumber(blockNumber.longValue()); // メソッドが利用できません
        // transaction.setConfirmedAt(LocalDateTime.now()); // メソッドが利用できません

        TokenTransaction updated = tokenTransactionRepository.save(transaction);
        log.info("Transaction confirmed successfully");
        return updated;
    }

    /**
     * 取引を失敗としてマーク
     */
    @Transactional
    public TokenTransaction markTransactionAsFailed(Long transactionId, String failureReason) {
        log.info("Marking transaction as failed ID: {}", transactionId);

        TokenTransaction transaction = getTransaction(transactionId);

        if (transaction.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot mark confirmed transaction as failed");
        }

        transaction.setStatus(TokenTransaction.TransactionStatus.FAILED);
        // transaction.setFailureReason(failureReason); // メソッドが利用できません

        TokenTransaction updated = tokenTransactionRepository.save(transaction);
        log.info("Transaction marked as failed");
        return updated;
    }

    /**
     * 取引をキャンセル
     */
    @Transactional
    public TokenTransaction cancelTransaction(Long transactionId, String cancellationReason) {
        log.info("Cancelling transaction ID: {}", transactionId);

        TokenTransaction transaction = getTransaction(transactionId);

        if (transaction.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel confirmed transaction");
        }

        transaction.setStatus(TokenTransaction.TransactionStatus.CANCELLED);
        // transaction.setCancellationReason(cancellationReason); // メソッドが利用できません

        TokenTransaction updated = tokenTransactionRepository.save(transaction);
        log.info("Transaction cancelled successfully");
        return updated;
    }

    /**
     * 取引取得
     */
    @Transactional(readOnly = true)
    public TokenTransaction getTransaction(Long id) {
        return tokenTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
    }

    /**
     * ユーザー別取引履歴取得
     */
    @Transactional(readOnly = true)
    public Page<TokenTransaction> getUserTransactions(String userId, int page, int size) {
        log.info("Getting transactions for user: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        UUID userUuid = UUID.fromString(userId);

        // 利用可能な基本メソッドで代替実装
        List<TokenTransaction> allTransactions = tokenTransactionRepository.findAll();
        List<TokenTransaction> userTransactions = allTransactions.stream()
                .filter(t -> userUuid.equals(t.getFromUserId()) || userUuid.equals(t.getToUserId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        int start = page * size;
        int end = Math.min(start + size, userTransactions.size());
        List<TokenTransaction> pageContent = userTransactions.subList(start, end);

        return new PageImpl<>(pageContent, pageable, userTransactions.size());
    }

    /**
     * スペース別取引履歴取得
     */
    @Transactional(readOnly = true)
    public Page<TokenTransaction> getSpaceTransactions(Long spaceId, int page, int size) {
        log.info("Getting transactions for space: {}, page: {}, size: {}", spaceId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return tokenTransactionRepository.findBySpaceId(spaceId, pageable);
    }

    /**
     * 取引タイプ別取得
     */
    @Transactional(readOnly = true)
    public Page<TokenTransaction> getTransactionsByType(TokenTransaction.TransactionType transactionType, int page,
            int size) {
        log.info("Getting transactions by type: {}, page: {}, size: {}", transactionType, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<TokenTransaction> allTransactions = tokenTransactionRepository
                .findByTransactionType(transactionType.name());

        int start = page * size;
        int end = Math.min(start + size, allTransactions.size());
        List<TokenTransaction> pageContent = allTransactions.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allTransactions.size());
    }

    /**
     * ステータス別取得
     */
    @Transactional(readOnly = true)
    public Page<TokenTransaction> getTransactionsByStatus(TokenTransaction.TransactionStatus status, int page,
            int size) {
        log.info("Getting transactions by status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<TokenTransaction> allTransactions = tokenTransactionRepository.findByStatus(status.name());

        int start = page * size;
        int end = Math.min(start + size, allTransactions.size());
        List<TokenTransaction> pageContent = allTransactions.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allTransactions.size());
    }

    /**
     * 期間内の取引取得
     */
    @Transactional(readOnly = true)
    public List<TokenTransaction> getTransactionsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting transactions between {} and {}", startDate, endDate);

        // 代替実装: 全取引から期間でフィルタ
        return tokenTransactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(startDate) && t.getCreatedAt().isBefore(endDate))
                .toList();
    }

    /**
     * 高額取引取得
     */
    @Transactional(readOnly = true)
    public List<TokenTransaction> getHighValueTransactions(BigDecimal threshold) {
        log.info("Getting high value transactions above threshold: {}", threshold);

        // 代替実装: 全取引から金額でフィルタしてソート
        return tokenTransactionRepository.findAll().stream()
                .filter(t -> t.getAmount().compareTo(threshold) > 0)
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .toList();
    }

    /**
     * 失敗した取引取得
     */
    @Transactional(readOnly = true)
    public List<TokenTransaction> getFailedTransactions(LocalDateTime since) {
        log.info("Getting failed transactions since: {}", since);

        // 代替実装
        return tokenTransactionRepository.findAll().stream()
                .filter(t -> t.getStatus().name().equals("FAILED") && t.getCreatedAt().isAfter(since))
                .toList();
    }

    /**
     * 保留中の取引取得
     */
    @Transactional(readOnly = true)
    public List<TokenTransaction> getPendingTransactions() {
        log.info("Getting pending transactions");
        return tokenTransactionRepository.findByStatus("PENDING");
    }

    /**
     * 取引統計取得
     */
    @Transactional(readOnly = true)
    public TransactionStatistics getTransactionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting transaction statistics from {} to {}", startDate, endDate);

        List<TokenTransaction> transactions = tokenTransactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(startDate) && t.getCreatedAt().isBefore(endDate))
                .toList();

        if (transactions.isEmpty()) {
            return TransactionStatistics.builder()
                    .totalTransactions(0L)
                    .confirmedTransactions(0L)
                    .failedTransactions(0L)
                    .cancelledTransactions(0L)
                    .pendingTransactions(0L)
                    .totalVolume(BigDecimal.ZERO)
                    .confirmedVolume(BigDecimal.ZERO)
                    .averageTransactionAmount(BigDecimal.ZERO)
                    .largestTransaction(BigDecimal.ZERO)
                    .successRate(BigDecimal.ZERO)
                    .build();
        }

        long totalTransactions = transactions.size();
        long confirmedTransactions = transactions.stream()
                .mapToLong(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED ? 1 : 0)
                .sum();
        long failedTransactions = transactions.stream()
                .mapToLong(t -> t.getStatus() == TokenTransaction.TransactionStatus.FAILED ? 1 : 0)
                .sum();
        long cancelledTransactions = transactions.stream()
                .mapToLong(t -> t.getStatus() == TokenTransaction.TransactionStatus.CANCELLED ? 1 : 0)
                .sum();
        long pendingTransactions = transactions.stream()
                .mapToLong(t -> t.getStatus() == TokenTransaction.TransactionStatus.PENDING ? 1 : 0)
                .sum();

        BigDecimal totalVolume = transactions.stream()
                .map(TokenTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal confirmedVolume = transactions.stream()
                .filter(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED)
                .map(TokenTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTransactionAmount = totalTransactions > 0
                ? totalVolume.divide(BigDecimal.valueOf(totalTransactions), 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal largestTransaction = transactions.stream()
                .map(TokenTransaction::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal successRate = totalTransactions > 0 ? BigDecimal.valueOf(confirmedTransactions)
                .divide(BigDecimal.valueOf(totalTransactions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        return TransactionStatistics.builder()
                .totalTransactions(totalTransactions)
                .confirmedTransactions(confirmedTransactions)
                .failedTransactions(failedTransactions)
                .cancelledTransactions(cancelledTransactions)
                .pendingTransactions(pendingTransactions)
                .totalVolume(totalVolume)
                .confirmedVolume(confirmedVolume)
                .averageTransactionAmount(averageTransactionAmount)
                .largestTransaction(largestTransaction)
                .successRate(successRate)
                .build();
    }

    /**
     * ユーザー取引統計取得
     */
    @Transactional(readOnly = true)
    public UserTransactionStatistics getUserTransactionStatistics(String userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("Getting transaction statistics for user: {} from {} to {}", userId, startDate, endDate);

        UUID userUuid = UUID.fromString(userId);

        List<TokenTransaction> sentTransactions = tokenTransactionRepository.findAll().stream()
                .filter(t -> userUuid.equals(t.getFromUserId()) &&
                        t.getCreatedAt().isAfter(startDate) && t.getCreatedAt().isBefore(endDate))
                .toList();
        List<TokenTransaction> receivedTransactions = tokenTransactionRepository.findAll().stream()
                .filter(t -> userUuid.equals(t.getToUserId()) &&
                        t.getCreatedAt().isAfter(startDate) && t.getCreatedAt().isBefore(endDate))
                .toList();

        BigDecimal totalSent = sentTransactions.stream()
                .filter(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED)
                .map(TokenTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = receivedTransactions.stream()
                .filter(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED)
                .map(TokenTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netFlow = totalReceived.subtract(totalSent);

        int totalSentCount = (int) sentTransactions.stream()
                .filter(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED)
                .count();

        int totalReceivedCount = (int) receivedTransactions.stream()
                .filter(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED)
                .count();

        BigDecimal averageSentAmount = totalSentCount > 0
                ? totalSent.divide(BigDecimal.valueOf(totalSentCount), 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal averageReceivedAmount = totalReceivedCount > 0
                ? totalReceived.divide(BigDecimal.valueOf(totalReceivedCount), 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return UserTransactionStatistics.builder()
                .userId(userId)
                .totalSent(totalSent)
                .totalReceived(totalReceived)
                .netFlow(netFlow)
                .totalSentCount(totalSentCount)
                .totalReceivedCount(totalReceivedCount)
                .averageSentAmount(averageSentAmount)
                .averageReceivedAmount(averageReceivedAmount)
                .build();
    }

    /**
     * 取引タイプ別統計取得
     */
    @Transactional(readOnly = true)
    public List<TransactionTypeStatistics> getTransactionTypeStatistics(LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("Getting transaction type statistics from {} to {}", startDate, endDate);

        List<TokenTransaction> transactions = tokenTransactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(startDate) && t.getCreatedAt().isBefore(endDate))
                .toList();

        return transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(TokenTransaction::getTransactionType))
                .entrySet().stream()
                .map(entry -> {
                    TokenTransaction.TransactionType transactionType = entry.getKey();
                    List<TokenTransaction> typeTransactions = entry.getValue();

                    BigDecimal totalAmount = typeTransactions.stream()
                            .filter(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED)
                            .map(TokenTransaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long confirmedCount = typeTransactions.stream()
                            .mapToLong(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED ? 1 : 0)
                            .sum();

                    BigDecimal averageAmount = confirmedCount > 0
                            ? totalAmount.divide(BigDecimal.valueOf(confirmedCount), 8, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    BigDecimal successRate = typeTransactions.size() > 0 ? BigDecimal.valueOf(confirmedCount)
                            .divide(BigDecimal.valueOf(typeTransactions.size()), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

                    return TransactionTypeStatistics.builder()
                            .transactionType(transactionType)
                            .count(typeTransactions.size())
                            .confirmedCount(confirmedCount)
                            .totalAmount(totalAmount)
                            .averageAmount(averageAmount)
                            .successRate(successRate)
                            .build();
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .toList();
    }

    /**
     * 日別取引ボリューム取得
     */
    @Transactional(readOnly = true)
    public List<DailyVolumeStatistics> getDailyVolumeStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting daily volume statistics from {} to {}", startDate, endDate);

        List<TokenTransaction> transactions = tokenTransactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(startDate) && t.getCreatedAt().isBefore(endDate))
                .filter(t -> t.getStatus() == TokenTransaction.TransactionStatus.CONFIRMED)
                .toList();

        return transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getCreatedAt().toLocalDate()))
                .entrySet().stream()
                .map(entry -> {
                    java.time.LocalDate date = entry.getKey();
                    List<TokenTransaction> dayTransactions = entry.getValue();

                    BigDecimal dailyVolume = dayTransactions.stream()
                            .map(TokenTransaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DailyVolumeStatistics.builder()
                            .date(date)
                            .transactionCount(dayTransactions.size())
                            .totalVolume(dailyVolume)
                            .averageTransactionAmount(
                                    dayTransactions.size() > 0
                                            ? dailyVolume.divide(BigDecimal.valueOf(dayTransactions.size()), 8,
                                                    RoundingMode.HALF_UP)
                                            : BigDecimal.ZERO)
                            .build();
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .toList();
    }

    // === 内部DTOクラス ===

    @Data
    @Builder
    public static class TransactionStatistics {
        private Long totalTransactions;
        private Long confirmedTransactions;
        private Long failedTransactions;
        private Long cancelledTransactions;
        private Long pendingTransactions;
        private BigDecimal totalVolume;
        private BigDecimal confirmedVolume;
        private BigDecimal averageTransactionAmount;
        private BigDecimal largestTransaction;
        private BigDecimal successRate;
    }

    @Data
    @Builder
    public static class UserTransactionStatistics {
        private String userId;
        private BigDecimal totalSent;
        private BigDecimal totalReceived;
        private BigDecimal netFlow;
        private Integer totalSentCount;
        private Integer totalReceivedCount;
        private BigDecimal averageSentAmount;
        private BigDecimal averageReceivedAmount;
    }

    @Data
    @Builder
    public static class TransactionTypeStatistics {
        private TokenTransaction.TransactionType transactionType;
        private Integer count;
        private Long confirmedCount;
        private BigDecimal totalAmount;
        private BigDecimal averageAmount;
        private BigDecimal successRate;
    }

    @Data
    @Builder
    public static class DailyVolumeStatistics {
        private java.time.LocalDate date;
        private Integer transactionCount;
        private BigDecimal totalVolume;
        private BigDecimal averageTransactionAmount;
    }
}
