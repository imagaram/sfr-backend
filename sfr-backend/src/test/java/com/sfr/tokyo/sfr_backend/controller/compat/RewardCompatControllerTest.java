package com.sfr.tokyo.sfr_backend.controller.compat;

import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.sfr.tokyo.sfr_backend.config.SecurityConfiguration;

import java.math.BigDecimal;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.regex.Pattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = RewardCompatController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class))
@AutoConfigureMockMvc(addFilters = false)
public class RewardCompatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserBalanceService userBalanceService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitService rateLimitService; // RateLimitConfig 依存解決用

    @BeforeEach
    void setup() {
        Mockito.when(rateLimitService.isAllowed(Mockito.anyString())).thenReturn(true);
    }

    @Test
    @WithMockUser(username = "99999999-9999-9999-9999-999999999999")
    void calculate_reward_ok() throws Exception {
        String body = "{" +
                "\"user_id\":\"99999999-9999-9999-9999-999999999999\"," +
                "\"activity_score\":50.0," +
                "\"evaluation_score\":4.5" +
                "}";
        mockMvc.perform(post("/api/v1/sfr/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value("99999999-9999-9999-9999-999999999999"))
                .andExpect(jsonPath("$.estimated_reward").exists())
                .andExpect(jsonPath("$.calculation_details.activity_score").value(50.0))
                .andExpect(jsonPath("$.calculation_details.evaluation_score").value(4.5))
                .andExpect(jsonPath("$.calculation_details.normalized_evaluation_score").value(90.0))
                .andExpect(jsonPath("$.calculation_details.weights.activity").isNumber())
                .andExpect(jsonPath("$.calculation_details.weights.evaluation").isNumber())
                .andExpect(jsonPath("$.calculation_details.weights.range_min").value(0.0))
                .andExpect(jsonPath("$.calculation_details.weights.range_max").value(1.0))
                .andExpect(jsonPath("$.calculation_details.divisors.calculate")
                        .value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.calculation_details.divisors.issue").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.calculation_details.rounding_mode").value("TRUNCATE_DOWN"))
                .andExpect(jsonPath("$.calculation_details.formula_version").value("v1"));
    }

    @Test
    @WithMockUser(username = "99999999-9999-9999-9999-999999999999")
    void issue_reward_ok() throws Exception {
        String uid = "99999999-9999-9999-9999-999999999999";
        UserBalance ub = new UserBalance();
        ub.setUserId(uid);
        ub.setSpaceId(1L);
        ub.setCurrentBalance(new BigDecimal("0"));
        Mockito.when(userBalanceService.getUserBalance(uid, 1L)).thenReturn(Optional.of(ub));

        String body = "{" +
                "\"user_id\":\"" + uid + "\"," +
                "\"activity_score\":70.0," +
                "\"evaluation_score\":5.0," +
                "\"reward_reason\":\"great work\"" +
                "}";
        mockMvc.perform(post("/api/v1/sfr/rewards/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(uid))
                .andExpect(jsonPath("$.reward_amount").value("0.17000000"))
                .andExpect(jsonPath("$.calculation_details.activity_score").value(70.0))
                .andExpect(jsonPath("$.calculation_details.evaluation_score").value(5.0))
                .andExpect(jsonPath("$.calculation_details.normalized_evaluation_score").value(100.0))
                .andExpect(jsonPath("$.calculation_details.weights.activity").isNumber())
                .andExpect(jsonPath("$.calculation_details.weights.evaluation").isNumber())
                .andExpect(jsonPath("$.calculation_details.weights.range_min").value(0.0))
                .andExpect(jsonPath("$.calculation_details.weights.range_max").value(1.0))
                .andExpect(jsonPath("$.calculation_details.divisors.calculate")
                        .value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.calculation_details.divisors.issue").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.calculation_details.rounding_mode").value("TRUNCATE_DOWN"))
                .andExpect(jsonPath("$.calculation_details.formula_version").value("v1"))
                .andExpect(jsonPath("$.calculation_details.formula").exists());
    }

    // 境界・異常系
    @Test
    @WithMockUser(username = "11111111-2222-3333-4444-555555555555")
    void calculate_reward_activity_out_of_range_high() throws Exception {
        String body = "{" +
                "\"user_id\":\"11111111-2222-3333-4444-555555555555\"," +
                "\"activity_score\":101.0," +
                "\"evaluation_score\":4.0" +
                "}";
        mockMvc.perform(post("/api/v1/sfr/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").exists());
    }

    @Test
    @WithMockUser(username = "11111111-2222-3333-4444-555555555555")
    void calculate_reward_activity_out_of_range_low() throws Exception {
        String body = "{" +
                "\"user_id\":\"11111111-2222-3333-4444-555555555555\"," +
                "\"activity_score\":-0.5," +
                "\"evaluation_score\":4.0" +
                "}";
        mockMvc.perform(post("/api/v1/sfr/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].message").exists());
    }

    @Test
    @WithMockUser(username = "11111111-2222-3333-4444-555555555555")
    void calculate_reward_evaluation_out_of_range_high() throws Exception {
        String body = "{" +
                "\"user_id\":\"11111111-2222-3333-4444-555555555555\"," +
                "\"activity_score\":10.0," +
                "\"evaluation_score\":5.5" +
                "}";
        mockMvc.perform(post("/api/v1/sfr/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").exists());
    }

    @Test
    @WithMockUser(username = "11111111-2222-3333-4444-555555555555")
    void calculate_reward_evaluation_out_of_range_low() throws Exception {
        String body = "{" +
                "\"user_id\":\"11111111-2222-3333-4444-555555555555\"," +
                "\"activity_score\":10.0," +
                "\"evaluation_score\":0.5" +
                "}";
        mockMvc.perform(post("/api/v1/sfr/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].message").exists());
    }

    @Test
    @WithMockUser(username = "22222222-2222-2222-2222-222222222222")
    void issue_reward_activity_high() throws Exception {
        String body = "{" +
                "\"user_id\":\"22222222-2222-2222-2222-222222222222\"," +
                "\"activity_score\":100.1," +
                "\"evaluation_score\":3.0," +
                "\"reward_reason\":\"reason\"" +
                "}";
        mockMvc.perform(post("/api/v1/sfr/rewards/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").exists());
    }

    @Test
    @WithMockUser(username = "22222222-2222-2222-2222-222222222222")
    void issue_reward_evaluation_low() throws Exception {
        String body = "{" +
                "\"user_id\":\"22222222-2222-2222-2222-222222222222\"," +
                "\"activity_score\":99.9," +
                "\"evaluation_score\":0.9," +
                "\"reward_reason\":\"reason\"" +
                "}";
        mockMvc.perform(post("/api/v1/sfr/rewards/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].message").exists());
    }
}
