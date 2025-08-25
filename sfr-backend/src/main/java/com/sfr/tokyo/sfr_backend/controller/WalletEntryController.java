package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.dto.WalletEntryDTO;
import com.sfr.tokyo.sfr_backend.dto.WalletBalanceDTO;
import com.sfr.tokyo.sfr_backend.service.WalletEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/wallet")
public class WalletEntryController {
    @Autowired
    private WalletEntryService walletEntryService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/entry")
    public WalletEntryDTO addEntry(@Valid @RequestBody WalletEntryDTO dto) {
        return walletEntryService.addEntry(dto);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/entries")
    public List<WalletEntryDTO> getEntries(@RequestParam Long teamId) {
        return walletEntryService.getEntriesByTeam(teamId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/balance")
    public WalletBalanceDTO getBalance(
            @RequestParam Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return walletEntryService.getBalance(teamId, start, end);
    }
}
