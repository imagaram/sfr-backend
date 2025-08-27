package com.sfr.tokyo.sfr_backend.council;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class CouncilApiNotFoundErrorHandlingTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("[ELECTION_NOT_FOUND] 存在しない選挙ID 取得")
    void electionByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/governance/council/elections/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ELECTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Election not found")))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("[ELECTION_NOT_FOUND] 現在選挙なしで /current 取得")
    void currentElectionNotFound() throws Exception {
        mockMvc.perform(get("/api/governance/council/elections/current"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ELECTION_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("[MANIFESTO_NOT_FOUND] 存在しない候補IDでマニフェスト取得")
    void manifestoNotFound() throws Exception {
        mockMvc.perform(get("/api/governance/council/candidates/{cid}/manifesto", 123456L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("MANIFESTO_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Manifesto not found")))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
