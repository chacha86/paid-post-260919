package com.rest1.domain.member.member.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1AdmMemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 다건 조회")
    void t1() throws Exception {

        Member actor = memberRepository.findByUsername("admin").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/members")
                                .cookie(new Cookie("apiKey", actor.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1AdmMemberController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[*].id", containsInRelativeOrder(1, 5)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].createDate").exists())
                .andExpect(jsonPath("$[0].modifyDate").exists())
                .andExpect(jsonPath("$[0].nickname").value("시스템"))
                .andExpect(jsonPath("$[0].username").value("system"));
    }

    @Test
    @DisplayName("회원 다건 조회, 권한이 없는 경우")
    void t2() throws Exception {

        Member actor = memberRepository.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/members")
                                .cookie(new Cookie("apiKey", actor.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }
}
