package tokyo.sfr.shop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_delivery_tokens")
public class ShopDeliveryToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "delivery_id", nullable = false)
    private Long deliveryId;
    
    @Column(name = "poa_token", nullable = false)
    private String poaToken;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }
    public String getPoaToken() { return poaToken; }
    public void setPoaToken(String poaToken) { this.poaToken = poaToken; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
