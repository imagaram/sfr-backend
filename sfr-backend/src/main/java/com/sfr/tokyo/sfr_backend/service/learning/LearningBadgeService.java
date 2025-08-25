package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningBadgeDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningBadge;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningBadge.BadgeType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningUserBadge;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningBadgeRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningUserBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * バッジサービス
 */
@Service
@Transactional
public class LearningBadgeService {

    @Autowired
    private LearningBadgeRepository badgeRepository;

    @Autowired
    private LearningUserBadgeRepository userBadgeRepository;

    /**
     * バッジ一覧取得
     */
    @Transactional(readOnly = true)
    public List<LearningBadgeDto> getAllBadges() {
        return badgeRepository.findByIsActiveTrueOrderById()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定されたスペースのバッジ一覧取得
     */
    @Transactional(readOnly = true)
    public List<LearningBadgeDto> getBadgesBySpace(UUID spaceId) {
        return badgeRepository.findAvailableBadgesForSpace(spaceId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * バッジタイプ別取得
     */
    @Transactional(readOnly = true)
    public List<LearningBadgeDto> getBadgesByType(BadgeType badgeType) {
        return badgeRepository.findByBadgeTypeAndIsActiveTrue(badgeType)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * バッジ詳細取得
     */
    @Transactional(readOnly = true)
    public Optional<LearningBadgeDto> getBadgeById(Long badgeId) {
        return badgeRepository.findById(badgeId)
                .map(this::convertToDto);
    }

    /**
     * バッジ作成
     */
    public LearningBadgeDto createBadge(LearningBadgeDto badgeDto) {
        LearningBadge badge = convertToEntity(badgeDto);
        LearningBadge savedBadge = badgeRepository.save(badge);
        return convertToDto(savedBadge);
    }

    /**
     * バッジ更新
     */
    public LearningBadgeDto updateBadge(Long badgeId, LearningBadgeDto badgeDto) {
        LearningBadge existingBadge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("バッジが見つかりません: " + badgeId));

        // 更新
        existingBadge.setName(badgeDto.getName());
        existingBadge.setDescription(badgeDto.getDescription());
        existingBadge.setIconUrl(badgeDto.getIconUrl());
        existingBadge.setBadgeType(badgeDto.getBadgeType());
        existingBadge.setRequiredValue(badgeDto.getRequiredValue());
        existingBadge.setSpaceId(badgeDto.getSpaceId());
        existingBadge.setIsActive(badgeDto.getIsActive());

        LearningBadge savedBadge = badgeRepository.save(existingBadge);
        return convertToDto(savedBadge);
    }

    /**
     * バッジ削除（論理削除）
     */
    public void deleteBadge(Long badgeId) {
        LearningBadge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("バッジが見つかりません: " + badgeId));

        badge.setIsActive(false);
        badgeRepository.save(badge);
    }

    /**
     * ユーザーのバッジ獲得チェック
     * 指定された条件で獲得可能なバッジを自動付与
     */
    public List<LearningUserBadge> awardBadgesIfEligible(UUID userId, UUID spaceId,
            BadgeType badgeType, Integer currentValue) {
        // 既に獲得済みのバッジIDを取得
        List<Long> earnedBadgeIds = userBadgeRepository.findByUserId(userId)
                .stream()
                .map(userBadge -> userBadge.getBadge().getId())
                .collect(Collectors.toList());

        // 獲得可能なバッジを検索（未獲得のもののみ）
        List<LearningBadge> eligibleBadges = badgeRepository.findByTypeAndCondition(badgeType, currentValue, spaceId)
                .stream()
                .filter(badge -> !earnedBadgeIds.contains(badge.getId()))
                .filter(badge -> badge.isEligible(currentValue))
                .collect(Collectors.toList());

        // バッジ獲得処理
        return eligibleBadges.stream()
                .map(badge -> awardBadge(userId, badge.getId(), currentValue,
                        String.format("条件達成により獲得 (必要値: %d)", badge.getRequiredValue())))
                .collect(Collectors.toList());
    }

    /**
     * バッジ手動付与
     */
    public LearningUserBadge awardBadge(UUID userId, Long badgeId, Integer earnedValue, String reason) {
        // 既に獲得済みかチェック
        if (userBadgeRepository.existsByUserIdAndBadge_Id(userId, badgeId)) {
            throw new IllegalArgumentException("このバッジは既に獲得済みです");
        }

        // バッジエンティティを取得
        LearningBadge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("バッジが見つかりません: " + badgeId));

        LearningUserBadge userBadge = new LearningUserBadge();
        userBadge.setUserId(userId);
        userBadge.setBadge(badge);
        userBadge.setEarnedValue(earnedValue);
        userBadge.setEarnedReason(reason);
        userBadge.setEarnedAt(LocalDateTime.now());

        return userBadgeRepository.save(userBadge);
    }

    /**
     * ユーザーバッジ剥奪
     */
    public void revokeBadge(UUID userId, Long badgeId) {
        LearningUserBadge userBadge = userBadgeRepository.findByUserIdAndBadge_Id(userId, badgeId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたバッジを獲得していません"));

        userBadgeRepository.delete(userBadge);
    }

    /**
     * バッジ名検索
     */
    @Transactional(readOnly = true)
    public List<LearningBadgeDto> searchBadgesByName(String name) {
        return badgeRepository.findByNameContaining(name)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * DTO変換
     */
    private LearningBadgeDto convertToDto(LearningBadge badge) {
        LearningBadgeDto dto = new LearningBadgeDto();
        dto.setId(badge.getId());
        dto.setName(badge.getName());
        dto.setDescription(badge.getDescription());
        dto.setIconUrl(badge.getIconUrl());
        dto.setBadgeType(badge.getBadgeType());
        dto.setRequiredValue(badge.getRequiredValue());
        dto.setSpaceId(badge.getSpaceId());
        dto.setIsActive(badge.getIsActive());
        dto.setCreatedAt(badge.getCreatedAt());
        dto.setUpdatedAt(badge.getUpdatedAt());
        return dto;
    }

    /**
     * エンティティ変換
     */
    private LearningBadge convertToEntity(LearningBadgeDto dto) {
        LearningBadge badge = new LearningBadge();
        badge.setName(dto.getName());
        badge.setDescription(dto.getDescription());
        badge.setIconUrl(dto.getIconUrl());
        badge.setBadgeType(dto.getBadgeType());
        badge.setRequiredValue(dto.getRequiredValue());
        badge.setSpaceId(dto.getSpaceId());
        badge.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return badge;
    }
}
