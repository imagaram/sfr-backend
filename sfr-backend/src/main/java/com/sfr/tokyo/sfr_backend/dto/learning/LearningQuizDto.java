package com.sfr.tokyo.sfr_backend.dto.learning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class LearningQuizDto {

    private Long id;

    @NotNull(message = "学習空間IDは必須です")
    private Long spaceId;

    @NotBlank(message = "クイズタイトルは必須です")
    @Size(min = 1, max = 200, message = "クイズタイトルは1文字以上200文字以下である必要があります")
    private String title;

    @NotEmpty(message = "クイズ問題は1つ以上必要です")
    @Valid
    private List<QuizQuestionDto> questions;

    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public LearningQuizDto() {
    }

    // コンストラクタ（作成用）
    public LearningQuizDto(Long spaceId, String title, List<QuizQuestionDto> questions) {
        this.spaceId = spaceId;
        this.title = title;
        this.questions = questions;
        this.questionCount = questions != null ? questions.size() : 0;
    }

    // コンストラクタ（レスポンス用）
    public LearningQuizDto(Long id, Long spaceId, String title, List<QuizQuestionDto> questions,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.spaceId = spaceId;
        this.title = title;
        this.questions = questions;
        this.questionCount = questions != null ? questions.size() : 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // クイズ問題のDTO
    public static class QuizQuestionDto {

        @NotBlank(message = "問題文は必須です")
        @Size(min = 1, max = 1000, message = "問題文は1文字以上1000文字以下である必要があります")
        private String question;

        @NotEmpty(message = "選択肢は2つ以上必要です")
        @Size(min = 2, max = 10, message = "選択肢は2つ以上10個以下である必要があります")
        private List<@NotBlank(message = "選択肢は空白不可") String> options;

        @NotBlank(message = "正解は必須です")
        private String answer;

        // デフォルトコンストラクタ
        public QuizQuestionDto() {
        }

        // コンストラクタ
        public QuizQuestionDto(String question, List<String> options, String answer) {
            this.question = question;
            this.options = options;
            this.answer = answer;
        }

        // カスタムバリデーション
        public boolean isAnswerValid() {
            return options != null && answer != null && options.contains(answer);
        }

        // Getter and Setter
        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }

    // クイズの問題数を計算
    public void calculateQuestionCount() {
        this.questionCount = questions != null ? questions.size() : 0;
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuizQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestionDto> questions) {
        this.questions = questions;
        calculateQuestionCount();
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
