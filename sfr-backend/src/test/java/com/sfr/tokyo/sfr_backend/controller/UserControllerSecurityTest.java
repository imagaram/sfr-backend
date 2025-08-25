package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.dto.UserDto;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

    private User buildUser() {
        UUID id = UUID.randomUUID();
        return User.builder().id(id).email("u@example.com").firstname("F").lastname("L").build();
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
    void getMe_withValidToken_shouldReturnOk() throws Exception {
        User user = buildUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

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

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void updateMe_withValidToken_shouldReturnOk() throws Exception {
        User user = buildUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

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

        String payload = "{\"firstname\":\"NewF\",\"lastname\":\"NewL\",\"email\":\"u@example.com\",\"role\":\"USER\"}";
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON).content(payload)
                .header("Authorization", "Bearer valid-token")).andExpect(status().isOk());
    }
}
