package com.sfr.tokyo.sfr_backend.council.dto;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilParameter;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 評議員制度パラメーターDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilParameterDto {

    private Long id;

    @NotBlank
    @Size(max = 120)
    private String paramKey;

    @Size(max = 300)
    private String description;

    @NotNull
    private CouncilParameter.ValueType valueType;

    @Size(max = 500)
    private String valueString;

    private BigDecimal valueNumber;

    @Size(max = 255)
    private String valueJson;

    private Instant createdAt;

    private Instant updatedAt;

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
