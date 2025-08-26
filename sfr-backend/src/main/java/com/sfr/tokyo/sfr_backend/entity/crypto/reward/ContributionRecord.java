package com.sfr.tokyo.sfr_backend.entity.crypto.reward;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 貢献記録エンティティ
 * ユーザーの各種貢献活動を記録するメインエンティティ
 */
@Entity
@Table(name = "contribution_records", indexes = {
        @Index(name = "idx_contribution_user_type", columnList = "user_id, contribution_type"),
        @Index(name = "idx_contribution_activity_date", columnList = "activity_date"),
        @Index(name = "idx_contribution_reference", columnList = "reference_type, reference_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contribution_type", nullable = false, length = 20)
    @NotNull(message = "貢献タイプは必須です")
    private ContributionType contributionType;

    @Column(name = "activity_type", nullable = false, length = 50)
    @NotBlank(message = "活動タイプは必須です")
    @Size(max = 50, message = "活動タイプは50文字以内で入力してください")
    private String activityType;

    @Column(name = "reference_id", length = 100)
    @Size(max = 100, message = "参照IDは100文字以内で入力してください")
    private String referenceId;

    @Column(name = "reference_type", length = 50)
    @Size(max = 50, message = "参照タイプは50文字以内で入力してください")
    private String referenceType;

    @Column(name = "metrics", columnDefinition = "JSON")
    private String metrics; // JSON形式でメトリクスを格納

    @Column(name = "contribution_score", nullable = false, precision = 10, scale = 4)
    @NotNull(message = "貢献度スコアは必須です")
    @DecimalMin(value = "0.0001", message = "貢献度スコアは0.0001以上である必要があります")
    @DecimalMax(value = "9999.9999", message = "貢献度スコアは9999.9999以下である必要があります")
    private BigDecimal contributionScore;

    @Column(name = "activity_date", nullable = false)
    @NotNull(message = "活動日時は必須です")
    private LocalDateTime activityDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 貢献タイプの列挙型
     */
    public enum ContributionType {
        DEVELOPMENT("development", "開発貢献"),
        LIQUIDITY("liquidity", "流動性提供"),
        GOVERNANCE("governance", "ガバナンス"),
        EDUCATION("education", "教育・普及"),
        COMMERCE("commerce", "商用利用"),
        UX("ux", "UX改善");

        private final String code;
        private final String displayName;

        ContributionType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ContributionType fromCode(String code) {
            for (ContributionType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("不正な貢献タイプコード: " + code);
        }
    }
}
