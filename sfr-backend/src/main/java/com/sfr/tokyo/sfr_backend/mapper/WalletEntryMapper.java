package com.sfr.tokyo.sfr_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sfr.tokyo.sfr_backend.dto.WalletEntryDTO;
import com.sfr.tokyo.sfr_backend.entity.WalletEntry;

import java.util.List;

/**
 * WalletEntryエンティティとWalletEntryDTOの変換を行うMapper
 */
@Mapper(componentModel = "spring")
public interface WalletEntryMapper {

    /**
     * WalletEntryエンティティをDTOに変換
     * 
     * @param walletEntry 変換元のエンティティ
     * @return 変換されたDTO
     */
    WalletEntryDTO toDto(WalletEntry walletEntry);

    /**
     * WalletEntryエンティティのリストをDTOのリストに変換
     * 
     * @param walletEntries 変換元のエンティティリスト
     * @return 変換されたDTOリスト
     */
    List<WalletEntryDTO> toDtoList(List<WalletEntry> walletEntries);

    /**
     * WalletEntryDTOをエンティティに変換
     * 
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     */
    @Mapping(target = "id", ignore = true) // IDは自動生成
    @Mapping(target = "timestamp", defaultExpression = "java(java.time.LocalDateTime.now())") // デフォルトで現在時刻
    WalletEntry toEntity(WalletEntryDTO dto);
}
