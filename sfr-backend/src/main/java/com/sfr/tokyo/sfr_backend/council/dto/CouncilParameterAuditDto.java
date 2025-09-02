package com.sfr.tokyo.sfr_backend.council.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * 評議員パラメータの監査証跡DTO
 * 制度的変更の完全な証跡を提供
 */
@Data
@Builder
public class CouncilParameterAuditDto {
    
    /**
     * 監査記録ID
     */
    private Long auditId;
    
    /**
     * パラメータキー
     */
    private String parameterKey;
    
    /**
     * 監査アクション
     */
    private AuditAction action;
    
    /**
     * 変更前の値
     */
    private String oldValue;
    
    /**
     * 変更後の値
     */
    private String newValue;
    
    /**
     * 変更理由
     */
    private String reason;
    
    /**
     * 変更実行者
     */
    private String actor;
    
    /**
     * 変更実行者の権限レベル
     */
    private String actorRole;
    
    /**
     * 変更実行時刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant timestamp;
    
    /**
     * 変更が有効になる時刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant effectiveFrom;
    
    /**
     * 変更が無効になる時刻（TTL）
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant effectiveUntil;
    
    /**
     * IPアドレス
     */
    private String ipAddress;
    
    /**
     * ユーザーエージェント
     */
    private String userAgent;
    
    /**
     * セッションID
     */
    private String sessionId;
    
    /**
     * リクエストID（トレーサビリティ用）
     */
    private String requestId;
    
    /**
     * バッチ操作ID（一括更新の場合）
     */
    private String batchId;
    
    /**
     * 承認プロセスの情報
     */
    private ApprovalInfo approvalInfo;
    
    /**
     * 制度的影響範囲
     */
    private SystemicImpact systemicImpact;
    
    /**
     * 追加メタデータ
     */
    private Map<String, Object> metadata;
    
    /**
     * 監査アクションの種類
     */
    public enum AuditAction {
        CREATE("作成"),
        UPDATE("更新"), 
        DELETE("削除"),
        ACTIVATE("有効化"),
        DEACTIVATE("無効化"),
        VALIDATE("検証"),
        APPROVE("承認"),
        REJECT("却下"),
        EXPIRE("期限切れ"),
        EMERGENCY_OVERRIDE("緊急上書き");
        
        private final String description;
        
        AuditAction(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 承認プロセス情報
     */
    @Data
    @Builder
    public static class ApprovalInfo {
        /**
         * 承認が必要か
         */
        private boolean requiresApproval;
        
        /**
         * 承認者ID
         */
        private String approverId;
        
        /**
         * 承認時刻
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant approvedAt;
        
        /**
         * 承認ステータス
         */
        private ApprovalStatus status;
        
        /**
         * 承認コメント
         */
        private String comment;
        
        public enum ApprovalStatus {
            PENDING("承認待ち"),
            APPROVED("承認済み"),
            REJECTED("却下"),
            AUTO_APPROVED("自動承認");
            
            private final String description;
            
            ApprovalStatus(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 制度的影響範囲
     */
    @Data
    @Builder
    public static class SystemicImpact {
        /**
         * 影響を受けるサブシステム
         */
        private java.util.List<String> affectedSubsystems;
        
        /**
         * 依存パラメータ
         */
        private java.util.List<String> dependentParameters;
        
        /**
         * リスクレベル
         */
        private RiskLevel riskLevel;
        
        /**
         * 影響予測
         */
        private String impactPrediction;
        
        /**
         * ロールバック可能性
         */
        private boolean rollbackable;
        
        public enum RiskLevel {
            LOW("低リスク"),
            MEDIUM("中リスク"), 
            HIGH("高リスク"),
            CRITICAL("重大リスク");
            
            private final String description;
            
            RiskLevel(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
}
