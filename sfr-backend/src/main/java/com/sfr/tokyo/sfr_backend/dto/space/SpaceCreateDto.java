package com.sfr.tokyo.sfr_backend.dto.space;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSpace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * スペース作成DTO（拡張版）
 * 
 * @author SFR Development Team
 * @version 3.0
 * @since 2025-09-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceCreateDto {

    @NotBlank(message = "名前は必須です")
    @Size(min = 1, max = 100, message = "名前は1〜100文字で入力してください")
    private String name;

    @NotNull(message = "モードは必須です")
    private LearningSpace.LearningMode mode;
    
    // 新規フィールド
    private String description;
    
    private Long ownerId;
    
    private Integer maxMembers;
    
    private Boolean isPublic;
}
