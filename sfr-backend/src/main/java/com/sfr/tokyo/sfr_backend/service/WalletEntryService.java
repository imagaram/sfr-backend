package com.sfr.tokyo.sfr_backend.service;

import com.sfr.tokyo.sfr_backend.dto.WalletEntryDTO;
import com.sfr.tokyo.sfr_backend.dto.WalletBalanceDTO;
import com.sfr.tokyo.sfr_backend.entity.WalletEntry;
import com.sfr.tokyo.sfr_backend.mapper.WalletEntryMapper;
import com.sfr.tokyo.sfr_backend.repository.WalletEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletEntryService {
    @Autowired
    private WalletEntryRepository walletEntryRepository;
    @Autowired
    private WalletEntryMapper walletEntryMapper;

    /**
     * 経済活動エントリ追加
     */
    @Transactional
    public WalletEntryDTO addEntry(WalletEntryDTO dto) {
        // DTOからエンティティに変換（Mapperを使用）
        WalletEntry entry = walletEntryMapper.toEntity(dto);

        // 保存
        WalletEntry saved = walletEntryRepository.save(entry);

        // エンティティからDTOに変換して返却（Mapperを使用）
        return walletEntryMapper.toDto(saved);
    }

    /**
     * チーム別エントリ一覧取得
     */
    @Transactional(readOnly = true)
    public List<WalletEntryDTO> getEntriesByTeam(Long teamId) {
        return walletEntryRepository.findByTeamId(teamId)
                .stream()
                .map(walletEntryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 期間指定での残高計算
     */
    @Transactional(readOnly = true)
    public WalletBalanceDTO getBalance(Long teamId, LocalDateTime start, LocalDateTime end) {
        List<WalletEntry> entries = walletEntryRepository.findByTeamIdAndTimestampBetween(teamId, start, end);

        // 収入・支出の計算
        double income = entries.stream()
                .filter(e -> e.getTransactionType().name().equals("INCOME"))
                .mapToDouble(WalletEntry::getAmount)
                .sum();

        double expense = entries.stream()
                .filter(e -> e.getTransactionType().name().equals("EXPENSE"))
                .mapToDouble(WalletEntry::getAmount)
                .sum();

        double total = income - expense;

        WalletBalanceDTO dto = new WalletBalanceDTO();
        dto.setWalletId(teamId);
        dto.setTotalBalance(total);
        return dto;
    }
}
