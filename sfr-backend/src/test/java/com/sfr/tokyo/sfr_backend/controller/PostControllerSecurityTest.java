package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.entity.PostEntity;
import com.sfr.tokyo.sfr_backend.repository.PostRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.service.FileStorageService;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import com.sfr.tokyo.sfr_backend.service.UserService;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

    private User buildUser() {
        UUID id = UUID.randomUUID();
        return User.builder().id(id).email("u@example.com").build();
    }

    @BeforeEach
    void before() throws Exception {
        // clear security context between tests
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        doAnswer((Answer<Void>) invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            jakarta.servlet.http.HttpServletRequest httpReq = (jakarta.servlet.http.HttpServletRequest) req;
            String auth = httpReq.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ") && auth.substring(7).trim().equals("valid-token")) {
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
    void createPost_withValidToken_shouldReturnCreated() throws Exception {
        User user = buildUser();
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", MediaType.TEXT_PLAIN_VALUE,
                "content".getBytes());

        when(fileStorageService.storeFile(any())).thenReturn("stored.txt");

        PostEntity saved = PostEntity.builder().id(1L).title("T").description("D")
                .fileUrl("/api/posts/downloadFile/stored.txt").user(user).build();
        when(postRepository.save(any())).thenReturn(saved);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));

        when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);

        // Jwt フィルターをスタブ
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            jakarta.servlet.http.HttpServletRequest httpReq = (jakarta.servlet.http.HttpServletRequest) req;
            String auth = httpReq.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ") && auth.substring(7).trim().equals("valid-token")) {
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, java.util.Collections.emptyList());
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(jakarta.servlet.ServletRequest.class),
                any(jakarta.servlet.ServletResponse.class), any(jakarta.servlet.FilterChain.class));

        mockMvc.perform(multipart("/api/posts")
                .file(file)
                .param("title", "T")
                .param("description", "D")
                .header("Authorization", "Bearer valid-token")).andExpect(status().isCreated());
    }

    @Test
    void getMyPosts_withValidToken_shouldReturnOk() throws Exception {
        User user = buildUser();
        PostEntity p = PostEntity.builder().id(1L).title("T").user(user).build();
        when(postRepository.findByUser_Id(user.getId())).thenReturn(java.util.Collections.singletonList(p));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));

        when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);

        // Jwt フィルターをスタブ
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            jakarta.servlet.http.HttpServletRequest httpReq = (jakarta.servlet.http.HttpServletRequest) req;
            String auth = httpReq.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ") && auth.substring(7).trim().equals("valid-token")) {
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, java.util.Collections.emptyList());
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(jakarta.servlet.ServletRequest.class),
                any(jakarta.servlet.ServletResponse.class), any(jakarta.servlet.FilterChain.class));

        mockMvc.perform(get("/api/posts/my-posts").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }
}
