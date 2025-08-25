package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningRankingDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningRanking;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningRanking.RankingType;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningRankingRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningPointRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningUserBadgeRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 学習ランキングサービス
 * ランキング計算・更新・取得を管理
 */
@Service
@Transactional
public class LearningRankingService {

    @Autowired
    private LearningRankingRepository rankingRepository;

    @Autowired
    private LearningPointRepository pointRepository;

    @Autowired
    private LearningUserBadgeRepository userBadgeRepository;

    @Autowired
    private LearningProgressRepository progressRepository;

    /**
     * グローバルランキング一覧取得
     */
    @Transactional(readOnly = true)
    public List<LearningRankingDto> getGlobalRanking(RankingType rankingType, int limit) {
        List<LearningRanking> rankings = rankingRepository
                .findBySpaceIdIsNullAndRankingTypeOrderByRankPosition(rankingType)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());

        return rankings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * スペース特定ランキング一覧取得
     */
    @Transactional(readOnly = true)
    public List<LearningRankingDto> getSpaceRanking(Long spaceId, RankingType rankingType, int limit) {
        List<LearningRanking> rankings = rankingRepository
                .findBySpaceIdAndRankingTypeOrderByRankPosition(spaceId, rankingType)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());

        return rankings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * ユーザーランキング情報取得
     */
    @Transactional(readOnly = true)
    public Optional<LearningRankingDto> getUserRanking(UUID userId, RankingType rankingType) {
        return rankingRepository.findByUserIdAndRankingType(userId, rankingType)
                .map(this::convertToDto);
    }

    /**
     * ユーザーのスペース特定ランキング情報取得
     */
    @Transactional(readOnly = true)
    public Optional<LearningRankingDto> getUserSpaceRanking(UUID userId, Long spaceId, RankingType rankingType) {
        return rankingRepository.findByUserIdAndSpaceIdAndRankingType(userId, spaceId, rankingType)
                .map(this::convertToDto);
    }

    /**
     * ユーザーの全ランキング情報取得
     */
    @Transactional(readOnly = true)
    public List<LearningRankingDto> getUserAllRankings(UUID userId) {
        List<LearningRanking> rankings = rankingRepository.findByUserId(userId);
        return rankings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * ポイントランキング更新
     */
    public void updatePointsRanking(Long spaceId) {
        // グローバルポイントランキング更新
        updateGlobalPointsRanking();

        // スペース特定ポイントランキング更新
        if (spaceId != null) {
            updateSpacePointsRanking(spaceId);
        }
    }

    /**
     * バッジランキング更新
     */
    public void updateBadgesRanking(Long spaceId) {
        // グローバルバッジランキング更新
        updateGlobalBadgesRanking();

        // スペース特定バッジランキング更新
        if (spaceId != null) {
            updateSpaceBadgesRanking(spaceId);
        }
    }

    /**
     * 総合ランキング更新
     */
    public void updateOverallRanking(Long spaceId) {
        // グローバル総合ランキング更新
        updateGlobalOverallRanking();

        // スペース特定総合ランキング更新
        if (spaceId != null) {
            updateSpaceOverallRanking(spaceId);
        }
    }

    /**
     * 全ランキング更新（スケジュール実行用）
     */
    public void updateAllRankings() {
        // 全てのスペースIDを取得
        List<Long> spaceIds = rankingRepository.findAll()
                .stream()
                .map(LearningRanking::getSpaceId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // グローバルランキング更新
        updateGlobalPointsRanking();
        updateGlobalBadgesRanking();
        updateGlobalOverallRanking();

        // 各スペースのランキング更新
        for (Long spaceId : spaceIds) {
            updateSpacePointsRanking(spaceId);
            updateSpaceBadgesRanking(spaceId);
            updateSpaceOverallRanking(spaceId);
        }
    }

    /**
     * ランクアップ通知対象ユーザー取得
     */
    @Transactional(readOnly = true)
    public List<LearningRankingDto> getRankUpUsers(LocalDateTime since) {
        List<LearningRanking> recentUpdates = rankingRepository.findRecentlyUpdatedRankings(since);
        return recentUpdates.stream()
                .filter(ranking -> {
                    // 前回順位と比較してランクアップしているかチェック
                    // 実装では履歴テーブルとの比較が必要
                    return ranking.getRankPosition() <= 10; // 仮実装：トップ10入り
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ランキング範囲取得
     */
    @Transactional(readOnly = true)
    public List<LearningRankingDto> getRankingRange(RankingType rankingType, int startRank, int endRank) {
        List<LearningRanking> rankings = rankingRepository.findRankingsByRange(rankingType, startRank, endRank);
        return rankings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * スペース特定ランキング範囲取得
     */
    @Transactional(readOnly = true)
    public List<LearningRankingDto> getSpaceRankingRange(Long spaceId, RankingType rankingType, int startRank,
            int endRank) {
        List<LearningRanking> rankings = rankingRepository.findSpaceRankingsByRange(spaceId, rankingType, startRank,
                endRank);
        return rankings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // === プライベートメソッド ===

    /**
     * グローバルポイントランキング更新
     */
    private void updateGlobalPointsRanking() {
        var globalPoints = pointRepository.findGlobalPointsRanking();

        for (int i = 0; i < globalPoints.size(); i++) {
            var pointRecord = globalPoints.get(i);
            UUID userId = pointRecord.getUserId();
            BigDecimal score = BigDecimal.valueOf(pointRecord.getPoints());

            updateOrCreateRanking(userId, null, RankingType.POINTS, i + 1, score);
        }
    }

    /**
     * スペース特定ポイントランキング更新
     */
    private void updateSpacePointsRanking(Long spaceId) {
        var spacePoints = pointRepository.findSpacePointsRanking(spaceId);

        for (int i = 0; i < spacePoints.size(); i++) {
            var pointRecord = spacePoints.get(i);
            UUID userId = pointRecord.getUserId();
            BigDecimal score = BigDecimal.valueOf(pointRecord.getPoints());

            updateOrCreateRanking(userId, spaceId, RankingType.POINTS, i + 1, score);
        }
    }

    /**
     * グローバルバッジランキング更新
     */
    private void updateGlobalBadgesRanking() {
        var badgeStats = userBadgeRepository.findTopBadgeEarners();

        for (int i = 0; i < badgeStats.size(); i++) {
            Object[] stat = badgeStats.get(i);
            UUID userId = (UUID) stat[0];
            Long badgeCount = (Long) stat[1];
            BigDecimal score = BigDecimal.valueOf(badgeCount);

            updateOrCreateRanking(userId, null, RankingType.BADGES, i + 1, score);
        }
    }

    /**
     * スペース特定バッジランキング更新
     */
    private void updateSpaceBadgesRanking(Long spaceId) {
        // スペース特定のバッジ獲得数集計（実装要）
        // 現在は基本実装のみ
        updateGlobalBadgesRanking();
    }

    /**
     * グローバル総合ランキング更新
     */
    private void updateGlobalOverallRanking() {
        // ポイント、バッジ、進捗を総合的に評価
        // 重み付け計算: ポイント*0.4 + バッジ数*100*0.3 + 進捗完了率*1000*0.3

        List<LearningRanking> allUsers = rankingRepository.findAll();
        List<RankingCandidate> candidates = new ArrayList<>();

        for (LearningRanking ranking : allUsers) {
            if (ranking.getSpaceId() == null && ranking.getRankingType() == RankingType.POINTS) {
                UUID userId = ranking.getUserId();

                // 統計データ収集
                Integer totalPoints = ranking.getTotalPoints() != null ? ranking.getTotalPoints() : 0;
                Integer badgeCount = ranking.getBadgeCount() != null ? ranking.getBadgeCount() : 0;

                // 総合スコア計算
                BigDecimal overallScore = BigDecimal.valueOf(
                        totalPoints * 0.4 + badgeCount * 100 * 0.3);

                candidates.add(new RankingCandidate(userId, overallScore));
            }
        }

        // スコア降順でソート
        candidates.sort((a, b) -> b.score.compareTo(a.score));

        // ランキング更新
        for (int i = 0; i < candidates.size(); i++) {
            RankingCandidate candidate = candidates.get(i);
            updateOrCreateRanking(candidate.userId, null, RankingType.OVERALL, i + 1, candidate.score);
        }
    }

    /**
     * スペース特定総合ランキング更新
     */
    private void updateSpaceOverallRanking(Long spaceId) {
        // スペース特定の総合ランキング（実装要）
        // 現在は基本実装のみ
        updateGlobalOverallRanking();
    }

    /**
     * ランキング更新または作成
     */
    private void updateOrCreateRanking(UUID userId, Long spaceId, RankingType rankingType,
            Integer position, BigDecimal score) {
        Optional<LearningRanking> existingOpt = spaceId == null
                ? rankingRepository.findByUserIdAndRankingType(userId, rankingType)
                : rankingRepository.findByUserIdAndSpaceIdAndRankingType(userId, spaceId, rankingType);

        if (existingOpt.isPresent()) {
            LearningRanking existing = existingOpt.get();
            existing.updateRanking(position, score);
            rankingRepository.save(existing);
        } else {
            LearningRanking newRanking = new LearningRanking(userId, spaceId, rankingType, position, score);
            rankingRepository.save(newRanking);
        }
    }

    /**
     * DTO変換
     */
    private LearningRankingDto convertToDto(LearningRanking ranking) {
        LearningRankingDto dto = new LearningRankingDto();
        dto.setId(ranking.getId());
        dto.setUserId(ranking.getUserId());
        dto.setSpaceId(ranking.getSpaceId());
        dto.setRankingType(ranking.getRankingType());
        dto.setRankPosition(ranking.getRankPosition());
        dto.setScore(ranking.getScore());
        dto.setTotalPoints(ranking.getTotalPoints());
        dto.setBadgeCount(ranking.getBadgeCount());
        dto.setContentCompleted(ranking.getContentCompleted());
        dto.setQuizCompleted(ranking.getQuizCompleted());
        dto.setStreakDays(ranking.getStreakDays());
        dto.setLastActivity(ranking.getLastActivity());
        dto.setCreatedAt(ranking.getCreatedAt());
        dto.setUpdatedAt(ranking.getUpdatedAt());
        return dto;
    }

    /**
     * ランキング候補者クラス
     */
    private static class RankingCandidate {
        UUID userId;
        BigDecimal score;

        RankingCandidate(UUID userId, BigDecimal score) {
            this.userId = userId;
            this.score = score;
        }
    }
}
