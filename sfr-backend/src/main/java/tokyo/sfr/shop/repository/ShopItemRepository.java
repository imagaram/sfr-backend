package tokyo.sfr.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tokyo.sfr.shop.model.ShopItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    
    // カテゴリ別検索
    List<ShopItem> findByCategory(String category);
    
    // 価格範囲検索
    List<ShopItem> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // 販売者ID別検索
    List<ShopItem> findByOwnerId(Long ownerId);
    
    // 在庫がある商品のみ検索
    List<ShopItem> findByStockGreaterThan(Integer stock);
    
    // 商品名での部分一致検索
    List<ShopItem> findByNameContainingIgnoreCase(String name);
    
    // 複合検索（カテゴリと価格範囲）
    @Query("SELECT s FROM ShopItem s WHERE " +
           "(:category IS NULL OR s.category = :category) AND " +
           "(:minPrice IS NULL OR s.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR s.price <= :maxPrice) AND " +
           "s.stock > 0")
    List<ShopItem> findItemsWithFilters(
        @Param("category") String category,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );
}
