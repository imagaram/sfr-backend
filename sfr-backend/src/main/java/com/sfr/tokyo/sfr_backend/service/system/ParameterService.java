package com.sfr.tokyo.sfr_backend.service.system;

import com.sfr.tokyo.sfr_backend.entity.system.SystemParameter;
import com.sfr.tokyo.sfr_backend.repository.system.SystemParameterRepository;
import com.sfr.tokyo.sfr_backend.repository.system.SystemParameterAuditRepository;
import com.sfr.tokyo.sfr_backend.entity.system.SystemParameterAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import com.sfr.tokyo.sfr_backend.exception.InvalidRequestException;

@Service
@RequiredArgsConstructor
public class ParameterService {

    private final SystemParameterRepository repository;
    private final SystemParameterAuditRepository auditRepository;

    // Lightweight in-memory cache (per instance) with TTL.
    private final Map<String, CachedEntry> localCache = new ConcurrentHashMap<>();

    private volatile long ttlMillis = 300_000; // 5 minutes default

    private record CachedEntry(Object value, long loadedAt) {}

    public static final String USER_WEIGHT = "council.reward.weight.user";
    public static final String PEER_WEIGHT = "council.reward.weight.peer";
    public static final String ADMIN_WEIGHT = "council.reward.weight.admin";
    public static final String MIN_VOTE_BALANCE = "council.vote.min.balance";
    public static final String MIN_VOTE_ACTIVITY_DAYS = "council.vote.min.activity_days";
    public static final String EVAL_WINDOW_DAYS = "council.evaluation.window.days";
    public static final String COUNCIL_MAX_SIZE = "council.size.max";

    @Transactional(readOnly = true)
    public BigDecimal getDecimal(String key) {
    CachedEntry cached = localCache.get(key);
    long now = System.currentTimeMillis();
    if (cached != null && (now - cached.loadedAt()) < ttlMillis) {
        Object v = cached.value();
        if (v instanceof BigDecimal bd) return bd;
    }
    SystemParameter p = repository.findById(key)
        .orElseThrow(() -> new NoSuchElementException("Parameter not found: " + key));
    BigDecimal value = p.getValueNumber();
    if (value == null) throw new IllegalStateException("Parameter has no numeric value: " + key);
    localCache.put(key, new CachedEntry(value, now));
    return value;
    }

    @Transactional(readOnly = true)
    public int getInt(String key) {
        BigDecimal bd = getDecimal(key);
        return bd.intValue();
    }

    @Transactional(readOnly = true)
    public double getDouble(String key) {
        BigDecimal bd = getDecimal(key);
        return bd.doubleValue();
    }

    @Transactional
    public void updateDecimal(String key, BigDecimal newValue, String reason, String changedBy) {
        // If updating a weight parameter we pre-validate the prospective total.
        if (isWeightKey(key)) {
            validateProspectiveWeightSum(key, newValue);
        }
        SystemParameter p = repository.findById(key).orElseThrow(() -> new NoSuchElementException("Parameter not found: " + key));
        BigDecimal old = p.getValueNumber();
        p.setValueNumber(newValue);
        repository.save(p);
    localCache.remove(key);
        SystemParameterAudit audit = SystemParameterAudit.builder()
                .paramKey(key)
                .oldValueNumber(old)
                .newValueNumber(newValue)
                .changedBy(changedBy)
                .reason(reason)
                .build();
        auditRepository.save(audit);
    }

    private boolean isWeightKey(String key) {
        return USER_WEIGHT.equals(key) || PEER_WEIGHT.equals(key) || ADMIN_WEIGHT.equals(key);
    }

    private void validateProspectiveWeightSum(String changingKey, BigDecimal newValue) {
        BigDecimal user = USER_WEIGHT.equals(changingKey) ? newValue : getDecimal(USER_WEIGHT);
        BigDecimal peer = PEER_WEIGHT.equals(changingKey) ? newValue : getDecimal(PEER_WEIGHT);
        BigDecimal admin = ADMIN_WEIGHT.equals(changingKey) ? newValue : getDecimal(ADMIN_WEIGHT);
        BigDecimal sum = user.add(peer).add(admin);
        // Allow tiny floating error margin
        if (sum.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.0000001")) > 0) {
            throw new InvalidRequestException("Reward weight total must be 1.0 (current attempt: " + sum + ")");
        }
        if (user.signum() < 0 || peer.signum() < 0 || admin.signum() < 0) {
            throw new InvalidRequestException("Reward weights must be non-negative");
        }
    }

    // Explicit validation endpoint support
    @Transactional(readOnly = true)
    public void validateCurrentWeights() {
        validateProspectiveWeightSum("noop", BigDecimal.valueOf(-1)); // sentinel call uses existing values except sentinel invalid newValue
    }

    @Transactional
    public void clearAllCache() { localCache.clear(); }

    public void evict(String key) { localCache.remove(key); }

    public void setTtlMillis(long ttlMillis) {
        if (ttlMillis < 1000) throw new InvalidRequestException("ttlMillis must be >= 1000");
        this.ttlMillis = ttlMillis;
    }

    public long getTtlMillis() { return ttlMillis; }

    @Transactional(readOnly = true)
    public List<ParameterView> listAll(String prefix) {
    return repository.findAll().stream()
        .filter(p -> prefix == null || p.getParamKey().startsWith(prefix))
        .sorted(Comparator.comparing(SystemParameter::getParamKey))
        .map(p -> new ParameterView(
            p.getParamKey(),
            p.getValueNumber(),
            p.getValueString(),
            p.getValueJson(),
            p.getValueType(),
            p.getDescription(),
            p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null
        ))
        .collect(Collectors.toList());
    }

    public record ParameterView(
        String key,
        BigDecimal valueNumber,
        String valueString,
        String valueJson,
        String valueType,
        String description,
        String updatedAt
    ) {}

    // Cache status representation
    public record CacheStatus(long ttlMillis, int size, List<CacheEntryStatus> entries) {}
    public record CacheEntryStatus(String key, long ageMillis) {}

    @Transactional(readOnly = true)
    public CacheStatus cacheStatus() {
        long now = System.currentTimeMillis();
        List<CacheEntryStatus> list = localCache.entrySet().stream()
                .map(e -> new CacheEntryStatus(e.getKey(), now - e.getValue().loadedAt()))
                .sorted(Comparator.comparing(CacheEntryStatus::key))
                .collect(Collectors.toList());
        return new CacheStatus(ttlMillis, list.size(), list);
    }
}
