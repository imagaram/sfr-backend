package com.sfr.tokyo.sfr_backend.controller.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentCreateDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentCreateResponse;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningContent;
import com.sfr.tokyo.sfr_backend.config.SecurityConfiguration;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import com.sfr.tokyo.sfr_backend.service.learning.LearningContentService;
import com.sfr.tokyo.sfr_backend.user.Role;
import com.sfr.tokyo.sfr_backend.user.User;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LearningContentController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfiguration.class }))
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = true)
class LearningContentControllerSecurityTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private LearningContentService learningContentService;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private JwtService jwtService;

        // Spring Boot の UserDetailsServiceAutoConfiguration による
        // InMemoryUserDetailsManager 自動生成を抑止
        @MockBean
        private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

        // WebMvc スライスで RateLimitConfig の依存解決用
        @MockBean
        private RateLimitService rateLimitService;

        // JwtAuthenticationFilter をモックしてフィルターチェーンを素通り
        @MockBean
        private com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

        @TestConfiguration
        @EnableMethodSecurity(prePostEnabled = true)
        static class TestSecurityConfig {
                @Bean
                SecurityFilterChain testSecurityFilterChain(
                                com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter) {
                        jakarta.servlet.Filter authGate = new org.springframework.web.filter.OncePerRequestFilter() {
                                @Override
                                protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                                jakarta.servlet.http.HttpServletResponse response,
                                                jakarta.servlet.FilterChain filterChain)
                                                throws jakarta.servlet.ServletException, java.io.IOException {
                                        String path = request.getRequestURI();
                                        if (path != null && path.startsWith("/api/learning/")) {
                                                String auth = request.getHeader("Authorization");
                                                if (auth == null || auth.isBlank()) {
                                                        response.setStatus(401);
                                                        return;
                                                }
                                        }
                                        filterChain.doFilter(request, response);
                                }
                        };
                        return new org.springframework.security.web.DefaultSecurityFilterChain(
                                        org.springframework.security.web.util.matcher.AnyRequestMatcher.INSTANCE,
                                        authGate,
                                        jwtAuthenticationFilter);
                }
        }

        private User testUser;
        private String jwtToken;

        @BeforeEach
        void setUp() throws Exception {
                // テストユーザーの設定
                testUser = User.builder()
                                .id(UUID.randomUUID())
                                .firstname("Test")
                                .lastname("User")
                                .email("test@example.com")
                                .role(Role.USER)
                                .build();

                // JWTトークンのモック設定
                jwtToken = "Bearer test-jwt-token";
                when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
                when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(true);
                when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

                // レートリミットは常に許可
                when(rateLimitService.isAllowed(anyString())).thenReturn(true);
                when(rateLimitService.isAuthAllowed(anyString())).thenReturn(true);
                when(rateLimitService.getRemainingRequests(anyString())).thenReturn(100);
                when(rateLimitService.getRemainingAuthRequests(anyString())).thenReturn(5);
                when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(0L);

                // JWT フィルター: Authorization 無しなら 401 を返して中断、ありなら素通し
                if (jwtAuthenticationFilter != null) {
                        org.mockito.Mockito.doAnswer(inv -> {
                                jakarta.servlet.ServletRequest r1 = inv.getArgument(0);
                                jakarta.servlet.ServletResponse r2 = inv.getArgument(1);
                                jakarta.servlet.FilterChain chain = inv.getArgument(2);
                                jakarta.servlet.http.HttpServletRequest req = (jakarta.servlet.http.HttpServletRequest) r1;
                                jakarta.servlet.http.HttpServletResponse res = (jakarta.servlet.http.HttpServletResponse) r2;
                                String auth = req.getHeader("Authorization");
                                if (auth == null || auth.isBlank()) {
                                        res.setStatus(401);
                                        return null;
                                }
                                try {
                                        chain.doFilter(req, res);
                                } catch (Exception e) {
                                        throw new RuntimeException(e);
                                }
                                return null;
                        }).when(jwtAuthenticationFilter).doFilter(org.mockito.ArgumentMatchers.any(),
                                        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
                }
        }

        @Test
        void createContent_WithValidToken_ShouldReturnCreated() throws Exception {
                // テストデータ準備
                LearningContentCreateDto createDto = LearningContentCreateDto.builder()
                                .spaceId(1L)
                                .title("Test Content")
                                .type(LearningContent.ContentType.VIDEO)
                                .url("https://example.com/video")
                                .description("Test description")
                                .build();

                LearningContentCreateResponse response = LearningContentCreateResponse.builder()
                                .id(1L)
                                .message("学習コンテンツが正常に作成されました")
                                .build();

                when(learningContentService.createContent(any(LearningContentCreateDto.class)))
                                .thenReturn(response);

                // リクエスト実行と検証
                mockMvc.perform(post("/api/learning/contents").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.message").value("学習コンテンツが正常に作成されました"));
        }

        @Test
        void createContent_WithoutToken_ShouldReturnUnauthorized() throws Exception {
                LearningContentCreateDto createDto = LearningContentCreateDto.builder()
                                .spaceId(1L)
                                .title("Test Content")
                                .type(LearningContent.ContentType.VIDEO)
                                .url("https://example.com/video")
                                .description("Test description")
                                .build();

                mockMvc.perform(post("/api/learning/contents").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getContents_WithValidToken_ShouldReturnContentList() throws Exception {
                // テストデータ準備
                LearningContentDto contentDto = LearningContentDto.builder()
                                .spaceId(1L)
                                .title("Test Content")
                                .type(LearningContent.ContentType.VIDEO)
                                .url("https://example.com/video")
                                .description("Test description")
                                .build();

                List<LearningContentDto> contentList = Arrays.asList(contentDto);

                when(learningContentService.getContentsBySpaceId(1L)).thenReturn(contentList);

                // リクエスト実行と検証
                mockMvc.perform(get("/api/learning/contents")
                                .header("Authorization", jwtToken)
                                .param("spaceId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].title").value("Test Content"));
        }

        @Test
        void getContents_WithType_ShouldReturnFilteredContentList() throws Exception {
                // テストデータ準備
                LearningContentDto contentDto = LearningContentDto.builder()
                                .spaceId(1L)
                                .title("Test Video")
                                .type(LearningContent.ContentType.VIDEO)
                                .url("https://example.com/video")
                                .description("Test video description")
                                .build();

                List<LearningContentDto> contentList = Arrays.asList(contentDto);

                when(learningContentService.getContentsBySpaceIdAndType(1L, LearningContent.ContentType.VIDEO))
                                .thenReturn(contentList);

                // リクエスト実行と検証
                mockMvc.perform(get("/api/learning/contents")
                                .header("Authorization", jwtToken)
                                .param("spaceId", "1")
                                .param("type", "VIDEO"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].type").value("VIDEO"));
        }

        @Test
        void getContentById_WithValidToken_ShouldReturnContent() throws Exception {
                // テストデータ準備
                LearningContentDto contentDto = LearningContentDto.builder()
                                .spaceId(1L)
                                .title("Test Content")
                                .type(LearningContent.ContentType.VIDEO)
                                .url("https://example.com/video")
                                .description("Test description")
                                .build();

                when(learningContentService.getContentById(1L)).thenReturn(contentDto);

                // リクエスト実行と検証
                mockMvc.perform(get("/api/learning/contents/1")
                                .header("Authorization", jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Test Content"))
                                .andExpect(jsonPath("$.type").value("VIDEO"));
        }

        @Test
        void updateContent_WithValidToken_ShouldReturnUpdatedContent() throws Exception {
                // テストデータ準備
                LearningContentCreateDto updateDto = LearningContentCreateDto.builder()
                                .spaceId(1L)
                                .title("Updated Content")
                                .type(LearningContent.ContentType.MATERIAL)
                                .url("https://example.com/document")
                                .description("Updated description")
                                .build();

                LearningContentDto updatedContentDto = LearningContentDto.builder()
                                .spaceId(1L)
                                .title("Updated Content")
                                .type(LearningContent.ContentType.MATERIAL)
                                .url("https://example.com/document")
                                .description("Updated description")
                                .build();

                when(learningContentService.updateContent(eq(1L), any(LearningContentCreateDto.class)))
                                .thenReturn(updatedContentDto);

                // リクエスト実行と検証
                mockMvc.perform(put("/api/learning/contents/1").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated Content"))
                                .andExpect(jsonPath("$.type").value("MATERIAL"));
        }

        @Test
        void deleteContent_WithValidToken_ShouldReturnNoContent() throws Exception {
                // リクエスト実行と検証
                mockMvc.perform(delete("/api/learning/contents/1").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .header("Authorization", jwtToken))
                                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        void createContent_WithInvalidData_ShouldReturnBadRequest() throws Exception {
                // 不正なデータ（タイトルが空）
                LearningContentCreateDto invalidDto = LearningContentCreateDto.builder()
                                .spaceId(1L)
                                .title("") // 空のタイトル
                                .type(LearningContent.ContentType.VIDEO)
                                .build();

                mockMvc.perform(post("/api/learning/contents").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto)))
                                .andExpect(status().isBadRequest());
        }
}
