package com.rest1.domain.post.comment.controller;


import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.domain.post.comment.entity.Comment;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.domain.post.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("댓글 다건 조회 - 1번 글에 대한 댓글")
    void t1() throws Exception {

        long targetPostId = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments".formatted(targetPostId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].id", containsInRelativeOrder(3, 1)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].createDate").exists())
                .andExpect(jsonPath("$[0].modifyDate").exists())
                .andExpect(jsonPath("$[0].content").value("댓글 1-3"))
                .andExpect(jsonPath("$[0].authorId").value(3))
                .andExpect(jsonPath("$[0].authorName").value("유저1"))
                .andExpect(jsonPath("$[0].postId").value(1));

    }

    @Test
    @DisplayName("댓글 단건 조회 - 1번 글의 1번 댓글")
    void t2() throws Exception {

        long targetPostId = 1;
        long targetCommentId = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.createDate").exists())
                .andExpect(jsonPath("$.modifyDate").exists())
                .andExpect(jsonPath("$.content").value("댓글 1-1"))
                .andExpect(jsonPath("$.authorId").value(3))
                .andExpect(jsonPath("$.authorName").value("유저1"))
                .andExpect(jsonPath("$.postId").value(1));
    }

    @Test
    @DisplayName("댓글 생성 - 1번 글에 생성")
    void t3() throws Exception {

        long targetPostId = 1;
        String content = "새로운 댓글";
        Member author = memberRepository.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(targetPostId))
                                .header("Authorization", "Bearer %s".formatted(author.getApiKey()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("createItem"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("6번 댓글이 생성되었습니다."))
                .andExpect(jsonPath("$.data.commentDto.id").value(6))
                .andExpect(jsonPath("$.data.commentDto.createDate").exists())
                .andExpect(jsonPath("$.data.commentDto.modifyDate").exists())
                .andExpect(jsonPath("$.data.commentDto.content").value(content))
                .andExpect(jsonPath("$.data.commentDto.authorId").value(author.getId()))
                .andExpect(jsonPath("$.data.commentDto.authorName").value(author.getName()));
    }

    @Test
    @DisplayName("댓글 수정 - 1번 글의 1번 댓글 수정")
    void t4() throws Exception {
        long targetPostId = 1;
        long targetCommentId = 1;
        String content = "댓글 내용 수정";

        Member author = memberRepository.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                                .header("Authorization", "Bearer %s".formatted(author.getApiKey()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("modifyItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 수정되었습니다.".formatted(targetCommentId)));

        Post post = postRepository.findById(targetPostId).get();
        Comment comment = post.findCommentById(targetCommentId).get();

        assertThat(comment.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("댓글 수정 - 다른 작성자의 댓글 수정")
    void t5() throws Exception {
        long targetPostId = 1;
        long targetCommentId = 1;
        String content = "댓글 내용 수정";

        Member author = memberRepository.findByUsername("user2").get();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                                .header("Authorization", "Bearer %s".formatted(author.getApiKey()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("modifyItem"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("댓글 수정 권한이 없습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 - 1번 글의 1번 댓글 삭제")
    void t6() throws Exception {
        long targetPostId = 1;
        long targetCommentId = 1;

        Member author = memberRepository.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                                .header("Authorization", "Bearer %s".formatted(author.getApiKey()))
                )
                .andDo(print());

        // 필수 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("deleteItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 삭제되었습니다.".formatted(targetCommentId)));

        // 선택적 검증
        Post post = postRepository.findById(targetPostId).orElse(null);
        Comment comment = post.findCommentById(targetCommentId).orElse(null);
        assertThat(comment).isNull();
    }

    @Test
    @DisplayName("댓글 삭제 - 다른 작성자의 댓글 삭제")
    void t7() throws Exception {
        long targetPostId = 1;
        long targetCommentId = 1;

        Member author = memberRepository.findByUsername("user2").get();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                                .header("Authorization", "Bearer %s".formatted(author.getApiKey()))
                )
                .andDo(print());

        // 필수 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("deleteItem"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-2"))
                .andExpect(jsonPath("$.msg").value("댓글 삭제 권한이 없습니다."));
    }
}
