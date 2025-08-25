package com.sfr.tokyo.sfr_backend.dto.learning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningMaterialDto {

    @NotNull(message = "セクションIDは必須です")
    private Long sectionId;

    @NotBlank(message = "テキストは必須です")
    @Size(max = 65535, message = "テキストは65535文字以下で入力してください")
    private String text;

    @Size(max = 65535, message = "メディアURLは65535文字以下で入力してください")
    private String mediaUrl;

    @NotNull(message = "表示順序は必須です")
    @Positive(message = "表示順序は正の整数で入力してください")
    private Integer displayOrder;
}
