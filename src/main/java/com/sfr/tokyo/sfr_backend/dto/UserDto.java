package com.sfr.tokyo.sfr_backend.dto;

import com.sfr.tokyo.sfr_backend.user.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ユーザー基本情報をクライアントに返すためのDTO
@Data // Lombokのアノテーションで、getter, setterなどを自動生成
@Builder // Lombokのアノテーションで、ビルダークラスを自動生成
@NoArgsConstructor // Lombokのアノテーションで、引数なしコンストラクタを自動生成
@AllArgsConstructor // Lombokのアノテーションで、全引数コンストラクタを自動生成
public class UserDto {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private Status status; // ユーザーのステータス
}
