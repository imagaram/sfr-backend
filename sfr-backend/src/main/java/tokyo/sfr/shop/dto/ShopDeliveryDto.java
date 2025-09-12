package tokyo.sfr.shop.dto;

import java.time.LocalDateTime;

public class ShopDeliveryDto {
    public Long id;
    public Long orderId;
    public String carrier;
    public String trackingNumber;
    public String labelUrl;
    public String status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
