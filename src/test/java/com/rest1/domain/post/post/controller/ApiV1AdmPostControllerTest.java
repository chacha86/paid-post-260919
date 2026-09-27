package com.rest1.domain.post.post.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.domain.post.post.repository.PostRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1AdmPostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("글 전체 개수 조회, count")
    void t1() throws Exception {

        Member actor = memberRepository.findByUsername("admin").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/posts/count")
                                .cookie(new Cookie("apiKey", actor.getApiKey()))
                )
                .andDo(print());


        long count = postRepository.count();

        resultActions
                .andExpect(handler().handlerType(ApiV1AdmPostController.class))
                .andExpect(handler().methodName("count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(count));
    }

    @Test
    @DisplayName("글 전체 개수 조회, count, 권한이 없는 경우")
    void t2() throws Exception {

        Member actor = memberRepository.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/posts/count")
                                .cookie(new Cookie("apiKey", actor.getApiKey()))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));

    }
}
