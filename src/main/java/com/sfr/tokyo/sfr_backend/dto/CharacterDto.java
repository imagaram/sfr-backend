package com.sfr.tokyo.sfr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// キャラクター情報のデータ転送オブジェクト
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterDto {
    private Long id;
    private String name;
    private String profile;
    private String imageUrl;
    private Long userId; // userIdフィールドを追加
}
