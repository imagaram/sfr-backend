package com.sfr.tokyo.sfr_backend.entity.shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 限定アイテムエンティティ
 * SFR決済対応商品の管理
 */
@Entity
@Table(name = "limited_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitedItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "seller_id", nullable = false)
    private String sellerId; // User ID of the seller
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "sfr_price", precision = 15, scale = 8, nullable = false)
    private BigDecimal sfrPrice;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(name = "sales_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer salesCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ItemStatus status = ItemStatus.DRAFT;
    
    @Column(name = "image_urls", columnDefinition = "JSON")
    private List<String> imageUrls;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ビジネスロジック
    
    /**
     * 在庫を減らす
     */
    public boolean decreaseStock(int quantity) {
        if (stockQuantity == null || stockQuantity < quantity) {
            return false;
        }
        stockQuantity -= quantity;
        salesCount += quantity;
        
        if (stockQuantity == 0) {
            status = ItemStatus.SOLD_OUT;
        }
        
        return true;
    }
    
    /**
     * 在庫を補充
     */
    public void addStock(int quantity) {
        if (stockQuantity == null) {
            stockQuantity = quantity;
        } else {
            stockQuantity += quantity;
        }
        
        if (status == ItemStatus.SOLD_OUT) {
            status = ItemStatus.ACTIVE;
        }
    }
    
    /**
     * 販売可能性確認
     */
    public boolean isAvailable() {
        return status == ItemStatus.ACTIVE && 
               stockQuantity != null && 
               stockQuantity > 0;
    }
    
    /**
     * 在庫・購入可能性チェック
     */
    public ItemAvailabilityResult checkAvailability(int requestedQuantity) {
        if (status != ItemStatus.ACTIVE) {
            return ItemAvailabilityResult.failure("商品は現在販売停止中です");
        }
        
        if (stockQuantity == null || stockQuantity < requestedQuantity) {
            return ItemAvailabilityResult.failure("在庫不足です（在庫: " + 
                (stockQuantity != null ? stockQuantity : 0) + "）");
        }
        
        return ItemAvailabilityResult.success();
    }

    // Enum: アイテムステータス
    public enum ItemStatus {
        DRAFT,      // 下書き
        ACTIVE,     // 販売中
        SOLD_OUT,   // 売り切れ
        DISCONTINUED // 販売終了
    }
    
    // 内部クラス: 在庫確認結果
    public static class ItemAvailabilityResult {
        private final boolean available;
        private final String message;
        
        private ItemAvailabilityResult(boolean available, String message) {
            this.available = available;
            this.message = message;
        }
        
        public static ItemAvailabilityResult success() {
            return new ItemAvailabilityResult(true, "購入可能");
        }
        
        public static ItemAvailabilityResult failure(String message) {
            return new ItemAvailabilityResult(false, message);
        }
        
        public boolean isAvailable() { return available; }
        public String getMessage() { return message; }
    }
}