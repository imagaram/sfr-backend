package com.sfr.tokyo.sfr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

// ユーザー登録リクエストのデータ転送オブジェクト
@Data // getter, setter, toString, equals, hashCodeを自動生成
@Builder // ビルダーパターンを自動生成
@AllArgsConstructor // 全フィールドを引数に持つコンストラクタを自動生成
@NoArgsConstructor // 引数なしのコンストラクタを自動生成
public class RegisterRequest {

    @NotNull(message = "姓は必須です")
    @Size(min = 1, max = 50, message = "姓は1〜50文字で入力してください")
    private String firstname;

    @NotNull(message = "名は必須です")
    @Size(min = 1, max = 50, message = "名は1〜50文字で入力してください")
    private String lastname;

    @NotNull(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    @NotNull(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8〜100文字で入力してください")
    private String password;
}
