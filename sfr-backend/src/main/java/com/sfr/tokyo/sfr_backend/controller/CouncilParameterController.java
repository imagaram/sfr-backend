package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.service.system.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/governance/parameters")
@RequiredArgsConstructor
@Tag(name = "Governance Parameters", description = "Council ガバナンス関連の動的パラメータ管理 API")
public class CouncilParameterController {

    private final ParameterService parameterService;

    @GetMapping
    @Operation(summary = "パラメータ一覧取得", description = "system_parameters からガバナンス関連パラメータを prefix フィルタ付きで取得")
    public ResponseEntity<Object> list(
            @Parameter(description = "キー prefix。指定時その文字列で始まるキーのみ返却", example = "council.")
            @RequestParam(value = "prefix", required = false) String prefix) {
        return ResponseEntity.ok(parameterService.listAll(prefix));
    }

    @PatchMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "数値パラメータ更新", description = "数値/小数パラメータの更新。報酬重みキーの場合は合計=1.0 検証後反映")
    public ResponseEntity<Void> updateDecimal(
            @Parameter(description = "param_key", example = "council.reward.weight.user")
            @PathVariable("key") String key,
            @RequestBody UpdateDecimalRequest req) {
        // Simple numeric update endpoint (decimal or integer). Access control (ROLE_ADMIN) should be enforced via security config.
        parameterService.updateDecimal(key, req.value(), req.reason(), req.changedBy());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate/weights")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "報酬重み検証", description = "現在の user/peer/admin 重み合計が 1.0 であることを検証")
    public ResponseEntity<Void> validateWeights() {
        parameterService.validateCurrentWeights();
        return ResponseEntity.noContent().build();
    }

    // --- Cache management (ADMIN) ---
    @GetMapping("/cache/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "キャッシュ状態取得", description = "TTL と各エントリ age を返却")
    public ResponseEntity<Object> cacheStatus() {
        return ResponseEntity.ok(parameterService.cacheStatus());
    }

    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "キャッシュ全クリア")
    public ResponseEntity<Void> clearCache() {
        parameterService.clearAllCache();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cache/evict/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "単一キーキャッシュ削除")
    public ResponseEntity<Void> evictKey(
            @Parameter(description = "キャッシュ削除対象 param_key", example = "council.vote.min.balance")
            @PathVariable("key") String key) {
        parameterService.evict(key);
        return ResponseEntity.noContent().build();
    }

    public record TtlRequest(long ttlMillis) {}

    @PostMapping("/cache/ttl")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "キャッシュTTL設定", description = "ミリ秒指定。最小 1000ms")
    public ResponseEntity<Void> setTtl(@RequestBody TtlRequest req) {
        parameterService.setTtlMillis(req.ttlMillis());
        return ResponseEntity.noContent().build();
    }

    public record UpdateDecimalRequest(
            @Schema(description = "新しい数値 (報酬重みは 0-1 内)") BigDecimal value,
            @Schema(description = "更新理由", example = "Quarterly adjustment") String reason,
            @Schema(description = "変更実行者 (監査用)", example = "admin_user") String changedBy) {}
}
