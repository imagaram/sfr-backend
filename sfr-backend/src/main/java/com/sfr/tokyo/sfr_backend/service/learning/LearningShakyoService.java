package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningShakyoDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningShakyo;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningShakyoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学習写経サービス - LearningShakyo Service
 */
@Service
@Transactional
public class LearningShakyoService {

    private final LearningShakyoRepository learningShakyoRepository;

    @Autowired
    public LearningShakyoService(LearningShakyoRepository learningShakyoRepository) {
        this.learningShakyoRepository = learningShakyoRepository;
    }

    // CRUD操作

    /**
     * 写経を作成
     */
    public LearningShakyoDto createShakyo(LearningShakyoDto dto) {
        LearningShakyo entity = convertToEntity(dto);
        entity.setShakyoStatus(LearningShakyo.ShakyoStatus.NOT_STARTED);
        entity = learningShakyoRepository.save(entity);
        return convertToDto(entity);
    }

    /**
     * 写経を取得
     */
    @Transactional(readOnly = true)
    public Optional<LearningShakyoDto> getShakyoById(Long id) {
        return learningShakyoRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 写経を更新
     */
    public LearningShakyoDto updateShakyo(Long id, LearningShakyoDto dto) {
        return learningShakyoRepository.findById(id)
                .map(existingShakyo -> {
                    updateEntityFromDto(existingShakyo, dto);
                    LearningShakyo saved = learningShakyoRepository.save(existingShakyo);
                    return convertToDto(saved);
                })
                .orElseThrow(() -> new RuntimeException("LearningShakyo not found with id: " + id));
    }

    /**
     * 写経を削除
     */
    public void deleteShakyo(Long id) {
        learningShakyoRepository.deleteById(id);
    }

    // 写経実行メソッド

    /**
     * 写経を開始
     */
    public LearningShakyoDto startShakyo(Long id) {
        return learningShakyoRepository.findById(id)
                .map(shakyo -> {
                    shakyo.startShakyo();
                    LearningShakyo saved = learningShakyoRepository.save(shakyo);
                    return convertToDto(saved);
                })
                .orElseThrow(() -> new RuntimeException("LearningShakyo not found with id: " + id));
    }

    /**
     * 写経を一時停止
     */
    public LearningShakyoDto pauseShakyo(Long id) {
        return learningShakyoRepository.findById(id)
                .map(shakyo -> {
                    shakyo.pauseShakyo();
                    LearningShakyo saved = learningShakyoRepository.save(shakyo);
                    return convertToDto(saved);
                })
                .orElseThrow(() -> new RuntimeException("LearningShakyo not found with id: " + id));
    }

    /**
     * 文字入力処理
     */
    public TypeResult typeCharacter(Long id, char inputChar) {
        return learningShakyoRepository.findById(id)
                .map(shakyo -> {
                    boolean isCorrect = shakyo.typeCharacter(inputChar);
                    shakyo.calculateTypingSpeed();
                    LearningShakyo saved = learningShakyoRepository.save(shakyo);

                    return new TypeResult(
                            isCorrect,
                            shakyo.getCurrentCharacter(),
                            shakyo.getCurrentPosition(),
                            shakyo.getAccuracyRate(),
                            shakyo.getTypingSpeedCpm(),
                            shakyo.isCompleted(),
                            convertToDto(saved));
                })
                .orElseThrow(() -> new RuntimeException("LearningShakyo not found with id: " + id));
    }

    /**
     * ヒント取得
     */
    public String getHint(Long id) {
        return learningShakyoRepository.findById(id)
                .map(shakyo -> {
                    String hint = shakyo.useHint();
                    learningShakyoRepository.save(shakyo);
                    return hint;
                })
                .orElse(null);
    }

    /**
     * 写経を完了
     */
    public LearningShakyoDto completeShakyo(Long id) {
        return learningShakyoRepository.findById(id)
                .map(shakyo -> {
                    shakyo.completeShakyo();
                    LearningShakyo saved = learningShakyoRepository.save(shakyo);
                    return convertToDto(saved);
                })
                .orElseThrow(() -> new RuntimeException("LearningShakyo not found with id: " + id));
    }

    // 検索・一覧取得メソッド

    /**
     * ユーザーの写経一覧を取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getShakyosByUserId(UUID userId) {
        return learningShakyoRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ユーザーとスペースの写経一覧を取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getShakyosByUserIdAndSpaceId(UUID userId, Long spaceId) {
        return learningShakyoRepository.findByUserIdAndSpaceIdOrderByCreatedAtDesc(userId, spaceId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 写経タイプ別一覧を取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getShakyosByUserIdAndType(UUID userId, LearningShakyo.ShakyoType shakyoType) {
        return learningShakyoRepository.findByUserIdAndShakyoTypeOrderByCreatedAtDesc(userId, shakyoType)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ステータス別一覧を取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getShakyosByUserIdAndStatus(UUID userId, LearningShakyo.ShakyoStatus shakyoStatus) {
        return learningShakyoRepository.findByUserIdAndShakyoStatusOrderByUpdatedAtDesc(userId, shakyoStatus)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 進行中の写経一覧を取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getInProgressShakyo(UUID userId) {
        List<LearningShakyo.ShakyoStatus> inProgressStatuses = Arrays.asList(
                LearningShakyo.ShakyoStatus.IN_PROGRESS,
                LearningShakyo.ShakyoStatus.PAUSED);
        return learningShakyoRepository.findByUserIdAndShakyoStatusInOrderByLastTypedAtDesc(userId, inProgressStatuses)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 複合条件検索
     */
    @Transactional(readOnly = true)
    public Page<LearningShakyoDto> searchShakyo(UUID userId, LearningShakyo.ShakyoType shakyoType,
            LearningShakyo.ShakyoStatus shakyoStatus,
            LearningShakyo.DifficultyLevel difficultyLevel,
            String programmingLanguage, Pageable pageable) {
        return learningShakyoRepository.findByUserIdWithFilters(userId, shakyoType, shakyoStatus,
                difficultyLevel, programmingLanguage, pageable)
                .map(this::convertToDto);
    }

    /**
     * タイトル検索
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> searchShakyoByTitle(UUID userId, String title) {
        return learningShakyoRepository.findByUserIdAndTitleContaining(userId, title)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 統計・分析メソッド

    /**
     * ユーザー統計を取得
     */
    @Transactional(readOnly = true)
    public UserShakyoStatistics getUserStatistics(UUID userId) {
        long totalCount = learningShakyoRepository.countByUserId(userId);
        long completedCount = learningShakyoRepository.countByUserIdAndShakyoStatus(userId,
                LearningShakyo.ShakyoStatus.COMPLETED);

        BigDecimal averageAccuracy = learningShakyoRepository.findAverageAccuracyRateByUserId(userId)
                .orElse(BigDecimal.ZERO);
        BigDecimal averageSpeed = learningShakyoRepository.findAverageTypingSpeedByUserId(userId)
                .orElse(BigDecimal.ZERO);
        Long totalTime = learningShakyoRepository.findTotalTypingTimeByUserId(userId).orElse(0L);
        BigDecimal maxScore = learningShakyoRepository.findMaxScoreByUserId(userId).orElse(BigDecimal.ZERO);
        BigDecimal averageScore = learningShakyoRepository.findAverageScoreByUserId(userId).orElse(BigDecimal.ZERO);

        return new UserShakyoStatistics(userId, totalCount, completedCount, averageAccuracy,
                averageSpeed, totalTime, maxScore, averageScore);
    }

    /**
     * 写経タイプ別統計を取得
     */
    @Transactional(readOnly = true)
    public Map<LearningShakyo.ShakyoType, Long> getTypeStatistics(UUID userId) {
        List<Object[]> results = learningShakyoRepository.countCompletedByUserIdAndShakyoType(userId);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (LearningShakyo.ShakyoType) result[0],
                        result -> (Long) result[1]));
    }

    /**
     * 難易度別統計を取得
     */
    @Transactional(readOnly = true)
    public Map<LearningShakyo.DifficultyLevel, Long> getDifficultyStatistics(UUID userId) {
        List<Object[]> results = learningShakyoRepository.countCompletedByUserIdAndDifficultyLevel(userId);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (LearningShakyo.DifficultyLevel) result[0],
                        result -> (Long) result[1]));
    }

    /**
     * 日別完了統計を取得
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> getDailyCompletionStatistics(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> results = learningShakyoRepository.findDailyCompletionsByUserId(userId, startDateTime,
                endDateTime);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((java.sql.Date) result[0]).toLocalDate(),
                        result -> (Long) result[1]));
    }

    /**
     * 月別進捗統計を取得
     */
    @Transactional(readOnly = true)
    public List<MonthlyProgress> getMonthlyProgress(UUID userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> results = learningShakyoRepository.findMonthlyProgressByUserId(userId, startDateTime,
                endDateTime);
        return results.stream()
                .map(result -> new MonthlyProgress(
                        (Integer) result[0], // year
                        (Integer) result[1], // month
                        (Long) result[2], // count
                        (BigDecimal) result[3], // avg accuracy
                        (BigDecimal) result[4], // avg speed
                        (BigDecimal) result[5] // avg score
                ))
                .collect(Collectors.toList());
    }

    // ランキングメソッド

    /**
     * 写経タイプ別ランキングを取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getTypeRanking(LearningShakyo.ShakyoType shakyoType, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return learningShakyoRepository.findTopScoresByShakyoType(shakyoType, pageable)
                .getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 難易度別ランキングを取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getDifficultyRanking(LearningShakyo.DifficultyLevel difficultyLevel, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return learningShakyoRepository.findTopScoresByDifficultyLevel(difficultyLevel, pageable)
                .getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 正確率ランキングを取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getAccuracyRanking(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return learningShakyoRepository.findTopAccuracyRates(pageable)
                .getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 入力速度ランキングを取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getSpeedRanking(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return learningShakyoRepository.findTopTypingSpeeds(pageable)
                .getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 管理・メンテナンスメソッド

    /**
     * 非アクティブな写経を検出
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> findInactiveShakyo(UUID userId, int inactiveDays) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(inactiveDays);
        return learningShakyoRepository.findInactiveShakyo(userId, beforeDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 最近の活動を取得
     */
    @Transactional(readOnly = true)
    public List<LearningShakyoDto> getRecentActivities(UUID userId, int recentDays) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(recentDays);
        return learningShakyoRepository.findRecentActivitiesByUserId(userId, sinceDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 変換メソッド

    private LearningShakyoDto convertToDto(LearningShakyo entity) {
        LearningShakyoDto dto = new LearningShakyoDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setSpaceId(entity.getSpaceId());
        dto.setTitle(entity.getTitle());
        dto.setShakyoType(entity.getShakyoType());
        dto.setShakyoStatus(entity.getShakyoStatus());
        dto.setProgrammingLanguage(entity.getProgrammingLanguage());
        dto.setDifficultyLevel(entity.getDifficultyLevel());
        dto.setOriginalText(entity.getOriginalText());
        dto.setCurrentText(entity.getCurrentText());
        dto.setTotalCharacters(entity.getTotalCharacters());
        dto.setTypedCharacters(entity.getTypedCharacters());
        dto.setCorrectCharacters(entity.getCorrectCharacters());
        dto.setAccuracyRate(entity.getAccuracyRate());
        dto.setTypingSpeedCpm(entity.getTypingSpeedCpm());
        dto.setTotalTypingTimeSeconds(entity.getTotalTypingTimeSeconds());
        dto.setErrorCount(entity.getErrorCount());
        dto.setCurrentLine(entity.getCurrentLine());
        dto.setCurrentPosition(entity.getCurrentPosition());
        dto.setHintUsedCount(entity.getHintUsedCount());
        dto.setPauseCount(entity.getPauseCount());
        dto.setScore(entity.getScore());
        dto.setStartedAt(entity.getStartedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setLastTypedAt(entity.getLastTypedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private LearningShakyo convertToEntity(LearningShakyoDto dto) {
        LearningShakyo entity = new LearningShakyo();
        entity.setUserId(dto.getUserId());
        entity.setSpaceId(dto.getSpaceId());
        entity.setTitle(dto.getTitle());
        entity.setShakyoType(dto.getShakyoType());
        entity.setDifficultyLevel(dto.getDifficultyLevel());
        entity.setOriginalText(dto.getOriginalText());
        entity.setProgrammingLanguage(dto.getProgrammingLanguage());
        return entity;
    }

    private void updateEntityFromDto(LearningShakyo entity, LearningShakyoDto dto) {
        if (dto.getTitle() != null)
            entity.setTitle(dto.getTitle());
        if (dto.getShakyoType() != null)
            entity.setShakyoType(dto.getShakyoType());
        if (dto.getDifficultyLevel() != null)
            entity.setDifficultyLevel(dto.getDifficultyLevel());
        if (dto.getProgrammingLanguage() != null)
            entity.setProgrammingLanguage(dto.getProgrammingLanguage());
        if (dto.getOriginalText() != null)
            entity.setOriginalText(dto.getOriginalText());
    }

    // 内部クラス

    /**
     * 文字入力結果
     */
    public static class TypeResult {
        private final boolean correct;
        private final char expectedChar;
        private final int currentPosition;
        private final BigDecimal accuracyRate;
        private final int typingSpeed;
        private final boolean completed;
        private final LearningShakyoDto shakyoDto;

        public TypeResult(boolean correct, char expectedChar, int currentPosition,
                BigDecimal accuracyRate, int typingSpeed, boolean completed,
                LearningShakyoDto shakyoDto) {
            this.correct = correct;
            this.expectedChar = expectedChar;
            this.currentPosition = currentPosition;
            this.accuracyRate = accuracyRate;
            this.typingSpeed = typingSpeed;
            this.completed = completed;
            this.shakyoDto = shakyoDto;
        }

        // Getters
        public boolean isCorrect() {
            return correct;
        }

        public char getExpectedChar() {
            return expectedChar;
        }

        public int getCurrentPosition() {
            return currentPosition;
        }

        public BigDecimal getAccuracyRate() {
            return accuracyRate;
        }

        public int getTypingSpeed() {
            return typingSpeed;
        }

        public boolean isCompleted() {
            return completed;
        }

        public LearningShakyoDto getShakyoDto() {
            return shakyoDto;
        }
    }

    /**
     * ユーザー写経統計
     */
    public static class UserShakyoStatistics {
        private final UUID userId;
        private final long totalCount;
        private final long completedCount;
        private final BigDecimal averageAccuracy;
        private final BigDecimal averageSpeed;
        private final long totalTime;
        private final BigDecimal maxScore;
        private final BigDecimal averageScore;

        public UserShakyoStatistics(UUID userId, long totalCount, long completedCount,
                BigDecimal averageAccuracy, BigDecimal averageSpeed,
                long totalTime, BigDecimal maxScore, BigDecimal averageScore) {
            this.userId = userId;
            this.totalCount = totalCount;
            this.completedCount = completedCount;
            this.averageAccuracy = averageAccuracy;
            this.averageSpeed = averageSpeed;
            this.totalTime = totalTime;
            this.maxScore = maxScore;
            this.averageScore = averageScore;
        }

        // Getters
        public UUID getUserId() {
            return userId;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getCompletedCount() {
            return completedCount;
        }

        public BigDecimal getAverageAccuracy() {
            return averageAccuracy;
        }

        public BigDecimal getAverageSpeed() {
            return averageSpeed;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public BigDecimal getMaxScore() {
            return maxScore;
        }

        public BigDecimal getAverageScore() {
            return averageScore;
        }

        public BigDecimal getCompletionRate() {
            if (totalCount == 0)
                return BigDecimal.ZERO;
            return BigDecimal.valueOf(completedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * 月別進捗統計
     */
    public static class MonthlyProgress {
        private final int year;
        private final int month;
        private final long completedCount;
        private final BigDecimal averageAccuracy;
        private final BigDecimal averageSpeed;
        private final BigDecimal averageScore;

        public MonthlyProgress(int year, int month, long completedCount,
                BigDecimal averageAccuracy, BigDecimal averageSpeed,
                BigDecimal averageScore) {
            this.year = year;
            this.month = month;
            this.completedCount = completedCount;
            this.averageAccuracy = averageAccuracy;
            this.averageSpeed = averageSpeed;
            this.averageScore = averageScore;
        }

        // Getters
        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public long getCompletedCount() {
            return completedCount;
        }

        public BigDecimal getAverageAccuracy() {
            return averageAccuracy;
        }

        public BigDecimal getAverageSpeed() {
            return averageSpeed;
        }

        public BigDecimal getAverageScore() {
            return averageScore;
        }
    }
}
