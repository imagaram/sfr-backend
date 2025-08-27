package com.sfr.tokyo.sfr_backend.council;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.council.dto.CouncilElectionDto;
import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import com.sfr.tokyo.sfr_backend.user.Role;
import com.sfr.tokyo.sfr_backend.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class CouncilApiAdditionalErrorHandlingTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired com.sfr.tokyo.sfr_backend.council.service.CouncilElectionService electionService;
    @Autowired com.sfr.tokyo.sfr_backend.council.service.CouncilCandidateService candidateService;
    @Autowired ObjectMapper objectMapper;

    private User persistUser(String email) {
        User u = User.builder().email(email).password("x").role(Role.USER).build();
        em.persist(u);
        return u;
    }

    private Long createElection(Instant start, Instant end, ElectionPhase phase, int seats) {
        CouncilElectionDto dto = CouncilElectionDto.builder()
                .phase(phase)
                .startAt(start)
                .endAt(end)
                .seats(seats)
                .build();
        return electionService.create(dto).getId();
    }

    @Test
    @DisplayName("[ELECTION_TIME_WINDOW] 投票期間外 (開始前) での投票")
    void voteOutsideTimeWindow() throws Exception {
        // start 10 minutes in future
        Instant start = Instant.now().plusSeconds(600);
        Instant end = start.plusSeconds(1800);
        Long electionId = createElection(start, end, ElectionPhase.PRE_ELECTION, 1);
        // advance to VOTING phase manually
        electionService.advancePhase(electionId, ElectionPhase.VOTING);
        User alice = persistUser("alice_window@test");
        em.flush();
        var candidate = candidateService.registerCandidate(electionId, alice.getId());
        // attempt to vote before startAt
        String json = objectMapper.writeValueAsString(new CastVote(candidate.getId()));
        mockMvc.perform(post("/api/governance/council/elections/{id}/vote", electionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", alice.getId())
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ELECTION_TIME_WINDOW"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Voting not active")))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("[RESULTS_UNAVAILABLE] 集計フェーズ前の結果参照")
    void resultsUnavailableBeforeCounting() throws Exception {
        Instant start = Instant.now();
        Instant end = start.plusSeconds(1800);
        Long electionId = createElection(start, end, ElectionPhase.PRE_ELECTION, 1);
        mockMvc.perform(get("/api/governance/council/elections/{id}/results", electionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("RESULTS_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Results not available")))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("[MANIFESTO_EDIT_CLOSED] 投票フェーズでのマニフェスト編集")
    void manifestoEditClosed() throws Exception {
        Instant start = Instant.now();
        Instant end = start.plusSeconds(1800);
        Long electionId = createElection(start, end, ElectionPhase.PRE_ELECTION, 1);
        User alice = persistUser("alice_manifesto@test");
        em.flush();
        var candidate = candidateService.registerCandidate(electionId, alice.getId());
        // move to VOTING
        electionService.advancePhase(electionId, ElectionPhase.VOTING);
        // attempt upsert
        String body = "{\"title\":\"My Vision\",\"summary\":\"S\"}";
        mockMvc.perform(post("/api/governance/council/candidates/{cid}/manifesto", candidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MANIFESTO_EDIT_CLOSED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Manifesto editing closed")))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("[MANIFESTO_QA_CLOSED] 投票フェーズでのQ&A追加")
    void manifestoQaClosed() throws Exception {
        Instant start = Instant.now();
        Instant end = start.plusSeconds(1800);
        Long electionId = createElection(start, end, ElectionPhase.PRE_ELECTION, 1);
        User alice = persistUser("alice_qa@test");
        em.flush();
        var candidate = candidateService.registerCandidate(electionId, alice.getId());
        // まず PRE_ELECTION 中に manifesto を作成
        // PRE_ELECTION 中に manifesto を MockMvc 経由で作成
        String createBody = "{\"title\":\"Title\"}";
        mockMvc.perform(post("/api/governance/council/candidates/{cid}/manifesto", candidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
                .andExpect(status().isOk());
        // フェーズを VOTING に変更
        electionService.advancePhase(electionId, ElectionPhase.VOTING);
        // QA 追加試行
        String qaBody = "{\"question\":\"Q?\",\"answer\":\"A!\"}";
        mockMvc.perform(post("/api/governance/council/candidates/{cid}/manifesto/qa", candidate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(qaBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MANIFESTO_QA_CLOSED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Q&A adding closed")))
                .andExpect(jsonPath("$.traceId").exists());
    }

    static class CastVote { public Long candidateId; CastVote(Long id){ this.candidateId=id; } }
}
