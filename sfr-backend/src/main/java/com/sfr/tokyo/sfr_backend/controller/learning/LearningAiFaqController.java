package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningAiFaqDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningAiFaq;
import com.sfr.tokyo.sfr_backend.service.learning.LearningAiFaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 学習AI FAQ コントローラー
 */
@RestController
@RequestMapping("/api/learning/ai-faq")
@RequiredArgsConstructor
@Slf4j
public class LearningAiFaqController {

    private final LearningAiFaqService faqService;

    // ========== 基本CRUD操作 ==========

    /**
     * FAQ作成
     */
    @PostMapping
    public ResponseEntity<LearningAiFaqDto> createFaq(
            @Valid @RequestBody LearningAiFaqDto faqDto) {
        log.info("FAQ作成API: question={}", faqDto.getQuestion());

        LearningAiFaqDto createdFaq = faqService.createFaq(faqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFaq);
    }

    /**
     * FAQ更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<LearningAiFaqDto> updateFaq(
            @PathVariable Long id,
            @Valid @RequestBody LearningAiFaqDto faqDto) {
        log.info("FAQ更新API: id={}", id);

        LearningAiFaqDto updatedFaq = faqService.updateFaq(id, faqDto);
        return ResponseEntity.ok(updatedFaq);
    }

    /**
     * FAQ削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        log.info("FAQ削除API: id={}", id);

        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * FAQ取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningAiFaqDto> getFaq(@PathVariable Long id) {
        log.info("FAQ取得API: id={}", id);

        LearningAiFaqDto faq = faqService.getFaqById(id);
        return ResponseEntity.ok(faq);
    }

    /**
     * FAQ取得（閲覧数増加なし）
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<LearningAiFaqDto> previewFaq(@PathVariable Long id) {
        log.info("FAQ プレビューAPI: id={}", id);

        return faqService.getFaqByIdWithoutIncrement(id)
                .map(faq -> ResponseEntity.ok(faq))
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== 検索機能 ==========

    /**
     * 公開FAQ一覧取得
     */
    @GetMapping("/published")
    public ResponseEntity<Page<LearningAiFaqDto>> getPublishedFaqs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<LearningAiFaqDto> faqs = faqService.getPublishedFaqs(pageable);
        return ResponseEntity.ok(faqs);
    }

    /**
     * カテゴリー別FAQ取得
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<LearningAiFaqDto>> getFaqsByCategory(
            @PathVariable LearningAiFaq.FaqCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "priority", "createdAt"));
        Page<LearningAiFaqDto> faqs = faqService.getFaqsByCategory(category, pageable);
        return ResponseEntity.ok(faqs);
    }

    /**
     * ステータス別FAQ取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LearningAiFaqDto>> getFaqsByStatus(
            @PathVariable LearningAiFaq.FaqStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<LearningAiFaqDto> faqs = faqService.getFaqsByStatus(status, pageable);
        return ResponseEntity.ok(faqs);
    }

    /**
     * 言語別FAQ取得
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<Page<LearningAiFaqDto>> getFaqsByLanguage(
            @PathVariable String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<LearningAiFaqDto> faqs = faqService.getFaqsByLanguage(language, pageable);
        return ResponseEntity.ok(faqs);
    }

    /**
     * キーワード検索
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LearningAiFaqDto>> searchFaqs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "popularityScore", "usefulnessScore"));
        Page<LearningAiFaqDto> faqs = faqService.searchFaqs(keyword, pageable);
        return ResponseEntity.ok(faqs);
    }

    /**
     * 高度な検索
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<LearningAiFaqDto>> advancedSearch(
            @RequestParam(required = false) LearningAiFaq.FaqCategory category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) LearningAiFaq.FaqStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "usefulnessScore"));
        Page<LearningAiFaqDto> faqs = faqService.advancedSearch(category, subCategory, keyword, language, status,
                pageable);
        return ResponseEntity.ok(faqs);
    }

    /**
     * タグ検索
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<LearningAiFaqDto>> searchByTag(@PathVariable String tag) {
        log.info("タグ検索API: tag={}", tag);

        List<LearningAiFaqDto> faqs = faqService.searchByTag(tag);
        return ResponseEntity.ok(faqs);
    }

    /**
     * 関連FAQ取得
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<List<LearningAiFaqDto>> getRelatedFaqs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("関連FAQ取得API: id={}, limit={}", id, limit);

        List<LearningAiFaqDto> relatedFaqs = faqService.getRelatedFaqs(id, limit);
        return ResponseEntity.ok(relatedFaqs);
    }

    // ========== 人気・品質関連 ==========

    /**
     * 人気FAQ取得
     */
    @GetMapping("/popular")
    public ResponseEntity<List<LearningAiFaqDto>> getPopularFaqs(
            @RequestParam(defaultValue = "10") int limit) {

        List<LearningAiFaqDto> popularFaqs = faqService.getPopularFaqs(limit);
        return ResponseEntity.ok(popularFaqs);
    }

    /**
     * 高品質FAQ取得
     */
    @GetMapping("/high-quality")
    public ResponseEntity<List<LearningAiFaqDto>> getHighQualityFaqs(
            @RequestParam(defaultValue = "7.0") BigDecimal minScore,
            @RequestParam(defaultValue = "10") int limit) {

        List<LearningAiFaqDto> highQualityFaqs = faqService.getHighQualityFaqs(minScore, limit);
        return ResponseEntity.ok(highQualityFaqs);
    }

    /**
     * 最新FAQ取得
     */
    @GetMapping("/recent")
    public ResponseEntity<List<LearningAiFaqDto>> getRecentFaqs(
            @RequestParam(defaultValue = "10") int limit) {

        List<LearningAiFaqDto> recentFaqs = faqService.getRecentFaqs(limit);
        return ResponseEntity.ok(recentFaqs);
    }

    /**
     * 最も閲覧されたFAQ取得
     */
    @GetMapping("/most-viewed")
    public ResponseEntity<List<LearningAiFaqDto>> getMostViewedFaqs(
            @RequestParam(defaultValue = "10") int limit) {

        List<LearningAiFaqDto> mostViewedFaqs = faqService.getMostViewedFaqs(limit);
        return ResponseEntity.ok(mostViewedFaqs);
    }
    // ========== 評価機能 ==========

    /**
     * FAQ評価
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<Void> rateFaq(
            @PathVariable Long id,
            @Valid @RequestBody LearningAiFaqDto.RatingRequest request) {

        log.info("FAQ評価API: id={}, helpful={}", id, request.getIsHelpful());

        faqService.rateFaq(id, request.getIsHelpful());
        return ResponseEntity.ok().build();
    }

    /**
     * 役に立った投票
     */
    @PostMapping("/{id}/helpful")
    public ResponseEntity<Void> markAsHelpful(@PathVariable Long id) {
        log.info("役に立った投票API: id={}", id);

        faqService.markAsHelpful(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 役に立たなかった投票
     */
    @PostMapping("/{id}/not-helpful")
    public ResponseEntity<Void> markAsNotHelpful(@PathVariable Long id) {
        log.info("役に立たなかった投票API: id={}", id);

        faqService.markAsNotHelpful(id);
        return ResponseEntity.ok().build();
    }

    // ========== AI関連機能 ==========

    /**
     * AI生成FAQ取得
     */
    @GetMapping("/ai-generated")
    public ResponseEntity<Page<LearningAiFaqDto>> getAiGeneratedFaqs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "aiConfidence", "createdAt"));
        Page<LearningAiFaqDto> faqs = faqService.getAiGeneratedFaqs(pageable);
        return ResponseEntity.ok(faqs);
    }

    /**
     * 高信頼度AI生成FAQ取得
     */
    @GetMapping("/ai-generated/high-confidence")
    public ResponseEntity<List<LearningAiFaqDto>> getHighConfidenceAiFaqs(
            @RequestParam(defaultValue = "0.8") BigDecimal minConfidence) {

        List<LearningAiFaqDto> faqs = faqService.getHighConfidenceAiFaqs(minConfidence);
        return ResponseEntity.ok(faqs);
    }

    /**
     * AI更新対象FAQ取得
     */
    @GetMapping("/ai-update-targets")
    public ResponseEntity<List<LearningAiFaqDto>> getFaqsForAiUpdate() {
        List<LearningAiFaqDto> faqs = faqService.getFaqsForAiUpdate();
        return ResponseEntity.ok(faqs);
    }

    /**
     * AI FAQ生成
     */
    @PostMapping("/generate")
    public ResponseEntity<LearningAiFaqDto> generateAiFaq(
            @Valid @RequestBody LearningAiFaqDto.AiGenerationRequest request) {

        log.info("AI FAQ生成API: category={}, language={}", request.getCategory(), request.getLanguage());

        LearningAiFaqDto generatedFaq = faqService.generateAiFaq(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(generatedFaq);
    }

    /**
     * AI更新実行
     */
    @PostMapping("/{id}/ai-update")
    public ResponseEntity<Void> executeAiUpdate(@PathVariable Long id) {
        log.info("AI更新実行API: id={}", id);

        faqService.executeAiUpdate(id);
        return ResponseEntity.ok().build();
    }

    // ========== 管理機能 ==========

    /**
     * FAQ承認
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveFaq(
            @PathVariable Long id,
            @RequestParam UUID approverId) {

        log.info("FAQ承認API: id={}, approverId={}", id, approverId);

        faqService.approveFaq(id, approverId);
        return ResponseEntity.ok().build();
    }

    /**
     * ステータス更新
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam LearningAiFaq.FaqStatus status,
            @RequestParam UUID updatedBy) {

        log.info("ステータス更新API: id={}, status={}, updatedBy={}", id, status, updatedBy);

        faqService.updateStatus(id, status, updatedBy);
        return ResponseEntity.ok().build();
    }

    /**
     * FAQ復元
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restoreFaq(@PathVariable Long id) {
        log.info("FAQ復元API: id={}", id);

        faqService.restoreFaq(id);
        return ResponseEntity.ok().build();
    }

    // ========== 統計・分析 ==========

    /**
     * カテゴリー別FAQ数取得
     */
    @GetMapping("/count/category/{category}")
    public ResponseEntity<Long> countByCategory(@PathVariable LearningAiFaq.FaqCategory category) {
        Long count = faqService.countByCategory(category);
        return ResponseEntity.ok(count);
    }

    /**
     * ステータス別FAQ数取得
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countByStatus(@PathVariable LearningAiFaq.FaqStatus status) {
        Long count = faqService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    /**
     * 期間別統計取得
     */
    @GetMapping("/statistics/date-range")
    public ResponseEntity<List<Map<String, Object>>> getFaqCountByDateRange(
            @RequestParam LocalDateTime fromDate,
            @RequestParam LocalDateTime toDate) {

        List<Map<String, Object>> statistics = faqService.getFaqCountByDateRange(fromDate, toDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * カテゴリー別統計取得
     */
    @GetMapping("/statistics/category")
    public ResponseEntity<List<Map<String, Object>>> getCategoryStatistics() {
        List<Map<String, Object>> statistics = faqService.getCategoryStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 全体統計取得
     */
    @GetMapping("/statistics/overall")
    public ResponseEntity<LearningAiFaqDto.FaqStatistics> getOverallStatistics() {
        LearningAiFaqDto.FaqStatistics statistics = faqService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ========== バッチ処理・メンテナンス ==========

    /**
     * 古いFAQのアーカイブ
     */
    @PostMapping("/maintenance/archive")
    public ResponseEntity<Void> archiveOldFaqs(
            @RequestParam LocalDateTime cutoffDate,
            @RequestParam Long minViewCount) {

        log.info("古いFAQアーカイブAPI: cutoffDate={}, minViewCount={}", cutoffDate, minViewCount);

        faqService.archiveOldFaqs(cutoffDate, minViewCount);
        return ResponseEntity.ok().build();
    }

    /**
     * 低品質FAQのマーク
     */
    @PostMapping("/maintenance/mark-low-quality")
    public ResponseEntity<Void> markLowQualityFaqs(
            @RequestParam BigDecimal minScore) {

        log.info("低品質FAQマークAPI: minScore={}", minScore);

        faqService.markLowQualityFaqs(minScore);
        return ResponseEntity.ok().build();
    }

    /**
     * スコア再計算
     */
    @PostMapping("/maintenance/recalculate-scores")
    public ResponseEntity<Void> recalculateScores() {
        log.info("スコア再計算API");

        faqService.recalculateScores();
        return ResponseEntity.ok().build();
    }

    /**
     * 検索ベクトル一括更新
     */
    @PostMapping("/maintenance/update-search-vectors")
    public ResponseEntity<Void> updateAllSearchVectors() {
        log.info("検索ベクトル一括更新API");

        faqService.updateAllSearchVectors();
        return ResponseEntity.ok().build();
    }

    // ========== ヘルスチェック ==========

    /**
     * FAQ システムヘルスチェック
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            LearningAiFaqDto.FaqStatistics stats = faqService.getOverallStatistics();

            Map<String, Object> health = Map.of(
                    "status", "healthy",
                    "timestamp", LocalDateTime.now(),
                    "totalFaqs", stats.getTotalFaqs(),
                    "publishedFaqs", stats.getPublishedFaqs(),
                    "aiGeneratedFaqs", stats.getAutoGeneratedFaqs());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("ヘルスチェックエラー", e);

            Map<String, Object> health = Map.of(
                    "status", "unhealthy",
                    "timestamp", LocalDateTime.now(),
                    "error", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
}
