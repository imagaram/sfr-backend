package tokyo.sfr.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShopItemDto {
    public Long id;
    public String name;
    public String description;
    public BigDecimal price;
    public Integer stock;
    public Long ownerId;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
