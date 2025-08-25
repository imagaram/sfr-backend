package com.sfr.tokyo.sfr_backend.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 作品情報のデータ転送オブジェクト
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;

    @NotNull(message = "タイトルは必須です")
    @Size(min = 1, max = 100, message = "タイトルは1〜100文字で入力してください")
    private String title;

    @Size(max = 500, message = "説明は最大500文字です")
    private String description;

    @Size(max = 255, message = "ファイルURLは最大255文字です")
    private String fileUrl;

    private UUID userId;
}
