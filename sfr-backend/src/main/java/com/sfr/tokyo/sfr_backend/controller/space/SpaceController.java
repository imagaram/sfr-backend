package com.sfr.tokyo.sfr_backend.controller.space;

import com.sfr.tokyo.sfr_backend.dto.space.SpaceCreateDto;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceCreateResponse;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceModeConfigDto;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceDto;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceStatisticsDto;
import com.sfr.tokyo.sfr_backend.entity.space.Space;
import com.sfr.tokyo.sfr_backend.service.space.SpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * スペース管理API（統合版）
 * 
 * 学習・交流・エンタメが融合したスペースの作成、設定管理を提供
 * 学校・オンラインサロン・公式ファンクラブの3モード対応
 * 
 * @author SFR Development Team
 * @version 3.0
 * @since 2025-09-10
 */
@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@Slf4j
public class SpaceController {

    private final SpaceService spaceService;

    /**
     * スペース作成
     * 
     * @param dto スペース作成DTO
     * @return 作成レスポンス
     */
    @PostMapping
    public ResponseEntity<SpaceCreateResponse> createSpace(
            @Valid @RequestBody SpaceCreateDto dto) {

        log.info("POST /api/spaces - Creating space: {}", dto.getName());

        try {
            SpaceCreateResponse response = spaceService.createSpace(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Space creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during space creation", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * スペース設定取得
     * 
     * @param id スペースID
     * @return 設定情報
     */
    @GetMapping("/{id}/config")
    public ResponseEntity<SpaceModeConfigDto> getConfig(@PathVariable Long id) {
        log.info("GET /api/spaces/{}/config - Retrieving space config", id);

        try {
            SpaceModeConfigDto config = spaceService.getConfig(id);
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            log.warn("Space config retrieval failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error during space config retrieval", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * スペース設定更新
     * 
     * @param id スペースID
     * @param dto 設定DTO
     * @return 更新結果
     */
    @PutMapping("/{id}/config")
    public ResponseEntity<Void> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody SpaceModeConfigDto dto) {

        log.info("PUT /api/spaces/{}/config - Updating space config", id);

        try {
            spaceService.updateConfig(id, dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Space config update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error during space config update", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * スペース詳細取得
     * 
     * @param id スペースID
     * @return スペース詳細情報
     */
    @GetMapping("/{id}")
    public ResponseEntity<SpaceDto> getSpace(@PathVariable Long id) {
        log.info("GET /api/spaces/{} - Retrieving space details", id);

        try {
            Optional<Space> spaceOpt = spaceService.findById(id);
            if (spaceOpt.isPresent()) {
                SpaceDto spaceDto = spaceService.convertToDto(spaceOpt.get());
                return ResponseEntity.ok(spaceDto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Unexpected error during space retrieval", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 人気スペース一覧取得
     * 
     * @param mode スペースモード（オプション）
     * @param page ページ番号（デフォルト0）
     * @param size ページサイズ（デフォルト20）
     * @return 人気スペースリスト
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<SpaceDto>> getPopularSpaces(
            @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/spaces/popular - mode: {}, page: {}, size: {}", mode, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SpaceDto> spaces = spaceService.getPopularSpaces(mode, pageable);
            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            log.error("Unexpected error during popular spaces retrieval", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * スペース検索
     * 
     * @param keyword 検索キーワード
     * @param page ページ番号（デフォルト0）
     * @param size ページサイズ（デフォルト20）
     * @return 検索結果
     */
    @GetMapping("/search")
    public ResponseEntity<Page<SpaceDto>> searchSpaces(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/spaces/search - keyword: {}, page: {}, size: {}", keyword, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SpaceDto> spaces = spaceService.searchSpaces(keyword, pageable);
            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            log.error("Unexpected error during space search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * スペース統計取得
     * 
     * @return 統計情報
     */
    @GetMapping("/statistics")
    public ResponseEntity<SpaceStatisticsDto> getStatistics() {
        log.info("GET /api/spaces/statistics - Retrieving space statistics");

        try {
            SpaceStatisticsDto stats = spaceService.getStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Unexpected error during statistics retrieval", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * スペース参加
     * 
     * @param id スペースID
     * @param userId ユーザーID（リクエストボディから）
     * @return 参加結果
     */
    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinSpace(
            @PathVariable Long id,
            @RequestParam Long userId) {

        log.info("POST /api/spaces/{}/join - User {} joining space", id, userId);

        try {
            boolean success = spaceService.joinSpace(id, userId);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build(); // Space full or not found
            }
        } catch (Exception e) {
            log.error("Unexpected error during space join", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
