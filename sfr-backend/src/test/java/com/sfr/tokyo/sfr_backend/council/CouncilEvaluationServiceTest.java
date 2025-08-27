package com.sfr.tokyo.sfr_backend.council;

import com.sfr.tokyo.sfr_backend.council.service.CouncilEvaluationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class CouncilEvaluationServiceTest {

    @Autowired CouncilEvaluationService service;

    @Test
    @DisplayName("submit & summarize evaluation weighting")
    void submitAndSummarize() {
        UUID member = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID peer1 = UUID.randomUUID();

        service.submitUserEvaluation(member, user1, 80, "good");
        service.submitUserEvaluation(member, user2, 60, null);
        service.submitPeerEvaluation(member, peer1, 70, "solid");
        service.submitAdminEvaluation(member, 90, "great");

        var summary = service.summarize(member);
        assertThat(summary.getUserScoreAvg()).isEqualTo(70.0); // (80+60)/2
        assertThat(summary.getPeerScoreAvg()).isEqualTo(70.0);
        assertThat(summary.getAdminScore()).isEqualTo(90);
        // weighted = 70*0.4 + 70*0.3 + 90*0.3 = 28 +21 +27 = 76 -> 76.0
        assertThat(summary.getWeightedScore()).isEqualTo(76.0);
    }
}
