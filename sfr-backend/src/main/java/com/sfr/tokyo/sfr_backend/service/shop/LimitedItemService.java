package com.sfr.tokyo.sfr_backend.service.shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sfr.tokyo.sfr_backend.entity.shop.LimitedItem;
import com.sfr.tokyo.sfr_backend.repository.shop.LimitedItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 限定アイテム管理サービス
 * CRUD操作・在庫管理・販売統計
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LimitedItemService {

    private final LimitedItemRepository limitedItemRepository;

    // === CRUD操作 ===

    /**
     * 新しい限定アイテム作成
     */
    @Transactional
    public LimitedItem createLimitedItem(LimitedItem item) {
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        
        log.info("新しい限定アイテム作成: sellerId={}, title={}", item.getSellerId(), item.getTitle());
        return limitedItemRepository.save(item);
    }

    /**
     * 限定アイテム更新
     */
    @Transactional
    public Optional<LimitedItem> updateLimitedItem(Long itemId, LimitedItem updatedItem) {
        return limitedItemRepository.findById(itemId)
            .map(existing -> {
                // 更新可能フィールドのみ変更
                existing.setTitle(updatedItem.getTitle());
                existing.setDescription(updatedItem.getDescription());
                existing.setSfrPrice(updatedItem.getSfrPrice());
                existing.setStockQuantity(updatedItem.getStockQuantity());
                existing.setImageUrls(updatedItem.getImageUrls());
                existing.setUpdatedAt(LocalDateTime.now());
                
                log.info("限定アイテム更新: id={}, title={}", itemId, existing.getTitle());
                return limitedItemRepository.save(existing);
            });
    }

    /**
     * アイテム有効化
     */
    @Transactional
    public Optional<LimitedItem> activateLimitedItem(Long itemId) {
        return limitedItemRepository.findById(itemId)
            .map(item -> {
                if (item.getStockQuantity() != null && item.getStockQuantity() > 0) {
                    item.setStatus(LimitedItem.ItemStatus.ACTIVE);
                    item.setUpdatedAt(LocalDateTime.now());
                    log.info("アイテム有効化: id={}", itemId);
                    return limitedItemRepository.save(item);
                } else {
                    throw new IllegalStateException("在庫がない商品は有効化できません");
                }
            });
    }

    /**
     * アイテム無効化
     */
    @Transactional
    public Optional<LimitedItem> deactivateLimitedItem(Long itemId) {
        return limitedItemRepository.findById(itemId)
            .map(item -> {
                item.setStatus(LimitedItem.ItemStatus.DISCONTINUED);
                item.setUpdatedAt(LocalDateTime.now());
                log.info("アイテム無効化: id={}", itemId);
                return limitedItemRepository.save(item);
            });
    }

    /**
     * アイテム削除（論理削除）
     */
    @Transactional
    public boolean deleteLimitedItem(Long itemId) {
        return limitedItemRepository.findById(itemId)
            .map(item -> {
                item.setStatus(LimitedItem.ItemStatus.DISCONTINUED);
                item.setUpdatedAt(LocalDateTime.now());
                limitedItemRepository.save(item);
                log.info("アイテム削除（論理削除）: id={}", itemId);
                return true;
            })
            .orElse(false);
    }

    // === 検索・取得 ===

    /**
     * アイテム詳細取得
     */
    public Optional<LimitedItem> getLimitedItemById(Long itemId) {
        return limitedItemRepository.findById(itemId);
    }

    /**
     * 販売中アイテム一覧
     */
    public Page<LimitedItem> getActiveItems(Pageable pageable) {
        return limitedItemRepository.findActiveItems(pageable);
    }

    /**
     * 販売者のアイテム一覧
     */
    public List<LimitedItem> getSellerItems(String sellerId) {
        return limitedItemRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    /**
     * キーワード検索
     */
    public Page<LimitedItem> searchItems(String keyword, Pageable pageable) {
        return limitedItemRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 価格帯検索
     */
    public Page<LimitedItem> getItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return limitedItemRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }

    // === 在庫管理 ===

    /**
     * 在庫補充
     */
    @Transactional
    public Optional<LimitedItem> restockItem(Long itemId, int additionalStock) {
        return limitedItemRepository.findById(itemId)
            .map(item -> {
                item.addStock(additionalStock);
                item.setUpdatedAt(LocalDateTime.now());
                log.info("在庫補充: id={}, 追加数={}, 新在庫={}", itemId, additionalStock, item.getStockQuantity());
                return limitedItemRepository.save(item);
            });
    }

    /**
     * 在庫警告リスト取得
     */
    public List<LimitedItem> getLowStockItems(int threshold) {
        return limitedItemRepository.findLowStockItems(threshold);
    }

    // === 統計・分析 ===

    /**
     * 販売者統計取得
     */
    public SellerSalesStats getSellerSalesStats(String sellerId) {
        int totalItems = limitedItemRepository.countActiveItemsBySeller(sellerId);
        int totalSold = limitedItemRepository.getTotalSalesBySeller(sellerId).orElse(0);
        BigDecimal totalRevenue = limitedItemRepository.getTotalRevenueBySeller(sellerId).orElse(BigDecimal.ZERO);
        
        return new SellerSalesStats(sellerId, totalItems, totalSold, totalRevenue);
    }

    /**
     * 最近の販売履歴
     */
    public List<LimitedItem> getRecentSales(String sellerId, int limitCount) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(30); // 30日間
        Pageable pageable = PageRequest.of(0, limitCount);
        return limitedItemRepository.getRecentSales(sellerId, fromDate, pageable);
    }

    /**
     * アイテム販売履歴
     */
    public List<LimitedItem> getItemSalesHistory(String sellerId) {
        return limitedItemRepository.getRecentSales(sellerId, LocalDateTime.now().minusMonths(1), 
            PageRequest.of(0, 100));
    }

    /**
     * 人気商品ランキング
     */
    public Page<LimitedItem> getPopularItems(Pageable pageable) {
        return limitedItemRepository.findPopularItems(pageable);
    }

    /**
     * 売上ランキング
     */
    public List<LimitedItem> getTopEarningItems(int limit) {
        return limitedItemRepository.getTopEarningItems(PageRequest.of(0, limit));
    }

    // === 内部クラス ===

    /**
     * 販売者統計データ
     */
    public static class SellerSalesStats {
        private final String sellerId;
        private final int totalItems;
        private final int totalSold;
        private final BigDecimal totalRevenue;

        public SellerSalesStats(String sellerId, int totalItems, int totalSold, BigDecimal totalRevenue) {
            this.sellerId = sellerId;
            this.totalItems = totalItems;
            this.totalSold = totalSold;
            this.totalRevenue = totalRevenue;
        }

        public String getSellerId() { return sellerId; }
        public int getTotalItems() { return totalItems; }
        public int getTotalSold() { return totalSold; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }
}