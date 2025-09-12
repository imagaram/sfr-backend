package com.sfr.tokyo.sfr_backend.dto.space;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * スペース統計情報DTO
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-09-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceStatisticsDto {
    
    private Long totalSpaces;
    
    private Long activeSpaces;
    
    private Long totalMembers;
    
    private Double averageMembers;
    
    private SpaceModeStatsDto schoolStats;
    
    private SpaceModeStatsDto salonStats;
    
    private SpaceModeStatsDto fanclubStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpaceModeStatsDto {
        private Long count;
        private Long memberCount;
        private Double averagePopularity;
    }
}
