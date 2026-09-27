package com.rest1.domain.member.member.controller;

import com.rest1.domain.member.member.dto.MemberDto;
import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.AuthTokenService;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.global.exception.ServiceException;
import com.rest1.global.rq.Rq;
import com.rest1.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class ApiV1MemberController {

    private final MemberService memberService;
    private final Rq rq;
    private final AuthTokenService authTokenService;

    record JoinReqBody(
            @NotBlank
            @Size(min = 2, max = 30)
            String username,

            @NotBlank
            @Size(min = 2, max = 30)
            String password,

            @NotBlank
            @Size(min = 2, max = 30)
            String nickname
    ) {
    }

    record JoinResBody(
            MemberDto memberDto
    ) {
    }

    @PostMapping("/join")
    public RsData<MemberDto> join(
            @RequestBody @Valid JoinReqBody reqBody
    ) {
        Member member = memberService.join(reqBody.username, reqBody.password, reqBody.nickname);

        return new RsData(
                "201-1",
                "회원가입이 완료되었습니다. %s님 환영합니다.".formatted(reqBody.nickname),
                new JoinResBody(
                        new MemberDto(member)
                )
        );
    }


    record LoginReqBody(
            @NotBlank
            @Size(min = 2, max = 30)
            String username,

            @NotBlank
            @Size(min = 2, max = 30)
            String password
    ) {
    }

    record LoginResBody(
            MemberDto memberDto,
            String apiKey,
            String accessToken
    ) {
    }

    @PostMapping("/login")
    public RsData<MemberDto> login(
            @RequestBody @Valid LoginReqBody reqBody
    ) {

        Member member = memberService.findByUsername(reqBody.username).orElseThrow(
                () -> new ServiceException("401-1", "존재하지 않는 아이디입니다.")
        );

        memberService.checkPassword(reqBody.password, member.getPassword());
        String accessToken = memberService.genAccessToken(member);

        rq.setCookie("apiKey", member.getApiKey());
        rq.setCookie("accessToken", accessToken);

        return new RsData(
                "200-1",
                "%s님 환영합니다.".formatted(reqBody.username),
                new LoginResBody(
                        new MemberDto(member),
                        member.getApiKey(),
                        accessToken
                )
        );
    }

    @DeleteMapping("/logout")
    public RsData<Void> logout() {

        rq.deleteCookie("apiKey");

        return new RsData<>(
                "200-1",
                "로그아웃 되었습니다."
        );
    }


    record MeResBody(
            MemberDto memberDto
    ) {
    }

    @GetMapping("/me")
    public RsData<MemberDto> me() {

        Member author = memberService.findById(rq.getActor().getId()).get();

        return new RsData(
                "200-1",
                "OK",
                new MeResBody(
                        new MemberDto(author)
                )
        );
    }

}
