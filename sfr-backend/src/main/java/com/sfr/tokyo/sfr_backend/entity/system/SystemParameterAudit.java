package com.sfr.tokyo.sfr_backend.entity.system;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "system_parameter_audit")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemParameterAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_key", length = 120, nullable = false)
    private String paramKey;

    @Column(name = "old_value_string", length = 500)
    private String oldValueString;

    @Column(name = "old_value_number", precision = 20, scale = 10)
    private BigDecimal oldValueNumber;

    @Column(name = "new_value_string", length = 500)
    private String newValueString;

    @Column(name = "new_value_number", precision = 20, scale = 10)
    private BigDecimal newValueNumber;

    @Column(name = "changed_by", length = 120)
    private String changedBy;

    @Column(name = "reason", length = 300)
    private String reason;

    @Column(name = "changed_at", updatable = false)
    private Instant changedAt;

    @PrePersist
    public void onCreate() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }
}
