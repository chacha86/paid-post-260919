package com.rest1.global.security;

import com.rest1.global.rq.Rq;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

// 리소스 서버 부품 ① "꺼내기" 에 우리 규칙을 꽂는 곳.
// 기본 구현(DefaultBearerTokenResolver)은 Authorization 헤더만 본다. 우리는 쿠키도 본다.
// 여기서 돌려준 문자열은 JwtDecoder 로 넘어간다. null 을 돌려주면 "인증 정보 없음"으로 취급되어 익명으로 진행한다.
@Component
@RequiredArgsConstructor
public class CustomBearerTokenResolver implements BearerTokenResolver {

    private final Rq rq;

    @Override
    public String resolve(HttpServletRequest request) {
        // 1순위 : Authorization: Bearer <accessToken>
        String headerAuthorization = rq.getHeader("Authorization", "");

        if (headerAuthorization.startsWith("Bearer ")) {
            return headerAuthorization.substring("Bearer ".length()).trim();
        }

        // 2순위 : 쿠키 accessToken
        String accessToken = rq.getCookieValue("accessToken", "");

        if (!accessToken.isBlank()) {
            return accessToken;
        }

        return null;
    }
}
