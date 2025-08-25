package com.sfr.tokyo.sfr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.sfr.tokyo.sfr_backend.entity.CharacterStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterDto {
    private Long id;

    @NotNull(message = "キャラクター名は必須です")
    @Size(min = 1, max = 50, message = "キャラクター名は1〜50文字で入力してください")
    private String name;

    @Size(max = 255, message = "プロフィールは最大255文字です")
    private String profile;

    @Size(max = 255, message = "画像URLは最大255文字です")
    private String imageUrl;

    private Integer lifespanPoints;

    private CharacterStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;
}
