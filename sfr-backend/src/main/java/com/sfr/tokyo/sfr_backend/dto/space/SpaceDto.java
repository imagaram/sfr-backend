package com.sfr.tokyo.sfr_backend.dto.space;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * スペース情報DTO
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-09-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceDto {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String mode; // SCHOOL, SALON, FANCLUB
    
    private String status; // ACTIVE, SUSPENDED, ARCHIVED
    
    private Long ownerId;
    
    private String ownerName;
    
    private Integer memberCount;
    
    private Integer maxMembers;
    
    private Boolean isPublic;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastActivity;
    
    private String[] tags;
    
    private Double popularityScore;
}
