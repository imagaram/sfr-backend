package com.sfr.tokyo.sfr_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ユーザーログインリクエストのデータを保持するDTO
@Data // Lombokのアノテーションで、getter, setterなどを自動生成
@Builder // Lombokのアノテーションで、ビルダークラスを自動生成
@AllArgsConstructor // Lombokのアノテーションで、全引数コンストラクタを自動生成
@NoArgsConstructor // Lombokのアノテーションで、引数なしコンストラクタを自動生成
public class AuthenticationRequest {

    private String email;
    private String password;
}
