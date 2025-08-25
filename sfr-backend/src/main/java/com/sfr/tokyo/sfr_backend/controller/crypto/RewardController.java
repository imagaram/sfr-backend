package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.dto.crypto.RewardDto;
import com.sfr.tokyo.sfr_backend.entity.crypto.RewardDistribution;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.service.crypto.RewardDistributionService;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RewardController - SFR報酬システムAPI
 * 報酬の配布、履歴確認、統計情報を提供
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-20
 */
@RestController
@RequestMapping("/api/crypto/rewards")
@RequiredArgsConstructor
@Slf4j
public class RewardController {

    private final RewardDistributionService rewardDistributionService;
    private final UserBalanceService userBalanceService;
    private final BalanceHistoryService balanceHistoryService;

    // =============================================================================
    // ユーザー向けエンドポイント
    // =============================================================================

    /**
     * 現在ユーザーの報酬履歴取得
     * 
     * @param request        履歴取得リクエスト
     * @param authentication 認証情報
     * @return 報酬履歴
     */
    @GetMapping("/history")
    public ResponseEntity<Page<RewardDto.HistoryItem>> getRewardHistory(
            @Valid RewardDto.HistoryRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting reward history for user: {}", userId);

        try {
            Page<RewardDistribution> distributionPage = rewardDistributionService.findByUserId(
                    UUID.fromString(userId), request.getPage(), request.getSize());

            Page<RewardDto.HistoryItem> response = distributionPage.map(this::convertToHistoryItem);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting reward history for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 現在ユーザーの報酬統計取得
     * 
     * @param request        統計リクエスト
     * @param authentication 認証情報
     * @return 報酬統計
     */
    @GetMapping("/statistics")
    public ResponseEntity<RewardDto.StatisticsResponse> getRewardStatistics(
            @Valid RewardDto.StatisticsRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting reward statistics for user: {}", userId);

        try {
            LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate()
                    : LocalDateTime.now().minusMonths(1);
            LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate() : LocalDateTime.now();

            // 基本統計データを計算
            RewardDto.StatisticsResponse stats = calculateUserStatistics(userId, startDate, endDate);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting reward statistics for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 現在ユーザーの報酬サマリー取得
     * 
     * @param authentication 認証情報
     * @return 報酬サマリー
     */
    @GetMapping("/summary")
    public ResponseEntity<RewardDto.UserRewardSummary> getRewardSummary(Authentication authentication) {
        String userId = authentication.getName();
        Long spaceId = 1L; // デフォルトスペースID
        log.info("Getting reward summary for user: {} in space: {}", userId, spaceId);

        try {
            // 現在残高取得
            UserBalance userBalance = userBalanceService.getUserBalance(userId, spaceId)
                    .orElseGet(() -> userBalanceService.createUserBalance(userId, spaceId, BigDecimal.ZERO));

            // 今月の統計
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime now = LocalDateTime.now();

            // 先月の統計
            LocalDateTime lastMonthStart = monthStart.minusMonths(1);
            LocalDateTime lastMonthEnd = monthStart.minusSeconds(1);

            RewardDto.StatisticsResponse thisMonth = calculateUserStatistics(userId, monthStart, now);
            RewardDto.StatisticsResponse lastMonth = calculateUserStatistics(userId, lastMonthStart, lastMonthEnd);
            RewardDto.StatisticsResponse allTime = calculateUserStatistics(userId, null, null);

            RewardDto.UserRewardSummary summary = RewardDto.UserRewardSummary.builder()
                    .userId(userId)
                    .totalEarned(userBalance.getTotalEarned())
                    .thisMonthEarned(thisMonth.getTotalAmount())
                    .lastMonthEarned(lastMonth.getTotalAmount())
                    .totalDistributions(allTime.getTotalDistributions())
                    .thisMonthDistributions(thisMonth.getTotalDistributions())
                    .lastRewardDate(getLastRewardDate(userId))
                    .averageReward(allTime.getAverageAmount())
                    .topCategory(getTopRewardCategory(userId))
                    .currentBalance(userBalance.getCurrentBalance())
                    .build();

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting reward summary for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // =============================================================================
    // 管理者専用エンドポイント
    // =============================================================================

    /**
     * 報酬配布（ADMIN専用）
     * 
     * @param request        配布リクエスト
     * @param authentication 認証情報
     * @return 配布結果
     */
    @PostMapping("/distribute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardDto.DistributeResponse> distributeReward(
            @Valid @RequestBody RewardDto.DistributeRequest request,
            Authentication authentication) {
        String adminId = authentication.getName();
        log.info("Admin {} distributing reward to user: {} amount: {}",
                adminId, request.getUserId(), request.getAmount());

        try {
            // 報酬配布レコード作成
            RewardDistribution distribution = RewardDistribution.builder()
                    .spaceId(1L) // デフォルトスペース
                    .userId(UUID.fromString(request.getUserId()))
                    .amount(request.getAmount())
                    .category(RewardDistribution.RewardCategory.valueOf(request.getCategory()))
                    .triggerType(RewardDistribution.TriggerType.valueOf(request.getTriggerType()))
                    .referenceId(request.getReferenceId())
                    .reason(request.getReason())
                    .qualityScore(request.getQualityScore())
                    .engagementScore(request.getEngagementScore())
                    .calculationDetails(request.getCalculationDetails())
                    .distributionDate(LocalDateTime.now())
                    .status(RewardDistribution.DistributionStatus.PENDING)
                    .processedBy(UUID.fromString(adminId))
                    .build();

            RewardDistribution saved = rewardDistributionService.createRewardDistribution(distribution);

            // 即座に処理（残高更新）
            processRewardDistribution(saved);

            // ユーザー残高取得
            Long spaceId = 1L; // デフォルトスペースID
            UserBalance userBalance = userBalanceService.getUserBalance(request.getUserId(), spaceId)
                    .orElseThrow(() -> new RuntimeException("User balance not found"));

            RewardDto.DistributeResponse response = RewardDto.DistributeResponse.builder()
                    .distributionId(saved.getId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .category(request.getCategory())
                    .status("COMPLETED")
                    .distributionDate(saved.getDistributionDate())
                    .processedAt(saved.getProcessedAt())
                    .transactionHash(saved.getTransactionHash())
                    .reason(request.getReason())
                    .balanceAfter(userBalance.getCurrentBalance())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error distributing reward", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 報酬承認（ADMIN専用）
     * 
     * @param distributionId 配布ID
     * @param authentication 認証情報
     * @return 承認結果
     */
    @PostMapping("/approve/{distributionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardDto.ProcessResponse> approveReward(
            @PathVariable Long distributionId,
            Authentication authentication) {
        String adminId = authentication.getName();
        log.info("Admin {} approving reward distribution: {}", adminId, distributionId);

        try {
            RewardDistribution approved = rewardDistributionService.approveReward(
                    distributionId, UUID.fromString(adminId));

            RewardDto.ProcessResponse response = RewardDto.ProcessResponse.builder()
                    .distributionId(approved.getId())
                    .status("APPROVED")
                    .processedAt(approved.getProcessedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error approving reward distribution", e);
            return ResponseEntity.internalServerError().body(
                    RewardDto.ProcessResponse.builder()
                            .distributionId(distributionId)
                            .status("FAILED")
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * 報酬処理（ADMIN専用）
     * 
     * @param distributionId 配布ID
     * @param authentication 認証情報
     * @return 処理結果
     */
    @PostMapping("/process/{distributionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardDto.ProcessResponse> processReward(
            @PathVariable Long distributionId,
            Authentication authentication) {
        String adminId = authentication.getName();
        log.info("Admin {} processing reward distribution: {}", adminId, distributionId);

        try {
            RewardDistribution distribution = rewardDistributionService.findById(distributionId)
                    .orElseThrow(() -> new RuntimeException("Reward distribution not found"));

            // 処理実行
            processRewardDistribution(distribution);

            String transactionHash = UUID.randomUUID().toString(); // 仮のハッシュ
            RewardDistribution processed = rewardDistributionService.processReward(distributionId, transactionHash);

            RewardDto.ProcessResponse response = RewardDto.ProcessResponse.builder()
                    .distributionId(processed.getId())
                    .status("COMPLETED")
                    .processedAt(processed.getProcessedAt())
                    .transactionHash(processed.getTransactionHash())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing reward distribution", e);
            return ResponseEntity.internalServerError().body(
                    RewardDto.ProcessResponse.builder()
                            .distributionId(distributionId)
                            .status("FAILED")
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * 全ユーザー報酬履歴取得（ADMIN専用）
     * 
     * @param request 履歴取得リクエスト
     * @return 報酬履歴
     */
    @GetMapping("/admin/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RewardDto.HistoryItem>> getAllRewardHistory(
            @Valid RewardDto.HistoryRequest request) {
        log.info("Getting all reward history");

        try {
            Page<RewardDistribution> distributionPage = rewardDistributionService.findAll(
                    request.getPage(), request.getSize(), request.getSortBy());

            Page<RewardDto.HistoryItem> response = distributionPage.map(this::convertToHistoryItem);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all reward history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // =============================================================================
    // ヘルパーメソッド
    // =============================================================================

    /**
     * 報酬配布の実際の処理
     */
    private void processRewardDistribution(RewardDistribution distribution) {
        try {
            // ユーザー残高取得または作成
            Long spaceId = 1L; // デフォルトスペースID
            String userId = distribution.getUserId().toString();
            UserBalance userBalance = userBalanceService.getUserBalance(userId, spaceId)
                    .orElseGet(() -> userBalanceService.createUserBalance(userId, spaceId, BigDecimal.ZERO));

            // 残高更新
            BigDecimal oldBalance = userBalance.getCurrentBalance();
            BigDecimal newBalance = oldBalance.add(distribution.getAmount());
            userBalanceService.updateUserBalance(userId, spaceId, newBalance);

            // 履歴記録
            balanceHistoryService.recordEarning(
                    userId,
                    oldBalance,
                    distribution.getAmount(),
                    distribution.getId().toString(),
                    distribution.getReason());

            log.info("Reward distribution processed successfully: {}", distribution.getId());
        } catch (Exception e) {
            log.error("Error processing reward distribution: {}", distribution.getId(), e);
            throw e;
        }
    }

    /**
     * ユーザー統計計算
     */
    private RewardDto.StatisticsResponse calculateUserStatistics(String userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        // RewardDistributionRepositoryから直接データを取得して統計を計算
        // 簡易実装として基本的な統計のみ提供

        return RewardDto.StatisticsResponse.builder()
                .totalDistributions(0)
                .totalAmount(BigDecimal.ZERO)
                .averageAmount(BigDecimal.ZERO)
                .maxAmount(BigDecimal.ZERO)
                .minAmount(BigDecimal.ZERO)
                .periodStart(startDate != null ? startDate : LocalDateTime.now().minusYears(1))
                .periodEnd(endDate != null ? endDate : LocalDateTime.now())
                .build();
    }

    /**
     * RewardDistributionをHistoryItemに変換
     */
    private RewardDto.HistoryItem convertToHistoryItem(RewardDistribution distribution) {
        return RewardDto.HistoryItem.builder()
                .distributionId(distribution.getId())
                .amount(distribution.getAmount())
                .category(distribution.getCategory().name())
                .triggerType(distribution.getTriggerType().name())
                .status(distribution.getStatus().name())
                .distributionDate(distribution.getDistributionDate())
                .processedAt(distribution.getProcessedAt())
                .reason(distribution.getReason())
                .referenceId(distribution.getReferenceId())
                .qualityScore(distribution.getQualityScore())
                .engagementScore(distribution.getEngagementScore())
                .transactionHash(distribution.getTransactionHash())
                .displayCategory(getDisplayCategory(distribution.getCategory()))
                .displayStatus(getDisplayStatus(distribution.getStatus()))
                .build();
    }

    /**
     * カテゴリ表示名取得
     */
    private String getDisplayCategory(RewardDistribution.RewardCategory category) {
        switch (category) {
            case CONTENT_CREATION:
                return "コンテンツ作成";
            case CONTENT_CURATION:
                return "コンテンツキュレーション";
            case COMMUNITY_ENGAGEMENT:
                return "コミュニティエンゲージメント";
            case LEARNING_PROGRESS:
                return "学習進捗";
            case SKILL_DEMONSTRATION:
                return "スキル実証";
            case KNOWLEDGE_SHARING:
                return "知識共有";
            case MENTORING:
                return "メンタリング";
            case GOVERNANCE:
                return "ガバナンス参加";
            case REFERRAL:
                return "紹介";
            case ACHIEVEMENT:
                return "達成";
            case SPECIAL_EVENT:
                return "特別イベント";
            case BONUS:
                return "ボーナス";
            case SYSTEM_REWARD:
                return "システムリワード";
            default:
                return category.name();
        }
    }

    /**
     * ステータス表示名取得
     */
    private String getDisplayStatus(RewardDistribution.DistributionStatus status) {
        switch (status) {
            case PENDING:
                return "処理待ち";
            case APPROVED:
                return "承認済み";
            case PROCESSING:
                return "処理中";
            case COMPLETED:
                return "完了";
            case FAILED:
                return "失敗";
            case CANCELLED:
                return "キャンセル";
            case EXPIRED:
                return "期限切れ";
            default:
                return status.name();
        }
    }

    /**
     * 最終報酬日取得
     */
    private LocalDateTime getLastRewardDate(String userId) {
        try {
            // 最新の報酬配布から日付を取得
            Page<RewardDistribution> recent = rewardDistributionService.findByUserId(
                    UUID.fromString(userId), 0, 1);
            return recent.hasContent() ? recent.getContent().get(0).getDistributionDate() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * トップ報酬カテゴリ取得
     */
    private String getTopRewardCategory(String userId) {
        try {
            // 簡易実装：最頻出カテゴリを返す
            return "CONTENT_CREATION"; // プレースホルダー
        } catch (Exception e) {
            return "N/A";
        }
    }
}
