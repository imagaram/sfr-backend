package com.sfr.tokyo.sfr_backend.service.space;

import com.sfr.tokyo.sfr_backend.dto.space.SpaceCreateDto;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceCreateResponse;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceModeConfigDto;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceDto;
import com.sfr.tokyo.sfr_backend.dto.space.SpaceStatisticsDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningModeConfigDto;
import com.sfr.tokyo.sfr_backend.entity.space.Space;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSpace;
import com.sfr.tokyo.sfr_backend.repository.space.SpaceRepository;
import com.sfr.tokyo.sfr_backend.service.learning.LearningSpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * スペースサービス（新しい統合型）
 * 
 * 新しいSpace entityを使用した統合スペース管理
 * データベース移行期間中は既存LearningSpaceServiceとの共存
 * 
 * @author SFR Development Team
 * @version 3.0
 * @since 2025-09-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final LearningSpaceService learningSpaceService; // 移行期間中の互換性維持

    /**
     * スペース作成（新実装）
     * 
     * @param dto スペース作成DTO
     * @return 作成レスポンス
     */
    @Transactional
    public SpaceCreateResponse createSpace(SpaceCreateDto dto) {
        log.info("Creating new space with name: {} and mode: {}", dto.getName(), dto.getMode());

        // LearningModeをSpaceModeに変換
        Space.SpaceMode spaceMode = convertLearningModeToSpaceMode(dto.getMode());

        // 新しいSpace entityで作成
        Space space = Space.builder()
                .name(dto.getName())
                .description(dto.getDescription() != null ? dto.getDescription() : "")
                .mode(spaceMode)
                .status(Space.SpaceStatus.ACTIVE)
                .ownerId(dto.getOwnerId() != null ? dto.getOwnerId().toString() : "1") // StringとしてIDを保存
                .memberCount(1) // オーナーが初期メンバー
                .maxMembers(dto.getMaxMembers() != null ? dto.getMaxMembers() : 100)
                .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true)
                .build();

        Space savedSpace = spaceRepository.save(space);

        return SpaceCreateResponse.builder()
                .spaceId(savedSpace.getId())
                .build();
    }
    
    /**
     * LearningModeをSpaceModeに変換
     */
    private Space.SpaceMode convertLearningModeToSpaceMode(LearningSpace.LearningMode learningMode) {
        switch (learningMode) {
            case SCHOOL:
                return Space.SpaceMode.SCHOOL;
            case SALON:
                return Space.SpaceMode.SALON;
            case FANCLUB:
                return Space.SpaceMode.FANCLUB;
            default:
                return Space.SpaceMode.SCHOOL; // デフォルト
        }
    }

    /**
     * スペース取得（新実装）
     * 
     * @param id スペースID
     * @return スペース情報
     */
    public Optional<Space> findById(Long id) {
        return spaceRepository.findById(id);
    }
    
    /**
     * スペースDTO変換
     * 
     * @param space スペースエンティティ
     * @return スペースDTO
     */
    public SpaceDto convertToDto(Space space) {
        return SpaceDto.builder()
                .id(space.getId())
                .name(space.getName())
                .description(space.getDescription())
                .mode(space.getMode().name())
                .status(space.getStatus().name())
                .ownerId(Long.valueOf(space.getOwnerId()))
                .memberCount(space.getMemberCount())
                .maxMembers(space.getMaxMembers())
                .isPublic(space.getIsPublic())
                .createdAt(space.getCreatedAt())
                .lastActivity(space.getUpdatedAt()) // updatedAtをlastActivityとして使用
                .popularityScore(calculatePopularityScore(space)) // 計算ロジック
                .build();
    }
    
    /**
     * 人気度スコア計算
     */
    private Double calculatePopularityScore(Space space) {
        // メンバー数と最新活動を基にスコア計算
        double memberScore = (double) space.getMemberCount() / space.getMaxMembers() * 100;
        long daysSinceUpdate = java.time.Duration.between(space.getUpdatedAt(), LocalDateTime.now()).toDays();
        double activityScore = Math.max(0, 100 - daysSinceUpdate * 2); // 日数に応じてスコア減少
        
        return (memberScore * 0.7 + activityScore * 0.3);
    }

    /**
     * 人気スペース取得
     * 
     * @param mode スペースモード（オプション）
     * @param pageable ページング情報
     * @return 人気スペースリスト
     */
    public Page<SpaceDto> getPopularSpaces(String mode, Pageable pageable) {
        List<Space> spaces;
        
        if (mode != null && !mode.isEmpty()) {
            Space.SpaceMode spaceMode = Space.SpaceMode.valueOf(mode.toUpperCase());
            spaces = spaceRepository.findPopularSpacesByMode(spaceMode, pageable);
        } else {
            spaces = spaceRepository.findPopularSpaces(pageable);
        }
        
        // DTO変換とページング
        List<SpaceDto> spaceDtos = spaces.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return new PageImpl<>(spaceDtos, pageable, spaceDtos.size());
    }

    /**
     * スペース検索
     * 
     * @param keyword 検索キーワード
     * @param pageable ページング情報
     * @return 検索結果
     */
    public Page<SpaceDto> searchSpaces(String keyword, Pageable pageable) {
        Page<Space> spaces = spaceRepository.findByKeyword(keyword, pageable);
        return spaces.map(this::convertToDto);
    }

    /**
     * スペース統計取得
     * 
     * @return 統計情報
     */
    public SpaceStatisticsDto getStatistics() {
        // 簡易的な統計計算（実際の実装では詳細なクエリが必要）
        long totalSpaces = spaceRepository.count();
        long activeSpaces = spaceRepository.countByStatus(Space.SpaceStatus.ACTIVE);
        
        return SpaceStatisticsDto.builder()
                .totalSpaces(totalSpaces)
                .activeSpaces(activeSpaces)
                .totalMembers(0L) // 実装が必要
                .averageMembers(0.0) // 実装が必要
                .schoolStats(convertModeStats("SCHOOL"))
                .salonStats(convertModeStats("SALON"))
                .fanclubStats(convertModeStats("FANCLUB"))
                .build();
    }
    
    private SpaceStatisticsDto.SpaceModeStatsDto convertModeStats(String mode) {
        // 実装は後で追加
        return SpaceStatisticsDto.SpaceModeStatsDto.builder()
                .count(0L)
                .memberCount(0L)
                .averagePopularity(0.0)
                .build();
    }

    /**
     * メンバー参加
     * 
     * @param spaceId スペースID
     * @param userId ユーザーID
     * @return 参加成功フラグ
     */
    @Transactional
    public boolean joinSpace(Long spaceId, Long userId) {
        Optional<Space> spaceOpt = spaceRepository.findById(spaceId);
        if (spaceOpt.isEmpty()) {
            return false;
        }
        
        Space space = spaceOpt.get();
        if (space.isFull()) {
            log.warn("Space {} is full, cannot join user {}", spaceId, userId);
            return false;
        }
        
        space.incrementMemberCount();
        spaceRepository.save(space);
        
        log.info("User {} joined space {}", userId, spaceId);
        return true;
    }

    // ===== 移行期間中の互換性メソッド =====
    
    /**
     * 旧形式でのスペース取得（互換性維持）
     * 
     * @param id スペースID
     * @return 旧形式スペース情報
     */
    @Deprecated
    public Optional<Object> findByIdLegacy(Long id) {
        return learningSpaceService.findById(id).map(space -> (Object) space);
    }

    /**
     * スペース設定取得
     * 
     * @param spaceId スペースID
     * @return 設定情報
     */
    public SpaceModeConfigDto getConfig(Long spaceId) {
        log.info("Getting config for space ID: {}", spaceId);

        LearningModeConfigDto learningConfig = learningSpaceService.getConfig(spaceId);

        return SpaceModeConfigDto.builder()
                .uiConfig(learningConfig.getUiConfig())
                .featureFlags(learningConfig.getFeatureFlags())
                .build();
    }

    /**
     * スペース設定更新
     * 
     * @param spaceId スペースID
     * @param dto 設定DTO
     */
    @Transactional
    public void updateConfig(Long spaceId, SpaceModeConfigDto dto) {
        log.info("Updating config for space ID: {}", spaceId);

        LearningModeConfigDto learningDto = LearningModeConfigDto.builder()
                .uiConfig(dto.getUiConfig())
                .featureFlags(dto.getFeatureFlags())
                .build();

        learningSpaceService.updateConfig(spaceId, learningDto);
    }
}
