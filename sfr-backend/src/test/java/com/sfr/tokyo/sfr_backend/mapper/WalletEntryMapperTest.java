package com.sfr.tokyo.sfr_backend.mapper;

import com.sfr.tokyo.sfr_backend.dto.WalletEntryDTO;
import com.sfr.tokyo.sfr_backend.entity.TransactionType;
import com.sfr.tokyo.sfr_backend.entity.WalletEntry;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static com.sfr.tokyo.sfr_backend.assertions.WalletEntryAssertions.assertThatPair;

class WalletEntryMapperTest {

    private final WalletEntryMapper walletEntryMapper = Mappers.getMapper(WalletEntryMapper.class);

    @Test
    void toDto_shouldMapEntityToDto() {
        WalletEntry entity = new WalletEntry();
        entity.setId(10L);
        entity.setTeamId(5L);
        entity.setAmount(123.45);
        entity.setDescription("Test Desc");
        entity.setTransactionType(TransactionType.INCOME);
        entity.setTimestamp(LocalDateTime.now());

        WalletEntryDTO dto = walletEntryMapper.toDto(entity);

    assertThat(dto).isNotNull();
    assertThatPair(entity, dto).hasSameCoreFields();
    assertThat(dto.getId()).isEqualTo(10L);
    assertThat(dto.getTimestamp()).isNotNull();
    }

    @Test
    void toEntity_shouldMapDtoToEntity() {
        WalletEntryDTO dto = new WalletEntryDTO();
        dto.setTeamId(7L);
        dto.setAmount(999.0);
        dto.setDescription("DTO Desc");
        dto.setTransactionType(TransactionType.EXPENSE);
        // timestamp は mapper 側で now() 設定 (defaultExpression)

        WalletEntry entity = walletEntryMapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getTeamId()).isEqualTo(7L);
        assertThat(entity.getAmount()).isEqualTo(999.0);
        assertThat(entity.getDescription()).isEqualTo("DTO Desc");
        assertThat(entity.getTransactionType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(entity.getTimestamp()).isNotNull();
    }

    @Test
    void toDtoList_shouldMapAll() {
        WalletEntry e1 = new WalletEntry();
        e1.setTeamId(1L); e1.setAmount(1.0); e1.setDescription("A"); e1.setTransactionType(TransactionType.INCOME); e1.setTimestamp(LocalDateTime.now());
        WalletEntry e2 = new WalletEntry();
        e2.setTeamId(2L); e2.setAmount(2.0); e2.setDescription("B"); e2.setTransactionType(TransactionType.EXPENSE); e2.setTimestamp(LocalDateTime.now());

        List<WalletEntryDTO> dtos = walletEntryMapper.toDtoList(List.of(e1, e2));
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getDescription()).isEqualTo("A");
        assertThat(dtos.get(1).getTransactionType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void roundTrip_shouldPreserveFieldsExceptIgnored() {
        WalletEntry entity = new WalletEntry();
        entity.setId(99L);
        entity.setTeamId(42L);
        entity.setAmount(10.5);
        entity.setDescription("RoundTrip");
        entity.setTransactionType(TransactionType.INCOME);
        entity.setTimestamp(LocalDateTime.now());
        WalletEntryDTO dto = walletEntryMapper.toDto(entity);
        WalletEntry mapped = walletEntryMapper.toEntity(dto);
    assertThatPair(mapped, dto).hasSameCoreFields();
    }

    @Test
    void nullHandling_shouldReturnNull() {
        assertThat(walletEntryMapper.toDto(null)).isNull();
        assertThat(walletEntryMapper.toEntity(null)).isNull();
    }

    @Test
    void emptyList_shouldReturnEmptyList() {
        assertThat(walletEntryMapper.toDtoList(List.of())).isEmpty();
    }
}
