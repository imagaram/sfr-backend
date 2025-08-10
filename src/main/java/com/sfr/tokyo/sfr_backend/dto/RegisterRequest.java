package com.sfr.tokyo.sfr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ユーザー登録リクエストのデータ転送オブジェクト
@Data // getter, setter, toString, equals, hashCodeを自動生成
@Builder // ビルダーパターンを自動生成
@AllArgsConstructor // 全フィールドを引数に持つコンストラクタを自動生成
@NoArgsConstructor // 引数なしのコンストラクタを自動生成
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
}
