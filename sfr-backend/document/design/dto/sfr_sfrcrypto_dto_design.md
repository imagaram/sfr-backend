# 🏗️ SFR暗号資産 DTO設計書

**プロジェクト**: SFR.TOKYO 暗号資産システム  
**最終更新日**: 2025年8月19日  
**バージョン**: 1.0  
**対象**: SFR暗号資産API用データ転送オブジェクト

---

## 📋 目次

1. [概要](#概要)
2. [基底クラス・共通DTO](#基底クラス共通dto)
3. [Token Management DTO](#token-management-dto)
4. [Rewards System DTO](#rewards-system-dto)
5. [Collections System DTO](#collections-system-dto)
6. [Governance DTO](#governance-dto)
7. [Statistics DTO](#statistics-dto)
8. [Oracle & Audit DTO](#oracle--audit-dto)
9. [バリデーション設計](#バリデーション設計)
10. [マッピング設計](#マッピング設計)

---

## 🎯 概要

### DTO設計原則
- **型安全性**: BigDecimalによる正確な金額計算
- **バリデーション**: Bean Validationによる入力検証
- **可読性**: 明確な命名・適切なコメント
- **保守性**: 共通基底クラス・再利用可能な構造

### 使用技術
- **Spring Boot 3.x**: REST API フレームワーク
- **Jakarta Validation**: Bean Validation 3.0
- **Jackson**: JSON シリアライゼーション
- **Lombok**: ボイラープレートコード削減
- **MapStruct**: Entity ↔ DTO マッピング

---

## 🏗️ 基底クラス・共通DTO

### BaseResponseDto
```java
package com.sfr.crypto.dto.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * レスポンスDTO基底クラス
 */
@Data
@SuperBuilder
public abstract class BaseResponseDto {
    
    /**
     * 処理タイムスタンプ
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * レスポンスID（トレーサビリティ用）
     */
    private String responseId;
    
    /**
     * 処理ステータス
     */
    private ResponseStatus status;
    
    public enum ResponseStatus {
        SUCCESS, PARTIAL_SUCCESS, WARNING, ERROR
    }
    
    /**
     * デフォルトコンストラクタ（現在時刻設定）
     */
    protected BaseResponseDto() {
        this.timestamp = LocalDateTime.now();
        this.status = ResponseStatus.SUCCESS;
    }
}
```

### ErrorResponseDto
```java
package com.sfr.crypto.dto.base;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * エラーレスポンスDTO
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ErrorResponseDto extends BaseResponseDto {
    
    /**
     * エラーコード
     */
    private String error;
    
    /**
     * エラーメッセージ
     */
    private String message;
    
    /**
     * エラー詳細情報
     */
    private Map<String, Object> details;
    
    /**
     * リクエストパス
     */
    private String path;
    
    /**
     * エラー固有ID
     */
    private String errorId;
}
```

### PaginationDto
```java
package com.sfr.crypto.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * ページネーション情報DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDto {
    
    /**
     * 現在ページ番号（1ベース）
     */
    @Min(value = 1, message = "ページ番号は1以上である必要があります")
    private Integer page;
    
    /**
     * 1ページあたりのアイテム数
     */
    @Min(value = 1, message = "表示件数は1以上である必要があります")
    @Max(value = 100, message = "表示件数は100以下である必要があります")
    private Integer limit;
    
    /**
     * 総ページ数
     */
    private Integer totalPages;
    
    /**
     * 総アイテム数
     */
    private Long totalCount;
    
    /**
     * 次ページ存在フラグ
     */
    private Boolean hasNext;
    
    /**
     * 前ページ存在フラグ
     */
    private Boolean hasPrevious;
}
```

### PagedResponseDto
```java
package com.sfr.crypto.dto.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * ページネーション対応レスポンスDTO
 * @param <T> データ型
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PagedResponseDto<T> extends BaseResponseDto {
    
    /**
     * データリスト
     */
    private List<T> data;
    
    /**
     * ページネーション情報
     */
    private PaginationDto pagination;
}
```

---

## 💰 Token Management DTO

### UserBalanceDto
```java
package com.sfr.crypto.dto.token;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ユーザー残高DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceDto {
    
    /**
     * ユーザーID
     */
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;
    
    /**
     * 現在残高（8桁小数）
     */
    @DecimalMin(value = "0.0", message = "残高は0以上である必要があります")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal currentBalance;
    
    /**
     * 累計獲得額
     */
    @DecimalMin(value = "0.0", message = "累計獲得額は0以上である必要があります")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalEarned;
    
    /**
     * 累計使用額
     */
    @DecimalMin(value = "0.0", message = "累計使用額は0以上である必要があります")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalSpent;
    
    /**
     * 累計徴収額
     */
    @DecimalMin(value = "0.0", message = "累計徴収額は0以上である必要があります")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalCollected;
    
    /**
     * 最終徴収日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastCollectionDate;
    
    /**
     * 徴収免除フラグ
     */
    @NotNull
    private Boolean collectionExempt;
    
    /**
     * 残高凍結フラグ
     */
    @NotNull
    private Boolean frozen;
    
    /**
     * 更新日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
```

### BalanceHistoryDto
```java
package com.sfr.crypto.dto.token;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 残高変動履歴DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceHistoryDto {
    
    /**
     * 履歴ID
     */
    @NotNull
    private String historyId;
    
    /**
     * ユーザーID
     */
    @NotNull
    private UUID userId;
    
    /**
     * トランザクション種別
     */
    @NotNull
    private TransactionType transactionType;
    
    /**
     * 変動金額
     */
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;
    
    /**
     * 変動前残高
     */
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal balanceBefore;
    
    /**
     * 変動後残高
     */
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal balanceAfter;
    
    /**
     * 変動理由
     */
    @NotNull
    @Size(max = 100, message = "変動理由は100文字以内で入力してください")
    private String reason;
    
    /**
     * 関連トランザクションID
     */
    private String referenceId;
    
    /**
     * 作成日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
```

### TransferRequestDto
```java
package com.sfr.crypto.dto.token;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * SFR送金リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    
    /**
     * 送金元ユーザーID
     */
    @NotNull(message = "送金元ユーザーIDは必須です")
    private UUID fromUserId;
    
    /**
     * 送金先ユーザーID
     */
    @NotNull(message = "送金先ユーザーIDは必須です")
    private UUID toUserId;
    
    /**
     * 送金金額
     */
    @NotNull(message = "送金金額は必須です")
    @DecimalMin(value = "0.00000001", message = "送金金額は0.00000001以上である必要があります")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;
    
    /**
     * 送金理由
     */
    @NotNull(message = "送金理由は必須です")
    @Size(max = 100, message = "送金理由は100文字以内で入力してください")
    private String reason;
    
    /**
     * 備考
     */
    @Size(max = 500, message = "備考は500文字以内で入力してください")
    private String note;
}
```

### TransferResponseDto
```java
package com.sfr.crypto.dto.token;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.dto.base.BaseResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SFR送金レスポンスDTO
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TransferResponseDto extends BaseResponseDto {
    
    /**
     * 送金ID
     */
    private String transferId;
    
    /**
     * 送金元ユーザーID
     */
    private UUID fromUserId;
    
    /**
     * 送金先ユーザーID
     */
    private UUID toUserId;
    
    /**
     * 送金金額
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;
    
    /**
     * 送金後の送金元残高
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal fromBalanceAfter;
    
    /**
     * 送金後の送金先残高
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal toBalanceAfter;
    
    /**
     * 処理完了日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
}
```

---

## 🎁 Rewards System DTO

### RewardIssueRequestDto
```java
package com.sfr.crypto.dto.rewards;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 報酬発行リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardIssueRequestDto {
    
    /**
     * ユーザーID
     */
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;
    
    /**
     * 活動スコア（0-100）
     */
    @NotNull(message = "活動スコアは必須です")
    @DecimalMin(value = "0.0", message = "活動スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "活動スコアは100以下である必要があります")
    private Double activityScore;
    
    /**
     * 評価スコア（1.0-5.0）
     */
    @NotNull(message = "評価スコアは必須です")
    @DecimalMin(value = "1.0", message = "評価スコアは1.0以上である必要があります")
    @DecimalMax(value = "5.0", message = "評価スコアは5.0以下である必要があります")
    private Double evaluationScore;
    
    /**
     * 報酬理由
     */
    @NotNull(message = "報酬理由は必須です")
    @Size(max = 200, message = "報酬理由は200文字以内で入力してください")
    private String rewardReason;
    
    /**
     * 強制発行フラグ（プール不足でも発行）
     */
    @Builder.Default
    private Boolean forceIssue = false;
}
```

### RewardIssueResponseDto
```java
package com.sfr.crypto.dto.rewards;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.dto.base.BaseResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 報酬発行レスポンスDTO
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RewardIssueResponseDto extends BaseResponseDto {
    
    /**
     * 報酬ID
     */
    private String rewardId;
    
    /**
     * ユーザーID
     */
    private UUID userId;
    
    /**
     * 発行金額
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal rewardAmount;
    
    /**
     * 対象プール日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate poolDate;
    
    /**
     * 複合スコア（0.6*評価 + 0.4*活動）
     */
    private Double combinedScore;
    
    /**
     * 当日の全体スコア合計
     */
    private Double totalPoolScore;
    
    /**
     * 計算詳細
     */
    private Map<String, Object> calculationDetails;
    
    /**
     * 発行日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime issuedAt;
}
```

### DailyDistributionRequestDto
```java
package com.sfr.crypto.dto.rewards;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 日次報酬分配リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDistributionRequestDto {
    
    /**
     * 対象日
     */
    @NotNull(message = "対象日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
    
    /**
     * ドライランフラグ（実行前テスト）
     */
    @Builder.Default
    private Boolean dryRun = false;
    
    /**
     * 再分配フラグ（既に分配済みでも再実行）
     */
    @Builder.Default
    private Boolean forceRedistribution = false;
}
```

### DailyDistributionResponseDto
```java
package com.sfr.crypto.dto.rewards;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.dto.base.BaseResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 日次報酬分配レスポンスDTO
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DailyDistributionResponseDto extends BaseResponseDto {
    
    /**
     * 対象日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
    
    /**
     * 参加者総数
     */
    private Integer totalParticipants;
    
    /**
     * 分配総額
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalDistributed;
    
    /**
     * 平均報酬額
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal averageReward;
    
    /**
     * 分配詳細リスト
     */
    private List<DistributionDetailDto> distributionDetails;
    
    /**
     * 処理完了日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
    
    /**
     * 分配詳細DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributionDetailDto {
        private UUID userId;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal rewardAmount;
        
        private Double combinedScore;
    }
}
```

---

## 💸 Collections System DTO

### CollectionRequestDto
```java
package com.sfr.crypto.dto.collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * トークン徴収リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRequestDto {
    
    /**
     * ユーザーID
     */
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;
    
    /**
     * 強制徴収フラグ（条件無視）
     */
    @Builder.Default
    private Boolean forceCollection = false;
    
    /**
     * 徴収率（0.0001-1.0）
     */
    @DecimalMin(value = "0.0001", message = "徴収率は0.0001以上である必要があります")
    @DecimalMax(value = "1.0", message = "徴収率は1.0以下である必要があります")
    private Double collectionRate;
    
    /**
     * 徴収理由
     */
    @Size(max = 200, message = "徴収理由は200文字以内で入力してください")
    private String collectionReason;
}
```

### CollectionResponseDto
```java
package com.sfr.crypto.dto.collections;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.dto.base.BaseResponseDto;
import com.sfr.crypto.enums.CollectionDestination;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * トークン徴収レスポンスDTO
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CollectionResponseDto extends BaseResponseDto {
    
    /**
     * 徴収ID
     */
    private String collectionId;
    
    /**
     * ユーザーID
     */
    private UUID userId;
    
    /**
     * 徴収前残高
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal balanceBefore;
    
    /**
     * 徴収金額
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal collectionAmount;
    
    /**
     * 徴収率
     */
    private Double collectionRate;
    
    /**
     * 徴収先（BURN/RESERVE/REDISTRIBUTE）
     */
    private CollectionDestination destination;
    
    /**
     * AI判断ID
     */
    private String aiDecisionId;
    
    /**
     * 処理日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
}
```

### BurnDecisionRequestDto
```java
package com.sfr.crypto.dto.collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * AIバーン判断リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BurnDecisionRequestDto {
    
    /**
     * 判断トリガーソース
     */
    @NotNull(message = "トリガーソースは必須です")
    @Size(max = 100, message = "トリガーソースは100文字以内で入力してください")
    private String triggerSource;
    
    /**
     * 市場データ
     */
    private MarketDataDto marketData;
    
    /**
     * 市場データDTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketDataDto {
        /**
         * 価格
         */
        private Double price;
        
        /**
         * 取引量
         */
        private Double volume;
        
        /**
         * 流動性
         */
        private Double liquidity;
    }
}
```

### BurnDecisionResponseDto
```java
package com.sfr.crypto.dto.collections;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.dto.base.BaseResponseDto;
import com.sfr.crypto.enums.BurnDecisionResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AIバーン判断レスポンスDTO
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BurnDecisionResponseDto extends BaseResponseDto {
    
    /**
     * 判断ID
     */
    private String decisionId;
    
    /**
     * 判断日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate decisionDate;
    
    /**
     * 総流通量
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalCirculation;
    
    /**
     * 総発行量
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalIssued;
    
    /**
     * AI信頼度（0.0-1.0）
     */
    private Double aiConfidence;
    
    /**
     * 判断結果
     */
    private BurnDecisionResult decisionResult;
    
    /**
     * バーン金額
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal burnedAmount;
    
    /**
     * リザーブ金額
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal reservedAmount;
    
    /**
     * AI判断理由
     */
    private String reasoning;
    
    /**
     * トリガー情報
     */
    private String triggeredBy;
    
    /**
     * 作成日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
```

---

## 🗳️ Governance DTO

### CreateProposalRequestDto
```java
package com.sfr.crypto.dto.governance;

import com.sfr.crypto.enums.ProposalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

/**
 * 提案作成リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProposalRequestDto {
    
    /**
     * 提案タイトル
     */
    @NotNull(message = "提案タイトルは必須です")
    @Size(max = 200, message = "提案タイトルは200文字以内で入力してください")
    private String title;
    
    /**
     * 提案詳細
     */
    @NotNull(message = "提案詳細は必須です")
    @Size(max = 10000, message = "提案詳細は10000文字以内で入力してください")
    private String description;
    
    /**
     * 提案種別
     */
    @NotNull(message = "提案種別は必須です")
    private ProposalType proposalType;
    
    /**
     * 投票期間（時間）
     */
    @Min(value = 24, message = "投票期間は24時間以上である必要があります")
    @Max(value = 720, message = "投票期間は720時間以下である必要があります")
    @Builder.Default
    private Integer votingDurationHours = 168; // 1週間
    
    /**
     * 必要定足数
     */
    @Min(value = 1, message = "必要定足数は1以上である必要があります")
    @Builder.Default
    private Integer quorumRequired = 3;
    
    /**
     * 可決閾値（0.5-1.0）
     */
    @DecimalMin(value = "0.5", message = "可決閾値は0.5以上である必要があります")
    @DecimalMax(value = "1.0", message = "可決閾値は1.0以下である必要があります")
    @Builder.Default
    private Double approvalThreshold = 0.6;
}
```

### ProposalDetailDto
```java
package com.sfr.crypto.dto.governance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.enums.ProposalType;
import com.sfr.crypto.enums.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 提案詳細DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalDetailDto {
    
    /**
     * 提案ID
     */
    private UUID proposalId;
    
    /**
     * 提案タイトル
     */
    private String title;
    
    /**
     * 提案詳細
     */
    private String description;
    
    /**
     * 提案種別
     */
    private ProposalType proposalType;
    
    /**
     * 作成者ID
     */
    private UUID createdBy;
    
    /**
     * 提案ステータス
     */
    private ProposalStatus status;
    
    /**
     * 投票開始日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime votingStart;
    
    /**
     * 投票終了日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime votingEnd;
    
    /**
     * 必要定足数
     */
    private Integer quorumRequired;
    
    /**
     * 可決閾値
     */
    private Double approvalThreshold;
    
    /**
     * 総投票数
     */
    private Integer totalVotes;
    
    /**
     * 賛成票数
     */
    private Integer yesVotes;
    
    /**
     * 反対票数
     */
    private Integer noVotes;
    
    /**
     * 棄権票数
     */
    private Integer abstainVotes;
    
    /**
     * 現在の可決率
     */
    private Double currentApprovalRate;
    
    /**
     * 定足数達成フラグ
     */
    private Boolean isQuorumMet;
    
    /**
     * 投票詳細リスト
     */
    private List<VoteDetailDto> votesDetail;
    
    /**
     * 作成日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
```

### VoteRequestDto
```java
package com.sfr.crypto.dto.governance;

import com.sfr.crypto.enums.VoteChoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 投票リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequestDto {
    
    /**
     * 投票選択
     */
    @NotNull(message = "投票選択は必須です")
    private VoteChoice voteChoice;
    
    /**
     * 投票コメント
     */
    @Size(max = 1000, message = "投票コメントは1000文字以内で入力してください")
    private String comment;
}
```

### VoteResponseDto
```java
package com.sfr.crypto.dto.governance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.dto.base.BaseResponseDto;
import com.sfr.crypto.enums.VoteChoice;
import com.sfr.crypto.enums.ProposalStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 投票レスポンスDTO
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class VoteResponseDto extends BaseResponseDto {
    
    /**
     * 投票ID
     */
    private String voteId;
    
    /**
     * 提案ID
     */
    private UUID proposalId;
    
    /**
     * ユーザーID
     */
    private UUID userId;
    
    /**
     * 投票選択
     */
    private VoteChoice voteChoice;
    
    /**
     * 投票権力
     */
    private Double votingPower;
    
    /**
     * 投票重み
     */
    private Double voteWeight;
    
    /**
     * 投票日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime castAt;
    
    /**
     * 投票後の提案ステータス
     */
    private ProposalStatus proposalStatusAfter;
}
```

---

## 📊 Statistics DTO

### StatsOverviewDto
```java
package com.sfr.crypto.dto.statistics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFR統計概要DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsOverviewDto {
    
    /**
     * 総流通量
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalCirculation;
    
    /**
     * 総発行量
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalIssued;
    
    /**
     * 総バーン量
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalBurned;
    
    /**
     * ホルダー総数
     */
    private Integer totalHolders;
    
    /**
     * アクティブホルダー数
     */
    private Integer activeHolders;
    
    /**
     * 平均残高
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal averageBalance;
    
    /**
     * 中央値残高
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal medianBalance;
    
    /**
     * 日次発行量
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal dailyIssuance;
    
    /**
     * 日次徴収量
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal dailyCollection;
    
    /**
     * バーン率
     */
    private Double burnRate;
    
    /**
     * 最終更新日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated;
}
```

### CirculationStatsDto
```java
package com.sfr.crypto.dto.statistics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sfr.crypto.enums.StatsPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 流通量統計DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CirculationStatsDto {
    
    /**
     * 統計期間
     */
    private StatsPeriod period;
    
    /**
     * 統計データリスト
     */
    private List<CirculationDataDto> data;
    
    /**
     * サマリー情報
     */
    private CirculationSummaryDto summary;
    
    /**
     * 流通量データDTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CirculationDataDto {
        
        /**
         * 対象日
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        
        /**
         * 流通量
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal circulation;
        
        /**
         * 発行量
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal issued;
        
        /**
         * バーン量
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal burned;
        
        /**
         * 徴収量
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal collected;
        
        /**
         * ホルダー数
         */
        private Integer holders;
    }
    
    /**
     * 流通量サマリーDTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CirculationSummaryDto {
        
        /**
         * 総変動量
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal totalChange;
        
        /**
         * 平均日次発行量
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal averageDailyIssuance;
        
        /**
         * 平均日次バーン量
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal averageDailyBurn;
        
        /**
         * 成長率
         */
        private Double growthRate;
    }
}
```

---

## 🔧 バリデーション設計

### カスタムバリデーション

#### SFRAmountValidator
```java
package com.sfr.crypto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SFR金額形式バリデーション（8桁小数まで）
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SFRAmountValidatorImpl.class)
public @interface SFRAmount {
    String message() default "SFR金額は8桁小数まで有効です";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    double min() default 0.0;
    double max() default Double.MAX_VALUE;
}
```

#### UserIdExistsValidator
```java
package com.sfr.crypto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ユーザーID存在チェックバリデーション
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserIdExistsValidatorImpl.class)
public @interface UserIdExists {
    String message() default "ユーザーが存在しません";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### バリデーショングループ

#### ValidationGroups
```java
package com.sfr.crypto.validation;

/**
 * バリデーショングループ定義
 */
public class ValidationGroups {
    
    /**
     * 作成時バリデーション
     */
    public interface Create {}
    
    /**
     * 更新時バリデーション
     */
    public interface Update {}
    
    /**
     * 管理者権限バリデーション
     */
    public interface Admin {}
    
    /**
     * 評議員権限バリデーション
     */
    public interface Council {}
}
```

---

## 🔄 マッピング設計

### MapStruct マッパー

#### UserBalanceMapper
```java
package com.sfr.crypto.mapper;

import com.sfr.crypto.dto.token.UserBalanceDto;
import com.sfr.crypto.entity.UserBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * ユーザー残高Entity ↔ DTO マッパー
 */
@Mapper(componentModel = "spring")
public interface UserBalanceMapper {
    
    /**
     * Entity → DTO変換
     */
    UserBalanceDto toDto(UserBalance entity);
    
    /**
     * DTO → Entity変換
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserBalance toEntity(UserBalanceDto dto);
    
    /**
     * DTO → Entity更新（既存Entityの部分更新）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget UserBalance entity, UserBalanceDto dto);
}
```

#### RewardMapper
```java
package com.sfr.crypto.mapper;

import com.sfr.crypto.dto.rewards.RewardIssueResponseDto;
import com.sfr.crypto.entity.RewardHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 報酬Entity ↔ DTO マッパー
 */
@Mapper(componentModel = "spring")
public interface RewardMapper {
    
    /**
     * Entity → レスポンスDTO変換
     */
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "responseId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "SUCCESS")
    @Mapping(target = "issuedAt", source = "createdAt")
    RewardIssueResponseDto toResponseDto(RewardHistory entity);
}
```

---

## 📝 使用例・コントローラー連携

### TokenController例
```java
package com.sfr.crypto.controller;

import com.sfr.crypto.dto.token.UserBalanceDto;
import com.sfr.crypto.dto.token.TransferRequestDto;
import com.sfr.crypto.dto.token.TransferResponseDto;
import com.sfr.crypto.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * SFRトークン管理コントローラー
 */
@RestController
@RequestMapping("/api/v1/sfr")
@RequiredArgsConstructor
@Validated
public class TokenController {
    
    private final TokenService tokenService;
    
    /**
     * ユーザー残高取得
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<UserBalanceDto> getBalance(
            @PathVariable UUID userId) {
        
        UserBalanceDto balance = tokenService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }
    
    /**
     * SFR送金実行
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> transfer(
            @Valid @RequestBody TransferRequestDto request) {
        
        TransferResponseDto response = tokenService.transfer(request);
        return ResponseEntity.ok(response);
    }
}
```

---

*このDTO設計により、SFR暗号資産APIの型安全で保守性の高い実装が可能になります。バリデーション・マッピング・エラーハンドリングが統一された設計で、開発効率と品質を向上させます。*
