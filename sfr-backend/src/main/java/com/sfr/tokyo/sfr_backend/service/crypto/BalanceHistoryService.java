package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory;
import com.sfr.tokyo.sfr_backend.repository.crypto.BalanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
import org.springframework.data.domain.Page;
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
import java.util.stream.Collectors;

/**
 * BalanceHistoryService - 残高変動履歴管理サービス
 * SFRトークンの残高変動履歴を記録・管理
 * 
 * @author SFR Development Team
 * @version 2.0
 * @since 2025-08-20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BalanceHistoryService {

        private final BalanceHistoryRepository balanceHistoryRepository;

        // =============================================================================
        // 残高変動記録メソッド
        // =============================================================================

        /**
         * 残高変動記録（基本メソッド）
         */
        @Transactional
        public BalanceHistory recordBalanceChange(String userId, BigDecimal balanceBefore, BigDecimal balanceAfter,
                        BigDecimal amount, BalanceHistory.TransactionType transactionType, String referenceId,
                        String reason) {
                log.info("Recording balance change for user: {}, before: {}, after: {}, amount: {}, type: {}",
                                userId, balanceBefore, balanceAfter, amount, transactionType);

                BalanceHistory history = BalanceHistory.builder()
                                .historyId(UUID.randomUUID().toString())
                                .userId(UUID.fromString(userId))
                                .balanceBefore(balanceBefore)
                                .balanceAfter(balanceAfter)
                                .amount(amount)
                                .transactionType(transactionType)
                                .referenceId(referenceId)
                                .reason(reason)
                                .build();

                BalanceHistory saved = balanceHistoryRepository.save(history);
                log.info("Balance change recorded with ID: {}", saved.getHistoryId());
                return saved;
        }

        /**
         * 報酬獲得による残高変更記録
         */
        @Transactional
        public BalanceHistory recordEarning(String userId, BigDecimal balanceBefore, BigDecimal earnedAmount,
                        String transactionId, String description) {
                BigDecimal balanceAfter = balanceBefore.add(earnedAmount);
                return recordBalanceChange(userId, balanceBefore, balanceAfter, earnedAmount,
                                BalanceHistory.TransactionType.EARN, transactionId, description);
        }

        /**
         * トークン使用による残高変更記録
         */
        @Transactional
        public BalanceHistory recordSpending(String userId, BigDecimal balanceBefore, BigDecimal spentAmount,
                        String transactionId, String description) {
                BigDecimal balanceAfter = balanceBefore.subtract(spentAmount);
                return recordBalanceChange(userId, balanceBefore, balanceAfter, spentAmount.negate(),
                                BalanceHistory.TransactionType.SPEND, transactionId, description);
        }

        /**
         * 徴収による残高変更記録
         */
        @Transactional
        public BalanceHistory recordCollection(String userId, BigDecimal balanceBefore, BigDecimal collectedAmount,
                        String transactionId, String description) {
                BigDecimal balanceAfter = balanceBefore.subtract(collectedAmount);
                return recordBalanceChange(userId, balanceBefore, balanceAfter, collectedAmount.negate(),
                                BalanceHistory.TransactionType.COLLECT, transactionId, description);
        }

        /**
         * バーン（焼却）による残高変更記録
         */
        @Transactional
        public BalanceHistory recordBurn(String userId, BigDecimal balanceBefore, BigDecimal burnedAmount,
                        String transactionId, String description) {
                BigDecimal balanceAfter = balanceBefore.subtract(burnedAmount);
                return recordBalanceChange(userId, balanceBefore, balanceAfter, burnedAmount.negate(),
                                BalanceHistory.TransactionType.BURN, transactionId, description);
        }

        /**
         * 転送による残高変更記録
         */
        @Transactional
        public BalanceHistory recordTransfer(String userId, BigDecimal balanceBefore, BigDecimal transferAmount,
                        boolean isSender, String transactionId) {
                BigDecimal amount = isSender ? transferAmount.negate() : transferAmount;
                BigDecimal balanceAfter = balanceBefore.add(amount);
                String description = isSender ? "SFRトークン送金" : "SFRトークン受取";
                return recordBalanceChange(userId, balanceBefore, balanceAfter, amount,
                                BalanceHistory.TransactionType.TRANSFER, transactionId, description);
        }

        // =============================================================================
        // 履歴取得メソッド
        // =============================================================================

        /**
         * 履歴取得（ID指定）
         */
        @Transactional(readOnly = true)
        public BalanceHistory getHistory(String historyId) {
                return balanceHistoryRepository.findById(historyId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Balance history not found with ID: " + historyId));
        }

        /**
         * ユーザー別残高履歴取得（ページング）
         */
        @Transactional(readOnly = true)
        public Page<BalanceHistory> getUserBalanceHistory(String userId, int page, int size) {
                log.info("Getting balance history for user: {}, page: {}, size: {}", userId, page, size);
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                return balanceHistoryRepository.findByUserId(UUID.fromString(userId), pageable);
        }

        /**
         * ユーザー別残高履歴取得（全件）
         */
        @Transactional(readOnly = true)
        public List<BalanceHistory> getUserBalanceHistoryAll(String userId) {
                log.info("Getting all balance history for user: {}", userId);
                return balanceHistoryRepository.findByUserId(UUID.fromString(userId)).stream()
                                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                                .collect(Collectors.toList());
        }

        /**
         * 期間指定履歴取得
         */
        @Transactional(readOnly = true)
        public List<BalanceHistory> getHistoryBetweenDates(String userId, LocalDateTime startDate,
                        LocalDateTime endDate) {
                log.info("Getting balance history for user: {} between {} and {}", userId, startDate, endDate);
                return balanceHistoryRepository.findByUserId(UUID.fromString(userId)).stream()
                                .filter(h -> h.getCreatedAt().isAfter(startDate) && h.getCreatedAt().isBefore(endDate))
                                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                                .collect(Collectors.toList());
        }

        /**
         * トランザクションタイプ別履歴取得
         */
        @Transactional(readOnly = true)
        public List<BalanceHistory> getHistoryByTransactionType(String userId,
                        BalanceHistory.TransactionType transactionType) {
                log.info("Getting balance history for user: {} with type: {}", userId, transactionType);
                return balanceHistoryRepository.findByUserId(UUID.fromString(userId)).stream()
                                .filter(h -> h.getTransactionType() == transactionType)
                                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                                .collect(Collectors.toList());
        }

        // =============================================================================
        // 残高計算・統計メソッド
        // =============================================================================

        /**
         * 現在残高計算（最新履歴から取得）
         */
        @Transactional(readOnly = true)
        public BigDecimal calculateCurrentBalance(String userId) {
                log.info("Calculating current balance for user: {}", userId);
                List<BalanceHistory> history = getUserBalanceHistoryAll(userId);

                if (history.isEmpty()) {
                        return BigDecimal.ZERO;
                }

                return history.get(0).getBalanceAfter();
        }

        /**
         * 残高統計取得
         */
        @Transactional(readOnly = true)
        public BalanceStatistics getBalanceStatistics(String userId, LocalDateTime startDate, LocalDateTime endDate) {
                log.info("Getting balance statistics for user: {} from {} to {}", userId, startDate, endDate);

                List<BalanceHistory> history = getHistoryBetweenDates(userId, startDate, endDate);

                if (history.isEmpty()) {
                        return BalanceStatistics.builder()
                                        .totalTransactions(0)
                                        .totalEarnings(BigDecimal.ZERO)
                                        .totalSpendings(BigDecimal.ZERO)
                                        .totalCollections(BigDecimal.ZERO)
                                        .totalBurns(BigDecimal.ZERO)
                                        .netChange(BigDecimal.ZERO)
                                        .averageTransactionAmount(BigDecimal.ZERO)
                                        .maxIncrease(BigDecimal.ZERO)
                                        .maxDecrease(BigDecimal.ZERO)
                                        .build();
                }

                List<BalanceHistory> sortedHistory = history.stream()
                                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                                .collect(Collectors.toList());

                // 種別ごとの集計
                BigDecimal totalEarnings = sortedHistory.stream()
                                .filter(h -> h.getTransactionType() == BalanceHistory.TransactionType.EARN)
                                .map(BalanceHistory::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalSpendings = sortedHistory.stream()
                                .filter(h -> h.getTransactionType() == BalanceHistory.TransactionType.SPEND)
                                .map(h -> h.getAmount().abs())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalCollections = sortedHistory.stream()
                                .filter(h -> h.getTransactionType() == BalanceHistory.TransactionType.COLLECT)
                                .map(h -> h.getAmount().abs())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalBurns = sortedHistory.stream()
                                .filter(h -> h.getTransactionType() == BalanceHistory.TransactionType.BURN)
                                .map(h -> h.getAmount().abs())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 基本統計
                int totalTransactions = sortedHistory.size();
                BigDecimal averageTransactionAmount = totalTransactions > 0 ? sortedHistory.stream()
                                .map(h -> h.getAmount().abs())
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(totalTransactions), 4, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                BigDecimal maxIncrease = sortedHistory.stream()
                                .filter(h -> h.getAmount().compareTo(BigDecimal.ZERO) > 0)
                                .map(BalanceHistory::getAmount)
                                .max(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);

                BigDecimal maxDecrease = sortedHistory.stream()
                                .filter(h -> h.getAmount().compareTo(BigDecimal.ZERO) < 0)
                                .map(h -> h.getAmount().abs())
                                .max(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);

                BigDecimal startBalance = sortedHistory.isEmpty() ? BigDecimal.ZERO
                                : sortedHistory.get(0).getBalanceBefore();
                BigDecimal endBalance = sortedHistory.isEmpty() ? BigDecimal.ZERO
                                : sortedHistory.get(sortedHistory.size() - 1).getBalanceAfter();

                return BalanceStatistics.builder()
                                .totalTransactions(totalTransactions)
                                .totalEarnings(totalEarnings)
                                .totalSpendings(totalSpendings)
                                .totalCollections(totalCollections)
                                .totalBurns(totalBurns)
                                .netChange(endBalance.subtract(startBalance))
                                .averageTransactionAmount(averageTransactionAmount)
                                .maxIncrease(maxIncrease)
                                .maxDecrease(maxDecrease)
                                .startingBalance(startBalance)
                                .endingBalance(endBalance)
                                .build();
        }

        /**
         * 履歴削除
         */
        @Transactional
        public void deleteHistory(String historyId, String reason) {
                log.info("Deleting balance history: {}, reason: {}", historyId, reason);
                BalanceHistory history = getHistory(historyId);
                balanceHistoryRepository.delete(history);
                log.info("Balance history deleted successfully");
        }

        // =============================================================================
        // DTOクラス
        // =============================================================================

        /**
         * フィルタ付き残高履歴取得
         */
        public Page<BalanceHistory> getBalanceHistoryWithFilters(String userId, int page, int size,
                        String transactionType, LocalDateTime startDate, LocalDateTime endDate) {
                log.info("フィルタ付き残高履歴取得 - ユーザー: {}, ページ: {}, サイズ: {}, タイプ: {}, 開始: {}, 終了: {}",
                                userId, page, size, transactionType, startDate, endDate);

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

                // フィルタに基づいて履歴を取得
                if (transactionType != null && !transactionType.isEmpty() && startDate != null && endDate != null) {
                        BalanceHistory.TransactionType type = BalanceHistory.TransactionType
                                        .valueOf(transactionType.toUpperCase());
                        return balanceHistoryRepository.findByUserIdAndTransactionTypeAndCreatedAtBetween(
                                        userId, type, startDate, endDate, pageable);
                } else if (transactionType != null && !transactionType.isEmpty()) {
                        BalanceHistory.TransactionType type = BalanceHistory.TransactionType
                                        .valueOf(transactionType.toUpperCase());
                        return balanceHistoryRepository.findByUserIdAndTransactionType(userId, type, pageable);
                } else if (startDate != null && endDate != null) {
                        return balanceHistoryRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate,
                                        pageable);
                } else {
                        return balanceHistoryRepository.findByUserId(userId, pageable);
                }
        }

        /**
         * テスト用サンプルデータ作成
         */
        @Transactional
        public void createSampleData(String userId) {
                log.info("サンプルデータ作成開始 - ユーザー: {}", userId);

                LocalDateTime now = LocalDateTime.now();
                BigDecimal currentBalance = new BigDecimal("1000.00");

                // サンプルデータ作成（過去30日分）
                for (int i = 30; i >= 0; i--) {
                        LocalDateTime transactionTime = now.minusDays(i);

                        // 収益
                        if (i % 3 == 0) {
                                BigDecimal amount = new BigDecimal("50.00");
                                currentBalance = currentBalance.add(amount);
                                recordBalanceChange(userId, currentBalance.subtract(amount), currentBalance,
                                                amount, BalanceHistory.TransactionType.EARN,
                                                UUID.randomUUID().toString(),
                                                "デイリー収益 - " + transactionTime.toLocalDate());
                        }

                        // 支出
                        if (i % 5 == 0 && i > 0) {
                                BigDecimal amount = new BigDecimal("25.00");
                                currentBalance = currentBalance.subtract(amount);
                                recordBalanceChange(userId, currentBalance.add(amount), currentBalance,
                                                amount.negate(), BalanceHistory.TransactionType.SPEND,
                                                UUID.randomUUID().toString(), "支払い - " + transactionTime.toLocalDate());
                        }

                        // 回収
                        if (i % 7 == 0) {
                                BigDecimal amount = new BigDecimal("100.00");
                                currentBalance = currentBalance.add(amount);
                                recordBalanceChange(userId, currentBalance.subtract(amount), currentBalance,
                                                amount, BalanceHistory.TransactionType.COLLECT,
                                                UUID.randomUUID().toString(), "回収 - " + transactionTime.toLocalDate());
                        }
                }

                log.info("サンプルデータ作成完了 - 最終残高: {}", currentBalance);
        }

        /**
         * 残高統計情報DTO
         */
        @Data
        @Builder
        public static class BalanceStatistics {
                private Integer totalTransactions;
                private BigDecimal totalEarnings;
                private BigDecimal totalSpendings;
                private BigDecimal totalCollections;
                private BigDecimal totalBurns;
                private BigDecimal netChange;
                private BigDecimal averageTransactionAmount;
                private BigDecimal maxIncrease;
                private BigDecimal maxDecrease;
                private BigDecimal startingBalance;
                private BigDecimal endingBalance;
        }
}
