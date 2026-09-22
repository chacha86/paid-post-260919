package com.rest1.domain.order.order.entity;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.global.exception.ServiceException;
import com.rest1.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주문: 회원이 유료 글 "하나"를 사는 기록.
//
// 가격 스냅샷: 만들 때의 글 가격을 price 에 복사해 둔다.
//   글을 참조해서 그때그때 읽으면, 작성자가 가격을 올린 순간 과거 주문 금액까지 같이 바뀐다.
//   (1차 프로젝트 8팀 중 6팀이 여기서 걸렸다.)
//
// 상태 전이 규칙은 이 클래스 안에만 둔다. 컨트롤러·서비스가 status 를 직접 대입하면
// 규칙이 여기저기로 흩어져 "어디서 바뀌었는지" 를 추적할 수 없게 된다.
//
// 테이블 이름 "order" 는 SQL 예약어라 "orders" 로 둔다.
@NoArgsConstructor
@Getter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private long price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order(Member buyer, Post post) {
        this.buyer = buyer;
        this.post = post;
        this.price = post.getPrice();
        this.status = OrderStatus.PENDING;   // 생성 = 대기. 포인트는 아직 빠지지 않는다
    }

    public void checkBuyer(Member actor) {
        if (!this.buyer.getId().equals(actor.getId())) {
            throw new ServiceException("403-3", "내 주문이 아닙니다.");
        }
    }

    public void checkPending() {
        if (this.status != OrderStatus.PENDING) {
            throw new ServiceException("409-2", "대기 상태의 주문만 확정할 수 있습니다.");
        }
    }

    // 전이: PENDING → CONFIRMED. 포인트 차감(WalletService.pay)과 같은 트랜잭션에서 불려야 한다
    public void confirm() {
        checkPending();
        this.status = OrderStatus.CONFIRMED;
    }
}
