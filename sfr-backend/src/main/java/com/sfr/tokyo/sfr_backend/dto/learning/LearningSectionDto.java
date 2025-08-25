package com.sfr.tokyo.sfr_backend.dto.learning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSectionDto {

    @NotNull(message = "コンテンツIDは必須です")
    private Long contentId;

    @NotBlank(message = "タイトルは必須です")
    @Size(min = 1, max = 255, message = "タイトルは1〜255文字で入力してください")
    private String title;

    @NotNull(message = "表示順序は必須です")
    @Positive(message = "表示順序は正の整数で入力してください")
    private Integer displayOrder;

    @Valid
    private List<LearningMaterialDto> materials;
}
