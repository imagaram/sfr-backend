package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

/**
 * Manifestoセクションエンティティ
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
 */
@Entity
@Table(name = "manifesto_sections",
       indexes = {
           @Index(name = "idx_section_document_id", columnList = "manifesto_document_id"),
           @Index(name = "idx_section_identifier", columnList = "section_id"),
           @Index(name = "idx_section_category", columnList = "category"),
           @Index(name = "idx_section_order", columnList = "display_order")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"manifestoDocument", "contents"})
public class ManifestoSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 所属Manifestoドキュメント
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifesto_document_id", nullable = false)
    private ManifestoDocument manifestoDocument;

    /**
     * セクション識別子（principles, rewards, governance等）
     */
    @Column(name = "section_id", nullable = false, length = 50)
    @NotBlank(message = "セクションIDは必須です")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "小文字英数字とアンダースコアのみ使用可能です")
    private String sectionId;

    /**
     * 表示順序
     */
    @Column(name = "display_order", nullable = false)
    @Min(value = 1, message = "表示順序は1以上である必要があります")
    private Integer displayOrder;

    /**
     * セクションカテゴリ
     */
    @Column(name = "category", length = 50)
    @Pattern(regexp = "^(foundation|governance|economics|participation|technical)$", 
             message = "有効なカテゴリを入力してください")
    private String category;

    /**
     * セクション分類タグ（JSON配列形式）
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

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
     * セクション最終修正日時（コンテンツ変更時に手動更新）
     */
    @Column(name = "last_modified")
    private Instant lastModified;

    /**
     * 関連多言語コンテンツ一覧
     */
    @OneToMany(mappedBy = "manifestoSection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ManifestoContent> contents;

    /**
     * セクション識別用の複合キー生成
     */
    public String getCompositeKey() {
        return manifestoDocument.getVersion() + ":" + sectionId;
    }
}
