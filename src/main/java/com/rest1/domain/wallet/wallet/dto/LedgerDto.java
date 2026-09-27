package com.rest1.domain.wallet.wallet.dto;

import com.rest1.domain.wallet.wallet.entity.Ledger;
import com.rest1.domain.wallet.wallet.entity.LedgerType;

import java.time.LocalDateTime;

public record LedgerDto(
        Long id,
        LocalDateTime createDate,
        LedgerType type,
        long amount,
        long balanceAfter
) {
    public LedgerDto(Ledger ledger) {
        this(ledger.getId(), ledger.getCreateDate(), ledger.getType(), ledger.getAmount(), ledger.getBalanceAfter());
    }
}
