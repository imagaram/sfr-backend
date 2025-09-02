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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;

@RestController
@RequestMapping("/api/governance/council/elections")
@RequiredArgsConstructor
@Tag(name = "council", description = "Council election operations")
public class CouncilElectionController {

    private final CouncilElectionService electionService;
    private final CouncilCandidateService candidateService;

    @GetMapping("/current")
    @Operation(summary = "Get current council election (implementation path)", description = "Returns current council election. Optionally include candidates.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(schema = @Schema(implementation = CouncilElectionDto.class)))
        })
    public ResponseEntity<?> getCurrent(@Parameter(description = "Include candidate list") @RequestParam(name = "includeCandidates", defaultValue = "false") boolean includeCandidates) {
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
    @Operation(summary = "Create council election", description = "Creates a new council election in PRE_ELECTION phase")
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
    @Operation(summary = "Advance council election phase (implementation path)")
    public ResponseEntity<?> advancePhase(@PathVariable Long id, @Valid @RequestBody AdvancePhaseRequest req) {
        boolean ok = electionService.advancePhase(id, req.getPhase());
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get council election by id")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return electionService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseThrow(() -> new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                    com.sfr.tokyo.sfr_backend.exception.ErrorCode.ELECTION_NOT_FOUND,
                    "Election not found: id=" + id));
    }
}
