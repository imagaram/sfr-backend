package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;
import com.sfr.tokyo.sfr_backend.entity.CharacterStatus;
import com.sfr.tokyo.sfr_backend.repository.CharacterRepository;
import com.sfr.tokyo.sfr_backend.service.ImageUploadService;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CharacterControllerSecurityTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CharacterRepository characterRepository;

        @MockBean
        private ImageUploadService imageUploadService;

        @MockBean
        private com.sfr.tokyo.sfr_backend.service.JwtService jwtService;

        @MockBean(name = "userDetailsService")
        private com.sfr.tokyo.sfr_backend.service.UserService userService;

        @MockBean
        private com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

        private User buildUser() {
                UUID id = UUID.randomUUID();
                return User.builder().id(id).email("u@example.com").build();
        }

        @BeforeEach
        void clearContext() throws Exception {
                SecurityContextHolder.clearContext();
                doAnswer((org.mockito.stubbing.Answer<Void>) invocation -> {
                        jakarta.servlet.ServletRequest req = invocation.getArgument(0);
                        jakarta.servlet.ServletResponse res = invocation.getArgument(1);
                        jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                        jakarta.servlet.http.HttpServletRequest httpReq = (jakarta.servlet.http.HttpServletRequest) req;
                        String auth = httpReq.getHeader("Authorization");
                        if (auth != null && auth.startsWith("Bearer ")
                                        && auth.substring(7).trim().equals("valid-token")) {
                                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                buildUser(), null, java.util.Collections.emptyList());
                                org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                .setAuthentication(authToken);
                        }
                        chain.doFilter(req, res);
                        return null;
                }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
        }

        @Test
        void authenticatedRequest_withValidToken_shouldReturnCreated() throws Exception {
                User user = buildUser();
                MockMultipartFile file = new MockMultipartFile("imageFile", "img.png", MediaType.IMAGE_PNG_VALUE,
                                "data".getBytes());
                when(imageUploadService.uploadImage(any())).thenReturn("uploaded.png");

                CharacterLifecycle saved = CharacterLifecycle.builder()
                                .id(1L)
                                .name("C1")
                                .profile("p")
                                .imageUrl("uploaded.png")
                                .user(user)
                                .status(CharacterStatus.ACTIVE)
                                .build();
                when(characterRepository.findByNameAndUser_Id(eq("C1"), eq(user.getId()))).thenReturn(null);
                when(characterRepository.save(any())).thenReturn(saved);

                // simulate jwt valid
                when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
                when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);
                when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);

                mockMvc.perform(multipart("/api/characters")
                                .file(file)
                                .param("name", "C1")
                                .param("profile", "p")
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .user(user))
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf()))
                                .andExpect(status().is2xxSuccessful());
        }

        @Test
        void unauthenticatedRequest_withInvalidToken_shouldReturnUnauthorized() throws Exception {
                when(jwtService.extractUsername("bad-token")).thenReturn(null);

                mockMvc.perform(get("/api/characters").header("Authorization", "Bearer bad-token"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void authenticatedRequest_withBearerToken_shouldPassFilter() throws Exception {
                User user = buildUser();
                CharacterLifecycle c = CharacterLifecycle.builder().id(1L).name("C1").user(user).build();
                when(characterRepository.findByUser_Id(user.getId()))
                                .thenReturn(java.util.Collections.singletonList(c));

                // mock jwt extraction and validation
                when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
                when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
                when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);

                // JwtAuthenticationFilter をスタブして valid-token のときだけ認証をセット
                doAnswer((org.mockito.stubbing.Answer<Void>) invocation -> {
                        jakarta.servlet.ServletRequest req = invocation.getArgument(0);
                        jakarta.servlet.ServletResponse res = invocation.getArgument(1);
                        jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                        jakarta.servlet.http.HttpServletRequest httpReq = (jakarta.servlet.http.HttpServletRequest) req;
                        String auth = httpReq.getHeader("Authorization");
                        if (auth != null && auth.startsWith("Bearer ")
                                        && auth.substring(7).trim().equals("valid-token")) {
                                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                user, null, java.util.Collections.emptyList());
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                        chain.doFilter(req, res);
                        return null;
                }).when(jwtAuthenticationFilter).doFilter(any(jakarta.servlet.ServletRequest.class),
                                any(jakarta.servlet.ServletResponse.class), any(jakarta.servlet.FilterChain.class));

                mockMvc.perform(get("/api/characters").header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isOk());
        }
}
