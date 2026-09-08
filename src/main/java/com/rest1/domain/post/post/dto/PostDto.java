package com.rest1.domain.post.post.dto;

import com.rest1.domain.post.post.entity.Post;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String title,
        String content,
        Long authorId,
        String authorName
) {
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getCreateDate(),
                post.getModifyDate(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getId(),
                post.getAuthor().getName()
        );
    }
}
