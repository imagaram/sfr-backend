package com.sfr.tokyo.sfr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMemberDto {
    private UUID id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @NotNull(message = "任期開始日は必須です")
    private LocalDate termStart;

    @NotNull(message = "任期終了日は必須です")
    private LocalDate termEnd;

    @NotNull(message = "役割は必須です")
    @Size(min = 1, max = 20, message = "役割は1〜20文字で入力してください")
    private String role; // 'MEDIATION', 'PROPOSAL', 'ARBITRATION'

    @Size(max = 255, message = "備考は最大255文字です")
    private String notes;
}
