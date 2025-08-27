package com.sfr.tokyo.sfr_backend.council.api;

import com.sfr.tokyo.sfr_backend.council.api.request.AddManifestoQARequest;
import com.sfr.tokyo.sfr_backend.council.api.request.UpsertManifestoRequest;
import com.sfr.tokyo.sfr_backend.council.dto.ManifestoDto;
import com.sfr.tokyo.sfr_backend.council.service.ManifestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/governance/council/candidates/{candidateId}/manifesto")
@RequiredArgsConstructor
public class ManifestoController {

    private final ManifestoService manifestoService;

    @GetMapping
    public ResponseEntity<?> get(@PathVariable Long candidateId) {
        ManifestoDto dto = manifestoService.get(candidateId);
        if (dto == null) {
            throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                com.sfr.tokyo.sfr_backend.exception.ErrorCode.MANIFESTO_NOT_FOUND,
                "Manifesto not found: candidateId=" + candidateId);
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> upsert(@PathVariable Long candidateId, @Valid @RequestBody UpsertManifestoRequest req) {
        ManifestoDto dto = ManifestoDto.builder()
                .candidateId(candidateId)
                .title(req.getTitle())
                .summary(req.getSummary())
                .details(req.getDetails())
                .endorsements(req.getEndorsements())
                .build();
        return ResponseEntity.ok(manifestoService.upsert(candidateId, dto));
    }

    @PostMapping("/qa")
    public ResponseEntity<?> addQA(@PathVariable Long candidateId, @Valid @RequestBody AddManifestoQARequest req) {
        ManifestoDto.QA qa = ManifestoDto.QA.builder().question(req.getQuestion()).answer(req.getAnswer()).build();
        return ResponseEntity.ok(manifestoService.addQA(candidateId, qa));
    }
}
