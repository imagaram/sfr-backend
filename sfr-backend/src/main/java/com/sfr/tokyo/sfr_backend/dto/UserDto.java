package com.sfr.tokyo.sfr_backend.dto;

import com.sfr.tokyo.sfr_backend.user.Role;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID id;

    @NotNull(message = "姓は必須です")
    @Size(min = 1, max = 50, message = "姓は1〜50文字で入力してください")
    private String firstname;

    @NotNull(message = "名は必須です")
    @Size(min = 1, max = 50, message = "名は1〜50文字で入力してください")
    private String lastname;

    @NotNull(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    @NotNull(message = "役割は必須です")
    private Role role;

    private boolean idVerified;
    private boolean myNumberVerified;
}
