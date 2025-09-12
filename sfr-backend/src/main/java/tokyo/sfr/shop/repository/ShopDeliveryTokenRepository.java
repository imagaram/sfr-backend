package tokyo.sfr.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tokyo.sfr.shop.model.ShopDeliveryToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopDeliveryTokenRepository extends JpaRepository<ShopDeliveryToken, Long> {
    
    // 配送ID別トークン取得
    List<ShopDeliveryToken> findByDeliveryId(Long deliveryId);
    
    // PoAトークンでの検索
    Optional<ShopDeliveryToken> findByPoaToken(String poaToken);
}
