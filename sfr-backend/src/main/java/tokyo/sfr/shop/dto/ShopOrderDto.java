package tokyo.sfr.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShopOrderDto {
    public Long id;
    public Long itemId;
    public Long buyerId;
    public Integer quantity;
    public BigDecimal totalPrice;
    public String status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
