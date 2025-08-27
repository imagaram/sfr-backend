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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class CouncilApiErrorHandlingTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager em;
    @Autowired
    com.sfr.tokyo.sfr_backend.council.service.CouncilElectionService electionService;
    @Autowired
    com.sfr.tokyo.sfr_backend.council.service.CouncilCandidateService candidateService;
    @Autowired
    ObjectMapper objectMapper;

    private User persistUser(String email) {
        User u = User.builder().email(email).password("x").role(Role.USER).build();
        em.persist(u);
        return u;
    }

    private Long createPreElection() {
        CouncilElectionDto dto = CouncilElectionDto.builder()
                .phase(ElectionPhase.PRE_ELECTION)
                .startAt(Instant.now())
                .endAt(Instant.now().plusSeconds(1800))
                .seats(1)
                .build();
        return electionService.create(dto).getId();
    }

    @Test
    @DisplayName("Voting before phase -> BUSINESS_RULE_VIOLATION error response")
    void votingBeforePhase() throws Exception {
        Long electionId = createPreElection();
        User alice = persistUser("alice_err@test");
        em.flush();
        var candidate = candidateService.registerCandidate(electionId, alice.getId());
        String json = objectMapper.writeValueAsString(new CastVote(candidate.getId()));
    mockMvc.perform(post("/api/governance/council/elections/{id}/vote", electionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", alice.getId())
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("ELECTION_PHASE_INVALID"))
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Voting not active")))
        .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("Duplicate vote -> BUSINESS_RULE_VIOLATION")
    void duplicateVote() throws Exception {
        Long electionId = createPreElection();
        User alice = persistUser("alice_dup@test");
        User bob = persistUser("bob_dup@test");
        em.flush();
        var cAlice = candidateService.registerCandidate(electionId, alice.getId());
    candidateService.registerCandidate(electionId, bob.getId());
        // advance phase to voting
        electionService.advancePhase(electionId, ElectionPhase.VOTING);
        // first vote
        String first = objectMapper.writeValueAsString(new CastVote(cAlice.getId()));
        mockMvc.perform(post("/api/governance/council/elections/{id}/vote", electionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", alice.getId())
                        .content(first))
                .andExpect(status().isCreated());
        // duplicate
        mockMvc.perform(post("/api/governance/council/elections/{id}/vote", electionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", alice.getId())
                        .content(first))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DUPLICATE_VOTE"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Already voted")))
                .andExpect(jsonPath("$.traceId").exists());
    }

    static class CastVote { public Long candidateId; CastVote(Long id){ this.candidateId=id; } }
}