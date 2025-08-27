package com.sfr.tokyo.sfr_backend.council.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegisterCandidateRequest {
    @NotNull
    private UUID userId; // 将来: 認証ユーザーから取得し Body 省略想定
}
