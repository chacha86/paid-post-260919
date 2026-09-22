package com.rest1.domain.post.post.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.entity.OrderStatus;
import com.rest1.domain.order.order.service.OrderService;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.domain.post.post.service.PostService;
import com.rest1.support.TestMySqlConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 유료 글 본문 열람 규칙: 구매(확정)한 회원과 작성자만 본문을 본다. 처음 열면 주문이 VIEWED 가 된다.
@SpringBootTest
@ActiveProfiles("test")
@Import(TestMySqlConfig.class)
@AutoConfigureMockMvc
@Transactional
public class PaidPostViewTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private OrderService orderService;

    // 4번 글 = "유료 글 1"(700), 작성자 user2

    @Test
    @DisplayName("로그인 없이 유료 글을 열면 본문이 가려진다")
    void t1() throws Exception {
        mvc
                .perform(
                        get("/api/v1/posts/4")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(700))
                .andExpect(jsonPath("$.content").value("유료 글입니다. 구매 후 열람할 수 있습니다."));
    }

    @Test
    @DisplayName("로그인했지만 안 샀으면 본문이 가려진다 (대기 주문만 있어도 마찬가지)")
    void t2() throws Exception {
        Member user1 = memberRepository.findByUsername("user1").get();
        Post post = postService.findById(4L).get();
        orderService.create(user1, post);   // PENDING 만 있는 상태

        mvc
                .perform(
                        get("/api/v1/posts/4")
                                .header("Authorization", "Bearer %s".formatted(user1.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("유료 글입니다. 구매 후 열람할 수 있습니다."));
    }

    @Test
    @DisplayName("확정한 회원이 열면 본문이 보이고 주문은 VIEWED 가 된다. 두 번째 열람도 본문이 보인다")
    void t3() throws Exception {
        Member user1 = memberRepository.findByUsername("user1").get();
        Post post = postService.findById(4L).get();
        Order order = orderService.create(user1, post);
        orderService.confirm(order, user1);   // 컨트롤러 없이 서비스로 확정 (테스트 트랜잭션 안)
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        mvc
                .perform(
                        get("/api/v1/posts/4")
                                .header("Authorization", "Bearer %s".formatted(user1.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("유료 본문 1 - 구매한 회원만 볼 수 있다"));
        assertThat(orderService.findById(order.getId()).get().getStatus()).isEqualTo(OrderStatus.VIEWED);

        mvc
                .perform(
                        get("/api/v1/posts/4")
                                .header("Authorization", "Bearer %s".formatted(user1.getApiKey()))
                )
                .andDo(print())
                .andExpect(jsonPath("$.content").value("유료 본문 1 - 구매한 회원만 볼 수 있다"));
        assertThat(orderService.findById(order.getId()).get().getStatus()).isEqualTo(OrderStatus.VIEWED);
    }

    @Test
    @DisplayName("작성자는 사지 않아도 자기 유료 글 본문을 본다")
    void t4() throws Exception {
        Member user2 = memberRepository.findByUsername("user2").get();

        mvc
                .perform(
                        get("/api/v1/posts/4")
                                .header("Authorization", "Bearer %s".formatted(user2.getApiKey()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("유료 본문 1 - 구매한 회원만 볼 수 있다"));
    }
}
