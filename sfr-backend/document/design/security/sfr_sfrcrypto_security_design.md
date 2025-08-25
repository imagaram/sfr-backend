# 🔐 SFR暗号資産 セキュリティ設計書

**プロジェクト**: SFR.TOKYO 暗号資産システム  
**最終更新日**: 2025年8月19日  
**バージョン**: 1.0  
**対象**: SFR暗号資産システムのセキュリティアーキテクチャ

---

## 📋 目次

1. [概要](#概要)
2. [認証・認可](#認証認可)
3. [API セキュリティ](#api-セキュリティ)
4. [データ保護](#データ保護)
5. [トランザクション整合性](#トランザクション整合性)
6. [監査・ログ](#監査ログ)
7. [脅威対策](#脅威対策)
8. [インフラセキュリティ](#インフラセキュリティ)
9. [セキュリティ運用](#セキュリティ運用)

---

## 🎯 概要

### セキュリティ方針
- **ゼロトラスト**: 全てのアクセスを検証・認可
- **多層防御**: 複数のセキュリティレイヤーによる保護
- **最小権限の原則**: 必要最小限の権限のみ付与
- **透明性**: 全操作の監査・トレーサビリティ確保

### セキュリティ要件
- 🔐 **認証**: 強力な多要素認証
- 🛡️ **認可**: ロールベースアクセス制御
- 🔒 **暗号化**: データの暗号化保護
- 📊 **監査**: 完全な操作ログ記録
- 🚨 **検知**: リアルタイム脅威検知

---

## 🔑 認証・認可

### JWT認証システム

#### JWT トークン構造
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "sfr-key-2025"
  },
  "payload": {
    "sub": "user-uuid",
    "iss": "sfr.tokyo",
    "aud": "sfr-crypto-api",
    "exp": 1640995200,
    "iat": 1640991600,
    "roles": ["USER", "COUNCIL_MEMBER"],
    "permissions": [
      "SFR_BALANCE_READ",
      "SFR_TRANSFER",
      "GOVERNANCE_VOTE"
    ],
    "session_id": "session-uuid",
    "device_id": "device-fingerprint",
    "ip_whitelist": ["192.168.1.100"],
    "mfa_verified": true
  }
}
```

#### 認証フロー実装
```java
@Component
@Slf4j
public class JWTAuthenticationProvider {
    
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * JWTトークン生成
     */
    public String generateToken(UserPrincipal user, AuthenticationContext context) {
        return JWT.create()
            .withIssuer("sfr.tokyo")
            .withAudience("sfr-crypto-api")
            .withSubject(user.getUserId().toString())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
            .withClaim("roles", user.getRoles())
            .withClaim("permissions", user.getPermissions())
            .withClaim("session_id", context.getSessionId())
            .withClaim("device_id", context.getDeviceFingerprint())
            .withClaim("mfa_verified", context.isMfaVerified())
            .sign(Algorithm.RSA256(publicKey, privateKey));
    }
    
    /**
     * トークン検証・デコード
     */
    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        // Redis でブラックリスト確認
        if (redisTemplate.hasKey("blacklist:token:" + DigestUtils.sha256Hex(token))) {
            throw new JWTVerificationException("Token is blacklisted");
        }
        
        JWTVerifier verifier = JWT.require(Algorithm.RSA256(publicKey, privateKey))
            .withIssuer("sfr.tokyo")
            .withAudience("sfr-crypto-api")
            .build();
            
        return verifier.verify(token);
    }
    
    /**
     * トークン無効化（ログアウト時）
     */
    public void invalidateToken(String token) {
        String tokenHash = DigestUtils.sha256Hex(token);
        // Redis で有効期限まで保持
        redisTemplate.opsForValue().set(
            "blacklist:token:" + tokenHash, 
            true, 
            Duration.ofMinutes(15)
        );
    }
}
```

### 多要素認証 (MFA)

#### TOTP認証実装
```java
@Service
public class MFAService {
    
    private final GoogleAuthenticator googleAuth = new GoogleAuthenticator();
    
    /**
     * MFA設定（QRコード生成）
     */
    public MFASetupDto setupMFA(UUID userId) {
        GoogleAuthenticatorKey key = googleAuth.generateCredentials();
        
        // 秘密鍵を暗号化してDB保存
        String encryptedSecret = encryptionService.encrypt(key.getKey());
        userMFARepository.saveMFASecret(userId, encryptedSecret);
        
        // QRコード用URL生成
        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
            "SFR.TOKYO",
            userId.toString(),
            key
        );
        
        return MFASetupDto.builder()
            .secretKey(key.getKey())
            .qrCodeUrl(qrCodeUrl)
            .backupCodes(generateBackupCodes())
            .build();
    }
    
    /**
     * TOTP検証
     */
    public boolean verifyMFA(UUID userId, int code) {
        String encryptedSecret = userMFARepository.getMFASecret(userId);
        String secret = encryptionService.decrypt(encryptedSecret);
        
        return googleAuth.authorize(secret, code);
    }
    
    /**
     * バックアップコード生成
     */
    private List<String> generateBackupCodes() {
        return IntStream.range(0, 10)
            .mapToObj(i -> RandomStringUtils.randomAlphanumeric(8).toUpperCase())
            .collect(Collectors.toList());
    }
}
```

### ロールベースアクセス制御 (RBAC)

#### 権限定義
```java
public enum SFRPermission {
    // トークン操作権限
    SFR_BALANCE_READ("SFR残高照会"),
    SFR_TRANSFER("SFR送金"),
    SFR_TRANSFER_RECEIVE("SFR受金"),
    
    // 報酬システム権限
    REWARD_ISSUE("報酬発行"),
    REWARD_CALCULATE("報酬計算"),
    REWARD_DISTRIBUTE("一括分配"),
    
    // 徴収システム権限
    COLLECTION_EXECUTE("徴収実行"),
    COLLECTION_VIEW_HISTORY("徴収履歴閲覧"),
    BURN_DECISION("バーン判断"),
    
    // ガバナンス権限
    PROPOSAL_CREATE("提案作成"),
    PROPOSAL_VIEW("提案閲覧"),
    GOVERNANCE_VOTE("投票権"),
    COUNCIL_MEMBER("評議員権限"),
    
    // 統計・監査権限
    STATS_VIEW("統計閲覧"),
    AUDIT_VIEW("監査ログ閲覧"),
    SYSTEM_PARAMETER_UPDATE("システムパラメータ更新"),
    
    // 管理者権限
    USER_MANAGEMENT("ユーザー管理"),
    SYSTEM_MAINTENANCE("システムメンテナンス"),
    EMERGENCY_STOP("緊急停止");
    
    private final String description;
}

@Entity
@Table(name = "roles")
public class Role {
    @Id
    private String roleName;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<SFRPermission> permissions;
    
    private String description;
    private boolean active;
}
```

#### 権限チェック実装
```java
@Component
public class SecurityService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 権限チェック
     */
    public boolean hasPermission(UUID userId, SFRPermission permission) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
            
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(p -> p.equals(permission));
    }
    
    /**
     * 複数権限の AND チェック
     */
    public boolean hasAllPermissions(UUID userId, SFRPermission... permissions) {
        return Arrays.stream(permissions)
            .allMatch(permission -> hasPermission(userId, permission));
    }
    
    /**
     * 複数権限の OR チェック
     */
    public boolean hasAnyPermission(UUID userId, SFRPermission... permissions) {
        return Arrays.stream(permissions)
            .anyMatch(permission -> hasPermission(userId, permission));
    }
}
```

### メソッドレベルセキュリティ

#### アノテーション定義
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    SFRPermission[] value();
    PermissionLogic logic() default PermissionLogic.AND;
    
    enum PermissionLogic {
        AND, OR
    }
}

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOwnership {
    String userIdParam() default "userId";
    boolean allowAdmin() default true;
}
```

#### セキュリティアスペクト
```java
@Aspect
@Component
@Slf4j
public class SecurityAspect {
    
    @Autowired
    private SecurityService securityService;
    
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) 
            throws Throwable {
        
        UUID currentUserId = getCurrentUserId();
        SFRPermission[] permissions = requirePermission.value();
        
        boolean hasAccess = switch (requirePermission.logic()) {
            case AND -> securityService.hasAllPermissions(currentUserId, permissions);
            case OR -> securityService.hasAnyPermission(currentUserId, permissions);
        };
        
        if (!hasAccess) {
            log.warn("Access denied for user {} to method {}", 
                currentUserId, joinPoint.getSignature().getName());
            throw new AccessDeniedException("Insufficient permissions");
        }
        
        return joinPoint.proceed();
    }
    
    @Around("@annotation(requireOwnership)")
    public Object checkOwnership(ProceedingJoinPoint joinPoint, RequireOwnership requireOwnership) 
            throws Throwable {
        
        UUID currentUserId = getCurrentUserId();
        UUID targetUserId = extractUserIdFromArgs(joinPoint.getArgs(), requireOwnership.userIdParam());
        
        // 管理者は所有者チェックをバイパス
        if (requireOwnership.allowAdmin() && 
            securityService.hasPermission(currentUserId, SFRPermission.USER_MANAGEMENT)) {
            return joinPoint.proceed();
        }
        
        if (!currentUserId.equals(targetUserId)) {
            log.warn("Ownership violation: user {} tried to access data of user {}", 
                currentUserId, targetUserId);
            throw new AccessDeniedException("Access denied: not resource owner");
        }
        
        return joinPoint.proceed();
    }
}
```

---

## 🛡️ API セキュリティ

### レート制限

#### Redis ベースレート制限
```java
@Component
public class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * レート制限チェック（トークンバケットアルゴリズム）
     */
    public boolean isAllowed(String clientId, String endpoint, RateLimitConfig config) {
        String key = String.format("rate_limit:%s:%s", clientId, endpoint);
        
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            String luaScript = """
                local key = KEYS[1]
                local capacity = tonumber(ARGV[1])
                local tokens = tonumber(ARGV[2])
                local interval = tonumber(ARGV[3])
                local now = tonumber(ARGV[4])
                
                local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
                local current_tokens = tonumber(bucket[1]) or capacity
                local last_refill = tonumber(bucket[2]) or now
                
                -- トークン補充計算
                local time_passed = now - last_refill
                local tokens_to_add = math.floor(time_passed / interval * tokens)
                current_tokens = math.min(capacity, current_tokens + tokens_to_add)
                
                if current_tokens >= 1 then
                    current_tokens = current_tokens - 1
                    redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', now)
                    redis.call('EXPIRE', key, interval * 2)
                    return 1
                else
                    return 0
                end
            """;
            
            List<String> keys = List.of(key);
            List<String> args = List.of(
                String.valueOf(config.getCapacity()),
                String.valueOf(config.getRefillRate()),
                String.valueOf(config.getIntervalSeconds()),
                String.valueOf(System.currentTimeMillis() / 1000)
            );
            
            Long result = (Long) connection.eval(luaScript.getBytes(), 
                ReturnType.INTEGER, keys.size(), 
                keys.toArray(new String[0]), args.toArray(new String[0]));
                
            return result == 1L;
        });
    }
}

@Data
@Builder
public class RateLimitConfig {
    private int capacity;      // バケット容量
    private int refillRate;    // 補充レート（トークン/秒）
    private int intervalSeconds; // 補充間隔
}
```

#### レート制限インターセプター
```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RateLimitService rateLimitService;
    
    // エンドポイント別制限設定
    private final Map<String, RateLimitConfig> rateLimitConfigs = Map.of(
        "/api/v1/sfr/transfer", RateLimitConfig.builder()
            .capacity(10).refillRate(1).intervalSeconds(60).build(),
        "/api/v1/sfr/rewards/issue", RateLimitConfig.builder()
            .capacity(100).refillRate(10).intervalSeconds(60).build(),
        "/api/v1/sfr/governance/proposals/*/vote", RateLimitConfig.builder()
            .capacity(1).refillRate(1).intervalSeconds(3600).build()
    );
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String clientId = extractClientId(request);
        String endpoint = getEndpointPattern(request.getRequestURI());
        
        RateLimitConfig config = rateLimitConfigs.get(endpoint);
        if (config != null && !rateLimitService.isAllowed(clientId, endpoint, config)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "error": "RATE_LIMIT_EXCEEDED",
                    "message": "リクエスト制限を超過しました",
                    "retry_after": 60
                }
                """);
            return false;
        }
        
        return true;
    }
}
```

### 入力検証・サニタイゼーション

#### カスタムバリデーター
```java
@Component
public class SFRAmountValidatorImpl implements ConstraintValidator<SFRAmount, BigDecimal> {
    
    private double min;
    private double max;
    
    @Override
    public void initialize(SFRAmount annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }
    
    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) return true; // @NotNull で別途チェック
        
        // 8桁小数まで許可
        if (value.scale() > 8) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("小数点以下は8桁まで有効です")
                   .addConstraintViolation();
            return false;
        }
        
        // 範囲チェック
        double doubleValue = value.doubleValue();
        if (doubleValue < min || doubleValue > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("値は %f から %f の範囲で入力してください", min, max))
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
```

#### SQL インジェクション対策
```java
@Repository
public class SecureUserBalanceRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * プリペアードステートメント使用（SQL インジェクション対策）
     */
    public List<BalanceHistory> findBalanceHistory(UUID userId, TransactionType type, 
                                                   LocalDate fromDate, LocalDate toDate,
                                                   int offset, int limit) {
        
        String sql = """
            SELECT history_id, user_id, transaction_type, amount, balance_before, 
                   balance_after, reason, reference_id, created_at
            FROM balance_history 
            WHERE user_id = ? 
            AND (?::transaction_type IS NULL OR transaction_type = ?::transaction_type)
            AND (?::date IS NULL OR created_at >= ?::date)
            AND (?::date IS NULL OR created_at <= ?::date)
            ORDER BY created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return jdbcTemplate.query(sql, 
            new Object[]{
                userId.toString(), 
                type != null ? type.name() : null, type != null ? type.name() : null,
                fromDate, fromDate,
                toDate, toDate,
                limit, offset
            },
            new BalanceHistoryRowMapper());
    }
}
```

---

## 🔒 データ保護

### 暗号化サービス

#### AES-GCM 暗号化
```java
@Service
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    @Value("${sfr.crypto.encryption.key}")
    private String encryptionKey;
    
    /**
     * データ暗号化
     */
    public String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey), ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // ランダムIV生成
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // IV + 暗号化データ を Base64 エンコード
            byte[] result = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, result, GCM_IV_LENGTH, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new CryptographicException("Encryption failed", e);
        }
    }
    
    /**
     * データ復号化
     */
    public String decrypt(String encryptedText) {
        try {
            byte[] encryptedData = Base64.getDecoder().decode(encryptedText);
            
            // IV と暗号化データを分離
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[encryptedData.length - GCM_IV_LENGTH];
            
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
            
            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey), ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            
            byte[] decryptedData = cipher.doFinal(cipherText);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptographicException("Decryption failed", e);
        }
    }
}
```

### データマスキング

#### 機密データマスキング
```java
@Component
public class DataMaskingService {
    
    /**
     * SFR残高マスキング（統計・ログ用）
     */
    public String maskBalance(BigDecimal balance) {
        if (balance == null) return "***";
        
        String balanceStr = balance.toPlainString();
        if (balanceStr.length() <= 4) {
            return "*".repeat(balanceStr.length());
        }
        
        // 最初の2桁と最後の2桁以外をマスク
        return balanceStr.substring(0, 2) + 
               "*".repeat(balanceStr.length() - 4) + 
               balanceStr.substring(balanceStr.length() - 2);
    }
    
    /**
     * ユーザーIDマスキング
     */
    public String maskUserId(UUID userId) {
        String userIdStr = userId.toString();
        return userIdStr.substring(0, 8) + "-****-****-****-" + userIdStr.substring(32);
    }
    
    /**
     * トランザクションIDマスキング
     */
    public String maskTransactionId(String transactionId) {
        if (transactionId.length() <= 8) return "***";
        return transactionId.substring(0, 4) + "***" + transactionId.substring(transactionId.length() - 4);
    }
}

/**
 * JSON シリアライゼーション時のマスキング
 */
@JsonSerialize(using = SensitiveDataSerializer.class)
public class SensitiveDataSerializer extends JsonSerializer<BigDecimal> {
    
    @Autowired
    private DataMaskingService maskingService;
    
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        
        // ログレベルまたはユーザー権限に応じてマスキング
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (shouldMask(auth)) {
            gen.writeString(maskingService.maskBalance(value));
        } else {
            gen.writeString(value.toPlainString());
        }
    }
    
    private boolean shouldMask(Authentication auth) {
        // 統計閲覧権限のみの場合はマスキング
        return auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .noneMatch(authority -> authority.equals("SFR_BALANCE_READ_FULL"));
    }
}
```

---

## 🔄 トランザクション整合性

### 分散トランザクション

#### Saga パターン実装
```java
@Service
@Transactional
public class SFRTransferSagaService {
    
    @Autowired
    private UserBalanceService balanceService;
    
    @Autowired
    private SagaTransactionManager sagaManager;
    
    /**
     * SFR送金 Saga トランザクション
     */
    public TransferResponseDto transferSFR(TransferRequestDto request) {
        
        SagaTransaction saga = sagaManager.beginSaga("SFR_TRANSFER");
        
        try {
            // Step 1: 送金元残高チェック・ロック
            SagaStep balanceCheckStep = saga.addStep("BALANCE_CHECK",
                () -> balanceService.lockAndValidateBalance(
                    request.getFromUserId(), request.getAmount()),
                () -> balanceService.unlockBalance(request.getFromUserId())
            );
            
            // Step 2: 送金元から差し引き
            SagaStep debitStep = saga.addStep("DEBIT_SENDER",
                () -> balanceService.debitBalance(
                    request.getFromUserId(), request.getAmount(), 
                    "TRANSFER_OUT:" + saga.getSagaId()),
                () -> balanceService.creditBalance(
                    request.getFromUserId(), request.getAmount(),
                    "TRANSFER_ROLLBACK:" + saga.getSagaId())
            );
            
            // Step 3: 送金先に追加
            SagaStep creditStep = saga.addStep("CREDIT_RECEIVER",
                () -> balanceService.creditBalance(
                    request.getToUserId(), request.getAmount(),
                    "TRANSFER_IN:" + saga.getSagaId()),
                () -> balanceService.debitBalance(
                    request.getToUserId(), request.getAmount(),
                    "TRANSFER_ROLLBACK:" + saga.getSagaId())
            );
            
            // Step 4: 履歴記録
            SagaStep historyStep = saga.addStep("RECORD_HISTORY",
                () -> recordTransferHistory(request, saga.getSagaId()),
                () -> deleteTransferHistory(saga.getSagaId())
            );
            
            // Saga 実行
            saga.execute();
            
            return buildTransferResponse(request, saga.getSagaId());
            
        } catch (Exception e) {
            saga.compensate(); // 補償トランザクション実行
            throw new TransferFailedException("Transfer failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * デッドロック回避のためのリトライ機構
     */
    @Retryable(
        value = {DataAccessException.class}, 
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void executeWithRetry(Runnable operation) {
        operation.run();
    }
}
```

### 楽観的ロック

#### バージョン管理
```java
@Entity
@Table(name = "user_balances")
public class UserBalance {
    
    @Id
    private UUID userId;
    
    @Column(precision = 26, scale = 8)
    private BigDecimal currentBalance;
    
    @Version
    private Long version; // 楽観的ロック用バージョン
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 残高更新時のバージョンチェック
    public void updateBalance(BigDecimal newBalance) {
        this.currentBalance = newBalance;
        this.updatedAt = LocalDateTime.now();
        // version は JPA が自動インクリメント
    }
}

@Repository
public class UserBalanceRepository extends JpaRepository<UserBalance, UUID> {
    
    /**
     * 楽観的ロック付き残高更新
     */
    @Modifying
    @Query("""
        UPDATE UserBalance ub 
        SET ub.currentBalance = :newBalance, 
            ub.updatedAt = CURRENT_TIMESTAMP,
            ub.version = ub.version + 1
        WHERE ub.userId = :userId 
        AND ub.version = :expectedVersion
        """)
    int updateBalanceWithVersion(@Param("userId") UUID userId, 
                                @Param("newBalance") BigDecimal newBalance,
                                @Param("expectedVersion") Long expectedVersion);
}
```

---

## 📊 監査・ログ

### 包括的監査ログ

#### 監査イベント定義
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    private String auditId;
    
    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;
    
    private UUID userId;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    
    @Column(columnDefinition = "TEXT")
    private String requestData;
    
    @Column(columnDefinition = "TEXT")
    private String responseData;
    
    private Boolean success;
    private String errorMessage;
    
    private LocalDateTime timestamp;
    private Duration processingTime;
    
    // 機密データは暗号化保存
    @Convert(converter = EncryptedStringConverter.class)
    private String sensitiveData;
}

public enum AuditEventType {
    // 認証関連
    USER_LOGIN, USER_LOGOUT, MFA_SETUP, MFA_VERIFY,
    
    // SFR操作
    SFR_TRANSFER, SFR_BALANCE_VIEW, REWARD_ISSUE, COLLECTION_EXECUTE,
    
    // ガバナンス
    PROPOSAL_CREATE, VOTE_CAST, COUNCIL_APPOINTMENT,
    
    // システム操作
    PARAMETER_UPDATE, SYSTEM_MAINTENANCE, EMERGENCY_STOP,
    
    // セキュリティイベント
    SUSPICIOUS_ACTIVITY, RATE_LIMIT_EXCEEDED, UNAUTHORIZED_ACCESS
}
```

#### 監査ログ記録アスペクト
```java
@Aspect
@Component
@Slf4j
public class AuditAspect {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) 
            throws Throwable {
        
        String auditId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        AuditContext context = AuditContext.builder()
            .auditId(auditId)
            .eventType(auditable.eventType())
            .userId(getCurrentUserId())
            .sessionId(getCurrentSessionId())
            .ipAddress(getCurrentIpAddress())
            .userAgent(getCurrentUserAgent())
            .methodName(joinPoint.getSignature().getName())
            .arguments(maskSensitiveArgs(joinPoint.getArgs(), auditable.sensitiveParams()))
            .timestamp(LocalDateTime.now())
            .build();
        
        try {
            Object result = joinPoint.proceed();
            
            context.setSuccess(true);
            context.setProcessingTime(Duration.ofMillis(System.currentTimeMillis() - startTime));
            
            if (auditable.logResponse()) {
                context.setResponseData(maskSensitiveResponse(result, auditable.sensitiveResponseFields()));
            }
            
            auditLogService.recordAudit(context);
            return result;
            
        } catch (Exception e) {
            context.setSuccess(false);
            context.setErrorMessage(e.getMessage());
            context.setProcessingTime(Duration.ofMillis(System.currentTimeMillis() - startTime));
            
            auditLogService.recordAudit(context);
            throw e;
        }
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    AuditEventType eventType();
    boolean logResponse() default false;
    String[] sensitiveParams() default {};
    String[] sensitiveResponseFields() default {};
}
```

### リアルタイム監視

#### セキュリティイベント検知
```java
@Service
public class SecurityMonitoringService {
    
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 異常パターン検知
     */
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        
        // 短時間での大量リクエスト検知
        if (detectHighFrequencyRequests(event)) {
            publishSecurityAlert(SecurityAlertType.SUSPICIOUS_ACTIVITY, 
                "High frequency requests detected", event);
        }
        
        // 異常な金額の取引検知
        if (detectAbnormalTransaction(event)) {
            publishSecurityAlert(SecurityAlertType.ABNORMAL_TRANSACTION,
                "Abnormal transaction amount detected", event);
        }
        
        // 不正なIP からのアクセス検知
        if (detectSuspiciousIP(event)) {
            publishSecurityAlert(SecurityAlertType.SUSPICIOUS_IP,
                "Access from suspicious IP detected", event);
        }
        
        // 権限昇格の試行検知
        if (detectPrivilegeEscalation(event)) {
            publishSecurityAlert(SecurityAlertType.PRIVILEGE_ESCALATION,
                "Privilege escalation attempt detected", event);
        }
    }
    
    /**
     * 短時間大量リクエスト検知
     */
    private boolean detectHighFrequencyRequests(AuditEvent event) {
        String key = String.format("request_count:%s:%s", 
            event.getUserId(), event.getEventType());
        
        Long requestCount = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(5));
        
        // 5分間で50回以上の同一操作
        return requestCount != null && requestCount > 50;
    }
    
    /**
     * セキュリティアラート発行
     */
    private void publishSecurityAlert(SecurityAlertType alertType, String message, AuditEvent event) {
        SecurityAlert alert = SecurityAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .alertType(alertType)
            .severity(alertType.getSeverity())
            .message(message)
            .userId(event.getUserId())
            .sessionId(event.getSessionId())
            .ipAddress(event.getIpAddress())
            .timestamp(LocalDateTime.now())
            .auditEventId(event.getAuditId())
            .build();
        
        eventPublisher.publishEvent(alert);
        
        // 高セベリティの場合は即座に管理者に通知
        if (alert.getSeverity() == AlertSeverity.HIGH) {
            notificationService.sendUrgentAlert(alert);
        }
    }
}
```

---

## 🚨 脅威対策

### DDoS 攻撃対策

#### 分散レート制限
```java
@Component
public class DDoSProtectionService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * IP別リクエスト制限
     */
    public boolean checkIPRateLimit(String clientIP) {
        String key = "ddos_protection:ip:" + clientIP;
        
        // スライディングウィンドウカウンター
        long now = System.currentTimeMillis();
        long windowStart = now - TimeUnit.MINUTES.toMillis(1);
        
        // 古いエントリを削除
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        
        // 現在のリクエスト数をカウント
        Long requestCount = redisTemplate.opsForZSet().count(key, windowStart, now);
        
        if (requestCount != null && requestCount >= 100) { // 1分間100リクエスト制限
            return false;
        }
        
        // 現在のリクエストを記録
        redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), now);
        redisTemplate.expire(key, Duration.ofMinutes(2));
        
        return true;
    }
    
    /**
     * 地理的異常アクセス検知
     */
    public boolean detectGeoAnomaly(String clientIP, UUID userId) {
        try {
            // IP の地理的位置を取得
            GeoLocation currentLocation = geoLocationService.getLocation(clientIP);
            
            // ユーザーの通常アクセス位置を取得
            String key = "user_geo_history:" + userId;
            Set<GeoLocation> historicalLocations = redisTemplate.opsForSet()
                .members(key).stream()
                .map(obj -> (GeoLocation) obj)
                .collect(Collectors.toSet());
            
            // 異常に遠い位置からのアクセスをチェック
            boolean isAnomalous = historicalLocations.stream()
                .noneMatch(loc -> calculateDistance(currentLocation, loc) < 1000); // 1000km 以内
            
            if (isAnomalous && !historicalLocations.isEmpty()) {
                return true;
            }
            
            // 正常な位置として記録（最大10件保持）
            redisTemplate.opsForSet().add(key, currentLocation);
            if (redisTemplate.opsForSet().size(key) > 10) {
                redisTemplate.opsForSet().pop(key);
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Geo anomaly detection failed", e);
            return false;
        }
    }
}
```

### フラウド検知

#### 機械学習ベース異常検知
```java
@Service
public class FraudDetectionService {
    
    @Autowired
    private MLModelService mlModelService;
    
    /**
     * 取引パターン異常検知
     */
    public FraudRiskLevel assessTransactionRisk(TransferRequestDto transfer, 
                                               TransactionContext context) {
        
        // 特徴量抽出
        Map<String, Double> features = extractFeatures(transfer, context);
        
        // ML モデルによるスコア計算
        double riskScore = mlModelService.predictRiskScore(features);
        
        // ルールベース追加チェック
        double ruleBasedScore = calculateRuleBasedRisk(transfer, context);
        
        // 総合リスクスコア
        double totalRisk = (riskScore * 0.7) + (ruleBasedScore * 0.3);
        
        return FraudRiskLevel.fromScore(totalRisk);
    }
    
    /**
     * 特徴量抽出
     */
    private Map<String, Double> extractFeatures(TransferRequestDto transfer, 
                                              TransactionContext context) {
        Map<String, Double> features = new HashMap<>();
        
        // 金額関連特徴量
        features.put("amount", transfer.getAmount().doubleValue());
        features.put("amount_percentile", getAmountPercentile(transfer.getFromUserId(), 
                                                            transfer.getAmount()));
        
        // 時間関連特徴量
        features.put("hour_of_day", (double) LocalDateTime.now().getHour());
        features.put("day_of_week", (double) LocalDateTime.now().getDayOfWeek().getValue());
        
        // ユーザー行動特徴量
        features.put("user_age_days", getUserAgeDays(transfer.getFromUserId()));
        features.put("recent_transaction_count", getRecentTransactionCount(transfer.getFromUserId()));
        features.put("avg_transaction_amount", getAverageTransactionAmount(transfer.getFromUserId()));
        
        // 相手先関連特徴量
        features.put("recipient_relationship", getRecipientRelationship(
            transfer.getFromUserId(), transfer.getToUserId()));
        
        // セッション関連特徴量
        features.put("session_duration", context.getSessionDuration().toMinutes());
        features.put("requests_in_session", (double) context.getRequestCount());
        
        return features;
    }
    
    /**
     * ルールベースリスク計算
     */
    private double calculateRuleBasedRisk(TransferRequestDto transfer, 
                                        TransactionContext context) {
        double risk = 0.0;
        
        // 高額取引
        if (transfer.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            risk += 0.3;
        }
        
        // 深夜取引
        int hour = LocalDateTime.now().getHour();
        if (hour >= 0 && hour <= 5) {
            risk += 0.2;
        }
        
        // 新規受取人
        if (!hasTransactionHistory(transfer.getFromUserId(), transfer.getToUserId())) {
            risk += 0.25;
        }
        
        // 短時間での連続取引
        if (getRecentTransactionCount(transfer.getFromUserId()) > 5) {
            risk += 0.35;
        }
        
        // 異常なセッション
        if (context.getRequestCount() > 50) {
            risk += 0.4;
        }
        
        return Math.min(risk, 1.0);
    }
}

public enum FraudRiskLevel {
    LOW(0.0, 0.3, "自動承認"),
    MEDIUM(0.3, 0.7, "追加認証要求"),
    HIGH(0.7, 0.9, "管理者承認必要"),
    CRITICAL(0.9, 1.0, "取引ブロック");
    
    private final double minScore;
    private final double maxScore;
    private final String action;
    
    public static FraudRiskLevel fromScore(double score) {
        return Arrays.stream(values())
            .filter(level -> score >= level.minScore && score < level.maxScore)
            .findFirst()
            .orElse(CRITICAL);
    }
}
```

---

## 🏗️ インフラセキュリティ

### ネットワークセキュリティ

#### WAF ルール設定
```yaml
# AWS WAF ルール例
web_acl_rules:
  - name: "SFR-RateLimiting"
    priority: 1
    action: "BLOCK"
    rate_based_statement:
      limit: 2000
      aggregate_key_type: "IP"
    
  - name: "SFR-SQLInjection"
    priority: 2
    action: "BLOCK"
    managed_rule_group:
      vendor_name: "AWS"
      name: "AWSManagedRulesSQLiRuleSet"
    
  - name: "SFR-XSS"
    priority: 3
    action: "BLOCK"
    managed_rule_group:
      vendor_name: "AWS"
      name: "AWSManagedRulesCommonRuleSet"
    
  - name: "SFR-GeoBlocking"
    priority: 4
    action: "BLOCK"
    geo_match_statement:
      country_codes: ["CN", "RU", "KP"]  # 高リスク国からのブロック
```

### コンテナセキュリティ

#### Docker セキュリティ設定
```dockerfile
# セキュアなDockerfile
FROM openjdk:17-jre-slim

# 非rootユーザー作成
RUN groupadd -r sfrapp && useradd -r -g sfrapp sfrapp

# セキュリティアップデート
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
    ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# アプリケーション配置
COPY --chown=sfrapp:sfrapp target/sfr-crypto.jar /app/sfr-crypto.jar

# 非rootユーザーで実行
USER sfrapp

# セキュリティオプション
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=production", \
    "-jar", "/app/sfr-crypto.jar"]
```

#### Kubernetes セキュリティポリシー
```yaml
apiVersion: v1
kind: SecurityContext
metadata:
  name: sfr-crypto-security-context
spec:
  securityContext:
    runAsNonRoot: true
    runAsUser: 1000
    runAsGroup: 1000
    fsGroup: 1000
    capabilities:
      drop:
        - ALL
    readOnlyRootFilesystem: true
    allowPrivilegeEscalation: false
  
---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: sfr-crypto-network-policy
spec:
  podSelector:
    matchLabels:
      app: sfr-crypto
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-system
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: database
    ports:
    - protocol: TCP
      port: 5432
```

---

## 🔧 セキュリティ運用

### セキュリティ自動化

#### 脆弱性スキャン自動化
```yaml
# GitHub Actions による定期セキュリティチェック
name: Security Scan
on:
  schedule:
    - cron: '0 2 * * *'  # 毎日 2:00 AM
  push:
    branches: [main]

jobs:
  dependency-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          project: 'SFR-Crypto'
          path: '.'
          format: 'JSON'
          
      - name: Upload Results
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: dependency-check-report.sarif

  container-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker Image
        run: docker build -t sfr-crypto:scan .
        
      - name: Trivy Container Scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'sfr-crypto:scan'
          format: 'sarif'
          output: 'trivy-results.sarif'
          
      - name: Upload Trivy Results
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
```

### インシデント対応

#### セキュリティインシデント対応プロセス
```java
@Service
public class IncidentResponseService {
    
    /**
     * セキュリティインシデント対応
     */
    @EventListener
    public void handleSecurityIncident(SecurityIncident incident) {
        
        IncidentSeverity severity = assessIncidentSeverity(incident);
        
        switch (severity) {
            case CRITICAL -> handleCriticalIncident(incident);
            case HIGH -> handleHighSeverityIncident(incident);
            case MEDIUM -> handleMediumSeverityIncident(incident);
            case LOW -> handleLowSeverityIncident(incident);
        }
        
        // インシデントレポート作成
        createIncidentReport(incident, severity);
    }
    
    /**
     * 緊急度: クリティカル対応
     */
    private void handleCriticalIncident(SecurityIncident incident) {
        // 1. 即座にサービス停止
        emergencyService.stopCriticalServices();
        
        // 2. 緊急通知
        notificationService.sendEmergencyAlert(incident);
        
        // 3. 証拠保全
        forensicsService.preserveEvidence(incident);
        
        // 4. 関係者招集
        incidentResponseTeam.assembleTeam(incident);
        
        log.error("CRITICAL security incident: {}", incident);
    }
    
    /**
     * 自動復旧処理
     */
    private void executeAutomaticRemediation(SecurityIncident incident) {
        switch (incident.getType()) {
            case BRUTE_FORCE_ATTACK -> {
                // IP ブロック
                firewallService.blockIP(incident.getSourceIP(), Duration.ofHours(24));
                // アカウントロック
                userService.lockAccount(incident.getTargetUserId(), Duration.ofHours(1));
            }
            case SUSPICIOUS_TRANSACTION -> {
                // 取引一時停止
                transactionService.freezeAccount(incident.getTargetUserId());
                // 追加認証要求
                authService.requireMFA(incident.getTargetUserId());
            }
            case DATA_EXFILTRATION -> {
                // アクセス全停止
                accessControlService.revokeAllSessions(incident.getTargetUserId());
                // 管理者通知
                notificationService.notifyAdministrators(incident);
            }
        }
    }
}
```

---

*このセキュリティ設計により、SFR暗号資産システムは包括的な多層防御体制を構築し、様々な脅威から資産とデータを保護します。*
