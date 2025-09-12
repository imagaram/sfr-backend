package tokyo.sfr.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tokyo.sfr.shop.model.ShopDelivery;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopDeliveryRepository extends JpaRepository<ShopDelivery, Long> {
    
    // 注文ID別配送情報取得
    Optional<ShopDelivery> findByOrderId(Long orderId);
    
    // 追跡番号での検索
    Optional<ShopDelivery> findByTrackingNumber(String trackingNumber);
    
    // 配送業者別検索
    List<ShopDelivery> findByCarrier(String carrier);
    
    // ステータス別検索
    List<ShopDelivery> findByStatus(String status);
    
    // 配送業者とステータス別検索
    List<ShopDelivery> findByCarrierAndStatus(String carrier, String status);
}
