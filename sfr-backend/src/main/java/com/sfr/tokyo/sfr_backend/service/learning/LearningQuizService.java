package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningQuizDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningQuiz;
import com.sfr.tokyo.sfr_backend.mapper.learning.LearningQuizMapper;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningQuizRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LearningQuizService {

    private final LearningQuizRepository quizRepository;
    private final LearningSpaceRepository spaceRepository;
    private final LearningQuizMapper quizMapper;

    public LearningQuizService(LearningQuizRepository quizRepository,
            LearningSpaceRepository spaceRepository,
            LearningQuizMapper quizMapper) {
        this.quizRepository = quizRepository;
        this.spaceRepository = spaceRepository;
        this.quizMapper = quizMapper;
    }

    /**
     * クイズ作成
     */
    public LearningQuizDto createQuiz(LearningQuizDto quizDto) {
        // 学習空間存在確認
        if (!spaceRepository.existsById(quizDto.getSpaceId())) {
            throw new EntityNotFoundException("学習空間が見つかりません: " + quizDto.getSpaceId());
        }

        // 同名クイズ存在確認
        if (quizRepository.existsBySpaceIdAndTitle(quizDto.getSpaceId(), quizDto.getTitle())) {
            throw new IllegalArgumentException("同じタイトルのクイズが既に存在します: " + quizDto.getTitle());
        }

        // 問題の正解バリデーション
        validateQuizQuestions(quizDto);

        LearningQuiz quiz = quizMapper.toEntity(quizDto);
        LearningQuiz savedQuiz = quizRepository.save(quiz);

        return quizMapper.toDto(savedQuiz);
    }

    /**
     * クイズ取得
     */
    @Transactional(readOnly = true)
    public LearningQuizDto getQuiz(Long quizId) {
        LearningQuiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("クイズが見つかりません: " + quizId));

        return quizMapper.toDto(quiz);
    }

    /**
     * 学習空間内のクイズ一覧取得
     */
    @Transactional(readOnly = true)
    public List<LearningQuizDto> getQuizzesBySpace(Long spaceId) {
        // 学習空間存在確認
        if (!spaceRepository.existsById(spaceId)) {
            throw new EntityNotFoundException("学習空間が見つかりません: " + spaceId);
        }

        List<LearningQuiz> quizzes = quizRepository.findBySpaceIdOrderByCreatedAtDesc(spaceId);
        return quizMapper.toDtoList(quizzes);
    }

    /**
     * クイズタイトル検索
     */
    @Transactional(readOnly = true)
    public List<LearningQuizDto> searchQuizzesByTitle(Long spaceId, String title) {
        // 学習空間存在確認
        if (!spaceRepository.existsById(spaceId)) {
            throw new EntityNotFoundException("学習空間が見つかりません: " + spaceId);
        }

        List<LearningQuiz> quizzes = quizRepository.findBySpaceIdAndTitleContainingIgnoreCase(spaceId, title);
        return quizMapper.toDtoList(quizzes);
    }

    /**
     * 最近のクイズ取得
     */
    @Transactional(readOnly = true)
    public List<LearningQuizDto> getRecentQuizzes(Long spaceId, int limit) {
        // 学習空間存在確認
        if (!spaceRepository.existsById(spaceId)) {
            throw new EntityNotFoundException("学習空間が見つかりません: " + spaceId);
        }

        Pageable pageable = PageRequest.of(0, limit);
        List<LearningQuiz> quizzes = quizRepository.findRecentQuizzesBySpaceId(spaceId, pageable);
        return quizMapper.toDtoList(quizzes);
    }

    /**
     * 期間指定クイズ取得
     */
    @Transactional(readOnly = true)
    public List<LearningQuizDto> getQuizzesByDateRange(Long spaceId, LocalDateTime startDate, LocalDateTime endDate) {
        // 学習空間存在確認
        if (!spaceRepository.existsById(spaceId)) {
            throw new EntityNotFoundException("学習空間が見つかりません: " + spaceId);
        }

        List<LearningQuiz> quizzes = quizRepository.findBySpaceIdAndCreatedAtBetween(spaceId, startDate, endDate);
        return quizMapper.toDtoList(quizzes);
    }

    /**
     * クイズ統計取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getQuizStatistics(Long spaceId) {
        // 学習空間存在確認
        if (!spaceRepository.existsById(spaceId)) {
            throw new EntityNotFoundException("学習空間が見つかりません: " + spaceId);
        }

        Long totalQuizzes = quizRepository.countBySpaceId(spaceId);
        Object[] stats = quizRepository.getQuizStatistics(spaceId);

        return Map.of(
                "totalQuizzes", totalQuizzes,
                "avgQuestions", stats.length > 1 && stats[1] != null ? stats[1] : 0,
                "minQuestions", stats.length > 2 && stats[2] != null ? stats[2] : 0,
                "maxQuestions", stats.length > 3 && stats[3] != null ? stats[3] : 0);
    }

    /**
     * クイズ更新
     */
    public LearningQuizDto updateQuiz(Long quizId, LearningQuizDto quizDto) {
        LearningQuiz existingQuiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("クイズが見つかりません: " + quizId));

        // 学習空間IDは変更不可
        if (!existingQuiz.getSpaceId().equals(quizDto.getSpaceId())) {
            throw new IllegalArgumentException("学習空間IDは変更できません");
        }

        // タイトル重複チェック（自分以外で同名がないか）
        if (!existingQuiz.getTitle().equals(quizDto.getTitle()) &&
                quizRepository.existsBySpaceIdAndTitle(quizDto.getSpaceId(), quizDto.getTitle())) {
            throw new IllegalArgumentException("同じタイトルのクイズが既に存在します: " + quizDto.getTitle());
        }

        // 問題の正解バリデーション
        validateQuizQuestions(quizDto);

        // 更新
        existingQuiz.setTitle(quizDto.getTitle());
        existingQuiz.setQuestions(quizMapper.questionsToEntity(quizDto.getQuestions()));

        LearningQuiz updatedQuiz = quizRepository.save(existingQuiz);
        return quizMapper.toDto(updatedQuiz);
    }

    /**
     * クイズ削除
     */
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new EntityNotFoundException("クイズが見つかりません: " + quizId);
        }

        quizRepository.deleteById(quizId);
    }

    /**
     * 学習空間とIDでクイズ取得（権限チェック用）
     */
    @Transactional(readOnly = true)
    public LearningQuizDto getQuizBySpaceAndId(Long spaceId, Long quizId) {
        LearningQuiz quiz = quizRepository.findByIdAndSpaceId(quizId, spaceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "指定された学習空間内にクイズが見つかりません: spaceId=" + spaceId + ", quizId=" + quizId));

        return quizMapper.toDto(quiz);
    }

    /**
     * クイズ問題のバリデーション
     */
    private void validateQuizQuestions(LearningQuizDto quizDto) {
        if (quizDto.getQuestions() == null || quizDto.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("クイズには最低1つの問題が必要です");
        }

        for (int i = 0; i < quizDto.getQuestions().size(); i++) {
            LearningQuizDto.QuizQuestionDto question = quizDto.getQuestions().get(i);

            if (!question.isAnswerValid()) {
                throw new IllegalArgumentException(
                        String.format("問題%d: 正解が選択肢に含まれていません", i + 1));
            }

            // 重複する選択肢をチェック
            if (question.getOptions().size() != question.getOptions().stream().distinct().count()) {
                throw new IllegalArgumentException(
                        String.format("問題%d: 選択肢に重複があります", i + 1));
            }
        }
    }
}
