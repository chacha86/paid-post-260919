package com.rest1.domain.member.member.dto;

import com.rest1.domain.member.member.entity.Member;

import java.time.LocalDateTime;

public record MemberDto(
        Long id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String name
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getCreateDate(),
                member.getModifyDate(),
                member.getName()
        );
    }
}
