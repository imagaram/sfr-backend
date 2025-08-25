package com.sfr.tokyo.sfr_backend.entity.learning;

import com.sfr.tokyo.sfr_backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "content_id" }))
public class LearningProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private LearningContent learningContent;

    @NotNull
    @DecimalMin(value = "0.0", message = "進捗は0%以上である必要があります")
    @DecimalMax(value = "100.0", message = "進捗は100%以下である必要があります")
    @Column(name = "progress_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressPercent;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public LearningProgress() {
    }

    // コンストラクタ
    public LearningProgress(User user, LearningContent learningContent, BigDecimal progressPercent) {
        this.user = user;
        this.learningContent = learningContent;
        this.progressPercent = progressPercent;
        // 100%完了時は完了日時を設定
        if (progressPercent.compareTo(BigDecimal.valueOf(100)) == 0) {
            this.completedAt = LocalDateTime.now();
        }
    }

    // 進捗を更新するメソッド
    public void updateProgress(BigDecimal newProgressPercent) {
        this.progressPercent = newProgressPercent;

        // 100%完了時は完了日時を設定
        if (newProgressPercent.compareTo(BigDecimal.valueOf(100)) == 0 && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
        // 100%未満に戻った場合は完了日時をクリア
        else if (newProgressPercent.compareTo(BigDecimal.valueOf(100)) < 0) {
            this.completedAt = null;
        }
    }

    // 完了判定メソッド
    public boolean isCompleted() {
        return progressPercent.compareTo(BigDecimal.valueOf(100)) == 0;
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LearningContent getLearningContent() {
        return learningContent;
    }

    public void setLearningContent(LearningContent learningContent) {
        this.learningContent = learningContent;
    }

    public BigDecimal getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(BigDecimal progressPercent) {
        this.progressPercent = progressPercent;
        // 100%完了時の処理
        if (progressPercent.compareTo(BigDecimal.valueOf(100)) == 0 && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
