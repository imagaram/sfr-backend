package com.sfr.tokyo.sfr_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sfr.tokyo.sfr_backend.dto.CommentDto;
import com.sfr.tokyo.sfr_backend.entity.Comment;

import java.util.List;

/**
 * CommentエンティティとCommentDtoの変換を行うMapper
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    /**
     * CommentエンティティをDTOに変換
     * 
     * @param comment 変換元のエンティティ
     * @return 変換されたDTO
     */
    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstname", target = "username") // usernameフィールドをfirstnameで代用
    @Mapping(source = "createdAt", target = "createdAt")
    CommentDto toDto(Comment comment);

    /**
     * Commentエンティティのリストをコメントのリストに変換
     * 
     * @param comments 変換元のエンティティリスト
     * @return 変換されたDTOリスト
     */
    List<CommentDto> toDtoList(List<Comment> comments);

    /**
     * CommentDTOをエンティティに変換
     * 
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     */
    @Mapping(target = "id", ignore = true) // IDは自動生成
    @Mapping(target = "post", ignore = true) // Postは別途設定
    @Mapping(target = "user", ignore = true) // Userは別途設定
    @Mapping(target = "createdAt", ignore = true)
    Comment toEntity(CommentDto dto);
}
