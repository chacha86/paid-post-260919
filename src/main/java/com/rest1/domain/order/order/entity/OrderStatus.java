package com.rest1.domain.order.order.entity;

// 주문 상태. 이 자료의 첫 실습에서 쓰는 셋만 둔다.
//   PENDING(대기) --확정--> CONFIRMED(확정) --본문 열람--> VIEWED(열람됨)
// 취소·만료·환불은 주제 4(상태머신·배치)에서 추가한다.
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    VIEWED
}
