package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningModeConfigDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningSpaceCreateDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningSpaceCreateResponse;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningModeConfig;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSpace;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningModeConfigRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningSpaceService {

    private final LearningSpaceRepository learningSpaceRepository;
    private final LearningModeConfigRepository configRepository;

    @Transactional
    public LearningSpaceCreateResponse createLearningSpace(LearningSpaceCreateDto dto) {
        log.info("Creating learning space with name: {} and mode: {}", dto.getName(), dto.getMode());

        // 名前の重複チェック
        if (learningSpaceRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("学習空間名が既に存在します: " + dto.getName());
        }

        // 学習空間の作成
        LearningSpace space = LearningSpace.builder()
                .name(dto.getName())
                .mode(dto.getMode())
                .build();

        LearningSpace savedSpace = learningSpaceRepository.save(space);

        // デフォルト設定の作成
        createDefaultConfig(savedSpace.getId(), dto.getMode());

        log.info("Successfully created learning space with ID: {}", savedSpace.getId());

        return LearningSpaceCreateResponse.builder()
                .spaceId(savedSpace.getId())
                .build();
    }

    public Optional<LearningSpace> findById(Long id) {
        return learningSpaceRepository.findById(id);
    }

    public LearningModeConfigDto getConfig(Long spaceId) {
        log.info("Getting config for learning space ID: {}", spaceId);

        Optional<LearningModeConfig> configOpt = configRepository.findBySpaceId(spaceId);

        if (configOpt.isEmpty()) {
            throw new IllegalArgumentException("指定された学習空間が見つかりません: " + spaceId);
        }

        LearningModeConfig config = configOpt.get();

        return LearningModeConfigDto.builder()
                .uiConfig(config.getUiConfig())
                .featureFlags(config.getFeatureFlags())
                .build();
    }

    @Transactional
    public void updateConfig(Long spaceId, LearningModeConfigDto dto) {
        log.info("Updating config for learning space ID: {}", spaceId);

        // 学習空間の存在確認
        if (!learningSpaceRepository.existsById(spaceId)) {
            throw new IllegalArgumentException("指定された学習空間が見つかりません: " + spaceId);
        }

        Optional<LearningModeConfig> configOpt = configRepository.findBySpaceId(spaceId);

        LearningModeConfig config;
        if (configOpt.isPresent()) {
            config = configOpt.get();
            config.setUiConfig(dto.getUiConfig());
            config.setFeatureFlags(dto.getFeatureFlags());
        } else {
            config = LearningModeConfig.builder()
                    .spaceId(spaceId)
                    .uiConfig(dto.getUiConfig())
                    .featureFlags(dto.getFeatureFlags())
                    .build();
        }

        configRepository.save(config);
        log.info("Successfully updated config for learning space ID: {}", spaceId);
    }

    private void createDefaultConfig(Long spaceId, LearningSpace.LearningMode mode) {
        Map<String, Object> defaultUiConfig = createDefaultUiConfig(mode);
        Map<String, Boolean> defaultFeatureFlags = createDefaultFeatureFlags(mode);

        LearningModeConfig config = LearningModeConfig.builder()
                .spaceId(spaceId)
                .uiConfig(defaultUiConfig)
                .featureFlags(defaultFeatureFlags)
                .build();

        configRepository.save(config);
    }

    private Map<String, Object> createDefaultUiConfig(LearningSpace.LearningMode mode) {
        Map<String, Object> uiConfig = new HashMap<>();

        switch (mode) {
            case SCHOOL:
                uiConfig.put("theme", "academic");
                uiConfig.put("primaryColor", "#2563eb");
                uiConfig.put("layout", "structured");
                break;
            case SALON:
                uiConfig.put("theme", "professional");
                uiConfig.put("primaryColor", "#059669");
                uiConfig.put("layout", "flexible");
                break;
            case FANCLUB:
                uiConfig.put("theme", "entertainment");
                uiConfig.put("primaryColor", "#dc2626");
                uiConfig.put("layout", "dynamic");
                break;
        }

        return uiConfig;
    }

    private Map<String, Boolean> createDefaultFeatureFlags(LearningSpace.LearningMode mode) {
        Map<String, Boolean> featureFlags = new HashMap<>();

        // 共通機能
        featureFlags.put("contentUpload", true);
        featureFlags.put("progress", true);
        featureFlags.put("forum", true);

        switch (mode) {
            case SCHOOL:
                featureFlags.put("liveSession", true);
                featureFlags.put("quiz", true);
                featureFlags.put("shakyo", true);
                featureFlags.put("aiTutor", true);
                featureFlags.put("subscription", false);
                break;
            case SALON:
                featureFlags.put("liveSession", true);
                featureFlags.put("quiz", false);
                featureFlags.put("shakyo", false);
                featureFlags.put("aiTutor", true);
                featureFlags.put("subscription", true);
                break;
            case FANCLUB:
                featureFlags.put("liveSession", true);
                featureFlags.put("quiz", false);
                featureFlags.put("shakyo", false);
                featureFlags.put("aiTutor", false);
                featureFlags.put("subscription", true);
                break;
        }

        return featureFlags;
    }
}
