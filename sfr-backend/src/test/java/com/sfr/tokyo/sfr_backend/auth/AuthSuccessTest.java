package com.sfr.tokyo.sfr_backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 認証正常系の統合テスト: register -> authenticate のフローで JWT が取得できること
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AuthSuccessTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    record Register(String firstname,String lastname,String email,String password) {}
    record Login(String email,String password) {}

    @Test
    @DisplayName("[AUTH_SUCCESS] 登録後に認証して JWT トークンを取得できる")
    void registerThenAuthenticate() throws Exception {
        String email = "user" + System.currentTimeMillis() + "@example.com"; // 衝突回避
        String registerBody = objectMapper.writeValueAsString(new Register(
                "Taro","Yamada",email,"password123"));

        // 1. register
        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode regJson = objectMapper.readTree(registerResponse);
        assertThat(regJson.get("token").asText()).isNotBlank();

        // 2. authenticate
        String loginBody = objectMapper.writeValueAsString(new Login(email,"password123"));
        String authResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode authJson = objectMapper.readTree(authResponse);
        assertThat(authJson.get("token").asText()).isNotBlank();
    }
}
