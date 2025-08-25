package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningLiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningLiveSessionRepository extends JpaRepository<LearningLiveSession, Long> {

    /**
     * オーナーIDでライブセッションを検索
     * 
     * @param ownerId オーナーID
     * @return ライブセッションリスト
     */
    List<LearningLiveSession> findByOwnerIdOrderByScheduledAtAsc(Long ownerId);

    /**
     * 開催予定日時の範囲でライブセッションを検索
     * 
     * @param startTime 開始日時
     * @param endTime   終了日時
     * @return ライブセッションリスト
     */
    List<LearningLiveSession> findByScheduledAtBetweenOrderByScheduledAtAsc(LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 未来のライブセッションを検索
     * 
     * @param currentTime 現在日時
     * @return ライブセッションリスト
     */
    List<LearningLiveSession> findByScheduledAtAfterOrderByScheduledAtAsc(LocalDateTime currentTime);

    /**
     * オーナーIDとセッションIDでライブセッションを検索
     * 
     * @param ownerId オーナーID
     * @param id      セッションID
     * @return ライブセッション（Optional）
     */
    Optional<LearningLiveSession> findByOwnerIdAndId(Long ownerId, Long id);

    /**
     * オーナーのライブセッション数を取得
     * 
     * @param ownerId オーナーID
     * @return セッション数
     */
    long countByOwnerId(Long ownerId);
}
