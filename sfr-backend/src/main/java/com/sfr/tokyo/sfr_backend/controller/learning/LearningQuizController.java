package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningQuizDto;
import com.sfr.tokyo.sfr_backend.service.learning.LearningQuizService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning/quizzes")
@PreAuthorize("hasRole('USER')")
public class LearningQuizController {

    private final LearningQuizService quizService;

    public LearningQuizController(LearningQuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * クイズ作成
     * POST /api/learning/quizzes
     */
    @PostMapping
    public ResponseEntity<LearningQuizDto> createQuiz(@Valid @RequestBody LearningQuizDto quizDto) {
        LearningQuizDto createdQuiz = quizService.createQuiz(quizDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    /**
     * クイズ詳細取得
     * GET /api/learning/quizzes/{quizId}
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<LearningQuizDto> getQuiz(@PathVariable Long quizId) {
        LearningQuizDto quiz = quizService.getQuiz(quizId);
        return ResponseEntity.ok(quiz);
    }

    /**
     * 学習空間内のクイズ一覧取得
     * GET /api/learning/quizzes?spaceId={spaceId}
     */
    @GetMapping
    public ResponseEntity<List<LearningQuizDto>> getQuizzesBySpace(
            @RequestParam Long spaceId) {
        List<LearningQuizDto> quizzes = quizService.getQuizzesBySpace(spaceId);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * クイズタイトル検索
     * GET /api/learning/quizzes/search?spaceId={spaceId}&title={title}
     */
    @GetMapping("/search")
    public ResponseEntity<List<LearningQuizDto>> searchQuizzes(
            @RequestParam Long spaceId,
            @RequestParam String title) {
        List<LearningQuizDto> quizzes = quizService.searchQuizzesByTitle(spaceId, title);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 最近のクイズ取得
     * GET /api/learning/quizzes/recent?spaceId={spaceId}&limit={limit}
     */
    @GetMapping("/recent")
    public ResponseEntity<List<LearningQuizDto>> getRecentQuizzes(
            @RequestParam Long spaceId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningQuizDto> quizzes = quizService.getRecentQuizzes(spaceId, limit);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 期間指定クイズ取得
     * GET
     * /api/learning/quizzes/date-range?spaceId={spaceId}&startDate={startDate}&endDate={endDate}
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<LearningQuizDto>> getQuizzesByDateRange(
            @RequestParam Long spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<LearningQuizDto> quizzes = quizService.getQuizzesByDateRange(spaceId, startDate, endDate);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * クイズ統計取得
     * GET /api/learning/quizzes/statistics?spaceId={spaceId}
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getQuizStatistics(@RequestParam Long spaceId) {
        Map<String, Object> statistics = quizService.getQuizStatistics(spaceId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * クイズ更新
     * PUT /api/learning/quizzes/{quizId}
     */
    @PutMapping("/{quizId}")
    public ResponseEntity<LearningQuizDto> updateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody LearningQuizDto quizDto) {
        LearningQuizDto updatedQuiz = quizService.updateQuiz(quizId, quizDto);
        return ResponseEntity.ok(updatedQuiz);
    }

    /**
     * クイズ削除
     * DELETE /api/learning/quizzes/{quizId}
     */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 学習空間とIDでクイズ取得（権限チェック用）
     * GET /api/learning/quizzes/{quizId}/space/{spaceId}
     */
    @GetMapping("/{quizId}/space/{spaceId}")
    public ResponseEntity<LearningQuizDto> getQuizBySpaceAndId(
            @PathVariable Long spaceId,
            @PathVariable Long quizId) {
        LearningQuizDto quiz = quizService.getQuizBySpaceAndId(spaceId, quizId);
        return ResponseEntity.ok(quiz);
    }
}
