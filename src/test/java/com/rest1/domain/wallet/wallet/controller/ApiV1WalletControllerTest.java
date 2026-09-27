package com.rest1.domain.wallet.wallet.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMySqlConfig.class)
@AutoConfigureMockMvc
@Transactional
public class ApiV1WalletControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WalletService walletService;

    @Test
    @DisplayName("내 지갑 조회 - user1 은 초기 잔액 1000, 원장은 충전 1줄")
    void t1() throws Exception {
        Member actor = memberRepository.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/wallets/me")
                                .header("Authorization", "Bearer %s".formatted(actor.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1WalletController.class))
                .andExpect(handler().methodName("getMine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.ledgers.length()").value(1))
                .andExpect(jsonPath("$.ledgers[0].type").value("CHARGE"))
                .andExpect(jsonPath("$.ledgers[0].amount").value(1000))
                .andExpect(jsonPath("$.ledgers[0].balanceAfter").value(1000));
    }

    @Test
    @DisplayName("내 지갑 조회 - 로그인 없이는 401")
    void t2() throws Exception {
        mvc
                .perform(get("/api/v1/wallets/me"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("불변식 - 모든 지갑에서 원장 합계 == 잔액")
    void t3() {
        for (Wallet wallet : walletService.findAll()) {
            long sum = walletService.sumLedger(wallet);
            System.out.println("EVIDENCE wallet=" + wallet.getId() + " balance=" + wallet.getBalance() + " ledgerSum=" + sum);
            assertThat(sum).isEqualTo(wallet.getBalance());
        }
        assertThat(walletService.findAll()).hasSize(5);   // 회원 5명 = 지갑 5개 (system, admin 은 잔액 0)
    }
}
