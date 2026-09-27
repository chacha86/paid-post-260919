package com.rest1.domain.wallet.wallet.entity;

import com.rest1.domain.order.order.entity.Order;
import com.rest1.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 원장: 지갑의 입출금 기록 한 줄.
// 지우지도 고치지도 않는다(append only). 고치기 시작하면 "원장 합계 == 잔액" 으로 검산할 수 없다.
// balanceAfter 를 같이 남기는 이유: 나중에 값이 어긋났을 때 몇 번째 줄부터 틀어졌는지 한눈에 찾기 위해서.
@NoArgsConstructor
@Getter
@Entity
public class Ledger extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    private LedgerType type;

    private long amount;        // 부호 있음. 충전 +1000, 구매 -700
    private long balanceAfter;  // 이 줄이 반영된 직후의 잔액 (나중에 "어디서 어긋났나" 추적용)

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;        // 구매·환불 줄은 어느 주문 때문인지 남긴다. 충전은 null

    public Ledger(Wallet wallet, LedgerType type, long amount, long balanceAfter) {
        this(wallet, type, amount, balanceAfter, null);
    }

    public Ledger(Wallet wallet, LedgerType type, long amount, long balanceAfter, Order order) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.order = order;
    }
}
