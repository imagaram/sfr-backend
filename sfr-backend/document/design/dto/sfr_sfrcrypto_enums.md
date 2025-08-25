# 🔢 SFR暗号資産 Enum定義

**プロジェクト**: SFR.TOKYO 暗号資産システム  
**最終更新日**: 2025年8月19日  
**バージョン**: 1.0  
**対象**: SFR暗号資産システム用列挙型定義

---

## 📋 Enum定義一覧

### TransactionType - トランザクション種別
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * トランザクション種別
 */
@Getter
@RequiredArgsConstructor
public enum TransactionType {
    
    /**
     * 報酬獲得
     */
    EARN("報酬獲得", "earning"),
    
    /**
     * 使用・支払い
     */
    SPEND("使用・支払い", "spending"),
    
    /**
     * 徴収
     */
    COLLECT("徴収", "collection"),
    
    /**
     * バーン（焼却）
     */
    BURN("バーン", "burning"),
    
    /**
     * 送金・転送
     */
    TRANSFER("送金・転送", "transfer");
    
    private final String displayName;
    private final String code;
    
    /**
     * コードから列挙型を取得
     */
    public static TransactionType fromCode(String code) {
        for (TransactionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type code: " + code);
    }
}
```

### CollectionDestination - 徴収先
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 徴収先区分
 */
@Getter
@RequiredArgsConstructor
public enum CollectionDestination {
    
    /**
     * バーン（焼却）
     */
    BURN("バーン", "burn", "徴収トークンを永続的に除去"),
    
    /**
     * リザーブ（準備金）
     */
    RESERVE("リザーブ", "reserve", "システム準備金として保管"),
    
    /**
     * 再分配
     */
    REDISTRIBUTE("再分配", "redistribute", "他ユーザーへの報酬として再分配");
    
    private final String displayName;
    private final String code;
    private final String description;
}
```

### BurnDecisionResult - AIバーン判断結果
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AIバーン判断結果
 */
@Getter
@RequiredArgsConstructor
public enum BurnDecisionResult {
    
    /**
     * バーン実行
     */
    BURN("バーン実行", "burn", "トークンを焼却して総供給量を減少"),
    
    /**
     * リザーブ保管
     */
    RESERVE("リザーブ保管", "reserve", "準備金として保管し将来利用");
    
    private final String displayName;
    private final String code;
    private final String description;
}
```

### ProposalType - 提案種別
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ガバナンス提案種別
 */
@Getter
@RequiredArgsConstructor
public enum ProposalType {
    
    /**
     * ポリシー提案
     */
    POLICY("ポリシー提案", "policy", "システム運営方針・ルールの変更"),
    
    /**
     * パラメータ提案
     */
    PARAMETER("パラメータ提案", "parameter", "システムパラメータの調整"),
    
    /**
     * 機能提案
     */
    FEATURE("機能提案", "feature", "新機能の追加・既存機能の変更"),
    
    /**
     * ガバナンス提案
     */
    GOVERNANCE("ガバナンス提案", "governance", "ガバナンス体制・評議員制度の変更");
    
    private final String displayName;
    private final String code;
    private final String description;
}
```

### ProposalStatus - 提案ステータス
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 提案ステータス
 */
@Getter
@RequiredArgsConstructor
public enum ProposalStatus {
    
    /**
     * 下書き
     */
    DRAFT("下書き", "draft", "作成中・未公開状態"),
    
    /**
     * 投票中
     */
    VOTING("投票中", "voting", "投票期間中・アクティブ状態"),
    
    /**
     * 可決
     */
    PASSED("可決", "passed", "投票により承認・実行予定"),
    
    /**
     * 否決
     */
    REJECTED("否決", "rejected", "投票により否決・実行されない"),
    
    /**
     * 期限切れ
     */
    EXPIRED("期限切れ", "expired", "投票期間終了・定足数不足");
    
    private final String displayName;
    private final String code;
    private final String description;
    
    /**
     * アクティブな提案かどうか
     */
    public boolean isActive() {
        return this == DRAFT || this == VOTING;
    }
    
    /**
     * 終了した提案かどうか
     */
    public boolean isFinished() {
        return this == PASSED || this == REJECTED || this == EXPIRED;
    }
}
```

### VoteChoice - 投票選択
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 投票選択
 */
@Getter
@RequiredArgsConstructor
public enum VoteChoice {
    
    /**
     * 賛成
     */
    YES("賛成", "yes", 1),
    
    /**
     * 反対
     */
    NO("反対", "no", -1),
    
    /**
     * 棄権
     */
    ABSTAIN("棄権", "abstain", 0);
    
    private final String displayName;
    private final String code;
    private final int weight;
    
    /**
     * 賛成票かどうか
     */
    public boolean isApproval() {
        return this == YES;
    }
    
    /**
     * 反対票かどうか
     */
    public boolean isRejection() {
        return this == NO;
    }
    
    /**
     * 有効票かどうか（棄権以外）
     */
    public boolean isValidVote() {
        return this != ABSTAIN;
    }
}
```

### CouncilStatus - 評議員ステータス
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 評議員ステータス
 */
@Getter
@RequiredArgsConstructor
public enum CouncilStatus {
    
    /**
     * 活動中
     */
    ACTIVE("活動中", "active", "任期中・投票権限あり"),
    
    /**
     * 任期満了
     */
    COMPLETED("任期満了", "completed", "正常に任期を完了"),
    
    /**
     * 辞任
     */
    RESIGNED("辞任", "resigned", "任期途中での自主的な辞任"),
    
    /**
     * 罷免
     */
    REMOVED("罷免", "removed", "不適切な行為により解任");
    
    private final String displayName;
    private final String code;
    private final String description;
    
    /**
     * 投票権限があるかどうか
     */
    public boolean hasVotingRights() {
        return this == ACTIVE;
    }
}
```

### StatsPeriod - 統計期間
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 統計集計期間
 */
@Getter
@RequiredArgsConstructor
public enum StatsPeriod {
    
    /**
     * 日次
     */
    DAILY("日次", "daily", 1),
    
    /**
     * 週次
     */
    WEEKLY("週次", "weekly", 7),
    
    /**
     * 月次
     */
    MONTHLY("月次", "monthly", 30);
    
    private final String displayName;
    private final String code;
    private final int days;
}
```

### OracleDataType - Oracle データ種別
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Oracle外部データ種別
 */
@Getter
@RequiredArgsConstructor
public enum OracleDataType {
    
    /**
     * 価格データ
     */
    PRICE("価格", "price", "トークン市場価格"),
    
    /**
     * 取引量データ
     */
    VOLUME("取引量", "volume", "24時間取引量"),
    
    /**
     * 流動性データ
     */
    LIQUIDITY("流動性", "liquidity", "DEX流動性プール情報"),
    
    /**
     * レートデータ
     */
    RATE("レート", "rate", "各種レート・比率情報");
    
    private final String displayName;
    private final String code;
    private final String description;
}
```

### ParameterType - システムパラメータ種別
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * システムパラメータ種別
 */
@Getter
@RequiredArgsConstructor
public enum ParameterType {
    
    /**
     * 文字列型
     */
    STRING("文字列", "string", String.class),
    
    /**
     * 数値型
     */
    NUMBER("数値", "number", Double.class),
    
    /**
     * 真偽値型
     */
    BOOLEAN("真偽値", "boolean", Boolean.class),
    
    /**
     * JSON型
     */
    JSON("JSON", "json", Object.class);
    
    private final String displayName;
    private final String code;
    private final Class<?> javaType;
    
    /**
     * 文字列値を適切な型に変換
     */
    @SuppressWarnings("unchecked")
    public <T> T parseValue(String value) {
        switch (this) {
            case STRING:
                return (T) value;
            case NUMBER:
                return (T) Double.valueOf(value);
            case BOOLEAN:
                return (T) Boolean.valueOf(value);
            case JSON:
                // JSON解析は別途実装が必要
                return (T) value;
            default:
                throw new IllegalArgumentException("Unsupported parameter type: " + this);
        }
    }
}
```

### TriggerType - 調整トリガー種別
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * パラメータ調整トリガー種別
 */
@Getter
@RequiredArgsConstructor
public enum TriggerType {
    
    /**
     * 手動調整
     */
    MANUAL("手動調整", "manual", "管理者による手動操作"),
    
    /**
     * AI自動調整
     */
    AUTO_AI("AI自動調整", "auto_ai", "AIシステムによる自動判断"),
    
    /**
     * Oracle連動調整
     */
    ORACLE("Oracle連動", "oracle", "外部データに基づく自動調整"),
    
    /**
     * ガバナンス決定
     */
    GOVERNANCE("ガバナンス決定", "governance", "評議員投票による決定");
    
    private final String displayName;
    private final String code;
    private final String description;
    
    /**
     * 自動調整かどうか
     */
    public boolean isAutomatic() {
        return this == AUTO_AI || this == ORACLE;
    }
    
    /**
     * 人的判断が関与するかどうか
     */
    public boolean isHumanInvolved() {
        return this == MANUAL || this == GOVERNANCE;
    }
}
```

### EvaluationType - 評価種別
```java
package com.sfr.crypto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ユーザー評価種別
 */
@Getter
@RequiredArgsConstructor
public enum EvaluationType {
    
    /**
     * 学習活動評価
     */
    LEARNING("学習活動", "learning", "教育コンテンツへの参加・成果"),
    
    /**
     * 創作活動評価
     */
    CREATION("創作活動", "creation", "作品投稿・創造的な貢献"),
    
    /**
     * コミュニティ貢献評価
     */
    CONTRIBUTION("コミュニティ貢献", "contribution", "コミュニティ活動・支援"),
    
    /**
     * 一般評価
     */
    GENERAL("一般評価", "general", "総合的な活動評価");
    
    private final String displayName;
    private final String code;
    private final String description;
}
```

---

## 🔧 Enum拡張ユーティリティ

### EnumUtils - 共通ユーティリティ
```java
package com.sfr.crypto.utils;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum操作ユーティリティクラス
 */
public class EnumUtils {
    
    /**
     * コードから列挙型を検索（汎用メソッド）
     */
    public static <T extends Enum<T>> Optional<T> findByCode(
            Class<T> enumClass, String code) {
        
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> {
                    try {
                        String enumCode = (String) e.getClass()
                                .getMethod("getCode")
                                .invoke(e);
                        return enumCode.equals(code);
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .findFirst();
    }
    
    /**
     * 表示名から列挙型を検索
     */
    public static <T extends Enum<T>> Optional<T> findByDisplayName(
            Class<T> enumClass, String displayName) {
        
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> {
                    try {
                        String enumDisplayName = (String) e.getClass()
                                .getMethod("getDisplayName")
                                .invoke(e);
                        return enumDisplayName.equals(displayName);
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .findFirst();
    }
    
    /**
     * 列挙型の説明文を取得
     */
    public static <T extends Enum<T>> String getDescription(T enumValue) {
        try {
            return (String) enumValue.getClass()
                    .getMethod("getDescription")
                    .invoke(enumValue);
        } catch (Exception ex) {
            return enumValue.name();
        }
    }
}
```

### Jackson カスタムシリアライザー
```java
package com.sfr.crypto.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.sfr.crypto.enums.*;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Enum用Jacksonシリアライザー（コード値でシリアライズ）
 */
public class EnumCodeSerializer extends JsonSerializer<Enum<?>> {
    
    @Override
    public void serialize(Enum<?> value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        
        try {
            Method getCodeMethod = value.getClass().getMethod("getCode");
            String code = (String) getCodeMethod.invoke(value);
            gen.writeString(code);
        } catch (Exception e) {
            // フォールバック: enum名を使用
            gen.writeString(value.name());
        }
    }
}
```

---

## 📝 使用例

### Controller での Enum 利用
```java
@GetMapping("/balance/history")
public ResponseEntity<PagedResponseDto<BalanceHistoryDto>> getBalanceHistory(
        @PathVariable UUID userId,
        @RequestParam(required = false) TransactionType transactionType,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int limit) {
    
    // TransactionType enumが自動的にバインドされる
    PagedResponseDto<BalanceHistoryDto> history = 
            tokenService.getBalanceHistory(userId, transactionType, fromDate, toDate, page, limit);
    
    return ResponseEntity.ok(history);
}
```

### Service での Enum 活用
```java
@Service
public class CollectionService {
    
    public CollectionResponseDto executeCollection(CollectionRequestDto request) {
        // AI判断結果に基づいて徴収先を決定
        BurnDecisionResult aiDecision = burnDecisionService.getLatestDecision();
        
        CollectionDestination destination = switch (aiDecision) {
            case BURN -> CollectionDestination.BURN;
            case RESERVE -> CollectionDestination.RESERVE;
        };
        
        // 徴収処理実行
        return processCollection(request, destination);
    }
}
```

---

*この Enum 設計により、SFR暗号資産システムの状態管理・分類・フロー制御が型安全かつ可読性の高い形で実装できます。*
