package tokyo.sfr.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tokyo.sfr.shop.model.ShopOrder;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {
    
    // 購入者別注文一覧
    List<ShopOrder> findByBuyerId(Long buyerId);
    
    // 商品ID別注文一覧
    List<ShopOrder> findByItemId(Long itemId);
    
    // ステータス別注文一覧
    List<ShopOrder> findByStatus(String status);
    
    // 購入者とステータスでの検索
    List<ShopOrder> findByBuyerIdAndStatus(Long buyerId, String status);
    
    // 日付範囲での注文検索
    List<ShopOrder> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 販売者の注文一覧（商品経由）
    @Query("SELECT o FROM ShopOrder o JOIN ShopItem i ON o.itemId = i.id WHERE i.ownerId = :sellerId")
    List<ShopOrder> findOrdersBySellerId(@Param("sellerId") Long sellerId);
    
    // 購入者の注文統計
    @Query("SELECT COUNT(o) FROM ShopOrder o WHERE o.buyerId = :buyerId")
    Long countOrdersByBuyerId(@Param("buyerId") Long buyerId);
}
