package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "learning_mode_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningModeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", insertable = false, updatable = false)
    private LearningSpace space;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ui_config", columnDefinition = "JSON")
    private Map<String, Object> uiConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_flags", columnDefinition = "JSON")
    private Map<String, Boolean> featureFlags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
