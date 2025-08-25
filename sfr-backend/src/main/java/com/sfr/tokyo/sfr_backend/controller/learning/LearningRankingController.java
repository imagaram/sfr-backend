package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningRankingDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningRanking.RankingType;
import com.sfr.tokyo.sfr_backend.service.learning.LearningRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 学習ランキングコントローラー
 * ランキング情報の取得・更新機能を提供
 */
@RestController
@RequestMapping("/api/learning/rankings")
public class LearningRankingController {

    @Autowired
    private LearningRankingService rankingService;

    /**
     * グローバルランキング一覧取得
     */
    @GetMapping("/global")
    public ResponseEntity<List<LearningRankingDto>> getGlobalRanking(
            @RequestParam RankingType type,
            @RequestParam(defaultValue = "50") int limit) {

        List<LearningRankingDto> rankings = rankingService.getGlobalRanking(type, limit);
        return ResponseEntity.ok(rankings);
    }

    /**
     * スペース特定ランキング一覧取得
     */
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<LearningRankingDto>> getSpaceRanking(
            @PathVariable Long spaceId,
            @RequestParam RankingType type,
            @RequestParam(defaultValue = "50") int limit) {

        List<LearningRankingDto> rankings = rankingService.getSpaceRanking(spaceId, type, limit);
        return ResponseEntity.ok(rankings);
    }

    /**
     * ユーザーのランキング情報取得
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<LearningRankingDto> getUserRanking(
            @PathVariable UUID userId,
            @RequestParam RankingType type) {

        Optional<LearningRankingDto> ranking = rankingService.getUserRanking(userId, type);
        return ranking.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ユーザーのスペース特定ランキング情報取得
     */
    @GetMapping("/user/{userId}/space/{spaceId}")
    public ResponseEntity<LearningRankingDto> getUserSpaceRanking(
            @PathVariable UUID userId,
            @PathVariable Long spaceId,
            @RequestParam RankingType type) {

        Optional<LearningRankingDto> ranking = rankingService.getUserSpaceRanking(userId, spaceId, type);
        return ranking.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ユーザーの全ランキング情報取得
     */
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<LearningRankingDto>> getUserAllRankings(
            @PathVariable UUID userId) {

        List<LearningRankingDto> rankings = rankingService.getUserAllRankings(userId);
        return ResponseEntity.ok(rankings);
    }

    /**
     * ランキング範囲取得
     */
    @GetMapping("/range")
    public ResponseEntity<List<LearningRankingDto>> getRankingRange(
            @RequestParam RankingType type,
            @RequestParam int startRank,
            @RequestParam int endRank) {

        List<LearningRankingDto> rankings = rankingService.getRankingRange(type, startRank, endRank);
        return ResponseEntity.ok(rankings);
    }

    /**
     * スペース特定ランキング範囲取得
     */
    @GetMapping("/space/{spaceId}/range")
    public ResponseEntity<List<LearningRankingDto>> getSpaceRankingRange(
            @PathVariable Long spaceId,
            @RequestParam RankingType type,
            @RequestParam int startRank,
            @RequestParam int endRank) {

        List<LearningRankingDto> rankings = rankingService.getSpaceRankingRange(spaceId, type, startRank, endRank);
        return ResponseEntity.ok(rankings);
    }

    /**
     * トップ10ランキング取得
     */
    @GetMapping("/top10")
    public ResponseEntity<List<LearningRankingDto>> getTop10Ranking(
            @RequestParam RankingType type) {

        List<LearningRankingDto> rankings = rankingService.getGlobalRanking(type, 10);
        return ResponseEntity.ok(rankings);
    }

    /**
     * スペーストップ10ランキング取得
     */
    @GetMapping("/space/{spaceId}/top10")
    public ResponseEntity<List<LearningRankingDto>> getSpaceTop10Ranking(
            @PathVariable Long spaceId,
            @RequestParam RankingType type) {

        List<LearningRankingDto> rankings = rankingService.getSpaceRanking(spaceId, type, 10);
        return ResponseEntity.ok(rankings);
    }

    /**
     * ランクアップユーザー取得
     */
    @GetMapping("/rankup")
    public ResponseEntity<List<LearningRankingDto>> getRankUpUsers(
            @RequestParam(defaultValue = "24") int hoursAgo) {

        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        List<LearningRankingDto> rankings = rankingService.getRankUpUsers(since);
        return ResponseEntity.ok(rankings);
    }

    /**
     * ポイントランキング更新
     */
    @PostMapping("/update/points")
    public ResponseEntity<String> updatePointsRanking(
            @RequestParam(required = false) Long spaceId) {

        rankingService.updatePointsRanking(spaceId);
        return ResponseEntity.ok("ポイントランキングを更新しました");
    }

    /**
     * バッジランキング更新
     */
    @PostMapping("/update/badges")
    public ResponseEntity<String> updateBadgesRanking(
            @RequestParam(required = false) Long spaceId) {

        rankingService.updateBadgesRanking(spaceId);
        return ResponseEntity.ok("バッジランキングを更新しました");
    }

    /**
     * 総合ランキング更新
     */
    @PostMapping("/update/overall")
    public ResponseEntity<String> updateOverallRanking(
            @RequestParam(required = false) Long spaceId) {

        rankingService.updateOverallRanking(spaceId);
        return ResponseEntity.ok("総合ランキングを更新しました");
    }

    /**
     * 全ランキング更新
     */
    @PostMapping("/update/all")
    public ResponseEntity<String> updateAllRankings() {

        rankingService.updateAllRankings();
        return ResponseEntity.ok("全ランキングを更新しました");
    }
}
