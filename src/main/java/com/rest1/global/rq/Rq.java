package com.rest1.global.rq;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {

    private final MemberService memberService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    // 로그인 안 했으면 null. 누구나 볼 수 있는 API(글 단건 조회)에서 "로그인했다면 누구인지"만 알고 싶을 때
    public Member getActorOrNull() {
        try {
            return getActor();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public Member getActor() {
        // 리소스 서버가 검증을 끝내면 SecurityContext의 principal은 Jwt 객체다.
        // 우리는 그 안의 클레임(id, username, nickname)으로 Member를 만든다. (DB 조회 없음, 68강과 같은 사상)
        return Optional
                .ofNullable(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof Jwt)
                .map(principal -> (Jwt) principal)
                .map(jwt -> new Member(
                        ((Number) jwt.getClaim("id")).longValue(),
                        jwt.getClaimAsString("username"),
                        jwt.getClaimAsString("nickname")
                ))
                .orElseThrow(() -> new RuntimeException("로그인 후 이용해주세요."));
    }

    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    public String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(request.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(request.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(cookies)
                                        .filter(cookie -> cookie.getName().equals(name))
                                        .map(Cookie::getValue)
                                        .filter(value -> !value.isBlank())
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "Strict");

        // 값이 없다면 해당 쿠키변수를 삭제하라는 뜻
        if (value.isBlank()) {
            cookie.setMaxAge(0);
        }

        response.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}
