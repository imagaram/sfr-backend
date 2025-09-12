# SFR/SFRTデュアルトークン実装ロードマップ

## 🎯 実装方針

### 基本原則
```
SFR：サイト内ポイント（前払式支払手段）
SFRT：投資可能トークン（法定通貨交換対応）
実装：段階的リリース・管理画面切り替え対応
運用：省庁許諾前から即座開始可能
```

### 優先度設定
```
Priority 1: SFRサイト内ポイントシステム（即座実装可能）
Priority 2: SFRT報酬システム（法的準備完了後）
Priority 3: SFRT取引所API連携（市場成熟後）
```

## 📋 Phase 1: SFRサイト内ポイント基盤構築（2週間）

### Week 1: コアシステム実装

#### 1.1 SFRポイント管理システム
```java
// SFRポイントエンティティの再定義
@Entity
@Table(name = "sfr_points")
public class SfrPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "balance", nullable = false, precision = 18, scale = 8)
    private BigDecimal balance;
    
    @Column(name = "locked_balance", precision = 18, scale = 8)
    private BigDecimal lockedBalance = BigDecimal.ZERO;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### 1.2 SFR購入システム
```java
// SFR購入トランザクション
@Entity
@Table(name = "sfr_purchase_transactions")
public class SfrPurchaseTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "jpy_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal jpyAmount;
    
    @Column(name = "sfr_amount", nullable = false, precision = 18, scale = 8)
    private BigDecimal sfrAmount;
    
    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 2)
    private BigDecimal exchangeRate = new BigDecimal("150.00"); // 1SFR = 150円固定
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

#### 1.3 SFR決済システム
```java
// SFR商品決済トランザクション
@Entity
@Table(name = "sfr_payment_transactions")
public class SfrPaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "sfr_amount", nullable = false, precision = 18, scale = 8)
    private BigDecimal sfrAmount;
    
    @Column(name = "platform_fee_rate", precision = 5, scale = 4)
    private BigDecimal platformFeeRate = new BigDecimal("0.05"); // 5%
    
    @Column(name = "platform_fee", precision = 18, scale = 8)
    private BigDecimal platformFee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### Week 2: 管理機能・API実装

#### 1.4 管理画面切り替え機能
```java
// システム設定エンティティ
@Entity
@Table(name = "system_settings")
public class SystemSetting {
    @Id
    private String settingKey;
    
    @Column(name = "setting_value", nullable = false)
    private String settingValue;
    
    @Column(name = "description")
    private String description;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 設定キー定数
    public static final String SFR_SYSTEM_ENABLED = "sfr.system.enabled";
    public static final String SFRT_SYSTEM_ENABLED = "sfrt.system.enabled";
    public static final String SFR_EXCHANGE_RATE = "sfr.exchange.rate";
    public static final String PLATFORM_FEE_RATE = "platform.fee.rate";
}
```

#### 1.5 SFR購入API
```java
@RestController
@RequestMapping("/api/v1/sfr")
public class SfrPointController {
    
    @PostMapping("/purchase")
    public ResponseEntity<SfrPurchaseResponse> purchaseSfr(
            @RequestBody @Valid SfrPurchaseRequest request,
            Authentication auth) {
        
        // バリデーション
        if (!systemSettingService.isSfrSystemEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("SFR system is currently disabled"));
        }
        
        // SFR購入処理
        SfrPurchaseResult result = sfrPointService.purchaseSfr(
            getUserId(auth),
            request.getJpyAmount()
        );
        
        return ResponseEntity.ok(new SfrPurchaseResponse(result));
    }
    
    @GetMapping("/balance")
    public ResponseEntity<SfrBalanceResponse> getBalance(Authentication auth) {
        BigDecimal balance = sfrPointService.getBalance(getUserId(auth));
        return ResponseEntity.ok(new SfrBalanceResponse(balance));
    }
}
```

## 📋 Phase 2: SFRT報酬システム構築（3週間）

### Week 3-4: SFRT基盤実装

#### 2.1 SFRTトークンエンティティ
```java
@Entity
@Table(name = "sfrt_tokens")
public class SfrtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "balance", nullable = false, precision = 18, scale = 8)
    private BigDecimal balance;
    
    @Column(name = "total_earned", precision = 18, scale = 8)
    private BigDecimal totalEarned = BigDecimal.ZERO;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### 2.2 SFRT報酬計算システム
```java
@Service
public class SfrtRewardService {
    
    private static final BigDecimal USER_REWARD_RATE = new BigDecimal("0.0125"); // 1.25%
    private static final BigDecimal PLATFORM_REWARD_RATE = new BigDecimal("0.025"); // 2.5%
    
    @Transactional
    public SfrtRewardResult distributeSfrtRewards(SfrPaymentTransaction transaction) {
        
        if (!systemSettingService.isSfrtSystemEnabled()) {
            return SfrtRewardResult.disabled();
        }
        
        BigDecimal transactionAmount = transaction.getSfrAmount();
        BigDecimal userReward = transactionAmount.multiply(USER_REWARD_RATE);
        BigDecimal platformReward = transactionAmount.multiply(PLATFORM_REWARD_RATE);
        
        // 購入者にSFRT付与
        sfrtTokenService.addReward(transaction.getBuyerId(), userReward, 
            RewardType.PURCHASE_REWARD);
            
        // 販売者にSFRT付与
        sfrtTokenService.addReward(transaction.getSellerId(), userReward, 
            RewardType.SALES_REWARD);
            
        // 運営プールにSFRT付与
        sfrtTokenService.addPlatformReward(platformReward);
        
        return SfrtRewardResult.success(userReward, platformReward);
    }
}
```

### Week 5: SFRT管理機能

#### 2.3 SFRT管理API
```java
@RestController
@RequestMapping("/api/v1/sfrt")
public class SfrtTokenController {
    
    @GetMapping("/balance")
    public ResponseEntity<SfrtBalanceResponse> getBalance(Authentication auth) {
        if (!systemSettingService.isSfrtSystemEnabled()) {
            return ResponseEntity.ok(SfrtBalanceResponse.disabled());
        }
        
        SfrtBalance balance = sfrtTokenService.getBalance(getUserId(auth));
        return ResponseEntity.ok(new SfrtBalanceResponse(balance));
    }
    
    @GetMapping("/rewards/history")
    public ResponseEntity<List<SfrtRewardHistory>> getRewardHistory(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<SfrtRewardHistory> history = sfrtTokenService.getRewardHistory(
            getUserId(auth), page, size);
        return ResponseEntity.ok(history);
    }
}
```

## 📋 Phase 3: 統合システム実装（2週間）

### Week 6: 決済統合

#### 3.1 統合決済フロー
```java
@Service
public class IntegratedPaymentService {
    
    @Transactional
    public PaymentResult processSfrPayment(SfrPaymentRequest request) {
        
        // 1. SFR決済処理
        SfrPaymentTransaction transaction = sfrPaymentService.processPayment(request);
        
        // 2. SFRT報酬配布（システム有効時のみ）
        SfrtRewardResult rewardResult = sfrtRewardService.distributeSfrtRewards(transaction);
        
        // 3. 統合レスポンス作成
        return PaymentResult.builder()
            .transactionId(transaction.getId())
            .sfrAmount(transaction.getSfrAmount())
            .platformFee(transaction.getPlatformFee())
            .sfrtRewardEnabled(rewardResult.isEnabled())
            .buyerSfrtReward(rewardResult.getBuyerReward())
            .sellerSfrtReward(rewardResult.getSellerReward())
            .build();
    }
}
```

### Week 7: 管理画面統合

#### 3.2 システム設定管理
```java
@RestController
@RequestMapping("/api/admin/system")
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingController {
    
    @PostMapping("/sfr/toggle")
    public ResponseEntity<SystemToggleResponse> toggleSfrSystem(
            @RequestBody @Valid SystemToggleRequest request) {
        
        systemSettingService.updateSetting(
            SystemSetting.SFR_SYSTEM_ENABLED, 
            request.isEnabled().toString()
        );
        
        return ResponseEntity.ok(new SystemToggleResponse(
            "SFR system " + (request.isEnabled() ? "enabled" : "disabled")
        ));
    }
    
    @PostMapping("/sfrt/toggle")
    public ResponseEntity<SystemToggleResponse> toggleSfrtSystem(
            @RequestBody @Valid SystemToggleRequest request) {
        
        systemSettingService.updateSetting(
            SystemSetting.SFRT_SYSTEM_ENABLED, 
            request.isEnabled().toString()
        );
        
        return ResponseEntity.ok(new SystemToggleResponse(
            "SFRT system " + (request.isEnabled() ? "enabled" : "disabled")
        ));
    }
}
```

## 📋 Phase 4: 取引所API連携準備（将来実装）

### 4.1 SFRT流動性管理（設計のみ）
```java
// 将来実装用の設計
@Service
public class SfrtLiquidityService {
    
    // 複数取引所API統合
    public void manageLiquidity() {
        // Binance, Coinbase, Kraken等のAPI連携
        // 価格監視・自動売買アルゴリズム
        // 流動性提供・価格安定化
    }
}
```

## 🚀 実装開始手順

### Step 1: 既存システムの確認
```bash
# 現在のSFR関連コードの確認
find . -name "*.java" -exec grep -l "SFR\|sfr" {} \;
```

### Step 2: データベーススキーマ更新
```sql
-- SFRポイントテーブル作成
-- SFRTトークンテーブル作成  
-- システム設定テーブル作成
```

### Step 3: 段階的実装
1. SFRポイント基盤（Week 1-2）
2. SFRT報酬システム（Week 3-5）
3. 統合システム（Week 6-7）

## ⚠️ 重要な考慮事項

### 法的コンプライアンス
```
SFR：前払式支払手段として適切な処理
SFRT：将来の法定通貨交換に向けた準備
税務：消費税・所得税の適切な処理
```

### セキュリティ
```
トランザクション整合性の確保
残高操作の厳格な制御
監査ログの完備
```

### パフォーマンス
```
大量取引への対応
リアルタイム残高更新
効率的なデータベース設計
```

このロードマップに従って、段階的に実装を進めてまいります。まず Phase 1 から開始いたしますか？
