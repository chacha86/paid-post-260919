package com.rest1.domain.order.order.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.global.exception.ServiceException;
import com.rest1.support.RaceFixture;
import com.rest1.support.RaceMySqlConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// 지갑 잔액 경쟁: 한 지갑(1000)으로 서로 다른 700 글 두 개의 대기 주문을 "동시에" 확정한다.
// 기대: 하나만 확정(OK), 하나는 잔액 부족(402-1)으로 대기 유지, 잔액 300, 구매 원장 1줄, 원장 합계 == 잔액.
//
// 이 클래스는 지금까지의 테스트와 세 가지가 다르다.
//  1) @Transactional 이 없다. 두 스레드가 각자 트랜잭션을 열고 실제로 커밋해야 경쟁이 생긴다.
//  2) 커밋하므로 다른 테스트와 DB 를 나눠 쓰면 안 된다 → properties 로 컨텍스트를 분리한다(전용 MySQL 컨테이너가 뜬다).
//     그리고 매 테스트 전에 user1 의 주문·원장을 지우고 잔액을 1000 으로 되돌린다(RaceFixture.resetUser1).
//  3) 컨트롤러를 거치지 않으므로 컨트롤러의 @Transactional 역할을 TransactionTemplate 이 대신한다.
@SpringBootTest(properties = "test.context=race")
@ActiveProfiles("test")
@Import({RaceMySqlConfig.class, RaceFixture.class})
public class OrderConfirmRaceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RaceFixture fixture;

    @BeforeEach
    void reset() {
        fixture.resetUser1();
    }

    // 확정 한 번 = 트랜잭션 한 개. 컨트롤러 confirmItem 이 하는 일(조회 → 서비스)을 그대로 옮겼다.
    // 결과는 문자열로 돌려준다: "OK" / "402-1" / "DEADLOCK" / 그 밖의 예외 이름. 예외를 삼키지 않는다.
    private String confirmInNewTransaction(Long orderId, Member actor) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                Order order = orderService.findById(orderId).get();
                orderService.confirm(order, actor);
            });
            return "OK";
        } catch (ServiceException e) {
            return e.getResultCode();
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage());
            return msg.contains("Deadlock") ? "DEADLOCK" : e.getClass().getSimpleName();
        }
    }

    // 두 확정을 동시에 실행하고 각 스레드의 결과를 모은다. 끝날 때까지 기다린다.
    private List<String> confirmConcurrently(List<Long> orderIds) throws Exception {
        Member actor = fixture.user1Actor();
        ExecutorService pool = Executors.newFixedThreadPool(orderIds.size());
        CountDownLatch start = new CountDownLatch(1);   // 출발선. 둘 다 준비되면 같이 출발
        List<Future<String>> futures = new ArrayList<>();
        for (Long orderId : orderIds) {
            futures.add(pool.submit(() -> {
                start.await();
                return confirmInNewTransaction(orderId, actor);
            }));
        }
        start.countDown();
        List<String> results = new ArrayList<>();
        for (Future<String> f : futures) {
            results.add(f.get(20, TimeUnit.SECONDS));   // 20초 넘으면 테스트 실패 (멈춘 스레드를 놓치지 않는다)
        }
        pool.shutdown();
        return results;
    }

    @Test
    @DisplayName("동시 확정 20회 - 매번 OK 하나 + 402-1 하나여야 하고, 잔액·원장·주문 상태가 맞아야 한다")
    void t1() throws Exception {
        int runs = 20;
        int clean = 0, lostUpdate = 0, deadlock = 0, other = 0;
        for (int i = 0; i < runs; i++) {
            fixture.resetUser1();
            List<Long> orderIds = fixture.createTwoPendingOrders();

            List<String> results = confirmConcurrently(orderIds);
            RaceFixture.Snapshot s = fixture.snapshot();   // JDBC 로 DB 를 직접 읽는다

            if (results.contains("DEADLOCK")) deadlock++;
            else if (!s.consistent()) lostUpdate++;
            else if (results.containsAll(List.of("OK", "402-1"))) clean++;
            else other++;
        }
        System.out.println("EVIDENCE natural runs=" + runs + " clean=" + clean + " lostUpdate=" + lostUpdate
                + " deadlock=" + deadlock + " other=" + other);

        assertThat(clean).as("%d회 중 깨끗하게 끝난 회수 (OK + 402-1, 상태 일치)".formatted(runs)).isEqualTo(runs);
    }
}
