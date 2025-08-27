package com.sfr.tokyo.sfr_backend.council.api;

import com.sfr.tokyo.sfr_backend.council.api.request.AdvancePhaseRequest;
import com.sfr.tokyo.sfr_backend.council.api.request.CreateElectionRequest;
import com.sfr.tokyo.sfr_backend.council.dto.CouncilElectionDto;
import com.sfr.tokyo.sfr_backend.council.service.CouncilElectionService;
import com.sfr.tokyo.sfr_backend.council.service.CouncilCandidateService;
import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/governance/council/elections")
@RequiredArgsConstructor
public class CouncilElectionController {

    private final CouncilElectionService electionService;
    private final CouncilCandidateService candidateService;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrent(@RequestParam(name = "includeCandidates", defaultValue = "false") boolean includeCandidates) {
        var election = electionService.getCurrentElection().orElseThrow(() ->
            new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                com.sfr.tokyo.sfr_backend.exception.ErrorCode.ELECTION_NOT_FOUND,
                "No current election"));
        if (includeCandidates) {
            HashMap<String, Object> body = new HashMap<>();
            body.put("election", election);
            body.put("candidates", candidateService.listByElection(election.getId()));
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok(election);
    }

    @PostMapping
    public ResponseEntity<CouncilElectionDto> create(@Valid @RequestBody CreateElectionRequest req) {
        CouncilElectionDto dto = CouncilElectionDto.builder()
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .seats(req.getSeats())
                .phase(ElectionPhase.PRE_ELECTION)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(electionService.create(dto));
    }

    @PatchMapping("/{id}/phase")
    public ResponseEntity<?> advancePhase(@PathVariable Long id, @Valid @RequestBody AdvancePhaseRequest req) {
        boolean ok = electionService.advancePhase(id, req.getPhase());
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return electionService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseThrow(() -> new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                    com.sfr.tokyo.sfr_backend.exception.ErrorCode.ELECTION_NOT_FOUND,
                    "Election not found: id=" + id));
    }
}
