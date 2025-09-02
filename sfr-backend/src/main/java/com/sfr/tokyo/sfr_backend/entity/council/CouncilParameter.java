package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 評議員制度のパラメーター管理エンティティ
 * 制度運営に必要な各種パラメーターの定義と管理を行う
 */
@Entity
@Table(name = "council_parameters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * パラメーターキー（一意識別子）
     */
    @Column(name = "param_key", length = 120, nullable = false, unique = true)
    @NotBlank
    @Size(max = 120)
    private String paramKey;

    /**
     * パラメーター説明
     */
    @Column(name = "description", length = 300)
    @Size(max = 300)
    private String description;

    /**
     * 値の型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 20, nullable = false)
    @NotNull
    private ValueType valueType;

    /**
     * 文字列値
     */
    @Column(name = "value_string", length = 500)
    @Size(max = 500)
    private String valueString;

    /**
     * 数値
     */
    @Column(name = "value_number", precision = 20, scale = 10)
    private BigDecimal valueNumber;

    /**
     * JSON形式データ
     */
    @Column(name = "value_json", length = 255)
    @Size(max = 255)
    private String valueJson;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 値の型定義
     */
    public enum ValueType {
        STRING,
        INTEGER,
        DECIMAL,
        PERCENTAGE,
        BOOLEAN,
        JSON,
        DATETIME,
        DURATION
    }

    /**
     * 型に応じた値の取得
     */
    public Object getValue() {
        return switch (valueType) {
            case STRING -> valueString;
            case INTEGER -> valueNumber != null ? valueNumber.intValue() : null;
            case DECIMAL, PERCENTAGE -> valueNumber;
            case BOOLEAN -> valueString != null ? Boolean.parseBoolean(valueString) : null;
            case JSON -> valueJson;
            case DATETIME -> valueString != null ? Instant.parse(valueString) : null;
            case DURATION -> valueString;
        };
    }

    /**
     * 型に応じた値の設定
     */
    public void setValue(Object value) {
        if (value == null) {
            valueString = null;
            valueNumber = null;
            valueJson = null;
            return;
        }

        switch (valueType) {
            case STRING, DATETIME, DURATION -> valueString = value.toString();
            case INTEGER -> {
                if (value instanceof Number number) {
                    valueNumber = BigDecimal.valueOf(number.longValue());
                } else {
                    valueNumber = new BigDecimal(value.toString());
                }
            }
            case DECIMAL, PERCENTAGE -> {
                if (value instanceof BigDecimal decimal) {
                    valueNumber = decimal;
                } else if (value instanceof Number number) {
                    valueNumber = BigDecimal.valueOf(number.doubleValue());
                } else {
                    valueNumber = new BigDecimal(value.toString());
                }
            }
            case BOOLEAN -> valueString = value.toString();
            case JSON -> valueJson = value.toString();
        }
    }
}
