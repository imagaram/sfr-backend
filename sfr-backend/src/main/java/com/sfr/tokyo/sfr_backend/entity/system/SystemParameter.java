package com.sfr.tokyo.sfr_backend.entity.system;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "system_parameters")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemParameter {

    @Id
    @Column(name = "param_key", length = 120)
    private String paramKey;

    @Column(name = "value_string", length = 500)
    private String valueString;

    @Column(name = "value_number", precision = 20, scale = 10)
    private BigDecimal valueNumber;

    @Column(name = "value_json")
    private String valueJson; // Store raw JSON text; application can parse.

    @Column(name = "value_type", length = 20, nullable = false)
    private String valueType; // STRING, NUMBER, DECIMAL, JSON, BOOLEAN

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}
