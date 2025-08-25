package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningLiveSessionCreateResponse;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningLiveSessionDto;
import com.sfr.tokyo.sfr_backend.service.learning.LearningLiveSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/live/sessions")
@RequiredArgsConstructor
public class LearningLiveSessionController {

    private final LearningLiveSessionService learningLiveSessionService;

    /**
     * ライブセッションを作成
     * 
     * @param sessionDto セッションDTO
     * @return 作成レスポンス
     */
    @PostMapping
    public ResponseEntity<LearningLiveSessionCreateResponse> createSession(
            @Valid @RequestBody LearningLiveSessionDto sessionDto) {
        LearningLiveSessionCreateResponse response = learningLiveSessionService.createSession(sessionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * セッション詳細を取得
     * 
     * @param sessionId セッションID
     * @return セッションDTO
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<LearningLiveSessionDto> getSession(@PathVariable Long sessionId) {
        LearningLiveSessionDto session = learningLiveSessionService.getSessionById(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * オーナーのセッション一覧を取得
     * 
     * @param ownerId オーナーID（Optional）
     * @return セッションリスト
     */
    @GetMapping
    public ResponseEntity<List<LearningLiveSessionDto>> getSessions(@RequestParam(required = false) Long ownerId) {
        List<LearningLiveSessionDto> sessions;
        if (ownerId != null) {
            sessions = learningLiveSessionService.getSessionsByOwnerId(ownerId);
        } else {
            sessions = learningLiveSessionService.getFutureSessions();
        }

        return ResponseEntity.ok(sessions);
    }

    /**
     * セッションを更新
     * 
     * @param sessionId  セッションID
     * @param sessionDto 更新DTO
     * @return 更新されたセッションDTO
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<LearningLiveSessionDto> updateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody LearningLiveSessionDto sessionDto) {

        LearningLiveSessionDto updatedSession = learningLiveSessionService.updateSession(sessionId, sessionDto);
        return ResponseEntity.ok(updatedSession);
    }

    /**
     * セッションを削除
     * 
     * @param sessionId セッションID
     * @return 削除レスポンス
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        learningLiveSessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
