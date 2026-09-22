package com.rest1.domain.order.order.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.repository.OrderRepository;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order create(Member buyer, Post post) {
        if (!post.isPaid()) {
            throw new ServiceException("400-3", "무료 글은 주문할 수 없습니다.");
        }

        return orderRepository.save(new Order(buyer, post));
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByBuyerId(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByIdAsc(buyerId);
    }
}
