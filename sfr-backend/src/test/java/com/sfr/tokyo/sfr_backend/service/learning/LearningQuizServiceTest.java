package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningQuizDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningQuiz;
import com.sfr.tokyo.sfr_backend.mapper.learning.LearningQuizMapper;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningQuizRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningQuizServiceTest {

    @Mock
    private LearningQuizRepository quizRepository;

    @Mock
    private LearningSpaceRepository spaceRepository;

    @Mock
    private LearningQuizMapper quizMapper;

    @InjectMocks
    private LearningQuizService quizService;

    private LearningQuizDto sampleQuizDto;
    private LearningQuiz sampleQuiz;

    @BeforeEach
    void setUp() {
        // サンプルクイズ問題の作成
        LearningQuizDto.QuizQuestionDto question1 = new LearningQuizDto.QuizQuestionDto();
        question1.setQuestion("Javaの基本データ型はどれですか？");
        question1.setOptions(Arrays.asList("int", "String", "List", "Map"));
        question1.setAnswer("int");

        LearningQuizDto.QuizQuestionDto question2 = new LearningQuizDto.QuizQuestionDto();
        question2.setQuestion("Spring Bootの特徴は？");
        question2.setOptions(Arrays.asList("設定が複雑", "軽量フレームワーク", "重いフレームワーク", "設定ファイルが多い"));
        question2.setAnswer("軽量フレームワーク");

        sampleQuizDto = new LearningQuizDto();
        sampleQuizDto.setId(1L);
        sampleQuizDto.setSpaceId(100L);
        sampleQuizDto.setTitle("Java基礎クイズ");
        sampleQuizDto.setQuestions(Arrays.asList(question1, question2));

        // エンティティのサンプル
        sampleQuiz = new LearningQuiz();
        sampleQuiz.setId(1L);
        sampleQuiz.setSpaceId(100L);
        sampleQuiz.setTitle("Java基礎クイズ");
    }

    @Test
    void createQuiz_Success() {
        // Given
        when(spaceRepository.existsById(100L)).thenReturn(true);
        when(quizRepository.existsBySpaceIdAndTitle(100L, "Java基礎クイズ")).thenReturn(false);
        when(quizMapper.toEntity(sampleQuizDto)).thenReturn(sampleQuiz);
        when(quizRepository.save(sampleQuiz)).thenReturn(sampleQuiz);
        when(quizMapper.toDto(sampleQuiz)).thenReturn(sampleQuizDto);

        // When
        LearningQuizDto result = quizService.createQuiz(sampleQuizDto);

        // Then
        assertNotNull(result);
        assertEquals("Java基礎クイズ", result.getTitle());
        assertEquals(100L, result.getSpaceId());
        verify(spaceRepository).existsById(100L);
        verify(quizRepository).existsBySpaceIdAndTitle(100L, "Java基礎クイズ");
        verify(quizRepository).save(sampleQuiz);
    }

    @Test
    void createQuiz_SpaceNotFound() {
        // Given
        when(spaceRepository.existsById(100L)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> quizService.createQuiz(sampleQuizDto));
        assertEquals("学習空間が見つかりません: 100", exception.getMessage());
        verify(spaceRepository).existsById(100L);
        verify(quizRepository, never()).save(any());
    }

    @Test
    void createQuiz_DuplicateTitle() {
        // Given
        when(spaceRepository.existsById(100L)).thenReturn(true);
        when(quizRepository.existsBySpaceIdAndTitle(100L, "Java基礎クイズ")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.createQuiz(sampleQuizDto));
        assertEquals("同じタイトルのクイズが既に存在します: Java基礎クイズ", exception.getMessage());
        verify(quizRepository, never()).save(any());
    }

    @Test
    void createQuiz_InvalidQuestions_EmptyList() {
        // Given
        sampleQuizDto.setQuestions(List.of()); // 空のリスト
        when(spaceRepository.existsById(100L)).thenReturn(true);
        when(quizRepository.existsBySpaceIdAndTitle(100L, "Java基礎クイズ")).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.createQuiz(sampleQuizDto));
        assertEquals("クイズには最低1つの問題が必要です", exception.getMessage());
    }

    @Test
    void createQuiz_InvalidQuestions_WrongAnswer() {
        // Given
        LearningQuizDto.QuizQuestionDto invalidQuestion = new LearningQuizDto.QuizQuestionDto();
        invalidQuestion.setQuestion("テスト問題");
        invalidQuestion.setOptions(Arrays.asList("選択肢1", "選択肢2", "選択肢3"));
        invalidQuestion.setAnswer("存在しない選択肢"); // 正解が選択肢にない

        sampleQuizDto.setQuestions(List.of(invalidQuestion));
        when(spaceRepository.existsById(100L)).thenReturn(true);
        when(quizRepository.existsBySpaceIdAndTitle(100L, "Java基礎クイズ")).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.createQuiz(sampleQuizDto));
        assertEquals("問題1: 正解が選択肢に含まれていません", exception.getMessage());
    }

    @Test
    void getQuiz_Success() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(sampleQuiz));
        when(quizMapper.toDto(sampleQuiz)).thenReturn(sampleQuizDto);

        // When
        LearningQuizDto result = quizService.getQuiz(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java基礎クイズ", result.getTitle());
        verify(quizRepository).findById(1L);
        verify(quizMapper).toDto(sampleQuiz);
    }

    @Test
    void getQuiz_NotFound() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> quizService.getQuiz(1L));
        assertEquals("クイズが見つかりません: 1", exception.getMessage());
    }

    @Test
    void getQuizzesBySpace_Success() {
        // Given
        List<LearningQuiz> quizzes = Arrays.asList(sampleQuiz);
        when(spaceRepository.existsById(100L)).thenReturn(true);
        when(quizRepository.findBySpaceIdOrderByCreatedAtDesc(100L)).thenReturn(quizzes);
        when(quizMapper.toDtoList(quizzes)).thenReturn(Arrays.asList(sampleQuizDto));

        // When
        List<LearningQuizDto> result = quizService.getQuizzesBySpace(100L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java基礎クイズ", result.get(0).getTitle());
        verify(spaceRepository).existsById(100L);
        verify(quizRepository).findBySpaceIdOrderByCreatedAtDesc(100L);
    }

    @Test
    void searchQuizzesByTitle_Success() {
        // Given
        List<LearningQuiz> quizzes = Arrays.asList(sampleQuiz);
        when(spaceRepository.existsById(100L)).thenReturn(true);
        when(quizRepository.findBySpaceIdAndTitleContainingIgnoreCase(100L, "Java")).thenReturn(quizzes);
        when(quizMapper.toDtoList(quizzes)).thenReturn(Arrays.asList(sampleQuizDto));

        // When
        List<LearningQuizDto> result = quizService.searchQuizzesByTitle(100L, "Java");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(quizRepository).findBySpaceIdAndTitleContainingIgnoreCase(100L, "Java");
    }

    @Test
    void getRecentQuizzes_Success() {
        // Given
        List<LearningQuiz> quizzes = Arrays.asList(sampleQuiz);
        Pageable pageable = PageRequest.of(0, 5);
        when(spaceRepository.existsById(100L)).thenReturn(true);
        when(quizRepository.findRecentQuizzesBySpaceId(100L, pageable)).thenReturn(quizzes);
        when(quizMapper.toDtoList(quizzes)).thenReturn(Arrays.asList(sampleQuizDto));

        // When
        List<LearningQuizDto> result = quizService.getRecentQuizzes(100L, 5);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(quizRepository).findRecentQuizzesBySpaceId(eq(100L), any(Pageable.class));
    }

    @Test
    void getQuizStatistics_Success() {
        // Given
        when(spaceRepository.existsById(100L)).thenReturn(true);
        when(quizRepository.countBySpaceId(100L)).thenReturn(5L);
        when(quizRepository.getQuizStatistics(100L)).thenReturn(new Object[] { 5L, 3.5, 2, 5 });

        // When
        Map<String, Object> result = quizService.getQuizStatistics(100L);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.get("totalQuizzes"));
        assertEquals(3.5, result.get("avgQuestions"));
        assertEquals(2, result.get("minQuestions"));
        assertEquals(5, result.get("maxQuestions"));
    }

    @Test
    void updateQuiz_Success() {
        // Given
        LearningQuizDto updateDto = new LearningQuizDto();
        updateDto.setSpaceId(100L);
        updateDto.setTitle("更新されたクイズ");
        updateDto.setQuestions(sampleQuizDto.getQuestions());

        when(quizRepository.findById(1L)).thenReturn(Optional.of(sampleQuiz));
        when(quizRepository.existsBySpaceIdAndTitle(100L, "更新されたクイズ")).thenReturn(false);
        when(quizMapper.questionsToEntity(updateDto.getQuestions())).thenReturn(Arrays.asList());
        when(quizRepository.save(sampleQuiz)).thenReturn(sampleQuiz);
        when(quizMapper.toDto(sampleQuiz)).thenReturn(updateDto);

        // When
        LearningQuizDto result = quizService.updateQuiz(1L, updateDto);

        // Then
        assertNotNull(result);
        assertEquals("更新されたクイズ", result.getTitle());
        verify(quizRepository).save(sampleQuiz);
    }

    @Test
    void updateQuiz_SpaceIdChanged() {
        // Given
        LearningQuizDto updateDto = new LearningQuizDto();
        updateDto.setSpaceId(200L); // 異なる空間ID
        updateDto.setTitle("更新されたクイズ");

        when(quizRepository.findById(1L)).thenReturn(Optional.of(sampleQuiz));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> quizService.updateQuiz(1L, updateDto));
        assertEquals("学習空間IDは変更できません", exception.getMessage());
    }

    @Test
    void deleteQuiz_Success() {
        // Given
        when(quizRepository.existsById(1L)).thenReturn(true);

        // When
        quizService.deleteQuiz(1L);

        // Then
        verify(quizRepository).deleteById(1L);
    }

    @Test
    void deleteQuiz_NotFound() {
        // Given
        when(quizRepository.existsById(1L)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> quizService.deleteQuiz(1L));
        assertEquals("クイズが見つかりません: 1", exception.getMessage());
        verify(quizRepository, never()).deleteById(any());
    }

    @Test
    void getQuizBySpaceAndId_Success() {
        // Given
        when(quizRepository.findByIdAndSpaceId(1L, 100L)).thenReturn(Optional.of(sampleQuiz));
        when(quizMapper.toDto(sampleQuiz)).thenReturn(sampleQuizDto);

        // When
        LearningQuizDto result = quizService.getQuizBySpaceAndId(100L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getSpaceId());
        verify(quizRepository).findByIdAndSpaceId(1L, 100L);
    }

    @Test
    void getQuizBySpaceAndId_NotFound() {
        // Given
        when(quizRepository.findByIdAndSpaceId(1L, 100L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> quizService.getQuizBySpaceAndId(100L, 1L));
        assertTrue(exception.getMessage().contains("指定された学習空間内にクイズが見つかりません"));
    }
}
