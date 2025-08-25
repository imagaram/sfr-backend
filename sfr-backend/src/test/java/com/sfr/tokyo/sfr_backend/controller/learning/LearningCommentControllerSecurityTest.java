package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningCommentDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningComment;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningCommentRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import com.sfr.tokyo.sfr_backend.service.UserService;
import com.sfr.tokyo.sfr_backend.service.learning.LearningCommentService;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LearningCommentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LearningCommentRepository learningCommentRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private LearningCommentService learningCommentService;

    @MockitoBean
    private com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

    private User buildUser() {
        UUID id = UUID.randomUUID();
        return User.builder().id(id).email("u@example.com").firstname("F").build();
    }

    @BeforeEach
    void before() throws Exception {
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
    void getCommentsByTopicId_withAuthenticated_shouldReturnOk() throws Exception {
        User user = buildUser();
        Long topicId = 1L;

        // JwtService、UserService、UserRepositoryのモック設定
        when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));

        LearningComment comment = new LearningComment();
        comment.setId(1L);
        comment.setTopicId(topicId);
        comment.setAuthorId(user.getId());
        comment.setContent("Test learning comment");
        comment.setCommentType(LearningComment.CommentType.COMMENT);
        comment.setCommentStatus(LearningComment.CommentStatus.ACTIVE);
        comment.setCreatedAt(LocalDateTime.now());

        Page<LearningComment> commentPage = new PageImpl<>(Arrays.asList(comment));
        when(learningCommentRepository.findByTopicId(eq(topicId), any(PageRequest.class)))
                .thenReturn(commentPage);

        mockMvc.perform(get("/api/learning/comments/topic/" + topicId)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_withValidToken_shouldReturnCreated() throws Exception {
        User user = buildUser();
        Long topicId = 1L;

        when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));

        // LearningCommentDtoとサービスのモック設定
        LearningCommentDto expectedDto = new LearningCommentDto();
        expectedDto.setId(1L);
        expectedDto.setTopicId(topicId);
        expectedDto.setAuthorId(user.getId());
        expectedDto.setContent("New comment");
        expectedDto.setCommentType(LearningComment.CommentType.COMMENT);
        expectedDto.setCommentStatus(LearningComment.CommentStatus.ACTIVE);

        when(learningCommentService.createComment(any(LearningCommentDto.class))).thenReturn(expectedDto);

        String payload = "{\"topicId\":" + topicId +
                ",\"authorId\":\"" + user.getId() + "\"" +
                ",\"content\":\"New comment\"" +
                ",\"commentType\":\"COMMENT\"" +
                ",\"commentStatus\":\"ACTIVE\"}";
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

        mockMvc.perform(post("/api/learning/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Authorization", "Bearer valid-token")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .csrf()))
                .andExpect(status().isCreated());
    }
}
