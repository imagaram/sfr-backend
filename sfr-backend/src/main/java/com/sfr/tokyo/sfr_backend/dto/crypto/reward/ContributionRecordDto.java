package com.sfr.tokyo.sfr_backend.dto.crypto.reward;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.tokyo.sfr_backend.entity.crypto.reward.ContributionRecord;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 貢献記録DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionRecordDto {

    private Long id;
    private UUID userId;
    private ContributionRecord.ContributionType contributionType;
    private String activityType;
    private String referenceId;
    private String referenceType;
    private Map<String, Object> metrics;
    private BigDecimal contributionScore;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime activityDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 貢献記録作成リクエストDTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private ContributionRecord.ContributionType contributionType;
        private String activityType;
        private String referenceId;
        private String referenceType;
        private Map<String, Object> metrics;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime activityDate;
    }

    /**
     * 貢献記録検索条件DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchCriteria {
        private UUID userId;
        private ContributionRecord.ContributionType contributionType;
        private String activityType;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime fromDate;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime toDate;

        @Builder.Default
        private int page = 0;
        @Builder.Default
        private int size = 20;
    }
}
