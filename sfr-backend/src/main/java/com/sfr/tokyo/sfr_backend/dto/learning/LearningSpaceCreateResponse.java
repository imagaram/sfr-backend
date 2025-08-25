package com.sfr.tokyo.sfr_backend.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSpaceCreateResponse {

    private Long spaceId;
}
