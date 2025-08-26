package com.sfr.tokyo.sfr_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.sfr.tokyo.sfr_backend.dto.UserDto;
import com.sfr.tokyo.sfr_backend.user.User;

import java.util.List;

/**
 * UserエンティティとUserDtoの変換を行うMapper
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * UserエンティティをDTOに変換
     * 
     * @param user 変換元のエンティティ
     * @return 変換されたDTO
     */
    UserDto toDto(User user);

    /**
     * Userエンティティのリストをユーザーのリストに変換
     * 
     * @param users 変換元のエンティティリスト
     * @return 変換されたDTOリスト
     */
    List<UserDto> toDtoList(List<User> users);

    /**
     * UserDTOをエンティティに変換
     * 
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     */
    @Mapping(target = "id", ignore = true) // IDは自動生成
    @Mapping(target = "password", ignore = true) // パスワードは別途設定
    @Mapping(target = "status", ignore = true) // Status Enumは別途設定
    @Mapping(target = "state", ignore = true) // UserState Enumは別途設定
    User toEntity(UserDto dto);

    /**
     * 既存のエンティティを更新
     * 
     * @param dto  更新データのDTO
     * @param user 更新対象のエンティティ
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) // メール変更は別途処理
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "authorities", ignore = true) // Spring SecurityのUserDetailsインターフェースのメソッド
    void updateEntityFromDto(UserDto dto, @MappingTarget User user);
}
