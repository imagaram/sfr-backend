package com.sfr.tokyo.sfr_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.council.dto.CouncilParameterDto;
import com.sfr.tokyo.sfr_backend.council.service.CouncilParameterService;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * 評議員パラメータシステムの統合テスト
 * 制度的証跡、Audit記録、TTL挙動の自動検証を実施
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CouncilParameterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 1. パラメータ作成 → Audit記録生成の証跡検証
     */
    @Test
    @Order(1)
    @WithMockUser(authorities = "COUNCIL_ADMIN")
    @Transactional
    void testParameterCreationWithAuditTrail() throws Exception {
        // Given: 新しい評議員パラメータ
        CouncilParameterDto newParam = CouncilParameterDto.builder()
                .paramKey("VOTING_QUORUM_PERCENTAGE")
                .valueString("67.0")
                .valueType(CouncilParameter.ValueType.PERCENTAGE)
                .description("評議員選挙の定足数パーセンテージ")
                .build();

        // When: パラメータを作成
        String response = mockMvc.perform(post("/api/v1/council/parameters")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newParam)))
                // Then: 正常に作成され、レスポンスに証跡情報が含まれる
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paramKey").value("VOTING_QUORUM_PERCENTAGE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.version").value(1))
                .andReturn().getResponse().getContentAsString();

        // Audit記録の検証
        mockMvc.perform(get("/api/v1/council/parameters/VOTING_QUORUM_PERCENTAGE/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CREATE"))
                .andExpect(jsonPath("$[0].oldValue").isEmpty())
                .andExpect(jsonPath("$[0].newValue").value("67.0"))
                .andExpect(jsonPath("$[0].timestamp").exists())
                .andExpect(jsonPath("$[0].actor").exists());
    }

    /**
     * 2. パラメータ更新 → TTL挙動 → 制度的有効性の検証
     */
    @Test
    @Order(2)
    @WithMockUser(authorities = "COUNCIL_ADMIN")
    @Transactional
    void testParameterUpdateWithTTLValidation() throws Exception {
        // Given: 既存パラメータの更新
        CouncilParameterDto updateParam = CouncilParameterDto.builder()
                .paramKey("VOTING_QUORUM_PERCENTAGE")
                .valueString("75.0")
                .valueType(CouncilParameter.ValueType.PERCENTAGE)
                .description("評議員選挙の定足数パーセンテージ（更新版）")
                .build();

        // When: パラメータを更新
        mockMvc.perform(put("/api/v1/council/parameters/VOTING_QUORUM_PERCENTAGE")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateParam)))
                // Then: 更新が成功し、TTL情報が正しく設定される
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parameterValue").value("75.0"))
                .andExpect(jsonPath("$.effectiveFrom").exists())
                .andExpect(jsonPath("$.effectiveUntil").exists())
                .andExpect(jsonPath("$.version").value(2));

        // TTL挙動の検証 - 有効期間内
        mockMvc.perform(get("/api/v1/council/parameters/VOTING_QUORUM_PERCENTAGE/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.timeToExpiry").exists())
                .andExpect(jsonPath("$.effectiveStatus").value("ACTIVE"));

        // Audit証跡の検証
        mockMvc.perform(get("/api/v1/council/parameters/VOTING_QUORUM_PERCENTAGE/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // CREATE + UPDATE
                .andExpect(jsonPath("$[0].action").value("UPDATE"))
                .andExpect(jsonPath("$[0].oldValue").value("67.0"))
                .andExpect(jsonPath("$[0].newValue").value("75.0"));
    }

    /**
     * 3. 制度的整合性の検証 - 連鎖的パラメータ依存性
     */
    @Test
    @Order(3)
    @WithMockUser(authorities = "COUNCIL_ADMIN")
    @Transactional
    void testSystemicParameterConsistency() throws Exception {
        // Given: 関連パラメータの作成
        CouncilParameterDto minCandidatesParam = CouncilParameterDto.builder()
                .paramKey("MIN_CANDIDATES_COUNT")
                .valueString("3")
                .valueType(CouncilParameter.ValueType.INTEGER)
                .description("評議員選挙の最小候補者数")
                .build();

        CouncilParameterDto maxCandidatesParam = CouncilParameterDto.builder()
                .paramKey("MAX_CANDIDATES_COUNT")
                .valueString("12")
                .valueType(CouncilParameter.ValueType.INTEGER)
                .description("評議員選挙の最大候補者数")
                .build();

        // When: 関連パラメータを作成
        mockMvc.perform(post("/api/v1/council/parameters")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minCandidatesParam)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/council/parameters")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxCandidatesParam)))
                .andExpect(status().isCreated());

        // Then: 制度的整合性の検証
        mockMvc.perform(get("/api/v1/council/parameters/validate/system-consistency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isConsistent").value(true))
                .andExpect(jsonPath("$.validationResults").isArray())
                .andExpect(jsonPath("$.dependencies.MIN_CANDIDATES_COUNT").exists())
                .andExpect(jsonPath("$.dependencies.MAX_CANDIDATES_COUNT").exists());
    }

    /**
     * 4. 権限ベースのアクセス制御検証
     */
    @Test
    @Order(4)
    @WithMockUser(authorities = "COUNCIL_MEMBER") // 管理者権限なし
    void testParameterAccessControlValidation() throws Exception {
        CouncilParameterDto restrictedParam = CouncilParameterDto.builder()
                .paramKey("RESTRICTED_PARAM")
                .valueString("secret")
                .valueType(CouncilParameter.ValueType.STRING)
                .description("制限されたパラメータ")
                .build();

        // When: 権限不足でパラメータ作成を試行
        // Then: アクセス拒否される
        mockMvc.perform(post("/api/v1/council/parameters")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restrictedParam)))
                .andExpect(status().isForbidden());

        // 読み取り権限は許可される
        mockMvc.perform(get("/api/v1/council/parameters"))
                .andExpect(status().isOk());
    }

    /**
     * 5. バルク操作とパフォーマンス検証
     */
    @Test
    @Order(5)
    @WithMockUser(authorities = "COUNCIL_ADMIN")
    @Transactional
    void testBulkOperationsPerformance() throws Exception {
        // Given: 複数パラメータのバルク更新
        String bulkUpdateRequest = """
                {
                    "parameters": [
                        {
                            "parameterKey": "VOTING_QUORUM_PERCENTAGE",
                            "parameterValue": "80.0"
                        },
                        {
                            "parameterKey": "MIN_CANDIDATES_COUNT", 
                            "parameterValue": "5"
                        }
                    ],
                    "reason": "制度改正による一括更新"
                }
                """;

        long startTime = System.currentTimeMillis();

        // When: バルク更新を実行
        mockMvc.perform(put("/api/v1/council/parameters/bulk")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkUpdateRequest))
                // Then: 性能要件内で完了し、整合性が保たれる
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(2))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.auditTrailCreated").value(true));

        long executionTime = System.currentTimeMillis() - startTime;
        
        // パフォーマンス要件: 2秒以内
        assert executionTime < 2000 : "バルク更新が性能要件を満たしていません: " + executionTime + "ms";

        // 全体的整合性の最終検証
        mockMvc.perform(get("/api/v1/council/parameters/validate/system-consistency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isConsistent").value(true));
    }
}
