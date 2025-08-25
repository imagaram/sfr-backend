package com.sfr.tokyo.sfr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// ユーザーのステータス更新リクエストのデータを保持するDTO
@Data // Lombokのアノテーションで、getter, setterなどを自動生成
@Builder // Lombokのアノテーションで、ビルダークラスを自動生成
@NoArgsConstructor // Lombokのアノテーションで、引数なしコンストラクタを自動生成
@AllArgsConstructor // Lombokのアノテーションで、全引数コンストラクタを自動生成
public class UserStatusDto {
    @NotNull(message = "ステータスは必須です")
    @Size(min = 1, max = 20, message = "ステータスは1〜20文字で入力してください")
    private String status;
}
