package com.sfr.tokyo.sfr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

// ユーザーログインリクエストのデータを保持するDTO
@Data // Lombokのアノテーションで、getter, setterなどを自動生成
@Builder // Lombokのアノテーションで、ビルダークラスを自動生成
@AllArgsConstructor // Lombokのアノテーションで、全引数コンストラクタを自動生成
@NoArgsConstructor // Lombokのアノテーションで、引数なしコンストラクタを自動生成
public class AuthenticationRequest {

    @NotNull(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    @NotNull(message = "パスワードは必須です")
    private String password;
}
