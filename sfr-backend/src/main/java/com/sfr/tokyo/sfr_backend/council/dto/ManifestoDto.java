package com.sfr.tokyo.sfr_backend.council.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestoDto {
    private Long candidateId;
    private String title;
    private String summary;
    private List<String> details; // policy details
    private List<String> endorsements; // endorsements
    private List<QA> qa; // Q&A list
    private Instant updatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QA {
        private String question;
        private String answer;
    }
}
