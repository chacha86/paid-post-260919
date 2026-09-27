package com.rest1.domain.wallet.wallet.entity;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 지갑: 회원 한 명이 하나 가진다.
//
// 왜 잔액 컬럼과 원장(Ledger)을 둘 다 두나:
//   - 원장만 두면 잔액을 볼 때마다 전부 더해야 한다.
//   - 잔액만 두면 값이 틀렸을 때 "어디서" 틀렸는지 알 수 없다.
//   → 둘 다 두고 서로를 감시하게 한다.
//
// 이 자료 전체를 꿰는 불변식:  원장 amount 의 합 == balance
// 이 등식이 깨졌다면 돈이 새거나 두 번 빠진 것이다. 테스트가 매번 이 등식을 확인한다.
@NoArgsConstructor
@Getter
@Entity
public class Wallet extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Member member;

    private long balance;

    public Wallet(Member member) {
        this.member = member;
        this.balance = 0;
    }

    public void deposit(long amount) {
        this.balance += amount;
    }

    public boolean canPay(long price) {
        return this.balance >= price;
    }
}
