package com.rest1.domain.order.order.repository;

import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerIdOrderByIdAsc(Long buyerId);

    long countByBuyerIdAndStatus(Long buyerId, OrderStatus status);

    // 이 회원이 이 글을 산 주문(확정 또는 열람됨). 여러 개면 가장 최근 것
    Optional<Order> findFirstByBuyerIdAndPostIdAndStatusInOrderByIdDesc(Long buyerId, Long postId, Collection<OrderStatus> statuses);
}
