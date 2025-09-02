package com.sfr.tokyo.sfr_backend.council.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 多言語対応評議員マニフェストDTO
 * 制度の国際化とUXの包摂性を実現
 */
@Data
@Builder
public class CouncilManifestoI18nDto {
    
    /**
     * マニフェスト基本情報
     */
    private ManifestoBaseInfo baseInfo;
    
    /**
     * 多言語コンテンツ
     */
    private Map<String, LocalizedContent> localizedContent;
    
    /**
     * サポートされている言語一覧
     */
    private List<SupportedLanguage> supportedLanguages;
    
    /**
     * 国際化メタデータ
     */
    private InternationalizationMetadata i18nMetadata;
    
    /**
     * アクセシビリティ情報
     */
    private AccessibilityInfo accessibility;
    
    /**
     * マニフェスト基本情報
     */
    @Data
    @Builder
    public static class ManifestoBaseInfo {
        /**
         * マニフェストID
         */
        private Long manifestoId;
        
        /**
         * 候補者ID
         */
        private Long candidateId;
        
        /**
         * 選挙ID
         */
        private Long electionId;
        
        /**
         * デフォルト言語
         */
        private String defaultLanguage;
        
        /**
         * 作成時刻
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant createdAt;
        
        /**
         * 最終更新時刻
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") 
        private Instant updatedAt;
        
        /**
         * 公開ステータス
         */
        private PublicationStatus status;
        
        /**
         * バージョン
         */
        private Integer version;
        
        public enum PublicationStatus {
            DRAFT("下書き"),
            UNDER_REVIEW("審査中"),
            PUBLISHED("公開"),
            ARCHIVED("アーカイブ"),
            REJECTED("却下");
            
            private final String description;
            
            PublicationStatus(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 言語別ローカライズコンテンツ
     */
    @Data
    @Builder
    public static class LocalizedContent {
        /**
         * 言語コード（ISO 639-1）
         */
        private String languageCode;
        
        /**
         * 地域コード（ISO 3166-1）
         */
        private String regionCode;
        
        /**
         * マニフェストタイトル
         */
        private String title;
        
        /**
         * 候補者名
         */
        private String candidateName;
        
        /**
         * 短い紹介文
         */
        private String summary;
        
        /**
         * 政策項目
         */
        private List<PolicyItem> policies;
        
        /**
         * 経歴情報
         */
        private BiographyInfo biography;
        
        /**
         * 公約・ビジョン
         */
        private VisionStatement vision;
        
        /**
         * 重点課題
         */
        private List<KeyIssue> keyIssues;
        
        /**
         * 支援者からの推薦
         */
        private List<Endorsement> endorsements;
        
        /**
         * 翻訳情報
         */
        private TranslationInfo translationInfo;
    }
    
    /**
     * 政策項目
     */
    @Data
    @Builder
    public static class PolicyItem {
        /**
         * 政策項目ID
         */
        private String policyId;
        
        /**
         * カテゴリ
         */
        private PolicyCategory category;
        
        /**
         * 政策タイトル
         */
        private String title;
        
        /**
         * 政策説明
         */
        private String description;
        
        /**
         * 具体的施策
         */
        private List<String> specificMeasures;
        
        /**
         * 実現予定時期
         */
        private String timeline;
        
        /**
         * 優先度
         */
        private Priority priority;
        
        /**
         * 関連リンク
         */
        private List<ExternalLink> relatedLinks;
        
        public enum PolicyCategory {
            GOVERNANCE("ガバナンス"),
            ECONOMY("経済"),
            TECHNOLOGY("技術"),
            SOCIAL("社会"),
            ENVIRONMENT("環境"),
            INTERNATIONAL("国際");
            
            private final String description;
            
            PolicyCategory(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
        
        public enum Priority {
            HIGH("高"),
            MEDIUM("中"),
            LOW("低");
            
            private final String description;
            
            Priority(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 経歴情報
     */
    @Data
    @Builder
    public static class BiographyInfo {
        /**
         * 個人的背景
         */
        private String personalBackground;
        
        /**
         * 職歴
         */
        private List<CareerItem> careerHistory;
        
        /**
         * 学歴
         */
        private List<EducationItem> education;
        
        /**
         * 資格・認定
         */
        private List<String> qualifications;
        
        /**
         * 受賞歴
         */
        private List<AwardItem> awards;
        
        /**
         * 出版・論文
         */
        private List<PublicationItem> publications;
    }
    
    /**
     * 職歴項目
     */
    @Data
    @Builder
    public static class CareerItem {
        private String organization;
        private String position;
        private String startDate;
        private String endDate;
        private String description;
    }
    
    /**
     * 学歴項目
     */
    @Data
    @Builder
    public static class EducationItem {
        private String institution;
        private String degree;
        private String field;
        private String graduationDate;
        private String achievements;
    }
    
    /**
     * 受賞項目
     */
    @Data
    @Builder
    public static class AwardItem {
        private String awardName;
        private String organization;
        private String date;
        private String description;
    }
    
    /**
     * 出版項目
     */
    @Data
    @Builder
    public static class PublicationItem {
        private String title;
        private String type; // "book", "paper", "article"
        private String publisher;
        private String date;
        private String url;
    }
    
    /**
     * ビジョン声明
     */
    @Data
    @Builder
    public static class VisionStatement {
        /**
         * 長期ビジョン
         */
        private String longTermVision;
        
        /**
         * 価値観
         */
        private List<String> coreValues;
        
        /**
         * 目標
         */
        private List<Goal> goals;
        
        /**
         * スローガン
         */
        private String slogan;
    }
    
    /**
     * 目標
     */
    @Data
    @Builder
    public static class Goal {
        private String title;
        private String description;
        private String timeline;
        private List<String> successMetrics;
    }
    
    /**
     * 重点課題
     */
    @Data
    @Builder
    public static class KeyIssue {
        /**
         * 課題タイトル
         */
        private String title;
        
        /**
         * 課題説明
         */
        private String description;
        
        /**
         * 現状分析
         */
        private String currentSituation;
        
        /**
         * 解決策
         */
        private String proposedSolution;
        
        /**
         * 期待効果
         */
        private String expectedImpact;
        
        /**
         * 関連政策ID
         */
        private List<String> relatedPolicyIds;
    }
    
    /**
     * 推薦
     */
    @Data
    @Builder
    public static class Endorsement {
        /**
         * 推薦者名
         */
        private String endorserName;
        
        /**
         * 推薦者の肩書き
         */
        private String endorserTitle;
        
        /**
         * 推薦文
         */
        private String endorsementText;
        
        /**
         * 推薦日
         */
        private String endorsementDate;
        
        /**
         * 推薦者の写真URL
         */
        private String endorserPhotoUrl;
    }
    
    /**
     * 外部リンク
     */
    @Data
    @Builder
    public static class ExternalLink {
        private String title;
        private String url;
        private String description;
        private LinkType type;
        
        public enum LinkType {
            WEBSITE("ウェブサイト"),
            DOCUMENT("文書"),
            VIDEO("動画"),
            SOCIAL_MEDIA("SNS"),
            NEWS("ニュース"),
            RESEARCH("研究");
            
            private final String description;
            
            LinkType(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 翻訳情報
     */
    @Data
    @Builder
    public static class TranslationInfo {
        /**
         * 翻訳者名
         */
        private String translatorName;
        
        /**
         * 翻訳日
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant translatedAt;
        
        /**
         * 翻訳方式
         */
        private TranslationMethod method;
        
        /**
         * 翻訳品質スコア
         */
        private Double qualityScore;
        
        /**
         * レビュー済みフラグ
         */
        private boolean reviewed;
        
        /**
         * レビュー者名
         */
        private String reviewerName;
        
        public enum TranslationMethod {
            HUMAN("人間翻訳"),
            MACHINE("機械翻訳"),
            HYBRID("ハイブリッド翻訳");
            
            private final String description;
            
            TranslationMethod(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * サポートされている言語
     */
    @Data
    @Builder
    public static class SupportedLanguage {
        /**
         * 言語コード
         */
        private String languageCode;
        
        /**
         * 言語名（その言語での表記）
         */
        private String nativeName;
        
        /**
         * 言語名（英語表記）
         */
        private String englishName;
        
        /**
         * 右から左に書く言語か
         */
        private boolean rightToLeft;
        
        /**
         * 翻訳完了率
         */
        private Double completionRate;
        
        /**
         * 最終更新日
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant lastUpdated;
        
        /**
         * 言語の使用地域
         */
        private List<String> regions;
    }
    
    /**
     * 国際化メタデータ
     */
    @Data
    @Builder
    public static class InternationalizationMetadata {
        /**
         * 文字エンコーディング
         */
        private String encoding;
        
        /**
         * 文字方向
         */
        private TextDirection textDirection;
        
        /**
         * フォント推奨設定
         */
        private Map<String, FontSettings> fontSettings;
        
        /**
         * 日付フォーマット設定
         */
        private Map<String, String> dateFormats;
        
        /**
         * 数値フォーマット設定
         */
        private Map<String, String> numberFormats;
        
        /**
         * 通貨フォーマット設定
         */
        private Map<String, String> currencyFormats;
        
        /**
         * タイムゾーン情報
         */
        private String timezone;
        
        public enum TextDirection {
            LTR("左から右"),
            RTL("右から左"),
            VERTICAL("縦書き");
            
            private final String description;
            
            TextDirection(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * フォント設定
     */
    @Data
    @Builder
    public static class FontSettings {
        private List<String> fontFamilies;
        private String fontSize;
        private String lineHeight;
        private String fontWeight;
    }
    
    /**
     * アクセシビリティ情報
     */
    @Data
    @Builder
    public static class AccessibilityInfo {
        /**
         * 音声読み上げ対応
         */
        private ScreenReaderSupport screenReader;
        
        /**
         * 高コントラスト表示
         */
        private HighContrastSupport highContrast;
        
        /**
         * キーボードナビゲーション
         */
        private KeyboardNavigationSupport keyboardNavigation;
        
        /**
         * 文字サイズ調整
         */
        private FontSizeSupport fontSize;
        
        /**
         * 音声ガイド
         */
        private AudioGuideSupport audioGuide;
        
        /**
         * 手話対応
         */
        private SignLanguageSupport signLanguage;
    }
    
    /**
     * スクリーンリーダー対応
     */
    @Data
    @Builder
    public static class ScreenReaderSupport {
        private boolean supported;
        private List<String> altTexts;
        private Map<String, String> ariaLabels;
    }
    
    /**
     * 高コントラスト対応
     */
    @Data
    @Builder
    public static class HighContrastSupport {
        private boolean supported;
        private List<String> colorSchemes;
    }
    
    /**
     * キーボードナビゲーション対応
     */
    @Data
    @Builder
    public static class KeyboardNavigationSupport {
        private boolean supported;
        private Map<String, String> shortcuts;
    }
    
    /**
     * フォントサイズ対応
     */
    @Data
    @Builder
    public static class FontSizeSupport {
        private boolean supported;
        private List<String> availableSizes;
        private String defaultSize;
    }
    
    /**
     * 音声ガイド対応
     */
    @Data
    @Builder
    public static class AudioGuideSupport {
        private boolean supported;
        private List<String> availableLanguages;
        private List<AudioFile> audioFiles;
    }
    
    /**
     * 音声ファイル
     */
    @Data
    @Builder
    public static class AudioFile {
        private String language;
        private String url;
        private String duration;
        private String format;
    }
    
    /**
     * 手話対応
     */
    @Data
    @Builder
    public static class SignLanguageSupport {
        private boolean supported;
        private List<String> availableSignLanguages;
        private List<VideoFile> signLanguageVideos;
    }
    
    /**
     * 手話動画ファイル
     */
    @Data
    @Builder
    public static class VideoFile {
        private String signLanguage;
        private String url;
        private String duration;
        private String format;
        private String thumbnailUrl;
    }
}
