package com.rest1.domain.wallet.wallet.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.wallet.wallet.entity.Ledger;
import com.rest1.domain.wallet.wallet.entity.LedgerType;
import com.rest1.domain.wallet.wallet.entity.Wallet;
import com.rest1.domain.wallet.wallet.repository.LedgerRepository;
import com.rest1.domain.wallet.wallet.repository.WalletRepository;
import com.rest1.global.exception.ServiceException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;
    private final EntityManager entityManager;

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

    // 구매 차감: 확인+차감을 DB 문장 하나로(tryPay) 하고, 원장에 - 한 줄.
    // 잔액 부족이면 UPDATE 가 0행 → 402-1 → 호출한 쪽 트랜잭션이 통째로 롤백된다(원장도 주문 변경도 남지 않는다).
    public Ledger pay(Wallet wallet, Order order) {
        int updated = walletRepository.tryPay(wallet.getId(), order.getPrice());
        if (updated == 0) {
            throw new ServiceException("402-1", "잔액이 부족합니다.");
        }

        // ⚠️ 함정. 위 UPDATE 는 DB 에서만 일어났고, 메모리의 wallet 객체는 아직 옛 잔액을 들고 있다.
        // 이 한 줄이 없으면 바로 아래 원장의 balanceAfter 와 응답의 balance 가 차감 전 값으로 나간다.
        // 다시 조회(findByMemberId)해도 소용없다 — 같은 트랜잭션에서는 캐시된 같은 객체가 돌아온다(13강에서 확인).
        // @Modifying(clearAutomatically = true) 로 캐시를 통째로 비우는 방법도 있지만,
        // 그러면 같이 들고 있던 order 까지 떨어져 나가 order.confirm() 이 저장되지 않는다. 그래서 이 객체 하나만 새로 읽는다.
        entityManager.refresh(wallet);

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
