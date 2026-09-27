package com.rest1.domain.order.order.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.entity.OrderStatus;
import com.rest1.domain.order.order.service.OrderService;
import com.rest1.domain.wallet.wallet.entity.Ledger;
import com.rest1.domain.wallet.wallet.entity.LedgerType;
import com.rest1.domain.wallet.wallet.entity.Wallet;
import com.rest1.domain.wallet.wallet.service.WalletService;
import com.rest1.support.TestMySqlConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMySqlConfig.class)
@AutoConfigureMockMvc
@Transactional
public class ApiV1OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WalletService walletService;

    // 샘플 데이터: 4번 글 "유료 글 1"(700), 5번 글 "유료 글 2"(700), 작성자 user2. user1 지갑 1000.

    @Test
    @DisplayName("주문 생성 - 유료 글을 주문하면 대기 상태, 포인트는 아직 그대로")
    void t1() throws Exception {
        Member actor = memberRepository.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/4/orders")
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1OrderController.class))
                .andExpect(handler().methodName("createItem"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                // 주문 번호는 고정하지 않는다(AUTO_INCREMENT, 3강). 형식만 본다
                .andExpect(jsonPath("$.msg").value(matchesPattern("\\d+번 주문이 생성되었습니다.")))
                .andExpect(jsonPath("$.data.orderDto.postId").value(4))
                .andExpect(jsonPath("$.data.orderDto.postTitle").value("유료 글 1"))
                .andExpect(jsonPath("$.data.orderDto.price").value(700))
                .andExpect(jsonPath("$.data.orderDto.status").value("PENDING"));

        // 응답만 믿지 않고 DB 를 본다. 이 회원의 가장 최근 주문
        Order order = orderService.findByBuyerId(actor.getId()).getLast();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPrice()).isEqualTo(700);

        Wallet wallet = walletService.findByMemberId(actor.getId()).get();
        assertThat(wallet.getBalance()).isEqualTo(1000);          // 생성은 선점일 뿐, 차감은 확정 때
        assertThat(walletService.findLedgers(wallet)).hasSize(1); // 원장도 그대로
    }

    @Test
    @DisplayName("주문 생성 - 무료 글은 400-3")
    void t2() throws Exception {
        Member actor = memberRepository.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/1/orders")
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-3"))
                .andExpect(jsonPath("$.msg").value("무료 글은 주문할 수 없습니다."));
    }

    @Test
    @DisplayName("주문 단건 조회 - 남의 주문은 403-3, 내 주문은 200")
    void t3() throws Exception {
        Member buyer = memberRepository.findByUsername("user1").get();
        Member other = memberRepository.findByUsername("user3").get();

        mvc
                .perform(
                        post("/api/v1/posts/4/orders")
                                .header("Authorization", "Bearer %s".formatted(buyer.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        Order order = orderService.findByBuyerId(buyer.getId()).getLast();

        mvc
                .perform(
                        get("/api/v1/orders/%d".formatted(order.getId()))
                                .header("Authorization", "Bearer %s".formatted(other.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-3"));

        mvc
                .perform(
                        get("/api/v1/orders/%d".formatted(order.getId()))
                                .header("Authorization", "Bearer %s".formatted(buyer.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("주문 확정 - 잔액 1000 에서 700 글을 확정하면 잔액 300, 구매 원장 1줄, 주문 CONFIRMED")
    void t4() throws Exception {
        Member actor = memberRepository.findByUsername("user1").get();

        mvc
                .perform(
                        post("/api/v1/posts/4/orders")
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        Order order = orderService.findByBuyerId(actor.getId()).getLast();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/orders/%d/confirm".formatted(order.getId()))
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1OrderController.class))
                .andExpect(handler().methodName("confirmItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 주문이 확정되었습니다.".formatted(order.getId())))
                .andExpect(jsonPath("$.data.orderDto.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.balance").value(300));

        // 응답만 믿지 않고 DB 상태를 본다: 주문·지갑·원장 셋이 같이 바뀌었나
        assertThat(orderService.findById(order.getId()).get().getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        Wallet wallet = walletService.findByMemberId(actor.getId()).get();
        assertThat(wallet.getBalance()).isEqualTo(300);

        List<Ledger> ledgers = walletService.findLedgers(wallet);
        assertThat(ledgers).hasSize(2);
        assertThat(ledgers.get(1).getType()).isEqualTo(LedgerType.PURCHASE);
        assertThat(ledgers.get(1).getAmount()).isEqualTo(-700);
        assertThat(ledgers.get(1).getBalanceAfter()).isEqualTo(300);
        assertThat(ledgers.get(1).getOrder().getId()).isEqualTo(order.getId());
        assertThat(walletService.sumLedger(wallet)).isEqualTo(wallet.getBalance());   // 불변식
    }

    @Test
    @DisplayName("주문 확정 - 순차로 두 번째 700 글을 확정하면 잔액 부족 402-1, 주문은 대기로 남고 원장은 안 늘어난다")
    void t5() throws Exception {
        Member actor = memberRepository.findByUsername("user1").get();

        mvc
                .perform(
                        post("/api/v1/posts/4/orders")
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isCreated());
        Order first = orderService.findByBuyerId(actor.getId()).getLast();

        mvc
                .perform(
                        post("/api/v1/posts/5/orders")
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isCreated());
        Order second = orderService.findByBuyerId(actor.getId()).getLast();

        mvc
                .perform(
                        post("/api/v1/orders/%d/confirm".formatted(first.getId()))
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isOk());

        mvc
                .perform(
                        post("/api/v1/orders/%d/confirm".formatted(second.getId()))
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().is(402))
                .andExpect(jsonPath("$.resultCode").value("402-1"))
                .andExpect(jsonPath("$.msg").value("잔액이 부족합니다."));

        assertThat(orderService.findById(second.getId()).get().getStatus()).isEqualTo(OrderStatus.PENDING);

        Wallet wallet = walletService.findByMemberId(actor.getId()).get();
        assertThat(wallet.getBalance()).isEqualTo(300);
        assertThat(walletService.findLedgers(wallet)).hasSize(2);   // 충전 1 + 구매 1. 실패한 확정은 흔적이 없다
        assertThat(walletService.sumLedger(wallet)).isEqualTo(300);
    }

    @Test
    @DisplayName("주문 확정 - 남의 주문은 403-3, 이미 확정된 주문은 409-2")
    void t6() throws Exception {
        Member buyer = memberRepository.findByUsername("user1").get();
        Member other = memberRepository.findByUsername("user3").get();

        mvc
                .perform(
                        post("/api/v1/posts/4/orders")
                                .header("Authorization", "Bearer %s".formatted(buyer.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isCreated());
        Order order = orderService.findByBuyerId(buyer.getId()).getLast();

        mvc
                .perform(
                        post("/api/v1/orders/%d/confirm".formatted(order.getId()))
                                .header("Authorization", "Bearer %s".formatted(other.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-3"));

        mvc
                .perform(
                        post("/api/v1/orders/%d/confirm".formatted(order.getId()))
                                .header("Authorization", "Bearer %s".formatted(buyer.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isOk());

        mvc
                .perform(
                        post("/api/v1/orders/%d/confirm".formatted(order.getId()))
                                .header("Authorization", "Bearer %s".formatted(buyer.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-2"));

        // 두 번째 확정이 거절됐으니 잔액은 한 번만 빠져 있어야 한다
        assertThat(walletService.findByMemberId(buyer.getId()).get().getBalance()).isEqualTo(300);
    }
}
