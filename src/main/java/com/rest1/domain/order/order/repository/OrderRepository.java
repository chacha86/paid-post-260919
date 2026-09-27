package com.rest1.domain.order.order.repository;

import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerIdOrderByIdAsc(Long buyerId);

    long countByBuyerIdAndStatus(Long buyerId, OrderStatus status);
}
