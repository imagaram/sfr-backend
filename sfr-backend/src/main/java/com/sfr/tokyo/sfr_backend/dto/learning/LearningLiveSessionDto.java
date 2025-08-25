package com.sfr.tokyo.sfr_backend.dto.learning;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningLiveSessionDto {

    private Long id;

    @NotNull(message = "オーナーIDは必須です")
    private Long ownerId;

    @NotBlank(message = "タイトルは必須です")
    @Size(min = 1, max = 100, message = "タイトルは1〜100文字で入力してください")
    private String title;

    @NotNull(message = "開催予定日時は必須です")
    @Future(message = "開催予定日時は未来の日時を指定してください")
    private LocalDateTime scheduledAt;

    @NotNull(message = "最大参加者数は必須です")
    @Min(value = 1, message = "最大参加者数は1以上で入力してください")
    private Integer maxParticipants;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
