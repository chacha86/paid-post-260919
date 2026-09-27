package com.rest1.domain.post.comment.dto;

import com.rest1.domain.post.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String content,
        Long authorId,
        String authorName,
        Long postId
) {
    public CommentDto(Comment comment) {
        this(
                comment.getId(),
                comment.getCreateDate(),
                comment.getModifyDate(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getPost().getId()
        );
    }
}
