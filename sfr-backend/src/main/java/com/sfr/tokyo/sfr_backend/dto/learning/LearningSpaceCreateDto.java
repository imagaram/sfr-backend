package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSpace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSpaceCreateDto {

    @NotBlank(message = "名前は必須です")
    @Size(min = 1, max = 100, message = "名前は1〜100文字で入力してください")
    private String name;

    @NotNull(message = "モードは必須です")
    private LearningSpace.LearningMode mode;
}
