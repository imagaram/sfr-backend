package com.sfr.tokyo.sfr_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * スケジュールタスクサービス
 * 定期的なクリーンアップやメンテナンス処理を実行
 */
@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final RateLimitService rateLimitService;

    public ScheduledTaskService(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    /**
     * レート制限データのクリーンアップ
     * 毎時実行
     */
    @Scheduled(fixedRate = 3600000) // 1時間毎
    public void cleanupRateLimitData() {
        try {
            logger.info("Starting rate limit data cleanup");
            rateLimitService.cleanup();
            logger.info("Rate limit data cleanup completed");
        } catch (Exception e) {
            logger.error("Error during rate limit data cleanup", e);
        }
    }

    /**
     * セキュリティログの統計情報出力
     * 毎日午前2時に実行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateSecurityReport() {
        try {
            logger.info("Generating daily security report");
            // ここにセキュリティ統計の生成ロジックを実装
            // 例：不正アクセス試行回数、ブロックされたIP数など
            logger.info("Daily security report generation completed");
        } catch (Exception e) {
            logger.error("Error during security report generation", e);
        }
    }
}
