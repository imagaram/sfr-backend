package com.sfr.tokyo.sfr_backend.council.service;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilParameterDto;
import com.sfr.tokyo.sfr_backend.council.repository.CouncilParameterRepository;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 評議員制度パラメーターサービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouncilParameterService {

    private final CouncilParameterRepository repository;

    /**
     * パラメーター作成
     */
    public CouncilParameterDto createParameter(CouncilParameterDto dto) {
        log.info("Creating council parameter: {}", dto.getParamKey());
        
        if (repository.existsByParamKey(dto.getParamKey())) {
            throw new IllegalArgumentException("Parameter key already exists: " + dto.getParamKey());
        }

        CouncilParameter entity = dtoToEntity(dto);
        entity = repository.save(entity);
        return entityToDto(entity);
    }

    /**
     * パラメーター更新
     */
    public CouncilParameterDto updateParameter(String paramKey, CouncilParameterDto dto) {
        log.info("Updating council parameter: {}", paramKey);
        
        CouncilParameter entity = repository.findByParamKey(paramKey)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found: " + paramKey));

        // 値のタイプが変更される場合は既存の値をクリア
        if (!entity.getValueType().equals(dto.getValueType())) {
            entity.setValueString(null);
            entity.setValueNumber(null);
            entity.setValueJson(null);
        }

        entity.setDescription(dto.getDescription());
        entity.setValueType(dto.getValueType());
        entity.setValue(dto.getValue());

        entity = repository.save(entity);
        return entityToDto(entity);
    }

    /**
     * パラメーター削除
     */
    public void deleteParameter(String paramKey) {
        log.info("Deleting council parameter: {}", paramKey);
        
        CouncilParameter entity = repository.findByParamKey(paramKey)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found: " + paramKey));
        
        repository.delete(entity);
    }

    /**
     * パラメーター取得（キー指定）
     */
    @Transactional(readOnly = true)
    public Optional<CouncilParameterDto> getParameter(String paramKey) {
        return repository.findByParamKey(paramKey)
                .map(this::entityToDto);
    }

    /**
     * 全パラメーター取得
     */
    @Transactional(readOnly = true)
    public List<CouncilParameterDto> getAllParameters() {
        return repository.findAll().stream()
                .map(this::entityToDto)
                .toList();
    }

    /**
     * 値の型による検索
     */
    @Transactional(readOnly = true)
    public List<CouncilParameterDto> getParametersByType(CouncilParameter.ValueType valueType) {
        return repository.findByValueType(valueType).stream()
                .map(this::entityToDto)
                .toList();
    }

    /**
     * パラメーターキーの部分一致検索
     */
    @Transactional(readOnly = true)
    public List<CouncilParameterDto> searchParametersByKey(String keyPattern) {
        return repository.findByParamKeyContaining(keyPattern).stream()
                .map(this::entityToDto)
                .toList();
    }

    /**
     * パラメーター値の取得（型安全）
     */
    @Transactional(readOnly = true)
    public <T> Optional<T> getParameterValue(String paramKey, Class<T> type) {
        return repository.findByParamKey(paramKey)
                .map(param -> {
                    Object value = param.getValue();
                    if (value == null) return null;
                    
                    try {
                        return type.cast(value);
                    } catch (ClassCastException e) {
                        log.warn("Type mismatch for parameter {}: expected {}, got {}", 
                                paramKey, type.getSimpleName(), value.getClass().getSimpleName());
                        return null;
                    }
                });
    }

    /**
     * パラメーター値の設定（簡易メソッド）
     */
    public void setParameterValue(String paramKey, Object value, CouncilParameter.ValueType valueType) {
        Optional<CouncilParameter> existing = repository.findByParamKey(paramKey);
        
        if (existing.isPresent()) {
            CouncilParameter param = existing.get();
            param.setValueType(valueType);
            param.setValue(value);
            repository.save(param);
        } else {
            CouncilParameter param = CouncilParameter.builder()
                    .paramKey(paramKey)
                    .valueType(valueType)
                    .build();
            param.setValue(value);
            repository.save(param);
        }
    }

    /**
     * エンティティからDTOへの変換
     */
    private CouncilParameterDto entityToDto(CouncilParameter entity) {
        return CouncilParameterDto.builder()
                .id(entity.getId())
                .paramKey(entity.getParamKey())
                .description(entity.getDescription())
                .valueType(entity.getValueType())
                .valueString(entity.getValueString())
                .valueNumber(entity.getValueNumber())
                .valueJson(entity.getValueJson())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * DTOからエンティティへの変換
     */
    private CouncilParameter dtoToEntity(CouncilParameterDto dto) {
        CouncilParameter entity = CouncilParameter.builder()
                .paramKey(dto.getParamKey())
                .description(dto.getDescription())
                .valueType(dto.getValueType())
                .valueString(dto.getValueString())
                .valueNumber(dto.getValueNumber())
                .valueJson(dto.getValueJson())
                .build();
        
        // DTOに値が設定されている場合は適用
        if (dto.getValue() != null) {
            entity.setValue(dto.getValue());
        }
        
        return entity;
    }
}
