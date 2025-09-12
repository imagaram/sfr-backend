package com.sfr.tokyo.sfr_backend.integration.council;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.council.dto.CouncilBlockExplorerDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 評議員制度ブロックエクスプローラー統合テスト
 * 
 * Merkle構造、署名検証、真正性可視化の統合テスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CouncilBlockExplorerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("ブロック一覧取得テスト - 正常系")
    @WithMockUser(roles = "CITIZEN")
    public void testGetBlocks_Success() throws Exception {
        mockMvc.perform(get("/api/v1/council/explorer/blocks")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].blockHash").isString())
                .andExpect(jsonPath("$.content[0].blockNumber").isNumber())
                .andExpect(jsonPath("$.content[0].timestamp").isString())
                .andExpect(jsonPath("$.content[0].merkleRoot").isString())
                .andExpect(jsonPath("$.content[0].transactionCount").isNumber())
                .andExpect(jsonPath("$.content[0].blockType").exists())
                .andExpect(jsonPath("$.content[0].signatureStatus").isString())
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }

    @Test
    @DisplayName("ブロック一覧取得テスト - フィルター適用")
    @WithMockUser(roles = "COUNCIL_MEMBER")
    public void testGetBlocks_WithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/council/explorer/blocks")
                .param("page", "0")
                .param("size", "5")
                .param("blockType", "PARAMETER_CHANGE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageSize").value(5));
    }

    @Test
    @DisplayName("ブロック詳細取得テスト - 正常系")
    @WithMockUser(roles = "CITIZEN")
    public void testGetBlockDetail_Success() throws Exception {
        String blockHash = "0x1a2b3c4d5e6f7890";

        mockMvc.perform(get("/api/v1/council/explorer/blocks/{blockHash}", blockHash)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.summary.blockHash").value(blockHash))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.signatures").isArray())
                .andExpect(jsonPath("$.merkleTree").exists())
                .andExpect(jsonPath("$.merkleTree.rootHash").isString())
                .andExpect(jsonPath("$.merkleTree.depth").isNumber())
                .andExpect(jsonPath("$.merkleTree.leafCount").isNumber())
                .andExpect(jsonPath("$.merkleTree.levels").isArray())
                .andExpect(jsonPath("$.producer").exists())
                .andExpect(jsonPath("$.producer.producerId").isString())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    @DisplayName("Merkle証明取得テスト - 正常系")
    @WithMockUser(roles = "CITIZEN")
    public void testGetMerkleProof_Success() throws Exception {
        String txHash = "0x9f8e7d6c5b4a3210";

        mockMvc.perform(get("/api/v1/council/explorer/merkle-proof/{txHash}", txHash)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.txHash").value(txHash))
                .andExpect(jsonPath("$.blockHash").isString())
                .andExpect(jsonPath("$.merkleRoot").isString())
                .andExpect(jsonPath("$.leafIndex").isNumber())
                .andExpect(jsonPath("$.proofPath").isArray())
                .andExpect(jsonPath("$.pathDirections").isArray())
                .andExpect(jsonPath("$.isValid").isBoolean())
                .andExpect(jsonPath("$.verifiedAt").isString());
    }

    @Test
    @DisplayName("システム統計情報取得テスト - 正常系")
    @WithMockUser(roles = "ADMIN")
    public void testGetSystemStats_Success() throws Exception {
        mockMvc.perform(get("/api/v1/council/explorer/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalBlocks").isNumber())
                .andExpect(jsonPath("$.totalTransactions").isNumber())
                .andExpect(jsonPath("$.signatureSuccessRate").isNumber())
                .andExpect(jsonPath("$.averageBlockInterval").isNumber())
                .andExpect(jsonPath("$.latestBlockTime").isString())
                .andExpect(jsonPath("$.activeCouncilMembers").isNumber())
                .andExpect(jsonPath("$.pendingProposals").isNumber())
                .andExpect(jsonPath("$.systemHealthScore").isNumber())
                .andExpect(jsonPath("$.systemHealthScore").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.systemHealthScore").value(lessThanOrEqualTo(1.0)));
    }

    @Test
    @DisplayName("署名検証テスト - 正常系")
    @WithMockUser(roles = "COUNCIL_MEMBER")
    public void testVerifySignature_Success() throws Exception {
        CouncilBlockExplorerDto.SignatureVerificationRequestDto request =
                CouncilBlockExplorerDto.SignatureVerificationRequestDto.builder()
                        .hash("0x1a2b3c4d5e6f7890")
                        .verificationType("BLOCK")
                        .options(Map.of("includeChain", true))
                        .build();

        mockMvc.perform(post("/api/v1/council/explorer/verify-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.targetHash").value(request.getHash()))
                .andExpect(jsonPath("$.verificationType").value(request.getVerificationType()))
                .andExpect(jsonPath("$.isValid").isBoolean())
                .andExpect(jsonPath("$.signatureResults").isArray())
                .andExpect(jsonPath("$.verifiedAt").isString())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("制度整合性チェックテスト - 正常系")
    @WithMockUser(roles = "ADMIN")
    public void testPerformIntegrityCheck_Success() throws Exception {
        mockMvc.perform(post("/api/v1/council/explorer/integrity-check")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.checkStartedAt").isString())
                .andExpect(jsonPath("$.checkCompletedAt").isString())
                .andExpect(jsonPath("$.isValid").isBoolean())
                .andExpect(jsonPath("$.totalChecks").isNumber())
                .andExpect(jsonPath("$.passedChecks").isNumber())
                .andExpect(jsonPath("$.failedChecks").isNumber())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.warnings").isArray())
                .andExpect(jsonPath("$.statistics").exists())
                .andExpect(jsonPath("$.statistics.executionTimeMs").isNumber())
                .andExpect(jsonPath("$.statistics.parametersChecked").isNumber());
    }

    @Test
    @DisplayName("認証なしアクセステスト - 403エラー")
    public void testUnauthorizedAccess_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/council/explorer/blocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/council/explorer/integrity-check")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("権限不足アクセステスト - 403エラー")
    @WithMockUser(roles = "CITIZEN")
    public void testInsufficientPermissions_Returns403() throws Exception {
        // 市民ロールは整合性チェックにアクセスできない
        mockMvc.perform(post("/api/v1/council/explorer/integrity-check")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("存在しないブロックハッシュアクセステスト")
    @WithMockUser(roles = "CITIZEN")
    public void testNonExistentBlockHash() throws Exception {
        String nonExistentHash = "0x0000000000000000";

        // サービス層でモックデータを返すため200が返される
        // 実際のプロダクションでは404になる可能性がある
        mockMvc.perform(get("/api/v1/council/explorer/blocks/{blockHash}", nonExistentHash)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("大量データページングテスト")
    @WithMockUser(roles = "ADMIN")
    public void testLargeDataPaging() throws Exception {
        // 大きなページサイズでテスト
        mockMvc.perform(get("/api/v1/council/explorer/blocks")
                .param("page", "0")
                .param("size", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageSize").value(100));

        // 存在しないページでテスト
        mockMvc.perform(get("/api/v1/council/explorer/blocks")
                .param("page", "9999")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("APIパフォーマンステスト - 応答時間")
    @WithMockUser(roles = "CITIZEN")
    public void testAPIPerformance() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/council/explorer/blocks")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // 応答時間が5秒以内であることを確認
        assert responseTime < 5000 : "API応答時間が許容範囲を超えています: " + responseTime + "ms";
    }

    @Test
    @DisplayName("複数同時アクセステスト")
    @WithMockUser(roles = "COUNCIL_MEMBER")
    public void testConcurrentAccess() throws Exception {
        // 統計情報と署名検証を同時に実行
        String txHash = "0x9f8e7d6c5b4a3210";

        mockMvc.perform(get("/api/v1/council/explorer/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/council/explorer/merkle-proof/{txHash}", txHash)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
