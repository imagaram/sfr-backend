package com.sfr.tokyo.sfr_backend.dto.crypto.api;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OpenAPI仕様に合わせた互換用 UserBalance DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiUserBalanceDto {
    @NotBlank
    private String user_id;

    @NotBlank
    private String current_balance; // 8桁小数の文字列

    @NotBlank
    private String total_earned; // 8桁小数の文字列

    @NotBlank
    private String total_spent; // 8桁小数の文字列

    @NotBlank
    private String total_collected; // 8桁小数の文字列

    private LocalDate last_collection_date; // nullable

    @NotNull
    private Boolean collection_exempt;

    @NotNull
    private Boolean frozen;

    @NotNull
    private LocalDateTime updated_at;
}
