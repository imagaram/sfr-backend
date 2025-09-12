package tokyo.sfr.shop.service;

import tokyo.sfr.shop.model.ShopItem;
import tokyo.sfr.shop.model.ShopOrder;
import tokyo.sfr.shop.model.ShopDelivery;
import tokyo.sfr.shop.model.ShopDeliveryToken;
import tokyo.sfr.shop.controller.ShopController;
import tokyo.sfr.shop.repository.ShopItemRepository;
import tokyo.sfr.shop.repository.ShopOrderRepository;
import tokyo.sfr.shop.repository.ShopDeliveryRepository;
import tokyo.sfr.shop.repository.ShopDeliveryTokenRepository;
import java.util.List;
import java.util.Optional;
import com.sfr.tokyo.sfr_backend.service.crypto.RewardDistributionService;
import com.sfr.tokyo.sfr_backend.entity.crypto.RewardDistribution;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShopService {
    private final RewardDistributionService rewardDistributionService;
    private final ShopItemRepository shopItemRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final ShopDeliveryRepository shopDeliveryRepository;
    private final ShopDeliveryTokenRepository shopDeliveryTokenRepository;

    public ShopService(RewardDistributionService rewardDistributionService,
                      ShopItemRepository shopItemRepository,
                      ShopOrderRepository shopOrderRepository,
                      ShopDeliveryRepository shopDeliveryRepository,
                      ShopDeliveryTokenRepository shopDeliveryTokenRepository) {
        this.rewardDistributionService = rewardDistributionService;
        this.shopItemRepository = shopItemRepository;
        this.shopOrderRepository = shopOrderRepository;
        this.shopDeliveryRepository = shopDeliveryRepository;
        this.shopDeliveryTokenRepository = shopDeliveryTokenRepository;
    }

    // 商品一覧取得
    public List<ShopItem> getAllItems() { 
        return shopItemRepository.findAll();
    }

    // 商品登録
    public void addItem(ShopItem item) { 
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        shopItemRepository.save(item);
    }

    // 商品詳細取得
    public ShopItem getItem(Long id) { 
        return shopItemRepository.findById(id).orElse(null);
    }

    // 商品編集
    public void updateItem(ShopItem item) { 
        item.setUpdatedAt(LocalDateTime.now());
        shopItemRepository.save(item);
    }

    // 商品削除
    public void deleteItem(Long id) { 
        shopItemRepository.deleteById(id);
    }

    // 商品画像保存
    public String saveItemImage(Long id, MultipartFile file) {
        String uploadDir = "static/uploads/shop_images/";
        String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".") ? 
                     file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.')) : "";
        String fileName = "item_" + id + ext;
        File dest = new File(uploadDir + fileName);
        dest.getParentFile().mkdirs();
        try {
            file.transferTo(dest);
            // DBに画像パス保存する場合はここで更新
            return "/uploads/shop_images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("画像保存に失敗しました", e);
        }
    }

    // PoA提出処理
    public void submitPoA(Long itemId, ShopController.PoARequest poa) {
        // PoA提出記録をデータベースに保存
        // 実際の実装では、PoA専用のエンティティを作成することを推奨
        Optional<ShopItem> item = shopItemRepository.findById(itemId);
        if (item.isPresent()) {
            // ログとして記録（実装は簡略化）
            System.out.println("PoA submitted for item " + itemId + ": " + poa.text + " | " + poa.url);
        }
    }

    // 注文作成
    public void createOrder(ShopOrder order) { 
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus("PENDING"); // 初期ステータス
        shopOrderRepository.save(order);
        
        // 在庫減少処理
        Optional<ShopItem> item = shopItemRepository.findById(order.getItemId());
        if (item.isPresent()) {
            ShopItem shopItem = item.get();
            if (shopItem.getStock() >= order.getQuantity()) {
                shopItem.setStock(shopItem.getStock() - order.getQuantity());
                shopItemRepository.save(shopItem);
            } else {
                throw new RuntimeException("在庫不足です");
            }
        }
    }

    // 購入者別注文一覧取得
    public List<ShopOrder> getOrdersByBuyer(Long buyerId) { 
        return shopOrderRepository.findByBuyerId(buyerId);
    }

    // 注文詳細取得
    public ShopOrder getOrder(Long id) { 
        return shopOrderRepository.findById(id).orElse(null);
    }

    // 配送ラベル作成
    public ShopDelivery createDeliveryLabel(Long orderId, String carrier, String recipientInfo, String poaToken) {
        ShopDelivery delivery = new ShopDelivery();
        delivery.setOrderId(orderId);
        delivery.setCarrier(carrier);
        delivery.setTrackingNumber(generateTrackingNumber(carrier));
        delivery.setLabelUrl(generateLabelUrl(orderId, carrier));
        delivery.setStatus("LABEL_CREATED");
        delivery.setCreatedAt(LocalDateTime.now());
        delivery.setUpdatedAt(LocalDateTime.now());
        
        ShopDelivery savedDelivery = shopDeliveryRepository.save(delivery);
        
        // 配送トークン作成
        ShopDeliveryToken token = new ShopDeliveryToken();
        token.setDeliveryId(savedDelivery.getId());
        token.setPoaToken(poaToken);
        token.setCreatedAt(LocalDateTime.now());
        shopDeliveryTokenRepository.save(token);
        
        return savedDelivery;
    }

    // 配送追跡
    public ShopDelivery trackDelivery(String trackingNumber, String carrier) {
        return shopDeliveryRepository.findByTrackingNumber(trackingNumber).orElse(null);
    }

    // 配送トークン発行
    public ShopDeliveryToken issueDeliveryToken(Long deliveryId, String poaToken) {
        ShopDeliveryToken token = new ShopDeliveryToken();
        token.setDeliveryId(deliveryId);
        token.setPoaToken(poaToken);
        token.setCreatedAt(LocalDateTime.now());
        return shopDeliveryTokenRepository.save(token);
    }

    // 開発用メソッド - 全アイテム削除
    public void clearAllItems() {
        shopItemRepository.deleteAll();
    }
    
    // ヘルパーメソッド
    private String generateTrackingNumber(String carrier) {
        return carrier.toUpperCase() + "-" + System.currentTimeMillis();
    }
    
    private String generateLabelUrl(Long orderId, String carrier) {
        return "/api/shop/delivery/label/" + orderId + "?carrier=" + carrier;
    }
}
