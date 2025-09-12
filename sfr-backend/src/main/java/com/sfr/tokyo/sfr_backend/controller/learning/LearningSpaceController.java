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

/**
 * 学習空間管理API
 * 
 * @deprecated この API は非推奨です。新しい /api/spaces API を使用してください。
 * @see com.sfr.tokyo.sfr_backend.controller.space.SpaceController
 * @author SFR Development Team
 * @version 1.0 (deprecated)
 * @since 2025-08-20
 */
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class LearningSpaceController {

    private final LearningSpaceService learningSpaceService;

    /**
     * 学習空間作成
     * 
     * @deprecated 新しい POST /api/spaces を使用してください
     */
    @PostMapping("/spaces")
    @Deprecated
    public ResponseEntity<LearningSpaceCreateResponse> createLearningSpace(
            @Valid @RequestBody LearningSpaceCreateDto dto) {

        log.warn("DEPRECATED: POST /api/learning/spaces - Use POST /api/spaces instead. Creating learning space: {}", dto.getName());

        try {
            LearningSpaceCreateResponse response = learningSpaceService.createLearningSpace(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create learning space: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 学習空間設定取得
     * 
     * @deprecated 新しい GET /api/spaces/{id}/config を使用してください
     */
    @GetMapping("/spaces/{id}/config")
    @Deprecated
    public ResponseEntity<LearningModeConfigDto> getConfig(@PathVariable Long id) {

        log.warn("DEPRECATED: GET /api/learning/spaces/{}/config - Use GET /api/spaces/{}/config instead. Getting config for space: {}", id, id, id);

        try {
            LearningModeConfigDto config = learningSpaceService.getConfig(id);
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get config for space {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 学習空間設定更新
     * 
     * @deprecated 新しい PUT /api/spaces/{id}/config を使用してください
     */
    @PutMapping("/spaces/{id}/config")
    @Deprecated
    public ResponseEntity<Void> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody LearningModeConfigDto dto) {

        log.warn("DEPRECATED: PUT /api/learning/spaces/{}/config - Use PUT /api/spaces/{}/config instead. Updating config for space: {}", id, id, id);

        try {
            learningSpaceService.updateConfig(id, dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update config for space {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
