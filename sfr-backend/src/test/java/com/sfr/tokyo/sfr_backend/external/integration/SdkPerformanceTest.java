package com.sfr.tokyo.sfr_backend.external.integration;

import com.sfr.tokyo.sfr_backend.external.adapter.SfrManifestoAdapter;
import com.sfr.tokyo.sfr_backend.external.contract.ApiResponse;
import com.sfr.tokyo.sfr_backend.external.controller.ManifestoExternalControllerSimple;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SDKパフォーマンステスト
 * 
 * Step 4: 統合テストの自動化 - パフォーマンステスト編
 * 外部SDK生成システムのパフォーマンスと負荷テストを実装します。
 * 
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SdkPerformanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ManifestoExternalControllerSimple manifestoController;

    @Autowired
    private SfrManifestoAdapter manifestoAdapter;

    // Removed unused baseUrl field and setup

    @Test
    @Order(1)
    void testSingleRequestPerformance() {
        // Given: パフォーマンス測定環境

        StopWatch stopWatch = new StopWatch();
        
        // When: 単一リクエストの性能を測定
        stopWatch.start();
        ResponseEntity<ApiResponse<List<String>>> response = 
                manifestoController.getAvailableLanguages();
        stopWatch.stop();

        // Then: 適切な応答時間であること
        long executionTime = stopWatch.getTotalTimeMillis();
        assertTrue(executionTime < 1000, 
                "Single request should complete within 1 second, actual: " + executionTime + "ms");
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        
        System.out.println("Single request execution time: " + executionTime + "ms");
    }

    @Test
    @Order(2)
    void testConcurrentRequestsPerformance() throws InterruptedException {
        // Given: 並行リクエストテスト環境
        int numberOfThreads = 10;
        int requestsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads * requestsPerThread);
        
        StopWatch stopWatch = new StopWatch();
        
        // When: 複数の並行リクエストを実行
        stopWatch.start();
        
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        ResponseEntity<ApiResponse<List<String>>> response = 
                                manifestoController.getAvailableLanguages();
                        
                        // 各リクエストが成功することを確認
                        assertEquals(200, response.getStatusCode().value());
                        assertNotNull(response.getBody());
                        assertTrue(response.getBody().isSuccess());
                        
                    } catch (Exception e) {
                        fail("Concurrent request failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        
        // Then: 全てのリクエストが適切な時間内に完了
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        stopWatch.stop();
        
        assertTrue(completed, "All concurrent requests should complete within 30 seconds");
        
        long totalExecutionTime = stopWatch.getTotalTimeMillis();
        double averageTime = (double) totalExecutionTime / (numberOfThreads * requestsPerThread);
        
        assertTrue(averageTime < 2000, 
                "Average request time should be under 2 seconds, actual: " + averageTime + "ms");
        
        System.out.println("Concurrent requests (" + (numberOfThreads * requestsPerThread) + 
                          " total) completed in: " + totalExecutionTime + "ms");
        System.out.println("Average time per request: " + averageTime + "ms");
        
        executor.shutdown();
    }

    @Test
    @Order(3)
    void testAdapterPerformance() {
        // Given: アダプターパフォーマンステスト
        
        StopWatch stopWatch = new StopWatch();
        int iterations = 100;
        
        // When: アダプターメソッドを繰り返し実行
        stopWatch.start();
        
    for (int i = 0; i < iterations; i++) {
            assertDoesNotThrow(() -> {
                manifestoAdapter.getAvailableLanguages();
                manifestoAdapter.getAvailableVersions();
                manifestoAdapter.isConnectionAvailable();
                manifestoAdapter.getAdapterInfo();
            });
        }
        
        stopWatch.stop();
        
        // Then: 適切なパフォーマンスを維持
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTimePerIteration = (double) totalTime / iterations;
        
        assertTrue(averageTimePerIteration < 100, 
                "Average adapter call should be under 100ms, actual: " + averageTimePerIteration + "ms");
        
        System.out.println("Adapter performance test (" + iterations + " iterations) " +
                          "total time: " + totalTime + "ms, average: " + averageTimePerIteration + "ms");
    }

    @Test
    @Order(4)
    void testMemoryUsageUnderLoad() throws InterruptedException {
        // Given: メモリ使用量テスト環境
        
        Runtime runtime = Runtime.getRuntime();
        
        // 初期メモリ状態を記録
        System.gc(); // ガベージコレクションを実行
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // When: 大量のリクエストを実行
        int numberOfRequests = 1000;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    manifestoController.getAvailableLanguages();
                    manifestoController.getAvailableVersions();
                } catch (Exception e) {
                    // ログ出力のみ（テスト継続）
                    System.err.println("Request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Then: メモリリークがないことを確認
        System.gc(); // ガベージコレクションを実行
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // メモリ増加が100MB未満であることを確認
        assertTrue(memoryIncrease < 100 * 1024 * 1024, 
                "Memory increase should be under 100MB, actual: " + (memoryIncrease / 1024 / 1024) + "MB");
        
        System.out.println("Memory usage - Initial: " + (initialMemory / 1024 / 1024) + "MB, " +
                          "Final: " + (finalMemory / 1024 / 1024) + "MB, " +
                          "Increase: " + (memoryIncrease / 1024 / 1024) + "MB");
    }

    @Test
    @Order(5)
    void testAsynchronousRequestHandling() {
        // Given: 非同期リクエスト処理テスト
        
        int numberOfAsyncRequests = 50;
        
        // When: 非同期でリクエストを実行
        List<CompletableFuture<ResponseEntity<ApiResponse<List<String>>>>> futures = 
                IntStream.range(0, numberOfAsyncRequests)
                        .mapToObj(i -> CompletableFuture.supplyAsync(() -> 
                                manifestoController.getAvailableLanguages()))
                        .toList();
        
        // Then: 全ての非同期処理が正常に完了
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        
        assertDoesNotThrow(() -> {
            allFutures.get(30, TimeUnit.SECONDS);
        });
        
        stopWatch.stop();
        
        // 全てのレスポンスが成功していることを確認
        for (CompletableFuture<ResponseEntity<ApiResponse<List<String>>>> future : futures) {
            assertDoesNotThrow(() -> {
                ResponseEntity<ApiResponse<List<String>>> response = future.get();
                assertEquals(200, response.getStatusCode().value());
                assertNotNull(response.getBody());
                assertTrue(response.getBody().isSuccess());
            });
        }
        
        long executionTime = stopWatch.getTotalTimeMillis();
        double averageTime = (double) executionTime / numberOfAsyncRequests;
        
        System.out.println("Async requests (" + numberOfAsyncRequests + ") completed in: " + 
                          executionTime + "ms, average: " + averageTime + "ms");
    }

    @Test
    @Order(6)
    void testErrorHandlingPerformance() {
        // Given: エラーハンドリングパフォーマンステスト
        
        StopWatch stopWatch = new StopWatch();
        int iterations = 100;
        
        // When: エラーケースを含むリクエストを実行
        stopWatch.start();
        
        IntStream.range(0, iterations).forEach(idx -> {
            assertDoesNotThrow(() -> manifestoController.getCurrentManifesto("invalid_lang_" + idx));
        });
        
        stopWatch.stop();
        
        // Then: エラーハンドリングも適切な時間内に完了
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = (double) totalTime / iterations;
        
        assertTrue(averageTime < 200, 
                "Error handling should be under 200ms on average, actual: " + averageTime + "ms");
        
        System.out.println("Error handling performance (" + iterations + " errors) " +
                          "total time: " + totalTime + "ms, average: " + averageTime + "ms");
    }

    @Test
    @Order(7)
    void testSystemResourceUtilization() {
        // Given: システムリソース使用率テスト
        
        Runtime runtime = Runtime.getRuntime();
        int availableProcessors = runtime.availableProcessors();
        
        // When: CPU集約的なタスクを実行
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        List<CompletableFuture<Void>> cpuTasks = IntStream.range(0, availableProcessors * 2)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < 10; j++) {
                        manifestoController.getAvailableLanguages();
                        manifestoController.getAvailableVersions();
                    }
                }))
                .toList();
        
        // Then: 全てのタスクが適切な時間内に完了
        assertDoesNotThrow(() -> {
            CompletableFuture.allOf(cpuTasks.toArray(new CompletableFuture[0]))
                    .get(60, TimeUnit.SECONDS);
        });
        
        stopWatch.stop();
        
        long executionTime = stopWatch.getTotalTimeMillis();
        System.out.println("System resource utilization test (" + 
                          (availableProcessors * 2) + " parallel tasks) completed in: " + 
                          executionTime + "ms");
        
        assertTrue(executionTime < 30000, 
                "Resource utilization test should complete within 30 seconds");
    }
}
