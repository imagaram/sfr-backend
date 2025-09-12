package tokyo.sfr.shop.dto;

import java.time.LocalDateTime;

public class ShopDeliveryTokenDto {
    public Long id;
    public Long deliveryId;
    public String poaToken;
    public LocalDateTime createdAt;
}
