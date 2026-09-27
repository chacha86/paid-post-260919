package com.rest1.domain.wallet.wallet.entity;

public enum LedgerType {
    CHARGE,     // 충전: 잔액이 늘어난다 (+)
    PURCHASE,   // 구매: 잔액이 줄어든다 (-)
    REFUND      // 환불: 잔액이 다시 늘어난다 (+)  ※ 이 자료 범위에서는 아직 안 쓴다
}
