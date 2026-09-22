package com.rest1.domain.wallet.wallet.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.wallet.wallet.entity.Ledger;
import com.rest1.domain.wallet.wallet.entity.LedgerType;
import com.rest1.domain.wallet.wallet.entity.Wallet;
import com.rest1.domain.wallet.wallet.repository.LedgerRepository;
import com.rest1.domain.wallet.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;

    public Wallet create(Member member) {
        return walletRepository.save(new Wallet(member));
    }

    public Optional<Wallet> findByMemberId(Long memberId) {
        return walletRepository.findByMemberId(memberId);
    }

    // 충전: 잔액을 올리고 원장에 + 한 줄. 둘은 항상 같이 움직여야 불변식이 유지된다.
    // (카드 결제로 진짜 충전하는 것은 주제 5. 지금은 샘플 데이터·테스트용이다.)
    public Ledger charge(Wallet wallet, long amount) {
        wallet.deposit(amount);
        return ledgerRepository.save(new Ledger(wallet, LedgerType.CHARGE, amount, wallet.getBalance()));
    }

    // 구매 차감: 잔액에서 빼고 원장에 - 한 줄. 주문 확정과 같은 트랜잭션 안에서 불린다
    public Ledger pay(Wallet wallet, Order order) {
        wallet.pay(order.getPrice());
        return ledgerRepository.save(new Ledger(wallet, LedgerType.PURCHASE, -order.getPrice(), wallet.getBalance(), order));
    }

    public List<Ledger> findLedgers(Wallet wallet) {
        return ledgerRepository.findByWalletOrderByIdAsc(wallet);
    }

    public long sumLedger(Wallet wallet) {
        return ledgerRepository.sumAmountByWallet(wallet);
    }

    public List<Wallet> findAll() {
        return walletRepository.findAll();
    }

    public long countLedgers() {
        return ledgerRepository.count();
    }

    public long countLedgers(Wallet wallet, LedgerType type) {
        return ledgerRepository.countByWalletAndType(wallet, type);
    }
}
