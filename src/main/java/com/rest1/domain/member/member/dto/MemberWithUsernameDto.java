package com.rest1.domain.member.member.dto;

import com.rest1.domain.member.member.entity.Member;

import java.time.LocalDateTime;

public record MemberWithUsernameDto(
        Long id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String username,
        String nickname

) {
    public MemberWithUsernameDto(Member member) {
        this(
                member.getId(),
                member.getCreateDate(),
                member.getModifyDate(),
                member.getUsername(),
                member.getNickname()
        );
    }
}