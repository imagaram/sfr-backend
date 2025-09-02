package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

/**
 * SFR.TOKYO Manifesto i18n対応エンティティ
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
 * 
 * 注意: 外部依存や翻訳ライブラリとの統合はまだ不要
 */
@Entity
@Table(name = "manifesto_documents", 
       indexes = {
           @Index(name = "idx_manifesto_version", columnList = "version"),
           @Index(name = "idx_manifesto_phase", columnList = "phase"),
           @Index(name = "idx_manifesto_updated", columnList = "updatedAt")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"sections"})
public class ManifestoDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * スキーマバージョン（セマンティックバージョニング）
     */
    @Column(name = "version", nullable = false, length = 20)
    @NotBlank(message = "バージョンは必須です")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "セマンティックバージョニング形式で入力してください")
    private String version;

    /**
     * 作成者・編集者一覧（JSON形式で保存）
     */
    @Column(name = "authors", columnDefinition = "TEXT")
    private String authors;

    /**
     * 対応言語コード一覧（JSON形式で保存）
     * 例: ["ja", "en"]
     */
    @Column(name = "supported_languages", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "対応言語は必須です")
    private String supportedLanguages;

    /**
     * 公開フェーズ
     */
    @Column(name = "phase", nullable = false, length = 20)
    @NotBlank(message = "公開フェーズは必須です")
    @Pattern(regexp = "^(development|testing|production)$", message = "有効なフェーズを入力してください")
    private String phase;

    /**
     * アクティブフラグ（現在有効なバージョンかどうか）
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 文書説明・備考
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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
     * 関連セクション一覧
     */
    @OneToMany(mappedBy = "manifestoDocument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<ManifestoSection> sections;

    /**
     * バージョン比較用メソッド
     */
    public int compareVersion(String otherVersion) {
        if (otherVersion == null) return 1;
        
        String[] thisParts = this.version.split("\\.");
        String[] otherParts = otherVersion.split("\\.");
        
        for (int i = 0; i < Math.max(thisParts.length, otherParts.length); i++) {
            int thisNum = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
            int otherNum = i < otherParts.length ? Integer.parseInt(otherParts[i]) : 0;
            
            if (thisNum != otherNum) {
                return Integer.compare(thisNum, otherNum);
            }
        }
        return 0;
    }

    /**
     * 最新バージョンかどうかの判定
     */
    public boolean isLatestVersion() {
        return this.isActive != null && this.isActive;
    }
}
