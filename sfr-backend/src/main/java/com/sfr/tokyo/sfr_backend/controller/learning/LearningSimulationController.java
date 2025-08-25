package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningSimulationDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation.SimulationType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation.SimulationStatus;
import com.sfr.tokyo.sfr_backend.service.learning.LearningSimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 学習シミュレーション機能のREST APIコントローラー
 * 
 * シミュレーションのCRUD操作、進行状況管理、統計情報取得などのAPIを提供します。
 */
@RestController
@RequestMapping("/api/v1/learning/simulations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LearningSimulationController {

    private final LearningSimulationService simulationService;

    /**
     * 新しいシミュレーションを作成する
     */
    @PostMapping
    public ResponseEntity<LearningSimulationDto> createSimulation(
            @Valid @RequestBody LearningSimulationDto simulationDto) {

        log.info("Creating simulation for user: {} with type: {}",
                simulationDto.getUserId(), simulationDto.getSimulationType());

        LearningSimulationDto created = simulationService.createSimulation(simulationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * シミュレーション情報を更新する
     */
    @PutMapping("/{id}")
    public ResponseEntity<LearningSimulationDto> updateSimulation(
            @PathVariable Long id,
            @Valid @RequestBody LearningSimulationDto simulationDto) {

        log.info("Updating simulation with ID: {}", id);

        LearningSimulationDto updated = simulationService.updateSimulation(id, simulationDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * IDでシミュレーションを取得する
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningSimulationDto> getSimulation(@PathVariable Long id) {
        log.info("Getting simulation with ID: {}", id);

        LearningSimulationDto simulation = simulationService.getSimulationById(id);
        return ResponseEntity.ok(simulation);
    }

    /**
     * ユーザーのシミュレーション一覧を取得する（ページネーション対応）
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LearningSimulationDto>> getUserSimulations(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting simulations for user: {} with page: {}, size: {}", userId, page, size);

        Page<LearningSimulationDto> simulations = simulationService.getSimulationsByUserId(
                userId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(simulations);
    }

    /**
     * ユーザーの特定種類のシミュレーションを取得する
     */
    @GetMapping("/user/{userId}/type/{simulationType}")
    public ResponseEntity<List<LearningSimulationDto>> getUserSimulationsByType(
            @PathVariable UUID userId,
            @PathVariable SimulationType simulationType) {

        log.info("Getting simulations for user: {} with type: {}", userId, simulationType);

        List<LearningSimulationDto> simulations = simulationService.getSimulationsByUserIdAndType(userId,
                simulationType);
        return ResponseEntity.ok(simulations);
    }

    /**
     * ユーザーの特定ステータスのシミュレーションを取得する
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<LearningSimulationDto>> getUserSimulationsByStatus(
            @PathVariable UUID userId,
            @PathVariable SimulationStatus status) {

        log.info("Getting simulations for user: {} with status: {}", userId, status);

        List<LearningSimulationDto> simulations = simulationService.getSimulationsByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(simulations);
    }

    /**
     * 複数条件でシミュレーションを検索する
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LearningSimulationDto>> searchSimulations(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) SimulationType simulationType,
            @RequestParam(required = false) SimulationStatus status,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Double minScore,
            @RequestParam(required = false) Double maxScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Searching simulations with criteria - userId: {}, type: {}, status: {}",
                userId, simulationType, status);

        Page<LearningSimulationDto> simulations = simulationService.searchSimulations(
                userId, simulationType, status, difficultyLevel, minScore, maxScore,
                page, size, sortBy, sortDirection);

        return ResponseEntity.ok(simulations);
    }

    /**
     * シミュレーションを開始する
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<LearningSimulationDto> startSimulation(@PathVariable Long id) {
        log.info("Starting simulation with ID: {}", id);

        LearningSimulationDto simulation = simulationService.startSimulation(id);
        return ResponseEntity.ok(simulation);
    }

    /**
     * シミュレーションを一時停止する
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<LearningSimulationDto> pauseSimulation(@PathVariable Long id) {
        log.info("Pausing simulation with ID: {}", id);

        LearningSimulationDto simulation = simulationService.pauseSimulation(id);
        return ResponseEntity.ok(simulation);
    }

    /**
     * シミュレーションを完了する
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<LearningSimulationDto> completeSimulation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        log.info("Completing simulation with ID: {}", id);

        BigDecimal finalScore = request.get("finalScore") != null ? new BigDecimal(request.get("finalScore").toString())
                : null;

        LearningSimulationDto simulation = simulationService.completeSimulation(id, finalScore);
        return ResponseEntity.ok(simulation);
    }

    /**
     * シミュレーションを失敗として終了する
     */
    @PostMapping("/{id}/fail")
    public ResponseEntity<LearningSimulationDto> failSimulation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        log.info("Failing simulation with ID: {}", id);

        String reason = request.get("reason");
        LearningSimulationDto simulation = simulationService.failSimulation(id, reason);
        return ResponseEntity.ok(simulation);
    }

    /**
     * シミュレーションをキャンセルする
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<LearningSimulationDto> cancelSimulation(@PathVariable Long id) {
        log.info("Cancelling simulation with ID: {}", id);

        LearningSimulationDto simulation = simulationService.cancelSimulation(id);
        return ResponseEntity.ok(simulation);
    }

    /**
     * 進行状況を更新する
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<LearningSimulationDto> updateProgress(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        log.info("Updating progress for simulation ID: {}", id);

        BigDecimal progressPercentage = new BigDecimal(request.get("progressPercentage").toString());
        LearningSimulationDto simulation = simulationService.updateProgress(id, progressPercentage);
        return ResponseEntity.ok(simulation);
    }

    /**
     * ステップ進行状況を更新する
     */
    @PutMapping("/{id}/step")
    public ResponseEntity<LearningSimulationDto> updateStepProgress(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {

        log.info("Updating step progress for simulation ID: {}", id);

        Integer currentStep = request.get("currentStep");
        LearningSimulationDto simulation = simulationService.updateStepProgress(id, currentStep);
        return ResponseEntity.ok(simulation);
    }

    /**
     * 状態データを更新する
     */
    @PutMapping("/{id}/state")
    public ResponseEntity<LearningSimulationDto> updateStateData(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        log.info("Updating state data for simulation ID: {}", id);

        String stateData = request.get("stateData");
        LearningSimulationDto simulation = simulationService.updateStateData(id, stateData);
        return ResponseEntity.ok(simulation);
    }

    /**
     * フィードバックデータを更新する
     */
    @PutMapping("/{id}/feedback")
    public ResponseEntity<LearningSimulationDto> updateFeedbackData(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        log.info("Updating feedback data for simulation ID: {}", id);

        String feedbackData = request.get("feedbackData");
        LearningSimulationDto simulation = simulationService.updateFeedbackData(id, feedbackData);
        return ResponseEntity.ok(simulation);
    }

    /**
     * ユーザーの最新のシミュレーションを取得する
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<LearningSimulationDto>> getRecentSimulations(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Getting recent simulations for user: {} with limit: {}", userId, limit);

        List<LearningSimulationDto> simulations = simulationService.getRecentSimulations(userId, limit);
        return ResponseEntity.ok(simulations);
    }

    /**
     * ユーザーの統計情報を取得する
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable UUID userId) {
        log.info("Getting statistics for user: {}", userId);

        Long completedCount = simulationService.getCompletedSimulationCount(userId);
        Double averageScore = simulationService.getAverageScore(userId);
        Double maxScore = simulationService.getMaxScore(userId);
        Long totalDuration = simulationService.getTotalDuration(userId);

        Map<String, Object> stats = Map.of(
                "completedCount", completedCount != null ? completedCount : 0L,
                "averageScore", averageScore != null ? averageScore : 0.0,
                "maxScore", maxScore != null ? maxScore : 0.0,
                "totalDurationMinutes", totalDuration != null ? totalDuration : 0L);

        return ResponseEntity.ok(stats);
    }

    /**
     * シミュレーションを削除する
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulation(@PathVariable Long id) {
        log.info("Deleting simulation with ID: {}", id);

        simulationService.deleteSimulation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 実行中で長時間アクセスされていないシミュレーションをタイムアウトとする
     */
    @PostMapping("/timeout-stale")
    public ResponseEntity<Map<String, String>> timeoutStaleSimulations(
            @RequestParam(defaultValue = "24") int timeoutHours) {

        log.info("Timing out stale simulations with timeout: {} hours", timeoutHours);

        simulationService.timeoutStaleSimulations(timeoutHours);

        Map<String, String> response = Map.of(
                "message", "Stale simulations have been timed out",
                "timeoutHours", String.valueOf(timeoutHours));

        return ResponseEntity.ok(response);
    }

    /**
     * シミュレーション種類の一覧を取得する
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getSimulationTypes() {
        log.info("Getting simulation types");

        List<Map<String, String>> types = List.of(
                Map.of("value", "BUSINESS_CASE", "label", "ビジネスケース"),
                Map.of("value", "SCIENTIFIC_EXPERIMENT", "label", "科学実験"),
                Map.of("value", "LANGUAGE_PRACTICE", "label", "言語練習"),
                Map.of("value", "PROGRAMMING_CHALLENGE", "label", "プログラミングチャレンジ"),
                Map.of("value", "HISTORICAL_SCENARIO", "label", "歴史シナリオ"),
                Map.of("value", "MATHEMATICAL_PROBLEM", "label", "数学問題"),
                Map.of("value", "DEBATE_SIMULATION", "label", "ディベートシミュレーション"),
                Map.of("value", "ROLE_PLAYING", "label", "ロールプレイング"),
                Map.of("value", "DESIGN_THINKING", "label", "デザイン思考"),
                Map.of("value", "VIRTUAL_LAB", "label", "バーチャル実験室"));

        return ResponseEntity.ok(types);
    }

    /**
     * シミュレーションステータスの一覧を取得する
     */
    @GetMapping("/statuses")
    public ResponseEntity<List<Map<String, String>>> getSimulationStatuses() {
        log.info("Getting simulation statuses");

        List<Map<String, String>> statuses = List.of(
                Map.of("value", "NOT_STARTED", "label", "未開始"),
                Map.of("value", "IN_PROGRESS", "label", "実行中"),
                Map.of("value", "PAUSED", "label", "一時停止"),
                Map.of("value", "COMPLETED", "label", "完了"),
                Map.of("value", "FAILED", "label", "失敗"),
                Map.of("value", "CANCELLED", "label", "キャンセル"),
                Map.of("value", "TIMEOUT", "label", "タイムアウト"));

        return ResponseEntity.ok(statuses);
    }
}
