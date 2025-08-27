package com.sfr.tokyo.sfr_backend.council.api;

import com.sfr.tokyo.sfr_backend.council.api.request.CastVoteRequest;
import com.sfr.tokyo.sfr_backend.council.dto.CouncilVoteDto;
import com.sfr.tokyo.sfr_backend.council.dto.CandidateResultDto;
import com.sfr.tokyo.sfr_backend.council.service.CouncilVoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/governance/council/elections/{electionId}")
@RequiredArgsConstructor
public class CouncilVoteController {

    private final CouncilVoteService voteService;

    @PostMapping("/vote")
    public ResponseEntity<?> cast(@PathVariable Long electionId, @Valid @RequestBody CastVoteRequest req, @RequestHeader("X-User-Id") UUID userId) {
        CouncilVoteDto dto = voteService.castVote(electionId, req.getCandidateId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/results")
    public ResponseEntity<?> results(@PathVariable Long electionId) {
        java.util.List<CandidateResultDto> list = voteService.getResults(electionId);
        return ResponseEntity.ok(list);
    }
}
