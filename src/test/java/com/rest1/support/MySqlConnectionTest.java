package com.rest1.support;

import com.rest1.domain.member.member.repository.MemberRepository;
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

// 기존 컨트롤러 테스트와 같은 설정(같은 컨텍스트)이라 컨테이너를 공유한다.
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@Import(TestMySqlConfig.class)
public class MySqlConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("테스트가 H2 가 아니라 진짜 MySQL 컨테이너에 붙어 있다")
    void t1() {
        String version = jdbcTemplate.queryForObject("select version()", String.class);
        String isolation = jdbcTemplate.queryForObject("select @@transaction_isolation", String.class);
        long memberCount = memberRepository.count();   // BaseInitData 가 넣은 회원 5명

        System.out.println("EVIDENCE mysql=" + version + " isolation=" + isolation + " members=" + memberCount);

        assertThat(version).startsWith("8.4.6");
        assertThat(isolation).isEqualTo("REPEATABLE-READ");
        assertThat(memberCount).isEqualTo(5);
    }
}
