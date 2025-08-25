package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningContent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class LearningContentDto {

    @NotNull(message = "学習空間IDは必須です")
    private Long spaceId;

    @NotBlank(message = "タイトルは必須です")
    @Size(min = 1, max = 255, message = "タイトルは1〜255文字で入力してください")
    private String title;

    @NotNull(message = "コンテンツタイプは必須です")
    private LearningContent.ContentType type;

    @Size(max = 65535, message = "URLは65535文字以下で入力してください")
    private String url;

    @Size(max = 65535, message = "説明は65535文字以下で入力してください")
    private String description;

    @Valid
    private List<LearningSectionDto> sections;
}
