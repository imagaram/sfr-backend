package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningProgressDto;
import com.sfr.tokyo.sfr_backend.service.learning.LearningProgressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/learning/progress")
public class LearningProgressController {

    private final LearningProgressService progressService;

    public LearningProgressController(LearningProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * 学習進捗登録・更新
     * POST /learning/progress
     */
    @PostMapping
    public ResponseEntity<LearningProgressDto> saveProgress(
            @Valid @RequestBody LearningProgressDto progressDto,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        LearningProgressDto savedProgress = progressService.saveProgress(userId, progressDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProgress);
    }

    /**
     * ユーザーの全進捗取得
     * GET /learning/progress/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<LearningProgressDto>> getUserProgress(@PathVariable UUID userId) {
        List<LearningProgressDto> progressList = progressService.getUserProgress(userId);
        return ResponseEntity.ok(progressList);
    }

    /**
     * 認証ユーザーの進捗取得
     * GET /learning/progress
     */
    @GetMapping
    public ResponseEntity<List<LearningProgressDto>> getMyProgress(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<LearningProgressDto> progressList = progressService.getUserProgress(userId);
        return ResponseEntity.ok(progressList);
    }

    /**
     * 学習空間内の進捗取得
     * GET /learning/progress/{userId}/spaces/{spaceId}
     */
    @GetMapping("/{userId}/spaces/{spaceId}")
    public ResponseEntity<List<LearningProgressDto>> getUserProgressBySpace(
            @PathVariable UUID userId,
            @PathVariable Long spaceId) {

        List<LearningProgressDto> progressList = progressService.getUserProgressBySpace(userId, spaceId);
        return ResponseEntity.ok(progressList);
    }

    /**
     * 特定コンテンツの進捗取得
     * GET /learning/progress/{userId}/contents/{contentId}
     */
    @GetMapping("/{userId}/contents/{contentId}")
    public ResponseEntity<LearningProgressDto> getProgressByUserAndContent(
            @PathVariable UUID userId,
            @PathVariable Long contentId) {

        Optional<LearningProgressDto> progress = progressService.getProgressByUserAndContent(userId, contentId);
        return progress.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ユーザーの学習統計取得
     * GET /learning/progress/{userId}/stats
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserLearningStats(@PathVariable UUID userId) {
        Map<String, Object> stats = progressService.getUserLearningStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 認証ユーザーの学習統計取得
     * GET /learning/progress/my/stats
     */
    @GetMapping("/my/stats")
    public ResponseEntity<Map<String, Object>> getMyLearningStats(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Map<String, Object> stats = progressService.getUserLearningStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 学習空間内進捗ランキング取得
     * GET /learning/progress/spaces/{spaceId}/ranking
     */
    @GetMapping("/spaces/{spaceId}/ranking")
    public ResponseEntity<List<Map<String, Object>>> getProgressRanking(@PathVariable Long spaceId) {
        List<Map<String, Object>> ranking = progressService.getProgressRanking(spaceId);
        return ResponseEntity.ok(ranking);
    }

    /**
     * 指定期間の学習進捗取得
     * GET /learning/progress/{userId}/range?start={startDate}&end={endDate}
     */
    @GetMapping("/{userId}/range")
    public ResponseEntity<List<LearningProgressDto>> getProgressByDateRange(
            @PathVariable UUID userId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {

        List<LearningProgressDto> progressList = progressService.getProgressByDateRange(userId, start, end);
        return ResponseEntity.ok(progressList);
    }

    /**
     * 未完了の学習進捗取得
     * GET /learning/progress/{userId}/incomplete
     */
    @GetMapping("/{userId}/incomplete")
    public ResponseEntity<List<LearningProgressDto>> getIncompleteProgress(@PathVariable UUID userId) {
        List<LearningProgressDto> progressList = progressService.getIncompleteProgress(userId);
        return ResponseEntity.ok(progressList);
    }

    /**
     * 認証ユーザーの未完了進捗取得
     * GET /learning/progress/my/incomplete
     */
    @GetMapping("/my/incomplete")
    public ResponseEntity<List<LearningProgressDto>> getMyIncompleteProgress(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<LearningProgressDto> progressList = progressService.getIncompleteProgress(userId);
        return ResponseEntity.ok(progressList);
    }

    /**
     * 学習進捗削除
     * DELETE /learning/progress/{userId}/contents/{contentId}
     */
    @DeleteMapping("/{userId}/contents/{contentId}")
    public ResponseEntity<Void> deleteProgress(
            @PathVariable UUID userId,
            @PathVariable Long contentId,
            Authentication authentication) {

        // 認証ユーザーが自分の進捗のみ削除可能
        UUID authUserId = UUID.fromString(authentication.getName());
        if (!authUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        progressService.deleteProgress(userId, contentId);
        return ResponseEntity.noContent().build();
    }
}
