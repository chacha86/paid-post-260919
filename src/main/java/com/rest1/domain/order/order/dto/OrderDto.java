package com.rest1.domain.order.order.dto;

import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderDto(
        Long id,
        LocalDateTime createDate,
        Long postId,
        String postTitle,
        long price,
        OrderStatus status
) {
    public OrderDto(Order order) {
        this(
                order.getId(),
                order.getCreateDate(),
                order.getPost().getId(),
                order.getPost().getTitle(),
                order.getPrice(),
                order.getStatus()
        );
    }
}
