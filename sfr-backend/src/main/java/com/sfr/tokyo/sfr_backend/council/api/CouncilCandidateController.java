package com.sfr.tokyo.sfr_backend.council.api;

import com.sfr.tokyo.sfr_backend.council.api.request.RegisterCandidateRequest;
import com.sfr.tokyo.sfr_backend.council.dto.CouncilCandidateDto;
import com.sfr.tokyo.sfr_backend.council.service.CouncilCandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/governance/council/elections/{electionId}/candidates")
@RequiredArgsConstructor
public class CouncilCandidateController {

    private final CouncilCandidateService candidateService;

    @GetMapping
    public List<CouncilCandidateDto> list(@PathVariable Long electionId) {
        return candidateService.listByElection(electionId);
    }

    @PostMapping
    public ResponseEntity<CouncilCandidateDto> register(@PathVariable Long electionId, @Valid @RequestBody RegisterCandidateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.registerCandidate(electionId, req.getUserId()));
    }
}
