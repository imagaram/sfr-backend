package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningModeConfigDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningSpaceCreateDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningSpaceCreateResponse;
import com.sfr.tokyo.sfr_backend.service.learning.LearningSpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
@Slf4j
public class LearningSpaceController {

    private final LearningSpaceService learningSpaceService;

    @PostMapping("/spaces")
    public ResponseEntity<LearningSpaceCreateResponse> createLearningSpace(
            @Valid @RequestBody LearningSpaceCreateDto dto) {

        log.info("POST /api/learning/spaces - Creating learning space: {}", dto.getName());

        try {
            LearningSpaceCreateResponse response = learningSpaceService.createLearningSpace(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create learning space: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/spaces/{id}/config")
    public ResponseEntity<LearningModeConfigDto> getConfig(@PathVariable Long id) {

        log.info("GET /api/learning/spaces/{}/config - Getting config", id);

        try {
            LearningModeConfigDto config = learningSpaceService.getConfig(id);
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get config for space {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/spaces/{id}/config")
    public ResponseEntity<Void> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody LearningModeConfigDto dto) {

        log.info("PUT /api/learning/spaces/{}/config - Updating config", id);

        try {
            learningSpaceService.updateConfig(id, dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update config for space {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
