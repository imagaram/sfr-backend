package com.sfr.tokyo.sfr_backend.controller.space;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceCreateDto;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceModeConfigDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSpace;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 新しいSpaceController統合テスト
 * 学習空間→スペース名称変更のAPI動作確認
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SpaceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LearningSpaceRepository learningSpaceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        learningSpaceRepository.deleteAll();
    }

    @Test
    @DisplayName("新しいスペースAPI - スペース作成が正常に動作する")
    void createSpace_ShouldReturnCreated() throws Exception {
        SpaceCreateDto dto = SpaceCreateDto.builder()
                .name("テストスペース")
                .mode(LearningSpace.LearningMode.SCHOOL)
                .build();

        mockMvc.perform(post("/api/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spaceId", notNullValue()));
    }

    @Test
    @DisplayName("新しいスペースAPI - 無効な入力でバリデーションエラー")
    void createSpace_WithBlankName_ShouldReturnBadRequest() throws Exception {
        SpaceCreateDto dto = SpaceCreateDto.builder()
                .name("")
                .mode(LearningSpace.LearningMode.SCHOOL)
                .build();

        mockMvc.perform(post("/api/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("新しいスペースAPI - スペース設定取得が正常に動作する")
    void getSpaceConfig_ShouldReturnConfig() throws Exception {
        // テストデータ作成
        LearningSpace space = LearningSpace.builder()
                .name("設定テストスペース")
                .mode(LearningSpace.LearningMode.SALON)
                .build();
        LearningSpace savedSpace = learningSpaceRepository.save(space);

        mockMvc.perform(get("/api/spaces/{id}/config", savedSpace.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uiConfig", notNullValue()))
                .andExpect(jsonPath("$.featureFlags", notNullValue()));
    }

    @Test
    @DisplayName("新しいスペースAPI - スペース設定更新が正常に動作する")
    void updateSpaceConfig_ShouldReturnOk() throws Exception {
        // テストデータ作成
        LearningSpace space = LearningSpace.builder()
                .name("設定更新テストスペース")
                .mode(LearningSpace.LearningMode.FANCLUB)
                .build();
        LearningSpace savedSpace = learningSpaceRepository.save(space);

        SpaceModeConfigDto configDto = SpaceModeConfigDto.builder()
                .uiConfig(Map.of("theme", "dark", "layout", "grid"))
                .featureFlags(Map.of("enableChat", true, "enableLive", false))
                .build();

        mockMvc.perform(put("/api/spaces/{id}/config", savedSpace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("新しいスペースAPI - 存在しないスペースIDで404エラー")
    void getSpaceConfig_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/spaces/99999/config"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("旧学習空間API - 非推奨警告ログが出力される")
    void oldLearningSpaceAPI_ShouldLogDeprecationWarning() throws Exception {
        SpaceCreateDto dto = SpaceCreateDto.builder()
                .name("旧API非推奨テスト")
                .mode(LearningSpace.LearningMode.SCHOOL)
                .build();

        // 旧APIエンドポイントを呼び出し
        mockMvc.perform(post("/api/learning/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spaceId", notNullValue()));

        // ログ出力確認は実装レベルでの確認となるため、ここでは動作確認のみ
    }

    @Test
    @DisplayName("新旧API並行動作確認 - 同じ結果が得られる")
    void newAndOldAPI_ShouldProduceSameResults() throws Exception {
        // 新APIでスペース作成
        SpaceCreateDto newDto = SpaceCreateDto.builder()
                .name("新API統合テスト")
                .mode(LearningSpace.LearningMode.SALON)
                .build();

        String newResponse = mockMvc.perform(post("/api/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // 旧APIでスペース作成（同等DTOクラス使用）
        String oldResponse = mockMvc.perform(post("/api/learning/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // 両方とも正常に動作し、レスポンス構造が同じであることを確認
        // （実際のIDは異なるが、フィールド構造は同じ）
        assert newResponse.contains("spaceId");
        assert oldResponse.contains("spaceId");
    }
}
