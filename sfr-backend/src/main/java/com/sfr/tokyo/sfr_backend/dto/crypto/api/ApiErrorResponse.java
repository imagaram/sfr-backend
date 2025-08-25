package com.sfr.tokyo.sfr_backend.dto.crypto.api;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private String error; // エラーコード or シンボリックコード (HTTPステータスを文字列コード化)
    private String message; // エラーメッセージ
    private Object details; // 追加詳細（配列/オブジェクト両対応のためObjectに）
    private LocalDateTime timestamp; // 発生時刻
    private String path; // リクエストパス
}
