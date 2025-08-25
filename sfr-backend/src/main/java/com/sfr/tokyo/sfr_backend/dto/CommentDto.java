package com.sfr.tokyo.sfr_backend.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Data
@Builder
public class CommentDto {
    private Long id;

    @NotNull(message = "コメント内容は必須です")
    @Size(min = 1, max = 500, message = "コメントは1〜500文字で入力してください")
    private String content;

    @NotNull(message = "投稿IDは必須です")
    private Long postId;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @Size(max = 50, message = "ユーザー名は最大50文字です")
    private String username;

    private java.time.LocalDateTime createdAt;
}
