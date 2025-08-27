package com.sfr.tokyo.sfr_backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AuthInvalidCredentialsTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    record Login(String email, String password) {}

    @Test
    @DisplayName("[AUTH_INVALID_CREDENTIALS] 未登録メールで認証 → 401")
    void invalidCredentials() throws Exception {
        String body = objectMapper.writeValueAsString(new Login("notfound@example.com","secret"));
        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid credentials")))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
