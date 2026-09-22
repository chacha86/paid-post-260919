package com.rest1.domain.wallet.wallet.dto;

import com.rest1.domain.wallet.wallet.entity.Ledger;
import com.rest1.domain.wallet.wallet.entity.Wallet;

import java.util.List;

public record WalletDto(
        Long id,
        long balance,
        List<LedgerDto> ledgers
) {
    public WalletDto(Wallet wallet, List<Ledger> ledgers) {
        this(
                wallet.getId(),
                wallet.getBalance(),
                ledgers.stream().map(LedgerDto::new).toList()
        );
    }
}
