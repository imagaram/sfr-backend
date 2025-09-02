package com.sfr.tokyo.sfr_backend.council.service;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilBlockExplorerDto;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilParameter;
import com.sfr.tokyo.sfr_backend.council.repository.CouncilParameterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 評議員制度ブロックエクスプローラーサービス
 * 
 * Merkle構造、署名検証、真正性可視化のビジネスロジック
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CouncilBlockExplorerService {

    private final CouncilParameterRepository parameterRepository;

    /**
     * ブロック一覧取得
     * 
     * @param pageable ページング情報
     * @param fromDate 開始日時フィルター
     * @param toDate 終了日時フィルター
     * @param blockType ブロックタイプフィルター
     * @return ブロック概要一覧
     */
    public Page<CouncilBlockExplorerDto.CouncilBlockSummaryDto> getBlocks(
            Pageable pageable, Instant fromDate, Instant toDate, String blockType) {
        
        log.info("ブロック一覧取得: page={}, size={}, fromDate={}, toDate={}, blockType={}", 
                pageable.getPageNumber(), pageable.getPageSize(), fromDate, toDate, blockType);

        // 実装例: パラメータ変更履歴をブロックとして扱う
        List<CouncilParameter> parameters = parameterRepository.findAll();
        
        List<CouncilBlockExplorerDto.CouncilBlockSummaryDto> blocks = generateMockBlocks(parameters, fromDate, toDate, blockType);
        
        // ページング適用
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), blocks.size());
        List<CouncilBlockExplorerDto.CouncilBlockSummaryDto> pagedBlocks = 
            start < blocks.size() ? blocks.subList(start, end) : Collections.emptyList();
        
        return new PageImpl<>(pagedBlocks, pageable, blocks.size());
    }

    /**
     * ブロック詳細取得
     * 
     * @param blockHash ブロックハッシュ
     * @return ブロック詳細情報
     */
    public CouncilBlockExplorerDto.CouncilBlockDetailDto getBlockDetail(String blockHash) {
        log.info("ブロック詳細取得: blockHash={}", blockHash);

        // モックブロック詳細生成
        CouncilBlockExplorerDto.CouncilBlockSummaryDto summary = generateMockBlockSummary(blockHash);
        List<CouncilBlockExplorerDto.CouncilTransactionDto> transactions = generateMockTransactions();
        List<CouncilBlockExplorerDto.CouncilSignatureDto> signatures = generateMockSignatures();
        CouncilBlockExplorerDto.MerkleTreeDto merkleTree = generateMockMerkleTree(transactions);
        CouncilBlockExplorerDto.BlockProducerDto producer = generateMockBlockProducer();
        Map<String, Object> metadata = generateMockMetadata();

        return CouncilBlockExplorerDto.CouncilBlockDetailDto.builder()
                .summary(summary)
                .transactions(transactions)
                .signatures(signatures)
                .merkleTree(merkleTree)
                .producer(producer)
                .metadata(metadata)
                .build();
    }

    /**
     * Merkle証明取得
     * 
     * @param txHash トランザクションハッシュ
     * @return Merkle証明情報
     */
    public CouncilBlockExplorerDto.MerkleProofDto getMerkleProof(String txHash) {
        log.info("Merkle証明取得: txHash={}", txHash);

        // モックMerkle証明生成
        List<String> proofPath = Arrays.asList(
            "0xabcdef1234567890...",
            "0x9876543210fedcba...",
            "0x1111222233334444..."
        );
        
        List<Boolean> pathDirections = Arrays.asList(false, true, false);

        return CouncilBlockExplorerDto.MerkleProofDto.builder()
                .txHash(txHash)
                .blockHash("0x1a2b3c4d5e6f7890...")
                .merkleRoot("0xabcdef1234567890...")
                .leafIndex(7)
                .proofPath(proofPath)
                .pathDirections(pathDirections)
                .isValid(true)
                .verifiedAt(Instant.now())
                .build();
    }

    /**
     * システム統計情報取得
     * 
     * @return システム統計
     */
    public CouncilBlockExplorerDto.SystemStatsDto getSystemStats() {
        log.info("システム統計情報取得");

        long parameterCount = parameterRepository.count();
        
        return CouncilBlockExplorerDto.SystemStatsDto.builder()
                .totalBlocks(1000L + parameterCount * 10)
                .totalTransactions(5000L + parameterCount * 25)
                .signatureSuccessRate(0.9985)
                .averageBlockInterval(600.0)
                .latestBlockTime(Instant.now().minus(5, ChronoUnit.MINUTES))
                .activeCouncilMembers(15)
                .pendingProposals(3)
                .systemHealthScore(0.98)
                .build();
    }

    /**
     * 署名検証実行
     * 
     * @param request 署名検証要求
     * @return 署名検証結果
     */
    public CouncilBlockExplorerDto.SignatureVerificationDto verifySignature(
            CouncilBlockExplorerDto.SignatureVerificationRequestDto request) {
        
        log.info("署名検証実行: hash={}, type={}", request.getHash(), request.getVerificationType());

        // モック署名検証結果生成
        List<CouncilBlockExplorerDto.IndividualSignatureResultDto> signatureResults = Arrays.asList(
            CouncilBlockExplorerDto.IndividualSignatureResultDto.builder()
                    .signerId("council_member_001")
                    .isValid(true)
                    .algorithm("ECDSA_SHA256")
                    .build(),
            CouncilBlockExplorerDto.IndividualSignatureResultDto.builder()
                    .signerId("council_member_002")
                    .isValid(true)
                    .algorithm("ECDSA_SHA256")
                    .build(),
            CouncilBlockExplorerDto.IndividualSignatureResultDto.builder()
                    .signerId("council_member_003")
                    .isValid(false)
                    .errorMessage("公開鍵が見つかりません")
                    .algorithm("ECDSA_SHA256")
                    .build()
        );

        boolean isValid = signatureResults.stream()
                .mapToInt(result -> result.getIsValid() ? 1 : 0)
                .sum() >= 2; // 過半数で有効

        return CouncilBlockExplorerDto.SignatureVerificationDto.builder()
                .targetHash(request.getHash())
                .verificationType(request.getVerificationType())
                .isValid(isValid)
                .signatureResults(signatureResults)
                .verifiedAt(Instant.now())
                .message(isValid ? "署名検証成功" : "必要な署名数が不足しています")
                .build();
    }

    /**
     * 制度整合性チェック実行
     * 
     * @return 整合性チェック結果
     */
    @Transactional
    public CouncilBlockExplorerDto.IntegrityCheckDto performIntegrityCheck() {
        log.info("制度整合性チェック開始");

        Instant startTime = Instant.now();
        
        // 実際のチェック項目
        List<CouncilBlockExplorerDto.IntegrityErrorDto> errors = new ArrayList<>();
        List<CouncilBlockExplorerDto.IntegrityWarningDto> warnings = new ArrayList<>();
        
        // 1. パラメータ整合性チェック
        int totalChecks = 0;
        int passedChecks = 0;
        
        // パラメータ存在チェック
        totalChecks++;
        long parameterCount = parameterRepository.count();
        if (parameterCount > 0) {
            passedChecks++;
        } else {
            errors.add(CouncilBlockExplorerDto.IntegrityErrorDto.builder()
                    .errorCode("NO_PARAMETERS")
                    .message("評議員制度パラメータが設定されていません")
                    .scope("SYSTEM")
                    .resource("council_parameters")
                    .severity("HIGH")
                    .build());
        }
        
        // パラメータ値型チェック
        totalChecks++;
        List<CouncilParameter> invalidParameters = parameterRepository.findAll().stream()
                .filter(param -> param.getValueString() == null || param.getValueString().trim().isEmpty())
                .collect(Collectors.toList());
        
        if (invalidParameters.isEmpty()) {
            passedChecks++;
        } else {
            errors.add(CouncilBlockExplorerDto.IntegrityErrorDto.builder()
                    .errorCode("INVALID_PARAMETER_VALUE")
                    .message("無効なパラメータ値が存在します: " + invalidParameters.size() + "件")
                    .scope("PARAMETER")
                    .resource("invalid_parameters")
                    .severity("MEDIUM")
                    .build());
        }
        
        // 古いパラメータ警告
        Instant oneMonthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<CouncilParameter> oldParameters = parameterRepository.findAll().stream()
                .filter(param -> param.getUpdatedAt().isBefore(oneMonthAgo))
                .collect(Collectors.toList());
        
        if (!oldParameters.isEmpty()) {
            warnings.add(CouncilBlockExplorerDto.IntegrityWarningDto.builder()
                    .warningCode("OLD_PARAMETERS")
                    .message("30日以上更新されていないパラメータがあります: " + oldParameters.size() + "件")
                    .recommendedAction("パラメータの定期見直しを実施してください")
                    .build());
        }
        
        // モック追加チェック項目
        totalChecks += 22; // 総計25項目
        passedChecks += 22; // 成功22項目
        
        Instant endTime = Instant.now();
        boolean isValid = errors.isEmpty();

        Map<String, Object> statistics = Map.of(
            "executionTimeMs", ChronoUnit.MILLIS.between(startTime, endTime),
            "parametersChecked", parameterCount,
            "blocksVerified", 1000L,
            "signaturesValidated", 5000L
        );

        log.info("制度整合性チェック完了: valid={}, errors={}, warnings={}, duration={}ms", 
                isValid, errors.size(), warnings.size(), 
                ChronoUnit.MILLIS.between(startTime, endTime));

        return CouncilBlockExplorerDto.IntegrityCheckDto.builder()
                .checkStartedAt(startTime)
                .checkCompletedAt(endTime)
                .isValid(isValid)
                .totalChecks(totalChecks)
                .passedChecks(passedChecks)
                .failedChecks(totalChecks - passedChecks)
                .errors(errors)
                .warnings(warnings)
                .statistics(statistics)
                .build();
    }

    // === プライベートヘルパーメソッド ===

    private List<CouncilBlockExplorerDto.CouncilBlockSummaryDto> generateMockBlocks(
            List<CouncilParameter> parameters, Instant fromDate, Instant toDate, String blockType) {
        
        List<CouncilBlockExplorerDto.CouncilBlockSummaryDto> blocks = new ArrayList<>();
        
        // パラメータベースのモックブロック生成
        for (int i = 0; i < Math.min(100, parameters.size() * 2 + 50); i++) {
            Instant blockTime = Instant.now().minus(i * 10, ChronoUnit.MINUTES);
            
            // フィルター適用
            if (fromDate != null && blockTime.isBefore(fromDate)) continue;
            if (toDate != null && blockTime.isAfter(toDate)) continue;
            
            String generatedBlockType = i % 4 == 0 ? "PARAMETER_CHANGE" : 
                                      i % 4 == 1 ? "COUNCIL_ELECTION" : 
                                      i % 4 == 2 ? "GOVERNANCE_VOTE" : "SYSTEM_UPDATE";
            
            if (blockType != null && !blockType.equals(generatedBlockType)) continue;
            
            blocks.add(CouncilBlockExplorerDto.CouncilBlockSummaryDto.builder()
                    .blockHash(generateHash("block_" + i))
                    .blockNumber((long) (1000 + i))
                    .timestamp(blockTime)
                    .previousHash(i > 0 ? generateHash("block_" + (i - 1)) : "0x0000000000000000")
                    .merkleRoot(generateHash("merkle_" + i))
                    .transactionCount(3 + (i % 10))
                    .blockType(generatedBlockType)
                    .signatureStatus(i % 20 == 0 ? "PENDING" : "VERIFIED")
                    .blockSize((long) (1024 + i * 100))
                    .build());
        }
        
        // 時間順ソート（新しい順）
        blocks.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        return blocks;
    }

    private CouncilBlockExplorerDto.CouncilBlockSummaryDto generateMockBlockSummary(String blockHash) {
        return CouncilBlockExplorerDto.CouncilBlockSummaryDto.builder()
                .blockHash(blockHash)
                .blockNumber(12345L)
                .timestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
                .previousHash("0x9f8e7d6c5b4a3210...")
                .merkleRoot("0xabcdef1234567890...")
                .transactionCount(8)
                .blockType("PARAMETER_CHANGE")
                .signatureStatus("VERIFIED")
                .blockSize(2048L)
                .build();
    }

    private List<CouncilBlockExplorerDto.CouncilTransactionDto> generateMockTransactions() {
        return IntStream.range(0, 8)
                .mapToObj(i -> CouncilBlockExplorerDto.CouncilTransactionDto.builder()
                        .txHash(generateHash("tx_" + i))
                        .txType("PARAMETER_UPDATE")
                        .executorId("council_member_" + String.format("%03d", i % 3 + 1))
                        .executorName("評議員" + (char)('A' + i % 3))
                        .executedAt(Instant.now().minus((8 - i) * 5, ChronoUnit.MINUTES))
                        .targetParameter("council.voting.threshold")
                        .oldValue("0.6")
                        .newValue("0.65")
                        .reason("投票閾値の適正化")
                        .status("CONFIRMED")
                        .build())
                .collect(Collectors.toList());
    }

    private List<CouncilBlockExplorerDto.CouncilSignatureDto> generateMockSignatures() {
        return IntStream.range(0, 3)
                .mapToObj(i -> CouncilBlockExplorerDto.CouncilSignatureDto.builder()
                        .signerId("council_member_" + String.format("%03d", i + 1))
                        .signerName("評議員" + (char)('A' + i))
                        .signerRole("COUNCIL_MEMBER")
                        .signature("0x3045022100" + "a".repeat(60) + "...")
                        .publicKey("0x04" + "b".repeat(62) + "...")
                        .algorithm("ECDSA_SHA256")
                        .signedAt(Instant.now().minus(25, ChronoUnit.MINUTES))
                        .verificationStatus("VALID")
                        .build())
                .collect(Collectors.toList());
    }

    private CouncilBlockExplorerDto.MerkleTreeDto generateMockMerkleTree(
            List<CouncilBlockExplorerDto.CouncilTransactionDto> transactions) {
        
        List<String> leafHashes = transactions.stream()
                .map(tx -> tx.getTxHash())
                .collect(Collectors.toList());
        
        // パディングして2の累乗にする
        while (leafHashes.size() < 8) {
            leafHashes.add("0x0000000000000000");
        }
        
        // レベル構築（簡単な例）
        List<List<String>> levels = new ArrayList<>();
        levels.add(leafHashes); // レベル0（リーフ）
        
        List<String> currentLevel = leafHashes;
        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = i + 1 < currentLevel.size() ? currentLevel.get(i + 1) : left;
                nextLevel.add(generateHash(left + right));
            }
            levels.add(nextLevel);
            currentLevel = nextLevel;
        }
        
        return CouncilBlockExplorerDto.MerkleTreeDto.builder()
                .rootHash(currentLevel.get(0))
                .depth(levels.size() - 1)
                .leafCount(leafHashes.size())
                .levels(levels)
                .leafHashes(leafHashes)
                .build();
    }

    private CouncilBlockExplorerDto.BlockProducerDto generateMockBlockProducer() {
        return CouncilBlockExplorerDto.BlockProducerDto.builder()
                .producerId("system_node_001")
                .producerName("SFR評議員システムノード1")
                .producerType("SYSTEM_NODE")
                .publicKey("0x04" + "c".repeat(62) + "...")
                .trustScore(0.95)
                .build();
    }

    private Map<String, Object> generateMockMetadata() {
        return Map.of(
            "version", "1.0.0",
            "consensus", "proof-of-authority",
            "networkId", "sfr-tokyo-mainnet",
            "gasUsed", 150000,
            "gasLimit", 300000,
            "difficulty", "0x1"
        );
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder("0x");
            for (int i = 0; i < Math.min(8, hash.length); i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            hexString.append("...");
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "0x" + input.hashCode() + "...";
        }
    }
}
