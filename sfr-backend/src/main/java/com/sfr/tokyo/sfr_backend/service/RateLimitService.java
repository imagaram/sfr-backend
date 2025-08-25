package com.sfr.tokyo.sfr_backend.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * レート制限サービス
 * IPアドレス毎のリクエスト制限を管理
 */
@Service
public class RateLimitService {

    // IPアドレス毎のリクエスト回数カウンター
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    // IPアドレス毎の最後のリセット時刻
    private final ConcurrentHashMap<String, AtomicLong> lastResetTimes = new ConcurrentHashMap<>();

    // レート制限設定
    private static final int MAX_REQUESTS_PER_MINUTE = 60; // 1分間に60リクエスト
    private static final int MAX_AUTH_REQUESTS_PER_MINUTE = 5; // 認証系は1分間に5リクエスト
    private static final long TIME_WINDOW_MS = 60 * 1000; // 1分間のタイムウィンドウ

    /**
     * 通常のAPIエンドポイントのレート制限チェック
     */
    public boolean isAllowed(String clientIp) {
        return checkRateLimit(clientIp, MAX_REQUESTS_PER_MINUTE);
    }

    /**
     * 認証系エンドポイントのレート制限チェック
     */
    public boolean isAuthAllowed(String clientIp) {
        return checkRateLimit("auth_" + clientIp, MAX_AUTH_REQUESTS_PER_MINUTE);
    }

    /**
     * レート制限チェックの共通ロジック
     */
    private boolean checkRateLimit(String key, int maxRequests) {
        long currentTime = System.currentTimeMillis();
        AtomicInteger requestCount = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        AtomicLong lastResetTime = lastResetTimes.computeIfAbsent(key, k -> new AtomicLong(currentTime));

        // タイムウィンドウをリセット
        if (currentTime - lastResetTime.get() >= TIME_WINDOW_MS) {
            // 新しいウィンドウ開始: カウンターを 1 に (このリクエスト分)
            requestCount.set(1);
            lastResetTime.set(currentTime);
            return true;
        }
        // 既存ウィンドウ: CAS ループで厳密制御
        while (true) {
            int cur = requestCount.get();
            if (cur >= maxRequests) {
                return false; // 上限到達
            }
            if (requestCount.compareAndSet(cur, cur + 1)) {
                return true;
            }
            // 競合時は再試行
        }
    }

    /**
     * 残りリクエスト数を取得
     */
    public int getRemainingRequests(String clientIp) {
        return getRemainingRequests(clientIp, MAX_REQUESTS_PER_MINUTE);
    }

    /**
     * 認証系の残りリクエスト数を取得
     */
    public int getRemainingAuthRequests(String clientIp) {
        return getRemainingRequests("auth_" + clientIp, MAX_AUTH_REQUESTS_PER_MINUTE);
    }

    private int getRemainingRequests(String key, int maxRequests) {
        if (!requestCounts.containsKey(key)) {
            return maxRequests;
        }

        long currentTime = System.currentTimeMillis();
        AtomicLong lastResetTime = lastResetTimes.get(key);

        // タイムウィンドウ外の場合はフルリセット
        if (currentTime - lastResetTime.get() >= TIME_WINDOW_MS) {
            return maxRequests;
        }

        AtomicInteger requestCount = requestCounts.get(key);
        return Math.max(0, maxRequests - requestCount.get());
    }

    /**
     * 次のリセット時刻までの秒数を取得
     */
    public long getSecondsUntilReset(String clientIp) {
        if (!lastResetTimes.containsKey(clientIp)) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long lastResetTime = lastResetTimes.get(clientIp).get();
        long timeElapsed = currentTime - lastResetTime;

        if (timeElapsed >= TIME_WINDOW_MS) {
            return 0;
        }

        return (TIME_WINDOW_MS - timeElapsed) / 1000;
    }

    /**
     * 古いエントリをクリーンアップ（メモリリーク防止）
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();

        lastResetTimes.entrySet().removeIf(entry -> {
            boolean shouldRemove = currentTime - entry.getValue().get() > TIME_WINDOW_MS * 2;
            if (shouldRemove) {
                requestCounts.remove(entry.getKey());
            }
            return shouldRemove;
        });
    }

    /**
     * 古いエントリをクリーンアップ（メモリリーク防止）- 別名メソッド
     */
    public void cleanupOldEntries() {
        cleanup();
    }

    /**
     * 現在のリクエスト数を取得
     */
    public int getCurrentRequestCount(String clientIp) {
        AtomicInteger count = requestCounts.get(clientIp);
        return count != null ? count.get() : 0;
    }

    /**
     * 現在の認証リクエスト数を取得
     */
    public int getCurrentAuthRequestCount(String clientIp) {
        AtomicInteger count = requestCounts.get("auth_" + clientIp);
        return count != null ? count.get() : 0;
    }
}
