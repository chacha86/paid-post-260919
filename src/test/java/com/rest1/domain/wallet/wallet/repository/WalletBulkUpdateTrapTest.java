package com.rest1.domain.wallet.wallet.repository;

import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.domain.wallet.wallet.entity.Wallet;
import com.rest1.support.TestMySqlConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// 12강: 벌크 UPDATE(@Modifying JPQL)는 DB 만 바꾼다. 영속성 컨텍스트에 이미 올라온 엔티티는 그 사실을 모른다.
// 이 테스트는 그 "함정" 을 있는 그대로 보여준다. (트랜잭션은 테스트 끝에 롤백되므로 데이터는 남지 않는다)
@SpringBootTest
@ActiveProfiles("test")
@Import(TestMySqlConfig.class)
@AutoConfigureMockMvc
@Transactional
public class WalletBulkUpdateTrapTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("벌크 UPDATE 뒤에도 메모리의 엔티티는 옛 잔액을 들고 있다. 다시 조회해도 같은 객체다. refresh 해야 맞춰진다")
    void t1() {
        Long memberId = memberRepository.findByUsername("user1").get().getId();

        Wallet wallet = walletRepository.findByMemberId(memberId).get();   // 영속 상태, balance = 1000
        assertThat(wallet.getBalance()).isEqualTo(1000);

        int updated = walletRepository.tryPay(wallet.getId(), 700);        // DB: 1000 → 300
        long inDb = jdbcTemplate.queryForObject("select balance from wallet where id = ?", Long.class, wallet.getId());

        Wallet again = walletRepository.findByMemberId(memberId).get();     // SQL 은 나가지만, 돌려주는 건 이미 있던 그 객체

        System.out.println("EVIDENCE trap updatedRows=" + updated + " dbBalance=" + inDb
                + " entityBalance=" + wallet.getBalance() + " sameInstance=" + (again == wallet) + " againBalance=" + again.getBalance());

        assertThat(updated).isEqualTo(1);
        assertThat(inDb).isEqualTo(300);                 // DB 는 바뀌었고
        assertThat(wallet.getBalance()).isEqualTo(1000); // 메모리는 그대로다 (함정)
        assertThat(again).isSameAs(wallet);              // 같은 트랜잭션 안에서는 같은 id → 같은 객체
        assertThat(again.getBalance()).isEqualTo(1000);  // 그래서 다시 조회해도 옛 값

        entityManager.refresh(wallet);                    // DB 값으로 덮어쓴다
        assertThat(wallet.getBalance()).isEqualTo(300);
    }
}
