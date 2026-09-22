package com.rest1.domain.wallet.wallet.repository;

import com.rest1.domain.wallet.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByMemberId(Long memberId);

    // 조건부 원자 UPDATE: "잔액이 amount 이상일 때만 amount 를 뺀다" 를 SQL 한 문장으로.
    //
    // 핵심은 확인(where balance >= amount)과 변경(set balance = balance - amount)이 같은 문장 안에 있다는 것이다.
    // 자바에서 "읽고 → 비교하고 → 빼서 → 저장" 하면 그 사이에 다른 요청이 끼어들 틈이 생긴다.
    // 문장 하나면 DB 가 그 행을 잠근 채 처리하므로 틈이 없다.
    //
    // 돌려주는 값은 바뀐 행 수다.  1 = 차감됨,  0 = 조건 불만족(잔액 부족).  예외가 아니라 숫자로 온다.
    //
    // 버린 대안:
    //   - 비관적 락(select ... for update): 읽는 순간부터 잠근다. 읽고 계산해야 하는 복잡한 규칙에 맞다.
    //     여기 규칙은 ">= 이면 뺀다" 하나뿐이라 과하다.
    //   - 낙관적 락(@Version): 충돌이 나면 실패시키고 재시도한다. 재시도 코드가 늘어난다.
    //   두 대안은 다음 경쟁 지점(한정 수량·중복 구매)에서 다룬다.
    @Modifying
    @Query("update Wallet w set w.balance = w.balance - :amount where w.id = :walletId and w.balance >= :amount")
    int tryPay(@Param("walletId") Long walletId, @Param("amount") long amount);
}
