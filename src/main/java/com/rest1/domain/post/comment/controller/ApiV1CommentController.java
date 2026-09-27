package com.rest1.domain.post.comment.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.domain.post.comment.dto.CommentDto;
import com.rest1.domain.post.comment.entity.Comment;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.domain.post.post.service.PostService;
import com.rest1.global.rq.Rq;
import com.rest1.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "ApiV1CommentController", description = "댓글 API")
public class ApiV1CommentController {

    private final PostService postService;
    private final MemberService memberService;
    private final Rq rq;

    @GetMapping(value = "/{postId}/comments")
    @Operation(summary = "다건 조회")
    public List<CommentDto> getItems(
            @PathVariable Long postId
    ) {
        Post post = postService.findById(postId).get();
        return post.getComments().reversed().stream()
                .map(CommentDto::new)
                .toList();
    }

    @GetMapping(value = "/{postId}/comments/{commentId}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회")
    public CommentDto getItem(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Post post = postService.findById(postId).get();
        Comment comment = post.findCommentById(commentId).get();
        return new CommentDto(comment);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @Transactional
    @Operation(summary = "댓글 삭제")
    public RsData<Void> deleteItem(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {

        Member actor = rq.getActor();
        Post post = postService.findById(postId).get();
        Comment comment = post.findCommentById(commentId).get();
        comment.checkActorDelete(actor);
        postService.deleteComment(post, commentId);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 삭제되었습니다.".formatted(commentId)
        );
    }


    record CommentWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String content
    ) {
    }

    record CommentWriteResBody(
            CommentDto commentDto
    ) {}

    @PostMapping("/{postId}/comments")
    @Transactional
    @Operation(summary = "댓글 작성")
    public RsData<CommentWriteResBody> createItem(
            @PathVariable Long postId,
            @RequestBody @Valid CommentWriteReqBody reqBody
    ) {

        Member actor = rq.getActor();
        Post post = postService.findById(postId).get();
        Comment comment = postService.writeComment(actor, post, reqBody.content);

        postService.flush();

        return new RsData<>(
                "201-1",
                "%d번 댓글이 생성되었습니다.".formatted(comment.getId()),
                new CommentWriteResBody(
                        new  CommentDto(comment)
                )
        );
    }


    record CommentModifyReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String content
    ) {
    }

    @PutMapping("/{postId}/comments/{commentId}")
    @Transactional
    @Operation(summary = "댓글 수정")
    public RsData<Void> modifyItem(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentWriteReqBody reqBody
    ) {

        Member actor = rq.getActor();
        Post post = postService.findById(postId).get();
        Comment comment = post.findCommentById(commentId).get();
        comment.checkActorModify(actor);
        postService.modifyComment(post, commentId, reqBody.content);

        return new RsData<>(
                "200-1",
                ("%d번 댓글이 수정되었습니다.").formatted(commentId)
        );
    }

}
