package com.sfr.tokyo.sfr_backend.dto.space;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * スペースモード設定DTO
 * 
 * @author SFR Development Team
 * @version 2.0
 * @since 2025-09-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceModeConfigDto {

    private Map<String, Object> uiConfig;
    private Map<String, Boolean> featureFlags;
}
