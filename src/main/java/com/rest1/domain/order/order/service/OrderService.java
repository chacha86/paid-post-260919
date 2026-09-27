package com.rest1.domain.order.order.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.entity.OrderStatus;
import com.rest1.domain.order.order.repository.OrderRepository;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.domain.wallet.wallet.entity.Wallet;
import com.rest1.domain.wallet.wallet.service.WalletService;
import com.rest1.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WalletService walletService;

    public Order create(Member buyer, Post post) {
        if (!post.isPaid()) {
            throw new ServiceException("400-3", "무료 글은 주문할 수 없습니다.");
        }

        return orderRepository.save(new Order(buyer, post));
    }

    // 확정: "내 대기 주문인가" 확인 → 지갑에서 가격만큼 빼고 원장 기록 → 주문 CONFIRMED.
    //
    // 세 변경(잔액·원장·주문 상태)은 반드시 한 트랜잭션이어야 한다. 하나만 성공하면
    // 돈은 빠졌는데 주문은 대기이거나, 주문은 확정인데 원장이 없는 상태가 남는다.
    // 트랜잭션 경계는 여기가 아니라 호출한 컨트롤러의 @Transactional 이다(기존 글·댓글 코드와 같은 방식).
    // 그래서 이 메서드는 엔티티를 받고, 조회는 컨트롤러가 한다.
    public void confirm(Order order, Member actor) {
        order.checkBuyer(actor);
        order.checkPending();

        Wallet wallet = walletService.findByMemberId(actor.getId()).get();
        walletService.pay(wallet, order);   // 잔액 부족이면 여기서 402-1

        order.confirm();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    // 구매한 주문(CONFIRMED/VIEWED)이 있으면 돌려준다
    public Optional<Order> findPurchased(Long buyerId, Long postId) {
        return orderRepository.findFirstByBuyerIdAndPostIdAndStatusInOrderByIdDesc(
                buyerId, postId, List.of(OrderStatus.CONFIRMED, OrderStatus.VIEWED));
    }

    public List<Order> findByBuyerId(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByIdAsc(buyerId);
    }
}
