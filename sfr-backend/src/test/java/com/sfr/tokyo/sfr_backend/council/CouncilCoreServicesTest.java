package com.sfr.tokyo.sfr_backend.council;

import com.sfr.tokyo.sfr_backend.council.dto.*;
import com.sfr.tokyo.sfr_backend.council.service.*;
import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import com.sfr.tokyo.sfr_backend.user.User;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilVoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Execution(ExecutionMode.SAME_THREAD)
class CouncilCoreServicesTest {

    @Autowired
    CouncilElectionService electionService;
    @Autowired
    CouncilCandidateService candidateService;
    @Autowired
    CouncilVoteService voteService;
    @Autowired
    ManifestoService manifestoService;
    @Autowired
    CouncilVoteRepository voteRepository;
    @Autowired
    jakarta.persistence.EntityManager em;

    private User persistUser(String email) {
        User u = User.builder().email(email).password("x").role(com.sfr.tokyo.sfr_backend.user.Role.USER).build();
        em.persist(u);
        return u;
    }

    private Long createElection() {
        CouncilElectionDto dto = CouncilElectionDto.builder()
                .phase(ElectionPhase.PRE_ELECTION)
                .startAt(Instant.now())
                .endAt(Instant.now().plusSeconds(3600))
                .seats(2)
                .build();
        return electionService.create(dto).getId();
    }

    @Test
    @DisplayName("Election -> Candidate register -> Manifesto -> Phase change -> Voting -> Cast vote -> Count results")
    void fullHappyFlow() {
        Long electionId = createElection();
    User alice = persistUser("alice@test");
    User bob = persistUser("bob@test");
    User charlie = persistUser("charlie@test");
        em.flush();

        // register candidates
        var c1 = candidateService.registerCandidate(electionId, alice.getId());
        var c2 = candidateService.registerCandidate(electionId, bob.getId());
        assertThat(candidateService.listByElection(electionId)).hasSize(2);

        // manifesto for alice
        manifestoService.upsert(c1.getId(), ManifestoDto.builder().title("T1").summary("S").details(List.of("d1"))
                .endorsements(List.of("team"))
                .build());
        var man = manifestoService.get(c1.getId());
        assertThat(man.getDetails()).containsExactly("d1");

        // advance to voting
        boolean advanced = electionService.advancePhase(electionId, ElectionPhase.VOTING);
        assertThat(advanced).isTrue();

    // votes (1ユーザ1票ルール)
    voteService.castVote(electionId, c1.getId(), alice.getId()); // alice -> alice
    voteService.castVote(electionId, c2.getId(), bob.getId());   // bob -> bob
    voteService.castVote(electionId, c1.getId(), charlie.getId()); // charlie -> alice

        // move to counting
        electionService.advancePhase(electionId, ElectionPhase.COUNTING);
        var results = voteService.getResults(electionId);
        assertThat(results).hasSize(2);
        var top = results.get(0);
        assertThat(top.getCandidateId()).isEqualTo(c1.getId());
        assertThat(top.getVoteCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Duplicate vote is prevented")
    void duplicateVote() {
        Long electionId = createElection();
        User alice = persistUser("alice2@test");
        User bob = persistUser("bob2@test");
        em.flush();
        var c1 = candidateService.registerCandidate(electionId, alice.getId());
        electionService.advancePhase(electionId, ElectionPhase.VOTING);
        voteService.castVote(electionId, c1.getId(), bob.getId());
        assertThatThrownBy(() -> voteService.castVote(electionId, c1.getId(), bob.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Already voted");
    }

    @Test
    @DisplayName("Manifesto editing closed after voting phase")
    void manifestoEditClosed() {
        Long electionId = createElection();
        User alice = persistUser("alice3@test");
        em.flush();
        var c1 = candidateService.registerCandidate(electionId, alice.getId());
        // move to voting
        electionService.advancePhase(electionId, ElectionPhase.VOTING);
        assertThatThrownBy(() -> manifestoService.upsert(c1.getId(), ManifestoDto.builder().title("t").build()))
                .isInstanceOf(IllegalStateException.class);
    }
}
