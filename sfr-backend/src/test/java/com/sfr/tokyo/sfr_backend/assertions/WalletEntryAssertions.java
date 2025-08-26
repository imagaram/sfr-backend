package com.sfr.tokyo.sfr_backend.assertions;

import com.sfr.tokyo.sfr_backend.entity.WalletEntry;
import com.sfr.tokyo.sfr_backend.dto.WalletEntryDTO;
// BasePairAssert で共通処理

public class WalletEntryAssertions {

    public static WalletEntryPairAssert assertThatPair(WalletEntry entity, WalletEntryDTO dto) {
        return new WalletEntryPairAssert(entity, dto);
    }

    public static class WalletEntryPairAssert extends BasePairAssert<WalletEntryPairAssert, WalletEntry, WalletEntryDTO> {
        public WalletEntryPairAssert(WalletEntry actual, WalletEntryDTO dto) {
            super(actual, dto, WalletEntryPairAssert.class);
        }
        public WalletEntryPairAssert hasSameCoreFields() {
            isNotNull();
            requireDto();
            compare("teamId", actual.getTeamId(), dto.getTeamId());
            compare("amount", actual.getAmount(), dto.getAmount());
            compare("description", actual.getDescription(), dto.getDescription());
            compare("transactionType", actual.getTransactionType(), dto.getTransactionType());
            return this;
        }
    }
}
