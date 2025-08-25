package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentCreateDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentCreateResponse;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningContent;
import com.sfr.tokyo.sfr_backend.service.learning.LearningContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/contents")
@RequiredArgsConstructor
public class LearningContentController {

    private final LearningContentService learningContentService;

    /**
     * 学習コンテンツを作成
     * 
     * @param createDto 作成DTO
     * @return 作成レスポンス
     */
    @PostMapping
    public ResponseEntity<LearningContentCreateResponse> createContent(
            @Valid @RequestBody LearningContentCreateDto createDto) {
        LearningContentCreateResponse response = learningContentService.createContent(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 学習空間のコンテンツ一覧を取得
     * 
     * @param spaceId 学習空間ID
     * @param type    コンテンツタイプ（Optional）
     * @return コンテンツリスト
     */
    @GetMapping
    public ResponseEntity<List<LearningContentDto>> getContents(
            @RequestParam Long spaceId,
            @RequestParam(required = false) LearningContent.ContentType type) {

        List<LearningContentDto> contents;
        if (type != null) {
            contents = learningContentService.getContentsBySpaceIdAndType(spaceId, type);
        } else {
            contents = learningContentService.getContentsBySpaceId(spaceId);
        }

        return ResponseEntity.ok(contents);
    }

    /**
     * コンテンツ詳細を取得
     * 
     * @param contentId コンテンツID
     * @return コンテンツDTO
     */
    @GetMapping("/{contentId}")
    public ResponseEntity<LearningContentDto> getContent(@PathVariable Long contentId) {
        LearningContentDto content = learningContentService.getContentById(contentId);
        return ResponseEntity.ok(content);
    }

    /**
     * コンテンツを更新
     * 
     * @param contentId コンテンツID
     * @param updateDto 更新DTO
     * @return 更新されたコンテンツDTO
     */
    @PutMapping("/{contentId}")
    public ResponseEntity<LearningContentDto> updateContent(
            @PathVariable Long contentId,
            @Valid @RequestBody LearningContentCreateDto updateDto) {

        LearningContentDto updatedContent = learningContentService.updateContent(contentId, updateDto);
        return ResponseEntity.ok(updatedContent);
    }

    /**
     * コンテンツを削除
     * 
     * @param contentId コンテンツID
     * @return 削除レスポンス
     */
    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long contentId) {
        learningContentService.deleteContent(contentId);
        return ResponseEntity.noContent().build();
    }
}
