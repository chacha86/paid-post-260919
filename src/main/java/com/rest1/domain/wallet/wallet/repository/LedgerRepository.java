package com.rest1.domain.wallet.wallet.repository;

import com.rest1.domain.wallet.wallet.entity.Ledger;
import com.rest1.domain.wallet.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    List<Ledger> findByWalletOrderByIdAsc(Wallet wallet);

    // 불변식 검사용: 이 지갑의 원장 합계
    @Query("select coalesce(sum(l.amount), 0) from Ledger l where l.wallet = :wallet")
    long sumAmountByWallet(@Param("wallet") Wallet wallet);
}
