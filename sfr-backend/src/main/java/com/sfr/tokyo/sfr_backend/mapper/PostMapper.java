package com.sfr.tokyo.sfr_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.sfr.tokyo.sfr_backend.dto.PostDto;
import com.sfr.tokyo.sfr_backend.entity.PostEntity;

import java.util.List;

/**
 * PostエンティティとPostDtoの変換を行うMapper
 */
@Mapper(componentModel = "spring")
public interface PostMapper {

    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    /**
     * PostエンティティをDTOに変換
     * 
     * @param post 変換元のエンティティ
     * @return 変換されたDTO
     */
    @Mapping(source = "user.id", target = "userId")
    PostDto toDto(PostEntity post);

    /**
     * Postエンティティのリストをコメントのリストに変換
     * 
     * @param posts 変換元のエンティティリスト
     * @return 変換されたDTOリスト
     */
    List<PostDto> toDtoList(List<PostEntity> posts);

    /**
     * PostDTOをエンティティに変換
     * 
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     */
    @Mapping(target = "id", ignore = true) // IDは自動生成
    @Mapping(target = "user", ignore = true) // Userは別途設定
    PostEntity toEntity(PostDto dto);

    /**
     * 既存のエンティティを更新
     * 
     * @param dto  更新データのDTO
     * @param post 更新対象のエンティティ
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(PostDto dto, @MappingTarget PostEntity post);
}
