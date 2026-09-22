package com.rest1.support;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.domain.order.order.entity.Order;
import com.rest1.domain.order.order.service.OrderService;
import com.rest1.domain.post.post.service.PostService;
import com.rest1.domain.wallet.wallet.entity.Wallet;
import com.rest1.domain.wallet.wallet.repository.WalletRepository;
import com.rest1.domain.wallet.wallet.service.WalletService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 동시성 실습용 데이터 준비/초기화/관찰 도구. 테스트에서만 쓴다.
// 동시성 테스트는 트랜잭션을 실제로 커밋하므로 @Transactional 롤백에 기댈 수 없다.
// 대신 매 테스트 전에 user1 의 주문·원장을 지우고 잔액을 1,000(충전 원장 1줄)으로 되돌린다.
public class RaceFixture {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final OrderService orderService;
    private final PostService postService;
    private final JdbcTemplate jdbcTemplate;

    // 테스트 소스에는 롬복이 없어 생성자를 직접 쓴다
    public RaceFixture(MemberRepository memberRepository, WalletRepository walletRepository, WalletService walletService,
                       OrderService orderService, PostService postService, JdbcTemplate jdbcTemplate) {
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
        this.walletService = walletService;
        this.orderService = orderService;
        this.postService = postService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public static final long PAID_POST_1 = 4L;
    public static final long PAID_POST_2 = 5L;

    // Rq.getActor() 가 만드는 것과 같은 "id 만 있는" 회원 객체. 스레드마다 새 트랜잭션에서 쓴다
    public Member user1Actor() {
        Member m = memberRepository.findByUsername("user1").get();
        return new Member(m.getId(), m.getUsername(), m.getNickname());
    }

    // 초기화 범위: user1 의 주문 전부, user1 지갑의 원장 전부, user1 잔액. 다른 회원·글은 건드리지 않는다
    // ⚠️ id 를 얻으려고 지갑 엔티티를 먼저 로딩하면 안 된다. 그러면 그 엔티티(옛 잔액)가 영속성 컨텍스트에 남아,
    //    아래 JDBC UPDATE 로 0 을 써도 findById 가 캐시된 옛 잔액을 돌려주고 charge 가 그 위에 1000 을 더한다.
    //    (제작 중 실제로 겪은 함정: 매 초기화마다 잔액이 1000 씩 누적됐다 → 12강)
    @Transactional
    public void resetUser1() {
        Long memberId = memberRepository.findByUsername("user1").get().getId();
        Long walletId = jdbcTemplate.queryForObject("select id from wallet where member_id = ?", Long.class, memberId);

        jdbcTemplate.update("delete from ledger where wallet_id = ?", walletId);
        jdbcTemplate.update("delete from orders where buyer_id = ?", memberId);
        jdbcTemplate.update("update wallet set balance = 0 where id = ?", walletId);

        Wallet wallet = walletRepository.findById(walletId).get();   // 이 트랜잭션에서 지갑을 처음 읽는다 → DB 의 0
        walletService.charge(wallet, 1000);                          // 잔액 1000 + 충전 원장 1줄
    }

    // user1 이 4번·5번 유료 글(각 700)에 대기 주문을 하나씩 만든다 → [orderId1, orderId2]
    @Transactional
    public List<Long> createTwoPendingOrders() {
        Member user1 = memberRepository.findByUsername("user1").get();
        Order o1 = orderService.create(user1, postService.findById(PAID_POST_1).get());
        Order o2 = orderService.create(user1, postService.findById(PAID_POST_2).get());
        return List.of(o1.getId(), o2.getId());
    }

    // 결과 관찰은 JPA 캐시를 거치지 않고 DB 에서 바로 읽는다
    public Snapshot snapshot() {
        Long memberId = memberRepository.findByUsername("user1").get().getId();
        Long walletId = jdbcTemplate.queryForObject("select id from wallet where member_id = ?", Long.class, memberId);
        long balance = jdbcTemplate.queryForObject("select balance from wallet where id = ?", Long.class, walletId);
        long ledgerSum = jdbcTemplate.queryForObject("select coalesce(sum(amount), 0) from ledger where wallet_id = ?", Long.class, walletId);
        long purchaseLedgers = jdbcTemplate.queryForObject("select count(*) from ledger where wallet_id = ? and type = 'PURCHASE'", Long.class, walletId);
        long confirmed = jdbcTemplate.queryForObject("select count(*) from orders where buyer_id = ? and status = 'CONFIRMED'", Long.class, memberId);
        long pending = jdbcTemplate.queryForObject("select count(*) from orders where buyer_id = ? and status = 'PENDING'", Long.class, memberId);
        return new Snapshot(balance, ledgerSum, purchaseLedgers, confirmed, pending);
    }

    public record Snapshot(long balance, long ledgerSum, long purchaseLedgers, long confirmed, long pending) {
        // 이 실습의 불변식: 확정 1·대기 1, 잔액 300, 구매 원장 1줄, 원장 합계 == 잔액
        public boolean consistent() {
            return confirmed == 1 && pending == 1 && balance == 300 && purchaseLedgers == 1 && ledgerSum == balance;
        }
    }
}
