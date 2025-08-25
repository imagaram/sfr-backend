package com.sfr.tokyo.sfr_backend.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningModeConfigDto {

    private Map<String, Object> uiConfig;
    private Map<String, Boolean> featureFlags;
}
