package com.sfr.tokyo.sfr_backend.repository.shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sfr.tokyo.sfr_backend.entity.shop.LimitedItem;

/**
 * 限定アイテムリポジトリ
 * 商品検索・統計・管理機能
 */
@Repository
public interface LimitedItemRepository extends JpaRepository<LimitedItem, Long> {

    // 基本検索
    Optional<LimitedItem> findByIdAndStatus(Long id, LimitedItem.ItemStatus status);
    
    List<LimitedItem> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    
    Page<LimitedItem> findByStatusOrderByCreatedAtDesc(LimitedItem.ItemStatus status, Pageable pageable);

    // 販売中アイテム
    @Query("SELECT li FROM LimitedItem li WHERE li.status = 'ACTIVE' AND li.stockQuantity > 0 ORDER BY li.createdAt DESC")
    Page<LimitedItem> findActiveItems(Pageable pageable);

    // 価格帯検索
    @Query("SELECT li FROM LimitedItem li WHERE li.status = 'ACTIVE' AND li.sfrPrice BETWEEN :minPrice AND :maxPrice ORDER BY li.sfrPrice ASC")
    Page<LimitedItem> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                       @Param("maxPrice") BigDecimal maxPrice, 
                                       Pageable pageable);

    // テキスト検索（タイトル・説明）
    @Query("SELECT li FROM LimitedItem li WHERE li.status = 'ACTIVE' AND (li.title LIKE %:keyword% OR li.description LIKE %:keyword%) ORDER BY li.salesCount DESC")
    Page<LimitedItem> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 人気商品（売上順）
    @Query("SELECT li FROM LimitedItem li WHERE li.status = 'ACTIVE' ORDER BY li.salesCount DESC")
    Page<LimitedItem> findPopularItems(Pageable pageable);

    // 販売者統計
    @Query("SELECT COUNT(li) FROM LimitedItem li WHERE li.sellerId = :sellerId AND li.status = 'ACTIVE'")
    int countActiveItemsBySeller(@Param("sellerId") String sellerId);

    @Query("SELECT SUM(li.salesCount) FROM LimitedItem li WHERE li.sellerId = :sellerId")
    Optional<Integer> getTotalSalesBySeller(@Param("sellerId") String sellerId);

    @Query("SELECT SUM(li.sfrPrice * li.salesCount) FROM LimitedItem li WHERE li.sellerId = :sellerId")
    Optional<BigDecimal> getTotalRevenueBySeller(@Param("sellerId") String sellerId);

    // 期間別販売統計
    @Query("SELECT li FROM LimitedItem li WHERE li.sellerId = :sellerId AND li.updatedAt >= :fromDate ORDER BY li.salesCount DESC")
    List<LimitedItem> getRecentSales(@Param("sellerId") String sellerId, 
                                     @Param("fromDate") LocalDateTime fromDate, 
                                     Pageable pageable);

    // 在庫警告
    @Query("SELECT li FROM LimitedItem li WHERE li.status = 'ACTIVE' AND li.stockQuantity <= :threshold")
    List<LimitedItem> findLowStockItems(@Param("threshold") int threshold);

    // カテゴリ別（将来拡張用）
    // @Query("SELECT li FROM LimitedItem li WHERE li.category = :category AND li.status = 'ACTIVE'")
    // Page<LimitedItem> findByCategory(@Param("category") String category, Pageable pageable);

    // 売上ランキング
    @Query("SELECT li FROM LimitedItem li WHERE li.status IN ('ACTIVE', 'SOLD_OUT') ORDER BY (li.sfrPrice * li.salesCount) DESC")
    List<LimitedItem> getTopEarningItems(Pageable pageable);

    // 在庫復活候補
    @Query("SELECT li FROM LimitedItem li WHERE li.status = 'SOLD_OUT' AND li.salesCount > :minSales ORDER BY li.salesCount DESC")
    List<LimitedItem> getRestockCandidates(@Param("minSales") int minSales);
}