package com.rest1.domain.order.order.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.order.order.dto.OrderDto;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.service.OrderService;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.domain.post.post.service.PostService;
import com.rest1.domain.wallet.wallet.service.WalletService;
import com.rest1.global.rq.Rq;
import com.rest1.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "ApiV1OrderController", description = "주문 API")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1OrderController {

    private final OrderService orderService;
    private final PostService postService;
    private final WalletService walletService;
    private final Rq rq;

    record OrderCreateResBody(
            OrderDto orderDto
    ) {
    }

    @PostMapping("/posts/{postId}/orders")
    @Transactional
    @Operation(summary = "주문 생성 (대기) - 유료 글 하나를 산다. 포인트는 아직 빠지지 않는다")
    public RsData<OrderCreateResBody> createItem(
            @PathVariable Long postId
    ) {
        Member actor = rq.getActor();
        Post post = postService.findById(postId).get();

        Order order = orderService.create(actor, post);

        return new RsData<>(
                "201-1",
                "%d번 주문이 생성되었습니다.".formatted(order.getId()),
                new OrderCreateResBody(new OrderDto(order))
        );
    }

    record OrderConfirmResBody(
            OrderDto orderDto,
            long balance      // 확정 직후 잔액
    ) {
    }

    @PostMapping("/orders/{id}/confirm")
    @Transactional
    @Operation(summary = "주문 확정 - 지갑에서 포인트를 빼고 글을 내 것으로 만든다")
    public RsData<OrderConfirmResBody> confirmItem(
            @PathVariable Long id
    ) {
        Member actor = rq.getActor();

        Order order = orderService.findById(id).get();
        orderService.confirm(order, actor);
        long balance = walletService.findByMemberId(actor.getId()).get().getBalance();

        return new RsData<>(
                "200-1",
                "%d번 주문이 확정되었습니다.".formatted(order.getId()),
                new OrderConfirmResBody(new OrderDto(order), balance)
        );
    }

    @GetMapping("/orders/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "주문 단건 조회 (내 주문만)")
    public OrderDto getItem(
            @PathVariable Long id
    ) {
        Member actor = rq.getActor();
        Order order = orderService.findById(id).get();
        order.checkBuyer(actor);

        return new OrderDto(order);
    }

    @GetMapping("/orders")
    @Transactional(readOnly = true)
    @Operation(summary = "내 주문 목록")
    public List<OrderDto> getItems() {
        Member actor = rq.getActor();

        return orderService.findByBuyerId(actor.getId()).stream()
                .map(OrderDto::new)
                .toList();
    }
}
