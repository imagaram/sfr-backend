package com.sfr.tokyo.sfr_backend.controller.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningQuizDto;
import com.sfr.tokyo.sfr_backend.service.learning.LearningQuizService;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.config.SecurityConfiguration;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LearningQuizController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfiguration.class }))
@ImportAutoConfiguration(exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
})
@TestPropertySource(properties = {
                "spring.jpa.open-in-view=false"
})
@WithMockUser(roles = "USER")
class LearningQuizControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private LearningQuizService quizService;

        // WebMvcTest スライスで RateLimitConfig の依存解決用
        @MockBean
        private RateLimitService rateLimitService;

        // JwtAuthenticationFilter の依存解決用
        @MockBean
        private com.sfr.tokyo.sfr_backend.service.JwtService jwtService;

        @MockBean
        private UserDetailsService userDetailsService;

        // セキュリティフィルター自体もモック化してフィルターチェーン登録時の初期化を回避
        @MockBean
        private com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

        // 最小限のセキュリティ設定（全許可）を提供して、アプリ本体のSecurityConfigurationを置き換える
        @TestConfiguration
        @Import({})
        @EnableMethodSecurity(prePostEnabled = true)
        static class TestSecurityConfig {
                @Bean
                SecurityFilterChain testSecurityFilterChain() {
                        // 最小限のチェーン（フィルター無し、全リクエスト許可）
                        return new org.springframework.security.web.DefaultSecurityFilterChain(
                                        org.springframework.security.web.util.matcher.AnyRequestMatcher.INSTANCE);
                }

                // JPA 周りが誤って初期化されるケースへの防御: ダミービーンを提供
                @Bean(name = "entityManagerFactory")
                EntityManagerFactory entityManagerFactory() {
                        return Mockito.mock(EntityManagerFactory.class);
                }

                @Bean
                PlatformTransactionManager transactionManager() {
                        return Mockito.mock(PlatformTransactionManager.class);
                }
        }

        @Autowired
        private ObjectMapper objectMapper;

        private LearningQuizDto sampleQuizDto;

        @BeforeEach
        void setUp() throws Exception {
                // サンプルクイズ問題の作成
                LearningQuizDto.QuizQuestionDto question1 = new LearningQuizDto.QuizQuestionDto();
                question1.setQuestion("Javaの基本データ型はどれですか？");
                question1.setOptions(Arrays.asList("int", "String", "List", "Map"));
                question1.setAnswer("int");

                LearningQuizDto.QuizQuestionDto question2 = new LearningQuizDto.QuizQuestionDto();
                question2.setQuestion("Spring Bootの特徴は？");
                question2.setOptions(Arrays.asList("設定が複雑", "軽量フレームワーク", "重いフレームワーク", "設定ファイルが多い"));
                question2.setAnswer("軽量フレームワーク");

                sampleQuizDto = new LearningQuizDto();
                sampleQuizDto.setId(1L);
                sampleQuizDto.setSpaceId(100L);
                sampleQuizDto.setTitle("Java基礎クイズ");
                sampleQuizDto.setQuestions(Arrays.asList(question1, question2));
                sampleQuizDto.setQuestionCount(2);
                sampleQuizDto.setCreatedAt(LocalDateTime.now());
                sampleQuizDto.setUpdatedAt(LocalDateTime.now());

                // レートリミットは常に許可（インターセプターでブロックされないように）
                when(rateLimitService.isAllowed(anyString())).thenReturn(true);
                when(rateLimitService.isAuthAllowed(anyString())).thenReturn(true);
                when(rateLimitService.getRemainingRequests(anyString())).thenReturn(100);
                when(rateLimitService.getRemainingAuthRequests(anyString())).thenReturn(5);
                when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(0L);

                // Jwt フィルターがチェーンを止めないように
                if (jwtAuthenticationFilter != null) {
                        doAnswer(inv -> {
                                ServletRequest req = inv.getArgument(0);
                                ServletResponse res = inv.getArgument(1);
                                FilterChain chain = inv.getArgument(2);
                                chain.doFilter(req, res);
                                return null;
                        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
                }
        }

        @Test
        void createQuiz_Success() throws Exception {
                // Given
                when(quizService.createQuiz(any(LearningQuizDto.class))).thenReturn(sampleQuizDto);

                // When & Then
                mockMvc.perform(post("/api/learning/quizzes")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleQuizDto)))
                                .andDo(MockMvcResultHandlers.print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.title").value("Java基礎クイズ"))
                                .andExpect(jsonPath("$.spaceId").value(100L))
                                .andExpect(jsonPath("$.questionCount").value(2))
                                .andExpect(jsonPath("$.questions").isArray())
                                .andExpect(jsonPath("$.questions[0].question").value("Javaの基本データ型はどれですか？"))
                                .andExpect(jsonPath("$.questions[0].answer").value("int"));

                verify(quizService).createQuiz(any(LearningQuizDto.class));
        }

        @Test
        void createQuiz_ValidationError() throws Exception {
                // Given - タイトルが空のクイズ
                LearningQuizDto invalidQuiz = new LearningQuizDto();
                invalidQuiz.setSpaceId(100L);
                invalidQuiz.setTitle(""); // 空のタイトル
                invalidQuiz.setQuestions(Arrays.asList());

                // When & Then
                mockMvc.perform(post("/api/learning/quizzes")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidQuiz)))
                                .andExpect(status().isBadRequest());

                verify(quizService, never()).createQuiz(any());
        }

        @Test
        void getQuiz_Success() throws Exception {
                // Given
                when(quizService.getQuiz(1L)).thenReturn(sampleQuizDto);

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/1").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.title").value("Java基礎クイズ"))
                                .andExpect(jsonPath("$.spaceId").value(100L));

                verify(quizService).getQuiz(1L);
        }

        @Test
        void getQuiz_NotFound() throws Exception {
                // Given
                when(quizService.getQuiz(1L)).thenThrow(new EntityNotFoundException("クイズが見つかりません: 1"));

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/1").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());

                verify(quizService).getQuiz(1L);
        }

        @Test
        void getQuizzesBySpace_Success() throws Exception {
                // Given
                List<LearningQuizDto> quizzes = Arrays.asList(sampleQuizDto);
                when(quizService.getQuizzesBySpace(100L)).thenReturn(quizzes);

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes")
                                .param("spaceId", "100"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(1L))
                                .andExpect(jsonPath("$[0].title").value("Java基礎クイズ"));

                verify(quizService).getQuizzesBySpace(100L);
        }

        @Test
        void searchQuizzes_Success() throws Exception {
                // Given
                List<LearningQuizDto> quizzes = Arrays.asList(sampleQuizDto);
                when(quizService.searchQuizzesByTitle(100L, "Java")).thenReturn(quizzes);

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/search")
                                .param("spaceId", "100")
                                .param("title", "Java"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].title").value("Java基礎クイズ"));

                verify(quizService).searchQuizzesByTitle(100L, "Java");
        }

        @Test
        void getRecentQuizzes_Success() throws Exception {
                // Given
                List<LearningQuizDto> quizzes = Arrays.asList(sampleQuizDto);
                when(quizService.getRecentQuizzes(100L, 10)).thenReturn(quizzes);

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/recent")
                                .param("spaceId", "100")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].title").value("Java基礎クイズ"));

                verify(quizService).getRecentQuizzes(100L, 10);
        }

        @Test
        void getRecentQuizzes_DefaultLimit() throws Exception {
                // Given
                List<LearningQuizDto> quizzes = Arrays.asList(sampleQuizDto);
                when(quizService.getRecentQuizzes(100L, 10)).thenReturn(quizzes);

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/recent")
                                .param("spaceId", "100"))
                                .andExpect(status().isOk());

                verify(quizService).getRecentQuizzes(100L, 10); // デフォルト値10
        }

        @Test
        void getQuizzesByDateRange_Success() throws Exception {
                // Given
                List<LearningQuizDto> quizzes = Arrays.asList(sampleQuizDto);
                when(quizService.getQuizzesByDateRange(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(quizzes);

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/date-range")
                                .param("spaceId", "100")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-12-31T23:59:00"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].title").value("Java基礎クイズ"));

                verify(quizService).getQuizzesByDateRange(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        void getQuizStatistics_Success() throws Exception {
                // Given
                Map<String, Object> statistics = Map.of(
                                "totalQuizzes", 5L,
                                "avgQuestions", 3.5,
                                "minQuestions", 2,
                                "maxQuestions", 5);
                when(quizService.getQuizStatistics(100L)).thenReturn(statistics);

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/statistics")
                                .param("spaceId", "100"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalQuizzes").value(5))
                                .andExpect(jsonPath("$.avgQuestions").value(3.5))
                                .andExpect(jsonPath("$.minQuestions").value(2))
                                .andExpect(jsonPath("$.maxQuestions").value(5));

                verify(quizService).getQuizStatistics(100L);
        }

        @Test
        void updateQuiz_Success() throws Exception {
                // Given
                LearningQuizDto updatedQuiz = new LearningQuizDto();
                updatedQuiz.setId(1L);
                updatedQuiz.setSpaceId(100L);
                updatedQuiz.setTitle("更新されたJava基礎クイズ");
                updatedQuiz.setQuestions(sampleQuizDto.getQuestions());

                when(quizService.updateQuiz(eq(1L), any(LearningQuizDto.class))).thenReturn(updatedQuiz);

                // When & Then
                mockMvc.perform(put("/api/learning/quizzes/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedQuiz)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.title").value("更新されたJava基礎クイズ"));

                verify(quizService).updateQuiz(eq(1L), any(LearningQuizDto.class));
        }

        @Test
        void deleteQuiz_Success() throws Exception {
                // Given
                doNothing().when(quizService).deleteQuiz(1L);

                // When & Then
                mockMvc.perform(delete("/api/learning/quizzes/1")
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                verify(quizService).deleteQuiz(1L);
        }

        @Test
        void deleteQuiz_NotFound() throws Exception {
                // Given
                doThrow(new EntityNotFoundException("クイズが見つかりません: 1"))
                                .when(quizService).deleteQuiz(1L);

                // When & Then
                mockMvc.perform(delete("/api/learning/quizzes/1")
                                .with(csrf()))
                                .andExpect(status().isNotFound());

                verify(quizService).deleteQuiz(1L);
        }

        @Test
        void getQuizBySpaceAndId_Success() throws Exception {
                // Given
                when(quizService.getQuizBySpaceAndId(100L, 1L)).thenReturn(sampleQuizDto);

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/1/space/100").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.spaceId").value(100L))
                                .andExpect(jsonPath("$.title").value("Java基礎クイズ"));

                verify(quizService).getQuizBySpaceAndId(100L, 1L);
        }

        @Test
        void getQuizBySpaceAndId_NotFound() throws Exception {
                // Given
                when(quizService.getQuizBySpaceAndId(100L, 1L))
                                .thenThrow(new EntityNotFoundException("指定された学習空間内にクイズが見つかりません"));

                // When & Then
                mockMvc.perform(get("/api/learning/quizzes/1/space/100").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());

                verify(quizService).getQuizBySpaceAndId(100L, 1L);
        }

        @Test
        @WithMockUser(roles = "GUEST")
        void createQuiz_Unauthorized() throws Exception {
                // Given - ROLE_USERがないリクエスト
                mockMvc.perform(post("/api/learning/quizzes")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleQuizDto)))
                                .andExpect(status().isForbidden());

                verify(quizService, never()).createQuiz(any());
        }
}
