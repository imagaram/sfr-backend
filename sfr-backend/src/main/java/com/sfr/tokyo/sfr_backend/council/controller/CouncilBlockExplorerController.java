package com.sfr.tokyo.sfr_backend.council.controller;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilBlockExplorerDto;
import com.sfr.tokyo.sfr_backend.council.service.CouncilBlockExplorerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * 評議員制度ブロックエクスプローラーAPI
 * 
 * Merkle構造、ブロック署名、制度の真正性を可視化する
 * ブロックチェーン風UIによる透明性確保
 */
@RestController
@RequestMapping("/api/v1/council/explorer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Council Block Explorer", 
     description = "評議員制度ブロックエクスプローラー - Merkle構造・署名検証・真正性の可視化")
public class CouncilBlockExplorerController {

    private final CouncilBlockExplorerService explorerService;

    @Operation(
        summary = "制度ブロック一覧取得",
        description = "評議員制度の全ブロック情報をページング形式で取得。Merkle構造の可視化に使用"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", 
                    description = "ブロック一覧取得成功",
                    content = @Content(schema = @Schema(implementation = CouncilBlockExplorerDto.CouncilBlockSummaryDto.class))),
        @ApiResponse(responseCode = "403", description = "アクセス権限なし")
    })
    @GetMapping("/blocks")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('COUNCIL_MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<Page<CouncilBlockExplorerDto.CouncilBlockSummaryDto>> getBlocks(
            @Parameter(description = "ページング情報") Pageable pageable,
            @Parameter(description = "開始日時フィルター") @RequestParam(required = false) Instant fromDate,
            @Parameter(description = "終了日時フィルター") @RequestParam(required = false) Instant toDate,
            @Parameter(description = "ブロックタイプフィルター") @RequestParam(required = false) String blockType) {
        
        log.info("ブロック一覧取得要求: page={}, fromDate={}, toDate={}, blockType={}", 
                pageable.getPageNumber(), fromDate, toDate, blockType);
        
        Page<CouncilBlockExplorerDto.CouncilBlockSummaryDto> blocks = 
            explorerService.getBlocks(pageable, fromDate, toDate, blockType);
        
        return ResponseEntity.ok(blocks);
    }

    @Operation(
        summary = "特定ブロック詳細取得",
        description = "指定されたブロックハッシュの詳細情報を取得。Merkle Proof、署名検証データを含む"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", 
                    description = "ブロック詳細取得成功",
                    content = @Content(schema = @Schema(implementation = CouncilBlockExplorerDto.CouncilBlockDetailDto.class))),
        @ApiResponse(responseCode = "404", description = "ブロックが見つかりません"),
        @ApiResponse(responseCode = "403", description = "アクセス権限なし")
    })
    @GetMapping("/blocks/{blockHash}")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('COUNCIL_MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<CouncilBlockExplorerDto.CouncilBlockDetailDto> getBlockDetail(
            @Parameter(description = "ブロックハッシュ", example = "0x1a2b3c...") 
            @PathVariable String blockHash) {
        
        log.info("ブロック詳細取得要求: blockHash={}", blockHash);
        
        CouncilBlockExplorerDto.CouncilBlockDetailDto blockDetail = 
            explorerService.getBlockDetail(blockHash);
        
        return ResponseEntity.ok(blockDetail);
    }

    @Operation(
        summary = "Merkle証明取得",
        description = "指定されたトランザクションのMerkle証明を取得し、ブロック内の位置と真正性を検証"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", 
                    description = "Merkle証明取得成功",
                    content = @Content(schema = @Schema(implementation = CouncilBlockExplorerDto.MerkleProofDto.class))),
        @ApiResponse(responseCode = "404", description = "トランザクションが見つかりません"),
        @ApiResponse(responseCode = "403", description = "アクセス権限なし")
    })
    @GetMapping("/merkle-proof/{txHash}")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('COUNCIL_MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<CouncilBlockExplorerDto.MerkleProofDto> getMerkleProof(
            @Parameter(description = "トランザクションハッシュ", example = "0x9f8e7d...") 
            @PathVariable String txHash) {
        
        log.info("Merkle証明取得要求: txHash={}", txHash);
        
        CouncilBlockExplorerDto.MerkleProofDto merkleProof = 
            explorerService.getMerkleProof(txHash);
        
        return ResponseEntity.ok(merkleProof);
    }

    @Operation(
        summary = "制度統計情報取得",
        description = "評議員制度全体の統計情報を取得。ブロック数、トランザクション数、署名検証率など"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", 
                    description = "統計情報取得成功",
                    content = @Content(schema = @Schema(implementation = CouncilBlockExplorerDto.SystemStatsDto.class))),
        @ApiResponse(responseCode = "403", description = "アクセス権限なし")
    })
    @GetMapping("/stats")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('COUNCIL_MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<CouncilBlockExplorerDto.SystemStatsDto> getSystemStats() {
        
        log.info("制度統計情報取得要求");
        
        CouncilBlockExplorerDto.SystemStatsDto stats = explorerService.getSystemStats();
        
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "署名検証実行",
        description = "指定されたブロックまたはトランザクションの署名を検証し、真正性を確認"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", 
                    description = "署名検証成功",
                    content = @Content(schema = @Schema(implementation = CouncilBlockExplorerDto.SignatureVerificationDto.class))),
        @ApiResponse(responseCode = "400", description = "検証パラメータが不正"),
        @ApiResponse(responseCode = "403", description = "アクセス権限なし")
    })
    @PostMapping("/verify-signature")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('COUNCIL_MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<CouncilBlockExplorerDto.SignatureVerificationDto> verifySignature(
            @Parameter(description = "署名検証要求") 
            @RequestBody CouncilBlockExplorerDto.SignatureVerificationRequestDto request) {
        
        log.info("署名検証要求: hash={}, type={}", request.getHash(), request.getVerificationType());
        
        CouncilBlockExplorerDto.SignatureVerificationDto result = 
            explorerService.verifySignature(request);
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "制度整合性チェック",
        description = "評議員制度全体の整合性をチェックし、データの一貫性と真正性を検証"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", 
                    description = "整合性チェック完了",
                    content = @Content(schema = @Schema(implementation = CouncilBlockExplorerDto.IntegrityCheckDto.class))),
        @ApiResponse(responseCode = "403", description = "アクセス権限なし"),
        @ApiResponse(responseCode = "500", description = "整合性チェック実行エラー")
    })
    @PostMapping("/integrity-check")
    @PreAuthorize("hasRole('COUNCIL_MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<CouncilBlockExplorerDto.IntegrityCheckDto> performIntegrityCheck() {
        
        log.info("制度整合性チェック開始");
        
        CouncilBlockExplorerDto.IntegrityCheckDto result = 
            explorerService.performIntegrityCheck();
        
        log.info("制度整合性チェック完了: valid={}, errors={}", 
                result.getIsValid(), result.getErrors().size());
        
        return ResponseEntity.ok(result);
    }
}
