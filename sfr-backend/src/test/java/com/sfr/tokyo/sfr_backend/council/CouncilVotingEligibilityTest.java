package com.sfr.tokyo.sfr_backend.council;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.council.dto.CouncilElectionDto;
import com.sfr.tokyo.sfr_backend.council.service.CouncilCandidateService;
import com.sfr.tokyo.sfr_backend.council.service.CouncilElectionService;
import com.sfr.tokyo.sfr_backend.council.service.VotingEligibilityService;
import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import com.sfr.tokyo.sfr_backend.user.Role;
import com.sfr.tokyo.sfr_backend.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class CouncilVotingEligibilityTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired CouncilElectionService electionService;
    @Autowired CouncilCandidateService candidateService;
    @Autowired ObjectMapper objectMapper;
    @MockBean VotingEligibilityService eligibilityService; // モックで閾値未達を再現

    private User persistUser(String email) {
        User u = User.builder().email(email).password("x").role(Role.USER).build();
        em.persist(u);
        return u;
    }

    private Long createVotingElection() {
        CouncilElectionDto dto = CouncilElectionDto.builder()
                .phase(ElectionPhase.VOTING)
                .startAt(Instant.now().minusSeconds(60))
                .endAt(Instant.now().plusSeconds(3600))
                .seats(1)
                .build();
        return electionService.create(dto).getId();
    }

    record CastVote(Long candidateId) {}

    @Test
    @DisplayName("[VOTER_INSUFFICIENT_BALANCE] 残高不足で投票拒否")
    void insufficientBalance() throws Exception {
        Long electionId = createVotingElection();
        User alice = persistUser("alice_balance@test");
        em.flush();
        var candidate = candidateService.registerCandidate(electionId, alice.getId());
        when(eligibilityService.evaluate(alice.getId()))
                .thenReturn(new VotingEligibilityService.EligibilityResult(false, true, BigDecimal.ZERO, 80));
        String json = objectMapper.writeValueAsString(new CastVote(candidate.getId()));
        mockMvc.perform(post("/api/governance/council/elections/{id}/vote", electionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", alice.getId())
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VOTER_INSUFFICIENT_BALANCE"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient SFR balance")))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("[VOTER_INSUFFICIENT_ACTIVITY] アクティビティ不足で投票拒否")
    void insufficientActivity() throws Exception {
        Long electionId = createVotingElection();
        User alice = persistUser("alice_activity@test");
        em.flush();
        var candidate = candidateService.registerCandidate(electionId, alice.getId());
        when(eligibilityService.evaluate(alice.getId()))
                .thenReturn(new VotingEligibilityService.EligibilityResult(true, false, BigDecimal.ONE, 10));
        String json = objectMapper.writeValueAsString(new CastVote(candidate.getId()));
        mockMvc.perform(post("/api/governance/council/elections/{id}/vote", electionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", alice.getId())
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VOTER_INSUFFICIENT_ACTIVITY"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient activity score")))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
