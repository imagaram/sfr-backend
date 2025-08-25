package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningSimulationDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation.SimulationType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation.SimulationStatus;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSimulationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 学習シミュレーション機能のサービスクラス
 * 
 * シミュレーションの作成、更新、検索、統計情報の取得など
 * ビジネスロジックを提供します。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningSimulationService {

    private final LearningSimulationRepository simulationRepository;

    /**
     * シミュレーションを作成する
     */
    @Transactional
    public LearningSimulationDto createSimulation(LearningSimulationDto simulationDto) {
        log.info("Creating simulation for user: {} with type: {}",
                simulationDto.getUserId(), simulationDto.getSimulationType());

        LearningSimulation simulation = convertToEntity(simulationDto);
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        log.info("Created simulation with ID: {}", savedSimulation.getId());
        return convertToDto(savedSimulation);
    }

    /**
     * シミュレーションを更新する
     */
    @Transactional
    public LearningSimulationDto updateSimulation(Long id, LearningSimulationDto simulationDto) {
        log.info("Updating simulation with ID: {}", id);

        LearningSimulation existingSimulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        // 基本情報を更新
        existingSimulation.setTitle(simulationDto.getTitle());
        existingSimulation.setDescription(simulationDto.getDescription());
        existingSimulation.setDifficultyLevel(simulationDto.getDifficultyLevel());
        existingSimulation.setEstimatedDurationMinutes(simulationDto.getEstimatedDurationMinutes());
        existingSimulation.setMaxAttempts(simulationDto.getMaxAttempts());
        existingSimulation.setConfigurationData(simulationDto.getConfigurationData());

        LearningSimulation savedSimulation = simulationRepository.save(existingSimulation);
        log.info("Updated simulation with ID: {}", savedSimulation.getId());
        return convertToDto(savedSimulation);
    }

    /**
     * シミュレーションを開始する
     */
    @Transactional
    public LearningSimulationDto startSimulation(Long id) {
        log.info("Starting simulation with ID: {}", id);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.startSimulation();
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        log.info("Started simulation with ID: {}", savedSimulation.getId());
        return convertToDto(savedSimulation);
    }

    /**
     * シミュレーションを一時停止する
     */
    @Transactional
    public LearningSimulationDto pauseSimulation(Long id) {
        log.info("Pausing simulation with ID: {}", id);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.pauseSimulation();
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        log.info("Paused simulation with ID: {}", savedSimulation.getId());
        return convertToDto(savedSimulation);
    }

    /**
     * シミュレーションを完了する
     */
    @Transactional
    public LearningSimulationDto completeSimulation(Long id, BigDecimal finalScore) {
        log.info("Completing simulation with ID: {} with score: {}", id, finalScore);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.completeSimulation(finalScore);
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        log.info("Completed simulation with ID: {}", savedSimulation.getId());
        return convertToDto(savedSimulation);
    }

    /**
     * シミュレーションを失敗として終了する
     */
    @Transactional
    public LearningSimulationDto failSimulation(Long id, String reason) {
        log.info("Failing simulation with ID: {} with reason: {}", id, reason);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.failSimulation(reason);
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        log.info("Failed simulation with ID: {}", savedSimulation.getId());
        return convertToDto(savedSimulation);
    }

    /**
     * シミュレーションをキャンセルする
     */
    @Transactional
    public LearningSimulationDto cancelSimulation(Long id) {
        log.info("Cancelling simulation with ID: {}", id);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.cancelSimulation();
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        log.info("Cancelled simulation with ID: {}", savedSimulation.getId());
        return convertToDto(savedSimulation);
    }

    /**
     * 進行状況を更新する
     */
    @Transactional
    public LearningSimulationDto updateProgress(Long id, BigDecimal progressPercentage) {
        log.info("Updating progress for simulation ID: {} to {}%", id, progressPercentage);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.updateProgress(progressPercentage);
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        return convertToDto(savedSimulation);
    }

    /**
     * ステップ進行状況を更新する
     */
    @Transactional
    public LearningSimulationDto updateStepProgress(Long id, Integer currentStep) {
        log.info("Updating step progress for simulation ID: {} to step: {}", id, currentStep);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.updateStepProgress(currentStep);
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        return convertToDto(savedSimulation);
    }

    /**
     * 状態データを更新する
     */
    @Transactional
    public LearningSimulationDto updateStateData(Long id, String stateData) {
        log.info("Updating state data for simulation ID: {}", id);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.setStateData(stateData);
        simulation.setLastAccessedAt(LocalDateTime.now());
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        return convertToDto(savedSimulation);
    }

    /**
     * フィードバックデータを更新する
     */
    @Transactional
    public LearningSimulationDto updateFeedbackData(Long id, String feedbackData) {
        log.info("Updating feedback data for simulation ID: {}", id);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        simulation.setFeedbackData(feedbackData);
        LearningSimulation savedSimulation = simulationRepository.save(simulation);

        return convertToDto(savedSimulation);
    }

    /**
     * IDでシミュレーションを取得する
     */
    @Transactional(readOnly = true)
    public LearningSimulationDto getSimulationById(Long id) {
        log.info("Getting simulation with ID: {}", id);

        LearningSimulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found with ID: " + id));

        return convertToDto(simulation);
    }

    /**
     * ユーザーIDでシミュレーションを取得する
     */
    @Transactional(readOnly = true)
    public Page<LearningSimulationDto> getSimulationsByUserId(UUID userId, int page, int size, String sortBy,
            String sortDirection) {
        log.info("Getting simulations for user: {} with page: {}, size: {}", userId, page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<LearningSimulation> simulations = simulationRepository.findByUserId(userId, pageable);
        return simulations.map(this::convertToDto);
    }

    /**
     * ユーザーIDとシミュレーション種類でシミュレーションを取得する
     */
    @Transactional(readOnly = true)
    public List<LearningSimulationDto> getSimulationsByUserIdAndType(UUID userId, SimulationType simulationType) {
        log.info("Getting simulations for user: {} with type: {}", userId, simulationType);

        List<LearningSimulation> simulations = simulationRepository.findByUserIdAndSimulationType(userId,
                simulationType);
        return simulations.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * ユーザーIDとステータスでシミュレーションを取得する
     */
    @Transactional(readOnly = true)
    public List<LearningSimulationDto> getSimulationsByUserIdAndStatus(UUID userId, SimulationStatus status) {
        log.info("Getting simulations for user: {} with status: {}", userId, status);

        List<LearningSimulation> simulations = simulationRepository.findByUserIdAndStatus(userId, status);
        return simulations.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 複数条件でシミュレーションを検索する
     */
    @Transactional(readOnly = true)
    public Page<LearningSimulationDto> searchSimulations(
            UUID userId, SimulationType simulationType, SimulationStatus status,
            Integer difficultyLevel, Double minScore, Double maxScore,
            int page, int size, String sortBy, String sortDirection) {

        log.info("Searching simulations with multiple criteria for user: {}", userId);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<LearningSimulation> simulations = simulationRepository.findByMultipleCriteria(
                userId, simulationType, status, difficultyLevel, minScore, maxScore, pageable);

        return simulations.map(this::convertToDto);
    }

    /**
     * ユーザーの完了済みシミュレーション数を取得する
     */
    @Transactional(readOnly = true)
    public Long getCompletedSimulationCount(UUID userId) {
        log.info("Getting completed simulation count for user: {}", userId);
        return simulationRepository.countCompletedSimulationsByUserId(userId);
    }

    /**
     * ユーザーの平均スコアを取得する
     */
    @Transactional(readOnly = true)
    public Double getAverageScore(UUID userId) {
        log.info("Getting average score for user: {}", userId);
        return simulationRepository.findAverageScoreByUserId(userId);
    }

    /**
     * ユーザーの最高スコアを取得する
     */
    @Transactional(readOnly = true)
    public Double getMaxScore(UUID userId) {
        log.info("Getting max score for user: {}", userId);
        return simulationRepository.findMaxScoreByUserId(userId).orElse(0.0);
    }

    /**
     * ユーザーの総実行時間を取得する
     */
    @Transactional(readOnly = true)
    public Long getTotalDuration(UUID userId) {
        log.info("Getting total duration for user: {}", userId);
        return simulationRepository.findTotalDurationByUserId(userId).orElse(0L);
    }

    /**
     * ユーザーの最新のシミュレーションを取得する
     */
    @Transactional(readOnly = true)
    public List<LearningSimulationDto> getRecentSimulations(UUID userId, int limit) {
        log.info("Getting recent simulations for user: {} with limit: {}", userId, limit);

        List<LearningSimulation> simulations = simulationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        return simulations.stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 実行中で長時間アクセスされていないシミュレーションをタイムアウトとする
     */
    @Transactional
    public void timeoutStaleSimulations(int timeoutHours) {
        log.info("Checking for stale simulations with timeout: {} hours", timeoutHours);

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(timeoutHours);
        List<LearningSimulation> staleSimulations = simulationRepository.findStaleInProgressSimulations(cutoffTime);

        for (LearningSimulation simulation : staleSimulations) {
            simulation.setStatus(SimulationStatus.TIMEOUT);
            simulation.setCompletedAt(LocalDateTime.now());
            simulationRepository.save(simulation);
            log.info("Timed out simulation with ID: {}", simulation.getId());
        }

        log.info("Timed out {} stale simulations", staleSimulations.size());
    }

    /**
     * シミュレーションを削除する
     */
    @Transactional
    public void deleteSimulation(Long id) {
        log.info("Deleting simulation with ID: {}", id);

        if (!simulationRepository.existsById(id)) {
            throw new RuntimeException("Simulation not found with ID: " + id);
        }

        simulationRepository.deleteById(id);
        log.info("Deleted simulation with ID: {}", id);
    }

    /**
     * エンティティをDTOに変換する
     */
    private LearningSimulationDto convertToDto(LearningSimulation simulation) {
        LearningSimulationDto dto = new LearningSimulationDto();
        dto.setId(simulation.getId());
        dto.setUserId(simulation.getUserId());
        dto.setSimulationType(simulation.getSimulationType());
        dto.setTitle(simulation.getTitle());
        dto.setDescription(simulation.getDescription());
        dto.setStatus(simulation.getStatus());
        dto.setDifficultyLevel(simulation.getDifficultyLevel());
        dto.setEstimatedDurationMinutes(simulation.getEstimatedDurationMinutes());
        dto.setActualDurationMinutes(simulation.getActualDurationMinutes());
        dto.setProgressPercentage(simulation.getProgressPercentage());
        dto.setCurrentStep(simulation.getCurrentStep());
        dto.setTotalSteps(simulation.getTotalSteps());
        dto.setScore(simulation.getScore());
        dto.setMaxScore(simulation.getMaxScore());
        dto.setScorePercentage(simulation.getScorePercentage());
        dto.setAttemptsCount(simulation.getAttemptsCount());
        dto.setMaxAttempts(simulation.getMaxAttempts());
        dto.setConfigurationData(simulation.getConfigurationData());
        dto.setStateData(simulation.getStateData());
        dto.setResultData(simulation.getResultData());
        dto.setFeedbackData(simulation.getFeedbackData());
        dto.setStartedAt(simulation.getStartedAt());
        dto.setCompletedAt(simulation.getCompletedAt());
        dto.setLastAccessedAt(simulation.getLastAccessedAt());
        dto.setCreatedAt(simulation.getCreatedAt());
        dto.setUpdatedAt(simulation.getUpdatedAt());
        return dto;
    }

    /**
     * DTOをエンティティに変換する
     */
    private LearningSimulation convertToEntity(LearningSimulationDto dto) {
        LearningSimulation simulation = new LearningSimulation();
        simulation.setUserId(dto.getUserId());
        simulation.setSimulationType(dto.getSimulationType());
        simulation.setTitle(dto.getTitle());
        simulation.setDescription(dto.getDescription());
        simulation.setDifficultyLevel(dto.getDifficultyLevel());
        simulation.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
        simulation.setTotalSteps(dto.getTotalSteps());
        simulation.setMaxScore(dto.getMaxScore());
        simulation.setMaxAttempts(dto.getMaxAttempts());
        simulation.setConfigurationData(dto.getConfigurationData());
        return simulation;
    }
}
