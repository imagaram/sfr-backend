package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "learning_quiz")
public class LearningQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @NotBlank(message = "クイズタイトルは必須です")
    @Size(min = 1, max = 200, message = "クイズタイトルは1文字以上200文字以下である必要があります")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull(message = "クイズ問題は必須です")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", nullable = false, columnDefinition = "JSON")
    private List<QuizQuestion> questions;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public LearningQuiz() {
    }

    // コンストラクタ
    public LearningQuiz(Long spaceId, String title, List<QuizQuestion> questions) {
        this.spaceId = spaceId;
        this.title = title;
        this.questions = questions;
    }

    // クイズ問題の内部クラス
    public static class QuizQuestion {
        private String question;
        private List<String> options;
        private String answer;

        // デフォルトコンストラクタ
        public QuizQuestion() {
        }

        // コンストラクタ
        public QuizQuestion(String question, List<String> options, String answer) {
            this.question = question;
            this.options = options;
            this.answer = answer;
        }

        // バリデーション用メソッド
        public boolean isValid() {
            return question != null && !question.trim().isEmpty() &&
                    options != null && options.size() >= 2 &&
                    answer != null && !answer.trim().isEmpty() &&
                    options.contains(answer);
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

    // クイズの問題数を取得
    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }

    // クイズが有効かチェック
    public boolean isValid() {
        if (questions == null || questions.isEmpty()) {
            return false;
        }
        return questions.stream().allMatch(QuizQuestion::isValid);
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

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
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
