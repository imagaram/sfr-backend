package com.sfr.tokyo.sfr_backend.explorer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * ブロックチェーン風評議員制度の可視化用DTO
 * Merkle構造とブロック署名による制度の真正性表現
 */
@Data
@Builder
public class CouncilBlockExplorerDto {
    
    /**
     * ブロック基本情報
     */
    private BlockInfo blockInfo;
    
    /**
     * Merkle Tree構造
     */
    private MerkleTreeInfo merkleTree;
    
    /**
     * 評議員署名情報
     */
    private List<CouncilSignature> signatures;
    
    /**
     * ブロック内のトランザクション
     */
    private List<CouncilTransaction> transactions;
    
    /**
     * 制度的整合性証明
     */
    private IntegrityProof integrityProof;
    
    /**
     * ブロック基本情報
     */
    @Data
    @Builder
    public static class BlockInfo {
        /**
         * ブロック番号
         */
        private Long blockNumber;
        
        /**
         * ブロックハッシュ
         */
        private String blockHash;
        
        /**
         * 前ブロックハッシュ
         */
        private String previousBlockHash;
        
        /**
         * Merkleルートハッシュ
         */
        private String merkleRoot;
        
        /**
         * ブロック生成時刻
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant timestamp;
        
        /**
         * ブロック生成者（提案評議員）
         */
        private String proposer;
        
        /**
         * ブロックサイズ（バイト）
         */
        private Long size;
        
        /**
         * トランザクション数
         */
        private Integer transactionCount;
        
        /**
         * 必要署名数
         */
        private Integer requiredSignatures;
        
        /**
         * 実際の署名数
         */
        private Integer actualSignatures;
        
        /**
         * ブロック承認状態
         */
        private BlockStatus status;
        
        /**
         * ブロック承認度（パーセンテージ）
         */
        private Double approvalRate;
        
        public enum BlockStatus {
            PENDING("承認待ち"),
            APPROVED("承認済み"),
            REJECTED("却下"),
            EXPIRED("期限切れ");
            
            private final String description;
            
            BlockStatus(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * Merkle Tree情報
     */
    @Data
    @Builder
    public static class MerkleTreeInfo {
        /**
         * ルートハッシュ
         */
        private String rootHash;
        
        /**
         * 木の深さ
         */
        private Integer depth;
        
        /**
         * リーフノード数
         */
        private Integer leafCount;
        
        /**
         * Merkle証明パス
         */
        private List<MerkleNode> proofPath;
        
        /**
         * 各レベルのハッシュリスト
         */
        private Map<Integer, List<String>> levelHashes;
        
        /**
         * ツリー構造の可視化データ
         */
        private TreeVisualization visualization;
    }
    
    /**
     * Merkleノード
     */
    @Data
    @Builder
    public static class MerkleNode {
        /**
         * ノードハッシュ
         */
        private String hash;
        
        /**
         * ノード位置（レベル、インデックス）
         */
        private NodePosition position;
        
        /**
         * 左の子ノード
         */
        private String leftChild;
        
        /**
         * 右の子ノード
         */
        private String rightChild;
        
        /**
         * ノードの種類
         */
        private NodeType type;
        
        /**
         * 対応するトランザクションID（リーフノードの場合）
         */
        private String transactionId;
        
        public enum NodeType {
            ROOT("ルート"),
            INTERNAL("内部ノード"),
            LEAF("リーフ");
            
            private final String description;
            
            NodeType(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * ノード位置
     */
    @Data
    @Builder
    public static class NodePosition {
        /**
         * ツリーレベル（0がルート）
         */
        private Integer level;
        
        /**
         * レベル内のインデックス
         */
        private Integer index;
        
        /**
         * x座標（可視化用）
         */
        private Double x;
        
        /**
         * y座標（可視化用）
         */
        private Double y;
    }
    
    /**
     * ツリー可視化情報
     */
    @Data
    @Builder
    public static class TreeVisualization {
        /**
         * SVGパス要素
         */
        private List<SvgElement> elements;
        
        /**
         * 全体の幅
         */
        private Double width;
        
        /**
         * 全体の高さ
         */
        private Double height;
        
        /**
         * ノード間の線情報
         */
        private List<Connection> connections;
    }
    
    /**
     * SVG要素
     */
    @Data
    @Builder
    public static class SvgElement {
        private String type; // "circle", "text", "line"
        private Map<String, Object> attributes;
        private String content;
    }
    
    /**
     * ノード間接続情報
     */
    @Data
    @Builder
    public static class Connection {
        private NodePosition from;
        private NodePosition to;
        private String color;
        private Double strokeWidth;
    }
    
    /**
     * 評議員署名情報
     */
    @Data
    @Builder
    public static class CouncilSignature {
        /**
         * 署名者の評議員ID
         */
        private String councilMemberId;
        
        /**
         * 署名者名
         */
        private String signerName;
        
        /**
         * デジタル署名
         */
        private String signature;
        
        /**
         * 公開鍵
         */
        private String publicKey;
        
        /**
         * 署名時刻
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant signedAt;
        
        /**
         * 署名検証結果
         */
        private SignatureVerification verification;
        
        /**
         * 投票の重み
         */
        private Double votingWeight;
        
        /**
         * 署名理由/コメント
         */
        private String comment;
    }
    
    /**
     * 署名検証情報
     */
    @Data
    @Builder
    public static class SignatureVerification {
        /**
         * 検証結果
         */
        private boolean isValid;
        
        /**
         * 検証時刻
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant verifiedAt;
        
        /**
         * 検証アルゴリズム
         */
        private String algorithm;
        
        /**
         * 証明書チェーン
         */
        private List<String> certificateChain;
        
        /**
         * 検証エラー（ある場合）
         */
        private String errorMessage;
    }
    
    /**
     * 評議員トランザクション
     */
    @Data
    @Builder
    public static class CouncilTransaction {
        /**
         * トランザクションID
         */
        private String transactionId;
        
        /**
         * トランザクションハッシュ
         */
        private String hash;
        
        /**
         * トランザクション種別
         */
        private TransactionType type;
        
        /**
         * 実行者
         */
        private String executor;
        
        /**
         * 対象パラメータ
         */
        private String targetParameter;
        
        /**
         * 変更内容
         */
        private Map<String, Object> changes;
        
        /**
         * タイムスタンプ
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant timestamp;
        
        /**
         * ガス使用量（処理コスト指標）
         */
        private Long gasUsed;
        
        /**
         * 実行ステータス
         */
        private ExecutionStatus status;
        
        public enum TransactionType {
            PARAMETER_UPDATE("パラメータ更新"),
            COUNCIL_ELECTION("評議員選挙"),
            PROPOSAL_SUBMISSION("提案提出"),
            VOTING("投票"),
            SIGNATURE_VERIFICATION("署名検証"),
            SYSTEM_MAINTENANCE("システムメンテナンス");
            
            private final String description;
            
            TransactionType(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
        
        public enum ExecutionStatus {
            SUCCESS("成功"),
            FAILED("失敗"),
            PENDING("処理中"),
            REVERTED("取り消し");
            
            private final String description;
            
            ExecutionStatus(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 制度的整合性証明
     */
    @Data
    @Builder
    public static class IntegrityProof {
        /**
         * 整合性チェック結果
         */
        private boolean isIntegrityValid;
        
        /**
         * チェック実行時刻
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant checkedAt;
        
        /**
         * 検証したルール一覧
         */
        private List<ValidationRule> validatedRules;
        
        /**
         * 整合性スコア（0-100）
         */
        private Double integrityScore;
        
        /**
         * 制度的コンプライアンス状態
         */
        private ComplianceStatus complianceStatus;
        
        /**
         * 証明書
         */
        private String proofCertificate;
    }
    
    /**
     * バリデーションルール
     */
    @Data
    @Builder
    public static class ValidationRule {
        private String ruleName;
        private String description;
        private boolean passed;
        private String details;
    }
    
    /**
     * コンプライアンス状態
     */
    public enum ComplianceStatus {
        COMPLIANT("準拠"),
        NON_COMPLIANT("非準拠"),
        PARTIAL_COMPLIANCE("部分準拠"),
        UNDER_REVIEW("審査中");
        
        private final String description;
        
        ComplianceStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
