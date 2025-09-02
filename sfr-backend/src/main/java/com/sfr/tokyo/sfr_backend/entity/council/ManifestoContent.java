package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Manifesto多言語コンテンツエンティティ
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
 */
@Entity
@Table(name = "manifesto_contents",
       indexes = {
           @Index(name = "idx_content_section_id", columnList = "manifesto_section_id"),
           @Index(name = "idx_content_language", columnList = "language_code"),
           @Index(name = "idx_content_section_lang", columnList = "manifesto_section_id, language_code")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_section_language", 
                           columnNames = {"manifesto_section_id", "language_code"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"manifestoSection"})
public class ManifestoContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 所属Manifestoセクション
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifesto_section_id", nullable = false)
    private ManifestoSection manifestoSection;

    /**
     * 言語コード（ISO 639-1準拠）
     */
    @Column(name = "language_code", nullable = false, length = 2)
    @NotBlank(message = "言語コードは必須です")
    @Pattern(regexp = "^[a-z]{2}$", message = "ISO 639-1形式で入力してください")
    private String languageCode;

    /**
     * セクションタイトル
     */
    @Column(name = "title", nullable = false, length = 200)
    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    private String title;

    /**
     * コンテンツ要約
     */
    @Column(name = "summary", nullable = false, length = 150)
    @NotBlank(message = "要約は必須です")
    @Size(max = 150, message = "要約は150文字以内で入力してください")
    private String summary;

    /**
     * 詳細コンテンツ（JSON形式の構造化データ）
     * ContentBlockDto[]の配列として保存
     */
    @Column(name = "details", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "詳細コンテンツは必須です")
    private String details;

    /**
     * 参照文書・リンク情報（JSON形式）
     * ReferenceDto[]の配列として保存
     */
    @Column(name = "references", columnDefinition = "TEXT")
    private String references;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 最終更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 翻訳品質スコア（将来の自動翻訳機能用）
     */
    @Column(name = "translation_quality_score")
    @DecimalMin(value = "0.0", message = "翻訳品質スコアは0.0以上である必要があります")
    @DecimalMax(value = "1.0", message = "翻訳品質スコアは1.0以下である必要があります")
    private Double translationQualityScore;

    /**
     * 翻訳ステータス（将来拡張用）
     */
    @Column(name = "translation_status", length = 20)
    @Pattern(regexp = "^(original|translated|reviewed|approved)$", 
             message = "有効な翻訳ステータスを入力してください")
    private String translationStatus;

    /**
     * 言語固有の表示用キー生成
     */
    public String getDisplayKey() {
        return manifestoSection.getSectionId() + "." + languageCode;
    }
}
