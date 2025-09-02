package com.sfr.tokyo.sfr_backend.council.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * SFR.TOKYO Manifesto i18n対応DTO群
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
 */
public class ManifestoI18nDto {

    /**
     * Manifestoドキュメント全体DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "SFR.TOKYO Manifesto文書（多言語対応）")
    public static class ManifestoDocumentDto {
        
        @Schema(description = "スキーマバージョン", example = "1.0.0", required = true)
        @JsonProperty("version")
        @NotBlank(message = "バージョンは必須です")
        @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "セマンティックバージョニング形式で入力してください")
        private String version;
        
        @Schema(description = "Manifestoメタデータ")
        @JsonProperty("metadata")
        @Valid
        private ManifestoMetadataDto metadata;
        
        @Schema(description = "Manifestoセクション一覧", required = true)
        @JsonProperty("sections")
        @NotNull(message = "セクションは必須です")
        @Size(min = 3, message = "最低3つのセクションが必要です")
        @Valid
        private List<ManifestoSectionDto> sections;
    }

    /**
     * ManifestoメタデータDTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Manifestoメタデータ情報")
    public static class ManifestoMetadataDto {
        
        @Schema(description = "作成日時", required = true)
        @JsonProperty("createdAt")
        @NotNull(message = "作成日時は必須です")
        private Instant createdAt;
        
        @Schema(description = "最終更新日時", required = true)
        @JsonProperty("updatedAt")
        @NotNull(message = "更新日時は必須です")
        private Instant updatedAt;
        
        @Schema(description = "作成者・編集者一覧")
        @JsonProperty("authors")
        private List<String> authors;
        
        @Schema(description = "対応言語コード一覧", example = "[\"ja\", \"en\"]", required = true)
        @JsonProperty("supportedLanguages")
        @NotEmpty(message = "対応言語は最低1つ必要です")
        private List<@Pattern(regexp = "^[a-z]{2}$", message = "ISO 639-1形式で入力してください") String> supportedLanguages;
        
        @Schema(description = "公開フェーズ", example = "testing", 
                allowableValues = {"development", "testing", "production"})
        @JsonProperty("phase")
        @NotBlank(message = "公開フェーズは必須です")
        private String phase;
    }

    /**
     * Manifestoセクション個別DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Manifestoセクション（多言語対応）")
    public static class ManifestoSectionDto {
        
        @Schema(description = "セクション識別子", example = "principles", required = true)
        @JsonProperty("id")
        @NotBlank(message = "セクションIDは必須です")
        @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "小文字英数字とアンダースコアのみ使用可能です")
        private String id;
        
        @Schema(description = "表示順序", example = "1")
        @JsonProperty("order")
        @Min(value = 1, message = "表示順序は1以上である必要があります")
        private Integer order;
        
        @Schema(description = "セクションカテゴリ", example = "foundation",
                allowableValues = {"foundation", "governance", "economics", "participation", "technical"})
        @JsonProperty("category")
        private String category;
        
        @Schema(description = "セクションタイトル（多言語）", required = true)
        @JsonProperty("title")
        @NotNull(message = "タイトルは必須です")
        @Valid
        private LocalizedTextDto title;
        
        @Schema(description = "セクション内容（多言語・構造化）", required = true)
        @JsonProperty("content")
        @NotNull(message = "コンテンツは必須です")
        @Valid
        private LocalizedContentDto content;
        
        @Schema(description = "セクション分類タグ")
        @JsonProperty("tags")
        private List<String> tags;
        
        @Schema(description = "最終更新日時")
        @JsonProperty("lastModified")
        private Instant lastModified;
    }

    /**
     * 多言語対応テキストDTO（短文用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "多言語対応テキスト（タイトル等の短文用）")
    public static class LocalizedTextDto {
        
        @Schema(description = "日本語テキスト", required = true)
        @JsonProperty("ja")
        @NotBlank(message = "日本語テキストは必須です")
        @Size(max = 200, message = "日本語テキストは200文字以内で入力してください")
        private String ja;
        
        @Schema(description = "英語テキスト", required = true)
        @JsonProperty("en")
        @NotBlank(message = "英語テキストは必須です")
        @Size(max = 200, message = "英語テキストは200文字以内で入力してください")
        private String en;
        
        @Schema(description = "その他言語（将来拡張用）")
        @JsonProperty("additionalLanguages")
        private Map<String, String> additionalLanguages;
    }

    /**
     * 多言語対応コンテンツDTO（長文・構造化用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "多言語対応コンテンツ（構造化された長文用）")
    public static class LocalizedContentDto {
        
        @Schema(description = "日本語コンテンツ", required = true)
        @JsonProperty("ja")
        @NotNull(message = "日本語コンテンツは必須です")
        @Valid
        private ContentStructureDto ja;
        
        @Schema(description = "英語コンテンツ", required = true)
        @JsonProperty("en")
        @NotNull(message = "英語コンテンツは必須です")
        @Valid
        private ContentStructureDto en;
        
        @Schema(description = "その他言語コンテンツ（将来拡張用）")
        @JsonProperty("additionalLanguages")
        private Map<String, ContentStructureDto> additionalLanguages;
    }

    /**
     * 構造化コンテンツDTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "構造化コンテンツ（段落・リスト・表等）")
    public static class ContentStructureDto {
        
        @Schema(description = "コンテンツ要約", example = "このセクションでは...", required = true)
        @JsonProperty("summary")
        @NotBlank(message = "要約は必須です")
        @Size(max = 150, message = "要約は150文字以内で入力してください")
        private String summary;
        
        @Schema(description = "詳細コンテンツブロック一覧", required = true)
        @JsonProperty("details")
        @NotEmpty(message = "詳細コンテンツは最低1つ必要です")
        @Valid
        private List<ContentBlockDto> details;
        
        @Schema(description = "参照文書・リンク情報")
        @JsonProperty("references")
        @Valid
        private List<ReferenceDto> references;
    }

    /**
     * コンテンツブロックDTO（段落・リスト・表等）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "コンテンツブロック（段落・リスト・表等の個別要素）")
    public static class ContentBlockDto {
        
        @Schema(description = "コンテンツブロックタイプ", example = "paragraph", required = true,
                allowableValues = {"paragraph", "list", "table", "quote", "formula", "diagram"})
        @JsonProperty("type")
        @NotBlank(message = "ブロックタイプは必須です")
        private String type;
        
        @Schema(description = "ブロック内容（Markdown記法対応）", required = true)
        @JsonProperty("content")
        @NotBlank(message = "ブロック内容は必須です")
        private String content;
        
        @Schema(description = "ブロック追加情報")
        @JsonProperty("metadata")
        @Valid
        private ContentBlockMetadataDto metadata;
    }

    /**
     * コンテンツブロックメタデータDTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "コンテンツブロック追加情報")
    public static class ContentBlockMetadataDto {
        
        @Schema(description = "重要度", example = "high", 
                allowableValues = {"high", "medium", "low"})
        @JsonProperty("importance")
        @Builder.Default
        private String importance = "medium";
        
        @Schema(description = "カテゴリ", example = "principle")
        @JsonProperty("category")
        private String category;
    }

    /**
     * 参照文書DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "参照文書・リンク情報")
    public static class ReferenceDto {
        
        @Schema(description = "参照文書タイトル", required = true)
        @JsonProperty("title")
        @NotBlank(message = "参照文書タイトルは必須です")
        private String title;
        
        @Schema(description = "参照タイプ", example = "law", required = true,
                allowableValues = {"law", "regulation", "academic", "internal", "external"})
        @JsonProperty("type")
        @NotBlank(message = "参照タイプは必須です")
        private String type;
        
        @Schema(description = "参照URL")
        @JsonProperty("url")
        private String url;
        
        @Schema(description = "参照対象セクション・条項")
        @JsonProperty("section")
        private String section;
        
        @Schema(description = "参照文書日付")
        @JsonProperty("date")
        private String date;
    }

    /**
     * Manifesto検索・フィルター用DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Manifesto検索・フィルター条件")
    public static class ManifestoSearchDto {
        
        @Schema(description = "検索言語", example = "ja")
        @JsonProperty("language")
        @Pattern(regexp = "^[a-z]{2}$", message = "ISO 639-1形式で入力してください")
        private String language;
        
        @Schema(description = "セクションカテゴリフィルター")
        @JsonProperty("category")
        private String category;
        
        @Schema(description = "タグフィルター")
        @JsonProperty("tags")
        private List<String> tags;
        
        @Schema(description = "キーワード検索")
        @JsonProperty("keyword")
        private String keyword;
        
        @Schema(description = "重要度フィルター")
        @JsonProperty("importance")
        private String importance;
    }

    /**
     * Manifesto更新履歴DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Manifesto更新履歴")
    public static class ManifestoHistoryDto {
        
        @Schema(description = "履歴ID")
        @JsonProperty("id")
        private Long id;
        
        @Schema(description = "更新バージョン")
        @JsonProperty("version")
        private String version;
        
        @Schema(description = "更新日時")
        @JsonProperty("updatedAt")
        private Instant updatedAt;
        
        @Schema(description = "更新者")
        @JsonProperty("updatedBy")
        private String updatedBy;
        
        @Schema(description = "更新内容説明")
        @JsonProperty("changeDescription")
        private String changeDescription;
        
        @Schema(description = "影響セクション一覧")
        @JsonProperty("affectedSections")
        private List<String> affectedSections;
        
        @Schema(description = "更新タイプ", 
                allowableValues = {"content", "translation", "structure", "metadata"})
        @JsonProperty("changeType")
        private String changeType;
    }
}
