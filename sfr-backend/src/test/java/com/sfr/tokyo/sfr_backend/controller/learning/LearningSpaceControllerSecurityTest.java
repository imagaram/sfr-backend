package com.sfr.tokyo.sfr_backend.controller.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningModeConfigDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningSpaceCreateDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSpace;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningModeConfigRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSpaceRepository;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import com.sfr.tokyo.sfr_backend.service.UserService;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class LearningSpaceControllerSecurityTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private LearningSpaceRepository learningSpaceRepository;

        @Autowired
        private LearningModeConfigRepository configRepository;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private UserService userService;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

        private User testUser;

        @BeforeEach
        void setUp() throws Exception {
                SecurityContextHolder.clearContext();

                testUser = User.builder()
                                .email("test@example.com")
                                .firstname("Test")
                                .lastname("User")
                                .build();

                when(jwtService.extractUsername("valid-token")).thenReturn(testUser.getEmail());
                when(jwtService.isTokenValid("valid-token", testUser)).thenReturn(true);
                when(userService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
                when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

                doAnswer((org.mockito.stubbing.Answer<Void>) invocation -> {
                        jakarta.servlet.ServletRequest req = invocation.getArgument(0);
                        jakarta.servlet.ServletResponse res = invocation.getArgument(1);
                        jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                        jakarta.servlet.http.HttpServletRequest httpReq = (jakarta.servlet.http.HttpServletRequest) req;
                        String auth = httpReq.getHeader("Authorization");
                        if (auth != null && auth.startsWith("Bearer ")
                                        && auth.substring(7).trim().equals("valid-token")) {
                                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                testUser, null, java.util.Collections.emptyList());
                                org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                .setAuthentication(authToken);
                        }
                        chain.doFilter(req, res);
                        return null;
                }).when(jwtAuthenticationFilter).doFilter(
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.any());
        }

        @Test
        void createLearningSpace_ShouldReturnCreated() throws Exception {
                LearningSpaceCreateDto dto = LearningSpaceCreateDto.builder()
                                .name("テスト学習空間")
                                .mode(LearningSpace.LearningMode.SCHOOL)
                                .build();

                mockMvc.perform(post("/api/learning/spaces")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.spaceId").exists());
        }

        @Test
        void createLearningSpace_WithBlankName_ShouldReturnBadRequest() throws Exception {
                LearningSpaceCreateDto dto = LearningSpaceCreateDto.builder()
                                .name("")
                                .mode(LearningSpace.LearningMode.SCHOOL)
                                .build();

                mockMvc.perform(post("/api/learning/spaces")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getConfig_WithValidSpaceId_ShouldReturnConfig() throws Exception {
                Long spaceId = createSpaceAndReturnId("テスト空間", LearningSpace.LearningMode.SALON);

                mockMvc.perform(get("/api/learning/spaces/{id}/config", spaceId)
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.uiConfig").exists())
                                .andExpect(jsonPath("$.featureFlags").exists());
        }

        @Test
        void getConfig_WithInvalidSpaceId_ShouldReturnNotFound() throws Exception {
                mockMvc.perform(get("/api/learning/spaces/{id}/config", 999L)
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void updateConfig_WithValidData_ShouldReturnOk() throws Exception {
                LearningSpace space = LearningSpace.builder()
                                .name("テスト空間")
                                .mode(LearningSpace.LearningMode.FANCLUB)
                                .build();
                LearningSpace savedSpace = learningSpaceRepository.save(space);

                Map<String, Object> uiConfig = new HashMap<>();
                uiConfig.put("theme", "custom");
                uiConfig.put("primaryColor", "#ff0000");

                Map<String, Boolean> featureFlags = new HashMap<>();
                featureFlags.put("customFeature", true);

                LearningModeConfigDto configDto = LearningModeConfigDto.builder()
                                .uiConfig(uiConfig)
                                .featureFlags(featureFlags)
                                .build();

                mockMvc.perform(put("/api/learning/spaces/{id}/config", savedSpace.getId())
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(configDto))
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf()))
                                .andExpect(status().isOk());
        }

        @Test
        void updateConfig_WithInvalidSpaceId_ShouldReturnNotFound() throws Exception {
                LearningModeConfigDto configDto = LearningModeConfigDto.builder()
                                .uiConfig(new HashMap<>())
                                .featureFlags(new HashMap<>())
                                .build();

                mockMvc.perform(put("/api/learning/spaces/{id}/config", 999L)
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(configDto))
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void accessWithoutToken_ShouldReturnUnauthorized() throws Exception {
                LearningSpaceCreateDto dto = LearningSpaceCreateDto.builder()
                                .name("テスト学習空間")
                                .mode(LearningSpace.LearningMode.SCHOOL)
                                .build();

                mockMvc.perform(post("/api/learning/spaces")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isUnauthorized());
        }

        private Long createSpaceAndReturnId(String name, LearningSpace.LearningMode mode) throws Exception {
                LearningSpaceCreateDto dto = LearningSpaceCreateDto.builder()
                                .name(name)
                                .mode(mode)
                                .build();

                MvcResult result = mockMvc.perform(post("/api/learning/spaces")
                                .header("Authorization", "Bearer valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf()))
                                .andExpect(status().isCreated())
                                .andReturn();

                String content = result.getResponse().getContentAsString();
                JsonNode json = objectMapper.readTree(content);
                return json.get("spaceId").asLong();
        }
}
