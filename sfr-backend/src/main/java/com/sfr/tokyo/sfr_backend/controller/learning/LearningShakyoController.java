package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningShakyoDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningShakyo;
import com.sfr.tokyo.sfr_backend.service.learning.LearningShakyoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 学習写経コントローラー - LearningShakyo Controller
 */
@RestController
@RequestMapping("/api/learning/shakyo")
@CrossOrigin(origins = "*")
public class LearningShakyoController {

    private final LearningShakyoService learningShakyoService;

    @Autowired
    public LearningShakyoController(LearningShakyoService learningShakyoService) {
        this.learningShakyoService = learningShakyoService;
    }

    // CRUD操作

    /**
     * 写経作成
     */
    @PostMapping
    public ResponseEntity<LearningShakyoDto> createShakyo(@Valid @RequestBody LearningShakyoDto shakyoDto) {
        try {
            LearningShakyoDto created = learningShakyoService.createShakyo(shakyoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 写経取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningShakyoDto> getShakyoById(@PathVariable Long id) {
        return learningShakyoService.getShakyoById(id)
                .map(shakyo -> ResponseEntity.ok().body(shakyo))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 写経更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<LearningShakyoDto> updateShakyo(@PathVariable Long id,
            @Valid @RequestBody LearningShakyoDto shakyoDto) {
        try {
            LearningShakyoDto updated = learningShakyoService.updateShakyo(id, shakyoDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 写経削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShakyo(@PathVariable Long id) {
        try {
            learningShakyoService.deleteShakyo(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 写経実行操作

    /**
     * 写経開始
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<LearningShakyoDto> startShakyo(@PathVariable Long id) {
        try {
            LearningShakyoDto started = learningShakyoService.startShakyo(id);
            return ResponseEntity.ok(started);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 写経一時停止
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<LearningShakyoDto> pauseShakyo(@PathVariable Long id) {
        try {
            LearningShakyoDto paused = learningShakyoService.pauseShakyo(id);
            return ResponseEntity.ok(paused);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 文字入力
     */
    @PostMapping("/{id}/type")
    public ResponseEntity<LearningShakyoService.TypeResult> typeCharacter(@PathVariable Long id,
            @RequestParam char inputChar) {
        try {
            LearningShakyoService.TypeResult result = learningShakyoService.typeCharacter(id, inputChar);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ヒント取得
     */
    @GetMapping("/{id}/hint")
    public ResponseEntity<String> getHint(@PathVariable Long id) {
        String hint = learningShakyoService.getHint(id);
        if (hint != null) {
            return ResponseEntity.ok(hint);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 写経完了
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<LearningShakyoDto> completeShakyo(@PathVariable Long id) {
        try {
            LearningShakyoDto completed = learningShakyoService.completeShakyo(id);
            return ResponseEntity.ok(completed);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 検索・一覧取得

    /**
     * ユーザーの写経一覧取得
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LearningShakyoDto>> getShakyosByUserId(@PathVariable UUID userId) {
        List<LearningShakyoDto> shakyos = learningShakyoService.getShakyosByUserId(userId);
        return ResponseEntity.ok(shakyos);
    }

    /**
     * ユーザーとスペースの写経一覧取得
     */
    @GetMapping("/user/{userId}/space/{spaceId}")
    public ResponseEntity<List<LearningShakyoDto>> getShakyosByUserIdAndSpaceId(@PathVariable UUID userId,
            @PathVariable Long spaceId) {
        List<LearningShakyoDto> shakyos = learningShakyoService.getShakyosByUserIdAndSpaceId(userId, spaceId);
        return ResponseEntity.ok(shakyos);
    }

    /**
     * 写経タイプ別一覧取得
     */
    @GetMapping("/user/{userId}/type/{shakyoType}")
    public ResponseEntity<List<LearningShakyoDto>> getShakyosByUserIdAndType(@PathVariable UUID userId,
            @PathVariable LearningShakyo.ShakyoType shakyoType) {
        List<LearningShakyoDto> shakyos = learningShakyoService.getShakyosByUserIdAndType(userId, shakyoType);
        return ResponseEntity.ok(shakyos);
    }

    /**
     * ステータス別一覧取得
     */
    @GetMapping("/user/{userId}/status/{shakyoStatus}")
    public ResponseEntity<List<LearningShakyoDto>> getShakyosByUserIdAndStatus(@PathVariable UUID userId,
            @PathVariable LearningShakyo.ShakyoStatus shakyoStatus) {
        List<LearningShakyoDto> shakyos = learningShakyoService.getShakyosByUserIdAndStatus(userId, shakyoStatus);
        return ResponseEntity.ok(shakyos);
    }

    /**
     * 進行中の写経一覧取得
     */
    @GetMapping("/user/{userId}/in-progress")
    public ResponseEntity<List<LearningShakyoDto>> getInProgressShakyo(@PathVariable UUID userId) {
        List<LearningShakyoDto> shakyos = learningShakyoService.getInProgressShakyo(userId);
        return ResponseEntity.ok(shakyos);
    }

    /**
     * 複合条件検索
     */
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<Page<LearningShakyoDto>> searchShakyo(@PathVariable UUID userId,
            @RequestParam(required = false) LearningShakyo.ShakyoType shakyoType,
            @RequestParam(required = false) LearningShakyo.ShakyoStatus shakyoStatus,
            @RequestParam(required = false) LearningShakyo.DifficultyLevel difficultyLevel,
            @RequestParam(required = false) String programmingLanguage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LearningShakyoDto> shakyos = learningShakyoService.searchShakyo(userId, shakyoType, shakyoStatus,
                difficultyLevel, programmingLanguage, pageable);
        return ResponseEntity.ok(shakyos);
    }

    /**
     * タイトル検索
     */
    @GetMapping("/user/{userId}/search-title")
    public ResponseEntity<List<LearningShakyoDto>> searchShakyoByTitle(@PathVariable UUID userId,
            @RequestParam String title) {
        List<LearningShakyoDto> shakyos = learningShakyoService.searchShakyoByTitle(userId, title);
        return ResponseEntity.ok(shakyos);
    }

    // 統計・分析

    /**
     * ユーザー統計取得
     */
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<LearningShakyoService.UserShakyoStatistics> getUserStatistics(@PathVariable UUID userId) {
        LearningShakyoService.UserShakyoStatistics statistics = learningShakyoService.getUserStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 写経タイプ別統計取得
     */
    @GetMapping("/user/{userId}/statistics/type")
    public ResponseEntity<Map<LearningShakyo.ShakyoType, Long>> getTypeStatistics(@PathVariable UUID userId) {
        Map<LearningShakyo.ShakyoType, Long> statistics = learningShakyoService.getTypeStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 難易度別統計取得
     */
    @GetMapping("/user/{userId}/statistics/difficulty")
    public ResponseEntity<Map<LearningShakyo.DifficultyLevel, Long>> getDifficultyStatistics(
            @PathVariable UUID userId) {
        Map<LearningShakyo.DifficultyLevel, Long> statistics = learningShakyoService.getDifficultyStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 日別完了統計取得
     */
    @GetMapping("/user/{userId}/statistics/daily")
    public ResponseEntity<Map<LocalDate, Long>> getDailyCompletionStatistics(@PathVariable UUID userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Map<LocalDate, Long> statistics = learningShakyoService.getDailyCompletionStatistics(userId, startDate,
                endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 月別進捗統計取得
     */
    @GetMapping("/user/{userId}/statistics/monthly")
    public ResponseEntity<List<LearningShakyoService.MonthlyProgress>> getMonthlyProgress(@PathVariable UUID userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<LearningShakyoService.MonthlyProgress> progress = learningShakyoService.getMonthlyProgress(userId,
                startDate, endDate);
        return ResponseEntity.ok(progress);
    }

    // ランキング

    /**
     * 写経タイプ別ランキング取得
     */
    @GetMapping("/ranking/type/{shakyoType}")
    public ResponseEntity<List<LearningShakyoDto>> getTypeRanking(@PathVariable LearningShakyo.ShakyoType shakyoType,
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningShakyoDto> ranking = learningShakyoService.getTypeRanking(shakyoType, limit);
        return ResponseEntity.ok(ranking);
    }

    /**
     * 難易度別ランキング取得
     */
    @GetMapping("/ranking/difficulty/{difficultyLevel}")
    public ResponseEntity<List<LearningShakyoDto>> getDifficultyRanking(
            @PathVariable LearningShakyo.DifficultyLevel difficultyLevel,
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningShakyoDto> ranking = learningShakyoService.getDifficultyRanking(difficultyLevel, limit);
        return ResponseEntity.ok(ranking);
    }

    /**
     * 正確率ランキング取得
     */
    @GetMapping("/ranking/accuracy")
    public ResponseEntity<List<LearningShakyoDto>> getAccuracyRanking(@RequestParam(defaultValue = "10") int limit) {
        List<LearningShakyoDto> ranking = learningShakyoService.getAccuracyRanking(limit);
        return ResponseEntity.ok(ranking);
    }

    /**
     * 入力速度ランキング取得
     */
    @GetMapping("/ranking/speed")
    public ResponseEntity<List<LearningShakyoDto>> getSpeedRanking(@RequestParam(defaultValue = "10") int limit) {
        List<LearningShakyoDto> ranking = learningShakyoService.getSpeedRanking(limit);
        return ResponseEntity.ok(ranking);
    }

    // 管理・メンテナンス

    /**
     * 非アクティブな写経検出
     */
    @GetMapping("/user/{userId}/inactive")
    public ResponseEntity<List<LearningShakyoDto>> findInactiveShakyo(@PathVariable UUID userId,
            @RequestParam(defaultValue = "7") int inactiveDays) {
        List<LearningShakyoDto> inactiveShakyo = learningShakyoService.findInactiveShakyo(userId, inactiveDays);
        return ResponseEntity.ok(inactiveShakyo);
    }

    /**
     * 最近の活動取得
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<LearningShakyoDto>> getRecentActivities(@PathVariable UUID userId,
            @RequestParam(defaultValue = "7") int recentDays) {
        List<LearningShakyoDto> recentActivities = learningShakyoService.getRecentActivities(userId, recentDays);
        return ResponseEntity.ok(recentActivities);
    }

    // ヘルパーメソッド - 写経タイプ一覧取得

    /**
     * 写経タイプ一覧取得
     */
    @GetMapping("/types")
    public ResponseEntity<LearningShakyo.ShakyoType[]> getShakyoTypes() {
        return ResponseEntity.ok(LearningShakyo.ShakyoType.values());
    }

    /**
     * 写経ステータス一覧取得
     */
    @GetMapping("/statuses")
    public ResponseEntity<LearningShakyo.ShakyoStatus[]> getShakyoStatuses() {
        return ResponseEntity.ok(LearningShakyo.ShakyoStatus.values());
    }

    /**
     * 難易度レベル一覧取得
     */
    @GetMapping("/difficulty-levels")
    public ResponseEntity<LearningShakyo.DifficultyLevel[]> getDifficultyLevels() {
        return ResponseEntity.ok(LearningShakyo.DifficultyLevel.values());
    }

    // バッチ操作

    /**
     * 複数写経作成
     */
    @PostMapping("/batch")
    public ResponseEntity<List<LearningShakyoDto>> createBatchShakyo(
            @Valid @RequestBody List<LearningShakyoDto> shakyoDtoList) {
        try {
            List<LearningShakyoDto> created = shakyoDtoList.stream()
                    .map(learningShakyoService::createShakyo)
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * スコア範囲検索
     */
    @GetMapping("/user/{userId}/score-range")
    public ResponseEntity<List<LearningShakyoDto>> getShakyosByScoreRange(@PathVariable UUID userId,
            @RequestParam BigDecimal minScore,
            @RequestParam BigDecimal maxScore) {
        // Note: This would require a new service method
        // For now, returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }

    /**
     * 正確率範囲検索
     */
    @GetMapping("/user/{userId}/accuracy-range")
    public ResponseEntity<List<LearningShakyoDto>> getShakyosByAccuracyRange(@PathVariable UUID userId,
            @RequestParam BigDecimal minAccuracy,
            @RequestParam BigDecimal maxAccuracy) {
        // Note: This would require a new service method
        // For now, returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }

    /**
     * 入力速度範囲検索
     */
    @GetMapping("/user/{userId}/speed-range")
    public ResponseEntity<List<LearningShakyoDto>> getShakyosBySpeedRange(@PathVariable UUID userId,
            @RequestParam Integer minSpeed,
            @RequestParam Integer maxSpeed) {
        // Note: This would require a new service method
        // For now, returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }
}
