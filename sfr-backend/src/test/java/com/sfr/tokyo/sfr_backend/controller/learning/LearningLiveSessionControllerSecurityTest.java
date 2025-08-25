package com.sfr.tokyo.sfr_backend.controller.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningLiveSessionCreateResponse;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningLiveSessionDto;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import com.sfr.tokyo.sfr_backend.service.learning.LearningLiveSessionService;
import com.sfr.tokyo.sfr_backend.user.Role;
import com.sfr.tokyo.sfr_backend.user.User;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.config.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LearningLiveSessionController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfiguration.class }))
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = true)
class LearningLiveSessionControllerSecurityTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private LearningLiveSessionService learningLiveSessionService;

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
        void createSession_WithValidToken_ShouldReturnCreated() throws Exception {
                // テストデータ準備
                LearningLiveSessionDto sessionDto = LearningLiveSessionDto.builder()
                                .ownerId(1L)
                                .title("Test Live Session")
                                .scheduledAt(LocalDateTime.now().plusDays(1))
                                .maxParticipants(10)
                                .build();

                LearningLiveSessionCreateResponse response = LearningLiveSessionCreateResponse.builder()
                                .id(1L)
                                .message("ライブセッションが正常に作成されました")
                                .build();

                when(learningLiveSessionService.createSession(any(LearningLiveSessionDto.class)))
                                .thenReturn(response);

                // リクエスト実行と検証
                mockMvc.perform(post("/api/learning/live/sessions").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sessionDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.message").value("ライブセッションが正常に作成されました"));
        }

        @Test
        void createSession_WithoutToken_ShouldReturnUnauthorized() throws Exception {
                LearningLiveSessionDto sessionDto = LearningLiveSessionDto.builder()
                                .ownerId(1L)
                                .title("Test Live Session")
                                .scheduledAt(LocalDateTime.now().plusDays(1))
                                .maxParticipants(10)
                                .build();

                mockMvc.perform(post("/api/learning/live/sessions").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sessionDto)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getSession_WithValidToken_ShouldReturnSession() throws Exception {
                // テストデータ準備
                LearningLiveSessionDto sessionDto = LearningLiveSessionDto.builder()
                                .id(1L)
                                .ownerId(1L)
                                .title("Test Live Session")
                                .scheduledAt(LocalDateTime.now().plusDays(1))
                                .maxParticipants(10)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                when(learningLiveSessionService.getSessionById(1L)).thenReturn(sessionDto);

                // リクエスト実行と検証
                mockMvc.perform(get("/api/learning/live/sessions/1")
                                .header("Authorization", jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.title").value("Test Live Session"))
                                .andExpect(jsonPath("$.maxParticipants").value(10));
        }

        @Test
        void getSessions_WithValidToken_ShouldReturnSessionList() throws Exception {
                // テストデータ準備
                LearningLiveSessionDto sessionDto = LearningLiveSessionDto.builder()
                                .id(1L)
                                .ownerId(1L)
                                .title("Test Live Session")
                                .scheduledAt(LocalDateTime.now().plusDays(1))
                                .maxParticipants(10)
                                .build();

                List<LearningLiveSessionDto> sessionList = Arrays.asList(sessionDto);

                when(learningLiveSessionService.getFutureSessions()).thenReturn(sessionList);

                // リクエスト実行と検証
                mockMvc.perform(get("/api/learning/live/sessions")
                                .header("Authorization", jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].title").value("Test Live Session"));
        }

        @Test
        void createSession_WithInvalidData_ShouldReturnBadRequest() throws Exception {
                // 不正なデータ（タイトルが空）
                LearningLiveSessionDto invalidDto = LearningLiveSessionDto.builder()
                                .ownerId(1L)
                                .title("") // 空のタイトル
                                .scheduledAt(LocalDateTime.now().plusDays(1))
                                .maxParticipants(10)
                                .build();

                mockMvc.perform(post("/api/learning/live/sessions").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void createSession_WithPastDate_ShouldReturnBadRequest() throws Exception {
                // 過去の日時
                LearningLiveSessionDto invalidDto = LearningLiveSessionDto.builder()
                                .ownerId(1L)
                                .title("Test Session")
                                .scheduledAt(LocalDateTime.now().minusDays(1)) // 過去の日時
                                .maxParticipants(10)
                                .build();

                mockMvc.perform(post("/api/learning/live/sessions").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void createSession_WithZeroParticipants_ShouldReturnBadRequest() throws Exception {
                // 最大参加者数が0
                LearningLiveSessionDto invalidDto = LearningLiveSessionDto.builder()
                                .ownerId(1L)
                                .title("Test Session")
                                .scheduledAt(LocalDateTime.now().plusDays(1))
                                .maxParticipants(0) // 0以下の値
                                .build();

                mockMvc.perform(post("/api/learning/live/sessions").with(
                                org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto)))
                                .andExpect(status().isBadRequest());
        }
}
