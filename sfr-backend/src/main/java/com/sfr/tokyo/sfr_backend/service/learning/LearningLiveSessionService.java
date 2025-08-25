package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningLiveSessionCreateResponse;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningLiveSessionDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningLiveSession;
import com.sfr.tokyo.sfr_backend.exception.EntityNotFoundException;
import com.sfr.tokyo.sfr_backend.mapper.learning.LearningLiveSessionMapper;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningLiveSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningLiveSessionService {

    private final LearningLiveSessionRepository learningLiveSessionRepository;
    private final LearningLiveSessionMapper learningLiveSessionMapper;

    /**
     * ライブセッションを作成
     * 
     * @param sessionDto セッションDTO
     * @return 作成レスポンス
     */
    public LearningLiveSessionCreateResponse createSession(LearningLiveSessionDto sessionDto) {
        // 開催予定日時が未来の日時かチェック
        if (sessionDto.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("開催予定日時は未来の日時を指定してください");
        }

        // DTOからEntityに変換
        LearningLiveSession session = learningLiveSessionMapper.toEntity(sessionDto);

        // 保存
        LearningLiveSession savedSession = learningLiveSessionRepository.save(session);

        return LearningLiveSessionCreateResponse.builder()
                .id(savedSession.getId())
                .message("ライブセッションが正常に作成されました")
                .build();
    }

    /**
     * セッション詳細を取得
     * 
     * @param sessionId セッションID
     * @return セッションDTO
     */
    @Transactional(readOnly = true)
    public LearningLiveSessionDto getSessionById(Long sessionId) {
        LearningLiveSession session = learningLiveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたライブセッションが見つかりません: " + sessionId));

        return learningLiveSessionMapper.toDto(session);
    }

    /**
     * オーナーのセッション一覧を取得
     * 
     * @param ownerId オーナーID
     * @return セッションリスト
     */
    @Transactional(readOnly = true)
    public List<LearningLiveSessionDto> getSessionsByOwnerId(Long ownerId) {
        List<LearningLiveSession> sessions = learningLiveSessionRepository.findByOwnerIdOrderByScheduledAtAsc(ownerId);
        return learningLiveSessionMapper.toDtoList(sessions);
    }

    /**
     * 未来のセッション一覧を取得
     * 
     * @return セッションリスト
     */
    @Transactional(readOnly = true)
    public List<LearningLiveSessionDto> getFutureSessions() {
        List<LearningLiveSession> sessions = learningLiveSessionRepository
                .findByScheduledAtAfterOrderByScheduledAtAsc(LocalDateTime.now());
        return learningLiveSessionMapper.toDtoList(sessions);
    }

    /**
     * セッションを更新
     * 
     * @param sessionId  セッションID
     * @param sessionDto 更新DTO
     * @return 更新されたセッションDTO
     */
    public LearningLiveSessionDto updateSession(Long sessionId, LearningLiveSessionDto sessionDto) {
        LearningLiveSession existingSession = learningLiveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたライブセッションが見つかりません: " + sessionId));

        // 開催予定日時が未来の日時かチェック
        if (sessionDto.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("開催予定日時は未来の日時を指定してください");
        }

        // 更新可能フィールドのみ更新
        existingSession.setTitle(sessionDto.getTitle());
        existingSession.setScheduledAt(sessionDto.getScheduledAt());
        existingSession.setMaxParticipants(sessionDto.getMaxParticipants());

        LearningLiveSession updatedSession = learningLiveSessionRepository.save(existingSession);
        return learningLiveSessionMapper.toDto(updatedSession);
    }

    /**
     * セッションを削除
     * 
     * @param sessionId セッションID
     */
    public void deleteSession(Long sessionId) {
        if (!learningLiveSessionRepository.existsById(sessionId)) {
            throw new EntityNotFoundException("指定されたライブセッションが見つかりません: " + sessionId);
        }

        learningLiveSessionRepository.deleteById(sessionId);
    }
}
