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
        String authorName,
        long price
) {
    public static final String PAID_MASK = "유료 글입니다. 구매 후 열람할 수 있습니다.";

    public PostDto(Post post) {
        this(post, post.getContent());
    }

    // 본문을 다른 내용으로 바꿔 내보낼 때 (유료 글 가림: new PostDto(post, PostDto.PAID_MASK))
    public PostDto(Post post, String content) {
        this(
                post.getId(),
                post.getCreateDate(),
                post.getModifyDate(),
                post.getTitle(),
                content,
                post.getAuthor().getId(),
                post.getAuthor().getName(),
                post.getPrice()
        );
    }
}
