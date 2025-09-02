package com.sfr.tokyo.sfr_backend.council.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 評議員制度ブロックエクスプローラー用DTOクラス群
 * 
 * Merkle構造、ブロック署名、真正性可視化のためのデータ構造
 */
public class CouncilBlockExplorerDto {

    /**
     * ブロック概要情報DTO
     * ブロック一覧表示用
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "評議員制度ブロック概要情報")
    public static class CouncilBlockSummaryDto {
        
        @Schema(description = "ブロックハッシュ", example = "0x1a2b3c4d5e6f7890abcdef...")
        @JsonProperty("blockHash")
        private String blockHash;
        
        @Schema(description = "ブロック番号", example = "12345")
        @JsonProperty("blockNumber")
        private Long blockNumber;
        
        @Schema(description = "ブロック作成日時")
        @JsonProperty("timestamp")
        private Instant timestamp;
        
        @Schema(description = "前ブロックハッシュ", example = "0x9f8e7d6c5b4a3210...")
        @JsonProperty("previousHash")
        private String previousHash;
        
        @Schema(description = "Merkleルートハッシュ", example = "0xabcdef1234567890...")
        @JsonProperty("merkleRoot")
        private String merkleRoot;
        
        @Schema(description = "ブロック内トランザクション数", example = "15")
        @JsonProperty("transactionCount")
        private Integer transactionCount;
        
        @Schema(description = "ブロックタイプ", example = "PARAMETER_CHANGE", 
                allowableValues = {"PARAMETER_CHANGE", "COUNCIL_ELECTION", "GOVERNANCE_VOTE", "SYSTEM_UPDATE"})
        @JsonProperty("blockType")
        private String blockType;
        
        @Schema(description = "署名検証状態", example = "VERIFIED")
        @JsonProperty("signatureStatus")
        private String signatureStatus;
        
        @Schema(description = "ブロックサイズ（バイト）", example = "2048")
        @JsonProperty("blockSize")
        private Long blockSize;
    }

    /**
     * ブロック詳細情報DTO
     * 個別ブロック表示用
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "評議員制度ブロック詳細情報")
    public static class CouncilBlockDetailDto {
        
        @Schema(description = "ブロック概要情報")
        @JsonProperty("summary")
        private CouncilBlockSummaryDto summary;
        
        @Schema(description = "ブロック内トランザクション一覧")
        @JsonProperty("transactions")
        private List<CouncilTransactionDto> transactions;
        
        @Schema(description = "デジタル署名情報")
        @JsonProperty("signatures")
        private List<CouncilSignatureDto> signatures;
        
        @Schema(description = "Merkle Tree構造")
        @JsonProperty("merkleTree")
        private MerkleTreeDto merkleTree;
        
        @Schema(description = "ブロック生成者情報")
        @JsonProperty("producer")
        private BlockProducerDto producer;
        
        @Schema(description = "ブロックメタデータ")
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
    }

    /**
     * トランザクション情報DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "評議員制度トランザクション情報")
    public static class CouncilTransactionDto {
        
        @Schema(description = "トランザクションハッシュ", example = "0x9f8e7d...")
        @JsonProperty("txHash")
        private String txHash;
        
        @Schema(description = "トランザクションタイプ", example = "PARAMETER_UPDATE")
        @JsonProperty("txType")
        private String txType;
        
        @Schema(description = "実行者ID", example = "user123")
        @JsonProperty("executorId")
        private String executorId;
        
        @Schema(description = "実行者名", example = "田中太郎")
        @JsonProperty("executorName")
        private String executorName;
        
        @Schema(description = "実行日時")
        @JsonProperty("executedAt")
        private Instant executedAt;
        
        @Schema(description = "対象パラメータキー", example = "council.voting.threshold")
        @JsonProperty("targetParameter")
        private String targetParameter;
        
        @Schema(description = "変更前の値", example = "0.6")
        @JsonProperty("oldValue")
        private String oldValue;
        
        @Schema(description = "変更後の値", example = "0.65")
        @JsonProperty("newValue")
        private String newValue;
        
        @Schema(description = "変更理由・説明")
        @JsonProperty("reason")
        private String reason;
        
        @Schema(description = "トランザクション状態", example = "CONFIRMED")
        @JsonProperty("status")
        private String status;
    }

    /**
     * デジタル署名情報DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "デジタル署名情報")
    public static class CouncilSignatureDto {
        
        @Schema(description = "署名者ID", example = "council_member_001")
        @JsonProperty("signerId")
        private String signerId;
        
        @Schema(description = "署名者名", example = "山田花子")
        @JsonProperty("signerName")
        private String signerName;
        
        @Schema(description = "署名者役職", example = "COUNCIL_MEMBER")
        @JsonProperty("signerRole")
        private String signerRole;
        
        @Schema(description = "署名値", example = "0x3045022100...")
        @JsonProperty("signature")
        private String signature;
        
        @Schema(description = "公開鍵", example = "0x04abcdef...")
        @JsonProperty("publicKey")
        private String publicKey;
        
        @Schema(description = "署名アルゴリズム", example = "ECDSA_SHA256")
        @JsonProperty("algorithm")
        private String algorithm;
        
        @Schema(description = "署名日時")
        @JsonProperty("signedAt")
        private Instant signedAt;
        
        @Schema(description = "検証状態", example = "VALID")
        @JsonProperty("verificationStatus")
        private String verificationStatus;
    }

    /**
     * Merkle Tree構造DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Merkle Tree構造情報")
    public static class MerkleTreeDto {
        
        @Schema(description = "ルートハッシュ", example = "0xabcdef1234...")
        @JsonProperty("rootHash")
        private String rootHash;
        
        @Schema(description = "Tree深度", example = "4")
        @JsonProperty("depth")
        private Integer depth;
        
        @Schema(description = "リーフノード数", example = "16")
        @JsonProperty("leafCount")
        private Integer leafCount;
        
        @Schema(description = "Tree構造（階層別ハッシュ）")
        @JsonProperty("levels")
        private List<List<String>> levels;
        
        @Schema(description = "リーフハッシュ一覧")
        @JsonProperty("leafHashes")
        private List<String> leafHashes;
    }

    /**
     * Merkle証明DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Merkle証明情報")
    public static class MerkleProofDto {
        
        @Schema(description = "対象トランザクションハッシュ", example = "0x9f8e7d...")
        @JsonProperty("txHash")
        private String txHash;
        
        @Schema(description = "ブロックハッシュ", example = "0x1a2b3c...")
        @JsonProperty("blockHash")
        private String blockHash;
        
        @Schema(description = "Merkleルートハッシュ", example = "0xabcdef...")
        @JsonProperty("merkleRoot")
        private String merkleRoot;
        
        @Schema(description = "Tree内のインデックス", example = "7")
        @JsonProperty("leafIndex")
        private Integer leafIndex;
        
        @Schema(description = "証明パス（隣接ハッシュ一覧）")
        @JsonProperty("proofPath")
        private List<String> proofPath;
        
        @Schema(description = "パス方向（左: false, 右: true）")
        @JsonProperty("pathDirections")
        private List<Boolean> pathDirections;
        
        @Schema(description = "証明検証結果")
        @JsonProperty("isValid")
        private Boolean isValid;
        
        @Schema(description = "検証日時")
        @JsonProperty("verifiedAt")
        private Instant verifiedAt;
    }

    /**
     * ブロック生成者情報DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "ブロック生成者情報")
    public static class BlockProducerDto {
        
        @Schema(description = "生成者ID", example = "system_node_001")
        @JsonProperty("producerId")
        private String producerId;
        
        @Schema(description = "生成者名", example = "システムノード1")
        @JsonProperty("producerName")
        private String producerName;
        
        @Schema(description = "生成者タイプ", example = "SYSTEM_NODE")
        @JsonProperty("producerType")
        private String producerType;
        
        @Schema(description = "公開鍵", example = "0x04abcdef...")
        @JsonProperty("publicKey")
        private String publicKey;
        
        @Schema(description = "信頼スコア", example = "0.95")
        @JsonProperty("trustScore")
        private Double trustScore;
    }

    /**
     * システム統計情報DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "評議員制度システム統計情報")
    public static class SystemStatsDto {
        
        @Schema(description = "総ブロック数", example = "12345")
        @JsonProperty("totalBlocks")
        private Long totalBlocks;
        
        @Schema(description = "総トランザクション数", example = "98765")
        @JsonProperty("totalTransactions")
        private Long totalTransactions;
        
        @Schema(description = "署名検証成功率", example = "0.9985")
        @JsonProperty("signatureSuccessRate")
        private Double signatureSuccessRate;
        
        @Schema(description = "平均ブロック間隔（秒）", example = "600")
        @JsonProperty("averageBlockInterval")
        private Double averageBlockInterval;
        
        @Schema(description = "最新ブロック日時")
        @JsonProperty("latestBlockTime")
        private Instant latestBlockTime;
        
        @Schema(description = "アクティブ評議員数", example = "15")
        @JsonProperty("activeCouncilMembers")
        private Integer activeCouncilMembers;
        
        @Schema(description = "保留中提案数", example = "3")
        @JsonProperty("pendingProposals")
        private Integer pendingProposals;
        
        @Schema(description = "システム健全性スコア", example = "0.98")
        @JsonProperty("systemHealthScore")
        private Double systemHealthScore;
    }

    /**
     * 署名検証要求DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "署名検証要求")
    public static class SignatureVerificationRequestDto {
        
        @Schema(description = "検証対象ハッシュ", example = "0x1a2b3c...")
        @JsonProperty("hash")
        private String hash;
        
        @Schema(description = "検証タイプ", example = "BLOCK", 
                allowableValues = {"BLOCK", "TRANSACTION", "PARAMETER"})
        @JsonProperty("verificationType")
        private String verificationType;
        
        @Schema(description = "追加検証オプション")
        @JsonProperty("options")
        private Map<String, Object> options;
    }

    /**
     * 署名検証結果DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "署名検証結果")
    public static class SignatureVerificationDto {
        
        @Schema(description = "検証対象ハッシュ", example = "0x1a2b3c...")
        @JsonProperty("targetHash")
        private String targetHash;
        
        @Schema(description = "検証タイプ", example = "BLOCK")
        @JsonProperty("verificationType")
        private String verificationType;
        
        @Schema(description = "全体検証結果")
        @JsonProperty("isValid")
        private Boolean isValid;
        
        @Schema(description = "個別署名検証結果")
        @JsonProperty("signatureResults")
        private List<IndividualSignatureResultDto> signatureResults;
        
        @Schema(description = "検証実行日時")
        @JsonProperty("verifiedAt")
        private Instant verifiedAt;
        
        @Schema(description = "検証メッセージ")
        @JsonProperty("message")
        private String message;
    }

    /**
     * 個別署名検証結果DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "個別署名検証結果")
    public static class IndividualSignatureResultDto {
        
        @Schema(description = "署名者ID", example = "council_member_001")
        @JsonProperty("signerId")
        private String signerId;
        
        @Schema(description = "署名検証結果")
        @JsonProperty("isValid")
        private Boolean isValid;
        
        @Schema(description = "検証エラーメッセージ")
        @JsonProperty("errorMessage")
        private String errorMessage;
        
        @Schema(description = "署名アルゴリズム", example = "ECDSA_SHA256")
        @JsonProperty("algorithm")
        private String algorithm;
    }

    /**
     * 整合性チェック結果DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "制度整合性チェック結果")
    public static class IntegrityCheckDto {
        
        @Schema(description = "整合性チェック開始日時")
        @JsonProperty("checkStartedAt")
        private Instant checkStartedAt;
        
        @Schema(description = "整合性チェック完了日時")
        @JsonProperty("checkCompletedAt")
        private Instant checkCompletedAt;
        
        @Schema(description = "全体整合性結果")
        @JsonProperty("isValid")
        private Boolean isValid;
        
        @Schema(description = "チェック項目数", example = "25")
        @JsonProperty("totalChecks")
        private Integer totalChecks;
        
        @Schema(description = "成功項目数", example = "24")
        @JsonProperty("passedChecks")
        private Integer passedChecks;
        
        @Schema(description = "失敗項目数", example = "1")
        @JsonProperty("failedChecks")
        private Integer failedChecks;
        
        @Schema(description = "エラー詳細")
        @JsonProperty("errors")
        private List<IntegrityErrorDto> errors;
        
        @Schema(description = "警告一覧")
        @JsonProperty("warnings")
        private List<IntegrityWarningDto> warnings;
        
        @Schema(description = "チェック統計")
        @JsonProperty("statistics")
        private Map<String, Object> statistics;
    }

    /**
     * 整合性エラーDTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "整合性チェックエラー")
    public static class IntegrityErrorDto {
        
        @Schema(description = "エラーコード", example = "MERKLE_MISMATCH")
        @JsonProperty("errorCode")
        private String errorCode;
        
        @Schema(description = "エラーメッセージ")
        @JsonProperty("message")
        private String message;
        
        @Schema(description = "影響範囲", example = "BLOCK")
        @JsonProperty("scope")
        private String scope;
        
        @Schema(description = "対象リソース", example = "block_12345")
        @JsonProperty("resource")
        private String resource;
        
        @Schema(description = "重要度", example = "HIGH")
        @JsonProperty("severity")
        private String severity;
    }

    /**
     * 整合性警告DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "整合性チェック警告")
    public static class IntegrityWarningDto {
        
        @Schema(description = "警告コード", example = "SIGNATURE_AGING")
        @JsonProperty("warningCode")
        private String warningCode;
        
        @Schema(description = "警告メッセージ")
        @JsonProperty("message")
        private String message;
        
        @Schema(description = "推奨アクション")
        @JsonProperty("recommendedAction")
        private String recommendedAction;
    }
}
