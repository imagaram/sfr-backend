package tokyo.sfr.shop.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import tokyo.sfr.shop.model.ShopItem;
import tokyo.sfr.shop.service.ShopService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/shop/dev")
public class ShopDevController {
    
    private final ShopService shopService;
    
    public ShopDevController(ShopService shopService) {
        this.shopService = shopService;
    }
    
    /**
     * テスト用商品データ生成
     * 開発・デモ用途
     */
    @PostMapping("/seed-data")
    public ResponseEntity<String> seedTestData() {
        List<ShopItem> testItems = Arrays.asList(
            createTestItem(
                "SFR限定Tシャツ", 
                "SFR.TOKYO公式Tシャツ。100%オーガニックコットン使用。", 
                new BigDecimal("25.50"), 
                10
            ),
            createTestItem(
                "デジタルアート作品集", 
                "人気クリエイターによるデジタルアート作品集（NFT）", 
                new BigDecimal("15.00"), 
                50
            ),
            createTestItem(
                "SFRステッカーパック", 
                "防水加工済みステッカー10枚セット", 
                new BigDecimal("5.99"), 
                100
            ),
            createTestItem(
                "限定版アートブック", 
                "SFR.TOKYO年間ベストアート作品集（物理版）", 
                new BigDecimal("45.00"), 
                5
            ),
            createTestItem(
                "音楽アルバム「Future Sounds」", 
                "プラットフォーム参加アーティストによるコラボアルバム", 
                new BigDecimal("12.99"), 
                0
            )
        );
        
        for (ShopItem item : testItems) {
            shopService.addItem(item);
        }
        
        return ResponseEntity.ok("テストデータを5商品生成しました");
    }
    
    /**
     * 全商品削除
     * 開発・テスト用途
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<String> clearAllItems() {
        shopService.clearAllItems();
        return ResponseEntity.ok("全商品を削除しました");
    }
    
    private ShopItem createTestItem(String name, String description, BigDecimal price, int stock) {
        ShopItem item = new ShopItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setStock(stock);
        item.setOwnerId(1L); // テスト用オーナーID
        item.setCreatedAt(LocalDateTime.now());
        return item;
    }
}
