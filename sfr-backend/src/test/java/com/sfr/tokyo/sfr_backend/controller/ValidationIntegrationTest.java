package com.sfr.tokyo.sfr_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * バリデーションエラーハンドリングのテストクラス
 * 各Controllerの@Valid適用とGlobalExceptionHandlerの動作を確認
 */
@SpringBootTest
@AutoConfigureWebMvc
public class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 登録リクエストのバリデーションエラーテスト
     * 必須フィールドが空の場合、400 Bad Requestとエラー詳細が返されることを確認
     */
    @Test
    public void testRegisterValidationError() throws Exception {
        String invalidJson = """
                {
                    "firstname": "",
                    "lastname": "",
                    "email": "invalid-email",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    /**
     * ログイン認証リクエストのバリデーションエラーテスト
     * 必須フィールドが空の場合、400 Bad Requestとエラー詳細が返されることを確認
     */
    @Test
    public void testAuthenticationValidationError() throws Exception {
        String invalidJson = """
                {
                    "email": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("入力値にエラーがあります"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    /**
     * バリデーションエラーレスポンスの形式テスト
     * エラーレスポンスに必要なフィールドが含まれていることを確認
     */
    @Test
    public void testValidationErrorResponseStructure() throws Exception {
        String invalidJson = """
                {
                    "firstname": "",
                    "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.path").exists());
    }
}
