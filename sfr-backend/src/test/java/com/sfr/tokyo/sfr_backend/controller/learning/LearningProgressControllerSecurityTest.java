package com.sfr.tokyo.sfr_backend.controller.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningProgressDto;
import com.sfr.tokyo.sfr_backend.service.learning.LearningProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class LearningProgressControllerSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private LearningProgressService progressService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void saveProgress_認証ユーザー_成功() throws Exception {
        // Given
        LearningProgressDto inputDto = new LearningProgressDto(1L, new BigDecimal("50.0"));
        LearningProgressDto resultDto = new LearningProgressDto();
        resultDto.setId(1L);
        resultDto.setContentId(1L);
        resultDto.setProgressPercent(new BigDecimal("50.0"));

        when(progressService.saveProgress(any(UUID.class), any(LearningProgressDto.class)))
                .thenReturn(resultDto);

        // When & Then
        mockMvc.perform(post("/learning/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.contentId").value(1))
                .andExpect(jsonPath("$.progressPercent").value(50.0));
    }

    @Test
    void saveProgress_未認証ユーザー_401エラー() throws Exception {
        // Given
        LearningProgressDto inputDto = new LearningProgressDto(1L, new BigDecimal("50.0"));

        // When & Then
        mockMvc.perform(post("/learning/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getUserProgress_認証ユーザー_成功() throws Exception {
        // Given
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        List<LearningProgressDto> progressList = Arrays.asList(
                new LearningProgressDto(1L, new BigDecimal("50.0")),
                new LearningProgressDto(2L, new BigDecimal("100.0")));

        when(progressService.getUserProgress(eq(userId))).thenReturn(progressList);

        // When & Then
        mockMvc.perform(get("/learning/progress/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getMyProgress_認証ユーザー_成功() throws Exception {
        // Given
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        List<LearningProgressDto> progressList = Arrays.asList(
                new LearningProgressDto(1L, new BigDecimal("50.0")));

        when(progressService.getUserProgress(eq(userId))).thenReturn(progressList);

        // When & Then
        mockMvc.perform(get("/learning/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getProgressByUserAndContent_進捗あり_成功() throws Exception {
        // Given
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Long contentId = 1L;
        LearningProgressDto progressDto = new LearningProgressDto(contentId, new BigDecimal("75.0"));

        when(progressService.getProgressByUserAndContent(eq(userId), eq(contentId)))
                .thenReturn(Optional.of(progressDto));

        // When & Then
        mockMvc.perform(get("/learning/progress/{userId}/contents/{contentId}", userId, contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(1))
                .andExpect(jsonPath("$.progressPercent").value(75.0));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getProgressByUserAndContent_進捗なし_404エラー() throws Exception {
        // Given
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Long contentId = 999L;

        when(progressService.getProgressByUserAndContent(eq(userId), eq(contentId)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/learning/progress/{userId}/contents/{contentId}", userId, contentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void deleteProgress_自分の進捗削除_成功() throws Exception {
        // Given
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Long contentId = 1L;

        // When & Then
        mockMvc.perform(delete("/learning/progress/{userId}/contents/{contentId}", userId, contentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void deleteProgress_他ユーザーの進捗削除_403エラー() throws Exception {
        // Given
        UUID otherUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        Long contentId = 1L;

        // When & Then
        mockMvc.perform(delete("/learning/progress/{userId}/contents/{contentId}", otherUserId, contentId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void saveProgress_バリデーションエラー_400エラー() throws Exception {
        // Given - 進捗が範囲外（150%）
        LearningProgressDto invalidDto = new LearningProgressDto(1L, new BigDecimal("150.0"));

        // When & Then
        mockMvc.perform(post("/learning/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void saveProgress_必須項目なし_400エラー() throws Exception {
        // Given - contentIdがnull
        LearningProgressDto invalidDto = new LearningProgressDto();
        invalidDto.setProgressPercent(new BigDecimal("50.0"));

        // When & Then
        mockMvc.perform(post("/learning/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
