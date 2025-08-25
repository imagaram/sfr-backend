package com.sfr.tokyo.sfr_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimitServiceのテストクラス
 */
public class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    void testIsAllowed_FirstRequest_ShouldBeAllowed() {
        // Given
        String clientIp = "192.168.1.1";

        // When
        boolean result = rateLimitService.isAllowed(clientIp);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsAllowed_WithinLimit_ShouldBeAllowed() {
        // Given
        String clientIp = "192.168.1.2";

        // When & Then
        for (int i = 0; i < 60; i++) {
            assertTrue(rateLimitService.isAllowed(clientIp),
                    "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void testIsAllowed_ExceedsLimit_ShouldBeBlocked() {
        // Given
        String clientIp = "192.168.1.3";

        // When - 60回のリクエストを送信（制限内）
        for (int i = 0; i < 60; i++) {
            assertTrue(rateLimitService.isAllowed(clientIp));
        }

        // Then - 61回目のリクエストは拒否される
        assertFalse(rateLimitService.isAllowed(clientIp));
    }

    @Test
    void testIsAuthAllowed_WithinLimit_ShouldBeAllowed() {
        // Given
        String clientIp = "192.168.1.4";

        // When & Then
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitService.isAuthAllowed(clientIp),
                    "Auth request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void testIsAuthAllowed_ExceedsLimit_ShouldBeBlocked() {
        // Given
        String clientIp = "192.168.1.5";

        // When - 5回の認証リクエストを送信（制限内）
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitService.isAuthAllowed(clientIp));
        }

        // Then - 6回目のリクエストは拒否される
        assertFalse(rateLimitService.isAuthAllowed(clientIp));
    }

    @Test
    void testGetRemainingRequests_InitialState() {
        // Given
        String clientIp = "192.168.1.6";

        // When
        int remaining = rateLimitService.getRemainingRequests(clientIp);

        // Then
        assertEquals(60, remaining);
    }

    @Test
    void testGetRemainingRequests_AfterSomeRequests() {
        // Given
        String clientIp = "192.168.1.7";

        // When
        for (int i = 0; i < 10; i++) {
            rateLimitService.isAllowed(clientIp);
        }
        int remaining = rateLimitService.getRemainingRequests(clientIp);

        // Then
        assertEquals(50, remaining);
    }

    @Test
    void testGetRemainingAuthRequests_InitialState() {
        // Given
        String clientIp = "192.168.1.8";

        // When
        int remaining = rateLimitService.getRemainingAuthRequests(clientIp);

        // Then
        assertEquals(5, remaining);
    }

    @Test
    void testGetRemainingAuthRequests_AfterSomeRequests() {
        // Given
        String clientIp = "192.168.1.9";

        // When
        for (int i = 0; i < 3; i++) {
            rateLimitService.isAuthAllowed(clientIp);
        }
        int remaining = rateLimitService.getRemainingAuthRequests(clientIp);

        // Then
        assertEquals(2, remaining);
    }

    @Test
    void testGetSecondsUntilReset_InitialState() {
        // Given
        String clientIp = "192.168.1.10";

        // When
        rateLimitService.isAllowed(clientIp);
        long seconds = rateLimitService.getSecondsUntilReset(clientIp);

        // Then
        assertTrue(seconds > 0 && seconds <= 60);
    }

    @Test
    void testCleanupOldEntries() {
        // Given
        String clientIp = "192.168.1.11";
        rateLimitService.isAllowed(clientIp);

        // When
        rateLimitService.cleanupOldEntries();

        // Then - メソッドが例外なく実行されることを確認
        assertDoesNotThrow(() -> rateLimitService.cleanupOldEntries());
    }

    @Test
    void testDifferentIPs_IndependentLimits() {
        // Given
        String clientIp1 = "192.168.1.12";
        String clientIp2 = "192.168.1.13";

        // When - IP1で制限まで使用
        for (int i = 0; i < 60; i++) {
            rateLimitService.isAllowed(clientIp1);
        }

        // Then - IP2はまだ使用可能
        assertTrue(rateLimitService.isAllowed(clientIp2));
        assertEquals(59, rateLimitService.getRemainingRequests(clientIp2));
    }

    @Test
    void testNormalAndAuthLimits_AreIndependent() {
        // Given
        String clientIp = "192.168.1.14";

        // When - 通常のAPIで制限まで使用
        for (int i = 0; i < 60; i++) {
            rateLimitService.isAllowed(clientIp);
        }

        // Then - 認証APIはまだ使用可能
        assertTrue(rateLimitService.isAuthAllowed(clientIp));
        assertEquals(4, rateLimitService.getRemainingAuthRequests(clientIp));
    }

    @Test
    void testConcurrentAccess_ThreadSafety() throws InterruptedException {
        // Given
        String clientIp = "192.168.1.15";
        int numberOfThreads = 10;
        int requestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // When - 複数スレッドから同時アクセス
        for (int i = 0; i < numberOfThreads; i++) {
            for (int j = 0; j < requestsPerThread; j++) {
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
                        () -> rateLimitService.isAllowed(clientIp), executor);
                futures.add(future);
            }
        }

        // すべてのタスクの完了を待つ
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then - 総リクエスト数は制限内であることを確認
        long allowedRequests = futures.stream()
                .mapToLong(future -> future.join() ? 1 : 0)
                .sum();

        assertTrue(allowedRequests <= 60, "Allowed requests should not exceed limit: " + allowedRequests);
        assertTrue(allowedRequests > 0, "At least some requests should be allowed");
    }

    @RepeatedTest(3)
    void testEdgeCases_EmptyAndNullIPs() {
        // Test with empty string
        assertDoesNotThrow(() -> rateLimitService.isAllowed(""));
        assertDoesNotThrow(() -> rateLimitService.getRemainingRequests(""));

        // Test with very long IP string
        String longIp = "a".repeat(1000);
        assertDoesNotThrow(() -> rateLimitService.isAllowed(longIp));
    }

    @Test
    void testRateLimitReset_AfterTimeWindow() throws InterruptedException {
        // Given
        String clientIp = "192.168.1.16";

        // When - 制限まで使用
        for (int i = 0; i < 60; i++) {
            rateLimitService.isAllowed(clientIp);
        }
        assertFalse(rateLimitService.isAllowed(clientIp)); // 制限に達

        // 短時間待機（実際のテストでは時間を操作する方が良いが、簡易版）
        Thread.sleep(100);

        // Then - まだ制限中であることを確認
        assertFalse(rateLimitService.isAllowed(clientIp));
    }

    @Test
    void testGetCurrentRequestCount() {
        // Given
        String clientIp = "192.168.1.17";

        // When
        int initialCount = rateLimitService.getCurrentRequestCount(clientIp);
        rateLimitService.isAllowed(clientIp);
        rateLimitService.isAllowed(clientIp);
        int afterRequests = rateLimitService.getCurrentRequestCount(clientIp);

        // Then
        assertEquals(0, initialCount);
        assertEquals(2, afterRequests);
    }

    @Test
    void testGetCurrentAuthRequestCount() {
        // Given
        String clientIp = "192.168.1.18";

        // When
        int initialCount = rateLimitService.getCurrentAuthRequestCount(clientIp);
        rateLimitService.isAuthAllowed(clientIp);
        int afterRequest = rateLimitService.getCurrentAuthRequestCount(clientIp);

        // Then
        assertEquals(0, initialCount);
        assertEquals(1, afterRequest);
    }
}
