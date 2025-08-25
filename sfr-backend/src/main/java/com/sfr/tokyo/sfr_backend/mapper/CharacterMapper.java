package com.sfr.tokyo.sfr_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.sfr.tokyo.sfr_backend.dto.CharacterDto;
import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;

import java.util.List;

/**
 * CharacterエンティティとCharacterDtoの変換を行うMapper
 */
@Mapper(componentModel = "spring")
public interface CharacterMapper {

    CharacterMapper INSTANCE = Mappers.getMapper(CharacterMapper.class);

    /**
     * CharacterエンティティをDTOに変換
     * 
     * @param character 変換元のエンティティ
     * @return 変換されたDTO
     */
    @Mapping(source = "user.id", target = "userId")
    CharacterDto toDto(CharacterLifecycle character);

    /**
     * CharacterエンティティのリストをDTOのリストに変換
     * 
     * @param characters 変換元のエンティティリスト
     * @return 変換されたDTOリスト
     */
    List<CharacterDto> toDtoList(List<CharacterLifecycle> characters);

    /**
     * CharacterDTOをエンティティに変換
     * 
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     */
    @Mapping(target = "user", ignore = true) // userは別途設定
    @Mapping(target = "id", ignore = true) // IDは自動生成
    @Mapping(target = "createdAt", ignore = true) // @PrePersistで設定
    @Mapping(target = "updatedAt", ignore = true) // @PrePersistで設定
    @Mapping(target = "lifespanPoints", defaultValue = "365")
    @Mapping(target = "status", defaultExpression = "java(com.sfr.tokyo.sfr_backend.entity.CharacterStatus.ACTIVE)")
    CharacterLifecycle toEntity(CharacterDto dto);

    /**
     * 既存のエンティティを更新
     * 
     * @param dto       更新データのDTO
     * @param character 更新対象のエンティティ
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lifespanPoints", ignore = true) // 既存値を保持
    @Mapping(target = "status", ignore = true) // 既存値を保持
    void updateEntityFromDto(CharacterDto dto, @MappingTarget CharacterLifecycle character);
}
