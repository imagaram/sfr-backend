package com.sfr.tokyo.sfr_backend.council.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.council.dto.ManifestoI18nDto;
import com.sfr.tokyo.sfr_backend.council.repository.ManifestoDocumentRepository;
import com.sfr.tokyo.sfr_backend.council.repository.ManifestoSectionRepository;
import com.sfr.tokyo.sfr_backend.council.repository.ManifestoContentRepository;
import com.sfr.tokyo.sfr_backend.entity.council.ManifestoDocument;
import com.sfr.tokyo.sfr_backend.entity.council.ManifestoSection;
import com.sfr.tokyo.sfr_backend.entity.council.ManifestoContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SFR.TOKYO Manifesto i18n対応サービス
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
 * 
 * 注意: 外部依存や翻訳ライブラリとの統合はまだ不要
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ManifestoI18nService {

    private final ManifestoDocumentRepository manifestoDocumentRepository;
    private final ManifestoSectionRepository manifestoSectionRepository;
    private final ManifestoContentRepository manifestoContentRepository;
    private final ObjectMapper objectMapper;

    /**
     * 現在アクティブなManifesto文書を取得（外部API用）
     * 
     * @param languageCode 言語コード（ja, en等）
     * @return Manifestoドキュメント
     */
    public ManifestoI18nDto getCurrentManifestoForExternal(String languageCode) {
        Optional<ManifestoDocument> activeDocument = manifestoDocumentRepository.findByIsActiveTrueOrderByVersionDesc()
                .stream().findFirst();
        
        if (activeDocument.isEmpty()) {
            return null;
        }
        
        Optional<ManifestoI18nDto.ManifestoDocumentDto> documentDto = convertToDto(activeDocument.get(), languageCode);
        if (documentDto.isEmpty()) {
            return null;
        }
        
        // 外部API用のラッパー作成
        return ManifestoI18nDto.builder()
                .document(documentDto.get())
                .build();
    }

    /**
     * 現在アクティブなManifesto文書を取得
     * 
     * @param languageCode 言語コード（ja, en等）
     * @return Manifestoドキュメント
     */
    public Optional<ManifestoI18nDto.ManifestoDocumentDto> getCurrentManifesto(String languageCode) {
        log.info("現在アクティブなManifesto取得: languageCode={}", languageCode);
        
        Optional<ManifestoDocument> document = manifestoDocumentRepository.findByIsActiveTrue();
        if (document.isEmpty()) {
            log.warn("アクティブなManifesto文書が見つかりません");
            return Optional.empty();
        }
        
        return Optional.of(convertToDto(document.get(), languageCode));
    }

    /**
     * バージョン別Manifestoドキュメントを取得
     * 
     * @param version バージョン番号
     * @param languageCode 言語コード
     * @return Manifestoドキュメント
     */
    public Optional<ManifestoI18nDto.ManifestoDocumentDto> getManifestoByVersion(String version, String languageCode) {
        log.info("バージョン別Manifesto取得: version={}, languageCode={}", version, languageCode);
        
        Optional<ManifestoDocument> document = manifestoDocumentRepository.findByVersion(version);
        if (document.isEmpty()) {
            log.warn("指定バージョンのManifesto文書が見つかりません: version={}", version);
            return Optional.empty();
        }
        
        return Optional.of(convertToDto(document.get(), languageCode));
    }

    /**
     * Manifestoセクション一覧を取得
     * 
     * @param languageCode 言語コード
     * @param pageable ページング情報
     * @return セクション一覧
     */
    public Page<ManifestoI18nDto.ManifestoSectionDto> getManifestoSections(String languageCode, Pageable pageable) {
        log.info("Manifestoセクション一覧取得: languageCode={}, page={}", languageCode, pageable.getPageNumber());
        
        Optional<ManifestoDocument> activeDocument = manifestoDocumentRepository.findByIsActiveTrue();
        if (activeDocument.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        
        Page<ManifestoSection> sectionsPage = manifestoSectionRepository
                .findByManifestoDocumentOrderByDisplayOrderAsc(activeDocument.get(), pageable);
        
        List<ManifestoI18nDto.ManifestoSectionDto> sectionDtos = sectionsPage.getContent().stream()
                .map(section -> convertSectionToDto(section, languageCode))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        
        return new PageImpl<>(sectionDtos, pageable, sectionsPage.getTotalElements());
    }

    /**
     * 特定セクションの詳細を取得
     * 
     * @param sectionId セクション識別子
     * @param languageCode 言語コード
     * @return セクション詳細
     */
    public Optional<ManifestoI18nDto.ManifestoSectionDto> getManifestoSection(String sectionId, String languageCode) {
        log.info("Manifestoセクション詳細取得: sectionId={}, languageCode={}", sectionId, languageCode);
        
        Optional<ManifestoDocument> activeDocument = manifestoDocumentRepository.findByIsActiveTrue();
        if (activeDocument.isEmpty()) {
            return Optional.empty();
        }
        
        Optional<ManifestoSection> section = manifestoSectionRepository
                .findByManifestoDocumentAndSectionId(activeDocument.get(), sectionId);
        
        if (section.isEmpty()) {
            log.warn("指定セクションが見つかりません: sectionId={}", sectionId);
            return Optional.empty();
        }
        
        return convertSectionToDto(section.get(), languageCode);
    }

    /**
     * キーワード検索
     * 
     * @param keyword 検索キーワード
     * @param languageCode 言語コード
     * @return 検索結果
     */
    public List<ManifestoI18nDto.ManifestoSectionDto> searchManifesto(String keyword, String languageCode) {
        log.info("Manifestoキーワード検索: keyword={}, languageCode={}", keyword, languageCode);
        
        List<ManifestoContent> contents = manifestoContentRepository
                .findByKeywordAndLanguageCode(keyword, languageCode);
        
        return contents.stream()
                .map(content -> convertSectionToDto(content.getManifestoSection(), languageCode))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 新しいManifesto文書を作成
     * 
     * @param manifestoDto Manifestoドキュメント
     * @return 作成されたManifesto
     */
    @Transactional
    public ManifestoI18nDto.ManifestoDocumentDto createManifesto(ManifestoI18nDto.ManifestoDocumentDto manifestoDto) {
        log.info("新しいManifesto文書作成: version={}", manifestoDto.getVersion());
        
        // 既存アクティブ文書を非アクティブ化
        manifestoDocumentRepository.findByIsActiveTrue()
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    manifestoDocumentRepository.save(existing);
                });
        
        // 新しい文書を作成
        ManifestoDocument document = ManifestoDocument.builder()
                .version(manifestoDto.getVersion())
                .authors(serializeToJson(manifestoDto.getMetadata().getAuthors()))
                .supportedLanguages(serializeToJson(manifestoDto.getMetadata().getSupportedLanguages()))
                .phase(manifestoDto.getMetadata().getPhase())
                .isActive(true)
                .description("Auto-generated from DTO")
                .build();
        
        document = manifestoDocumentRepository.save(document);
        
        // セクションとコンテンツを作成
        final ManifestoDocument finalDocument = document;
        manifestoDto.getSections().forEach(sectionDto -> {
            createManifestoSection(finalDocument, sectionDto);
        });
        
        return convertToDto(document, "ja");
    }

    /**
     * Manifestoセクションを更新
     * 
     * @param sectionId セクション識別子
     * @param sectionDto 更新内容
     * @param languageCode 言語コード
     * @return 更新されたセクション
     */
    @Transactional
    public Optional<ManifestoI18nDto.ManifestoSectionDto> updateManifestoSection(
            String sectionId, ManifestoI18nDto.ManifestoSectionDto sectionDto, String languageCode) {
        
        log.info("Manifestoセクション更新: sectionId={}, languageCode={}", sectionId, languageCode);
        
        Optional<ManifestoDocument> activeDocument = manifestoDocumentRepository.findByIsActiveTrue();
        if (activeDocument.isEmpty()) {
            return Optional.empty();
        }
        
        Optional<ManifestoSection> sectionOpt = manifestoSectionRepository
                .findByManifestoDocumentAndSectionId(activeDocument.get(), sectionId);
        
        if (sectionOpt.isEmpty()) {
            return Optional.empty();
        }
        
        ManifestoSection section = sectionOpt.get();
        section.setLastModified(Instant.now());
        section.setTags(serializeToJson(sectionDto.getTags()));
        
        // コンテンツ更新
        updateSectionContent(section, sectionDto, languageCode);
        
        manifestoSectionRepository.save(section);
        
        return convertSectionToDto(section, languageCode);
    }

    /**
     * Manifesto統計情報を取得
     * 
     * @return 統計情報
     */
    public Map<String, Object> getManifestoStatistics() {
        log.info("Manifesto統計情報取得");
        
        long totalDocuments = manifestoDocumentRepository.count();
        long totalSections = manifestoSectionRepository.count();
        long totalContents = manifestoContentRepository.count();
        
        // 言語別統計
        Map<String, Long> languageStats = Arrays.asList("ja", "en").stream()
                .collect(Collectors.toMap(
                    lang -> lang,
                    lang -> manifestoContentRepository.countByLanguageCode(lang)
                ));
        
        // カテゴリ別統計
        Map<String, Long> categoryStats = Arrays.asList("foundation", "governance", "economics", "participation", "technical").stream()
                .collect(Collectors.toMap(
                    category -> category,
                    category -> manifestoSectionRepository.countByCategory(category)
                ));
        
        return Map.of(
            "totalDocuments", totalDocuments,
            "totalSections", totalSections,
            "totalContents", totalContents,
            "languageStatistics", languageStats,
            "categoryStatistics", categoryStats,
            "lastUpdated", Instant.now(),
            "phase", "testing"
        );
    }

    // === プライベートヘルパーメソッド ===

    private ManifestoI18nDto.ManifestoDocumentDto convertToDto(ManifestoDocument document, String languageCode) {
        List<ManifestoSection> sections = manifestoSectionRepository
                .findByManifestoDocumentOrderByDisplayOrderAsc(document);
        
        List<ManifestoI18nDto.ManifestoSectionDto> sectionDtos = sections.stream()
                .map(section -> convertSectionToDto(section, languageCode))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        
        return ManifestoI18nDto.ManifestoDocumentDto.builder()
                .version(document.getVersion())
                .metadata(ManifestoI18nDto.ManifestoMetadataDto.builder()
                        .createdAt(document.getCreatedAt())
                        .updatedAt(document.getUpdatedAt())
                        .authors(deserializeFromJson(document.getAuthors(), new TypeReference<List<String>>() {}))
                        .supportedLanguages(deserializeFromJson(document.getSupportedLanguages(), new TypeReference<List<String>>() {}))
                        .phase(document.getPhase())
                        .build())
                .sections(sectionDtos)
                .build();
    }

    private Optional<ManifestoI18nDto.ManifestoSectionDto> convertSectionToDto(ManifestoSection section, String languageCode) {
        Optional<ManifestoContent> contentOpt = manifestoContentRepository
                .findByManifestoSectionAndLanguageCode(section, languageCode);
        
        if (contentOpt.isEmpty()) {
            log.warn("指定言語のコンテンツが見つかりません: sectionId={}, languageCode={}", 
                    section.getSectionId(), languageCode);
            return Optional.empty();
        }
        
        ManifestoContent content = contentOpt.get();
        
        return Optional.of(ManifestoI18nDto.ManifestoSectionDto.builder()
                .id(section.getSectionId())
                .order(section.getDisplayOrder())
                .category(section.getCategory())
                .title(createLocalizedText(section, languageCode, content.getTitle()))
                .content(createLocalizedContent(section, languageCode, content))
                .tags(deserializeFromJson(section.getTags(), new TypeReference<List<String>>() {}))
                .lastModified(section.getLastModified())
                .build());
    }

    private ManifestoI18nDto.LocalizedTextDto createLocalizedText(ManifestoSection section, String languageCode, String currentTitle) {
        Map<String, String> titles = new HashMap<>();
        
        // 全言語のタイトルを取得
        List<ManifestoContent> allContents = manifestoContentRepository
                .findByManifestoSectionOrderByLanguageCodeAsc(section);
        
        allContents.forEach(content -> titles.put(content.getLanguageCode(), content.getTitle()));
        
        return ManifestoI18nDto.LocalizedTextDto.builder()
                .ja(titles.getOrDefault("ja", currentTitle))
                .en(titles.getOrDefault("en", currentTitle))
                .additionalLanguages(titles.entrySet().stream()
                        .filter(entry -> !Arrays.asList("ja", "en").contains(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .build();
    }

    private ManifestoI18nDto.LocalizedContentDto createLocalizedContent(ManifestoSection section, String languageCode, ManifestoContent currentContent) {
        Map<String, ManifestoI18nDto.ContentStructureDto> contents = new HashMap<>();
        
        // 全言語のコンテンツを取得
        List<ManifestoContent> allContents = manifestoContentRepository
                .findByManifestoSectionOrderByLanguageCodeAsc(section);
        
        allContents.forEach(content -> {
            ManifestoI18nDto.ContentStructureDto structure = ManifestoI18nDto.ContentStructureDto.builder()
                    .summary(content.getSummary())
                    .details(deserializeFromJson(content.getDetails(), new TypeReference<List<ManifestoI18nDto.ContentBlockDto>>() {}))
                    .references(deserializeFromJson(content.getReferences(), new TypeReference<List<ManifestoI18nDto.ReferenceDto>>() {}))
                    .build();
            contents.put(content.getLanguageCode(), structure);
        });
        
        ManifestoI18nDto.ContentStructureDto currentStructure = ManifestoI18nDto.ContentStructureDto.builder()
                .summary(currentContent.getSummary())
                .details(deserializeFromJson(currentContent.getDetails(), new TypeReference<List<ManifestoI18nDto.ContentBlockDto>>() {}))
                .references(deserializeFromJson(currentContent.getReferences(), new TypeReference<List<ManifestoI18nDto.ReferenceDto>>() {}))
                .build();
        
        return ManifestoI18nDto.LocalizedContentDto.builder()
                .ja(contents.getOrDefault("ja", currentStructure))
                .en(contents.getOrDefault("en", currentStructure))
                .additionalLanguages(contents.entrySet().stream()
                        .filter(entry -> !Arrays.asList("ja", "en").contains(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .build();
    }

    @Transactional
    private void createManifestoSection(ManifestoDocument document, ManifestoI18nDto.ManifestoSectionDto sectionDto) {
        ManifestoSection section = ManifestoSection.builder()
                .manifestoDocument(document)
                .sectionId(sectionDto.getId())
                .displayOrder(sectionDto.getOrder())
                .category(sectionDto.getCategory())
                .tags(serializeToJson(sectionDto.getTags()))
                .lastModified(Instant.now())
                .build();
        
        section = manifestoSectionRepository.save(section);
        
        // 日本語コンテンツ作成
        createManifestoContent(section, "ja", sectionDto.getTitle().getJa(), sectionDto.getContent().getJa());
        
        // 英語コンテンツ作成
        createManifestoContent(section, "en", sectionDto.getTitle().getEn(), sectionDto.getContent().getEn());
    }

    @Transactional
    private void createManifestoContent(ManifestoSection section, String languageCode, String title, ManifestoI18nDto.ContentStructureDto contentStructure) {
        ManifestoContent content = ManifestoContent.builder()
                .manifestoSection(section)
                .languageCode(languageCode)
                .title(title)
                .summary(contentStructure.getSummary())
                .details(serializeToJson(contentStructure.getDetails()))
                .references(serializeToJson(contentStructure.getReferences()))
                .translationStatus("original")
                .translationQualityScore(1.0)
                .build();
        
        manifestoContentRepository.save(content);
    }

    @Transactional
    private void updateSectionContent(ManifestoSection section, ManifestoI18nDto.ManifestoSectionDto sectionDto, String languageCode) {
        Optional<ManifestoContent> contentOpt = manifestoContentRepository
                .findByManifestoSectionAndLanguageCode(section, languageCode);
        
        if (contentOpt.isPresent()) {
            ManifestoContent content = contentOpt.get();
            
            // 対応する言語のデータを取得
            String newTitle;
            ManifestoI18nDto.ContentStructureDto newContentStructure;
            
            if ("ja".equals(languageCode)) {
                newTitle = sectionDto.getTitle().getJa();
                newContentStructure = sectionDto.getContent().getJa();
            } else if ("en".equals(languageCode)) {
                newTitle = sectionDto.getTitle().getEn();
                newContentStructure = sectionDto.getContent().getEn();
            } else {
                return; // 対応外言語
            }
            
            content.setTitle(newTitle);
            content.setSummary(newContentStructure.getSummary());
            content.setDetails(serializeToJson(newContentStructure.getDetails()));
            content.setReferences(serializeToJson(newContentStructure.getReferences()));
            content.setTranslationStatus("reviewed");
            
            manifestoContentRepository.save(content);
        }
    }

    private String serializeToJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON シリアライズエラー", e);
            return null;
        }
    }

    private <T> T deserializeFromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON デシリアライズエラー: json={}", json, e);
            return null;
        }
    }

    // ========== 外部API用の追加メソッド ==========

    /**
     * 利用可能言語一覧を取得
     */
    public List<String> getAvailableLanguages() {
        return manifestoContentRepository.findDistinctLanguages();
    }

    /**
     * 利用可能バージョン一覧を取得
     */
    public List<String> getAvailableVersions() {
        return manifestoDocumentRepository.findDistinctVersions();
    }

    /**
     * Manifestoをページング検索（外部API用）
     */
    public Page<ManifestoI18nDto.ContentStructureDto> searchManifestoWithPaging(
            String keyword, String languageCode, Pageable pageable) {
        
        List<ManifestoContent> contents = manifestoContentRepository
                .findByLanguageCodeAndTitleContainingOrSummaryContaining(
                        languageCode, keyword, keyword);
        
        List<ManifestoI18nDto.ContentStructureDto> results = contents.stream()
                .map(this::convertContentToStructureDto)
                .collect(Collectors.toList());
        
        // 手動ページング（実際の実装ではSQLレベルで行う）
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        
        List<ManifestoI18nDto.ContentStructureDto> pageContent = 
                start < results.size() ? results.subList(start, end) : Collections.emptyList();
        
        return new PageImpl<>(pageContent, pageable, results.size());
    }

    private ManifestoI18nDto.ContentStructureDto convertContentToStructureDto(ManifestoContent content) {
        return ManifestoI18nDto.ContentStructureDto.builder()
                .summary(content.getSummary())
                .details(deserializeFromJson(content.getDetails(), 
                        new TypeReference<List<ManifestoI18nDto.ContentBlockDto>>() {}))
                .references(deserializeFromJson(content.getReferences(), 
                        new TypeReference<List<ManifestoI18nDto.ReferenceDto>>() {}))
                .build();
    }
}
