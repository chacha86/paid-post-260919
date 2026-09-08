package com.rest1.global.security;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.global.rq.Rq;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

// 리소스 서버 부품 ① "꺼내기" 에 우리 규칙을 꽂는 곳.
// 기본 구현(DefaultBearerTokenResolver)은 Authorization 헤더만 본다. 우리는 쿠키도 보고, apiKey 전략도 여기서 산다.
// 여기서 돌려준 문자열은 JwtDecoder 로 넘어간다. null 을 돌려주면 "인증 정보 없음"으로 취급되어 익명으로 진행한다.
@Component
@RequiredArgsConstructor
public class CustomBearerTokenResolver implements BearerTokenResolver {

    private final Rq rq;
    private final MemberService memberService;

    @Override
    public String resolve(HttpServletRequest request) {
        String accessToken;
        String apiKey;

        String headerAuthorization = rq.getHeader("Authorization", "");

        if (headerAuthorization.startsWith("Bearer ")) {
            // 1순위 : Authorization: Bearer <accessToken>
            accessToken = headerAuthorization.substring("Bearer ".length()).trim();
            apiKey = "";
        } else {
            // 2순위 : 쿠키
            accessToken = rq.getCookieValue("accessToken", "");
            apiKey = rq.getCookieValue("apiKey", "");
        }

        // accessToken 이 있고 아직 유효하면 그대로 넘긴다. (DB 조회 없음 — 가장 흔한 경로)
        if (!accessToken.isBlank() && memberService.payloadOrNull(accessToken) != null) {
            return accessToken;
        }

        // accessToken 이 없거나 만료됐다. apiKey 가 없으면 여기서 할 수 있는 게 없다.
        //   - 둘 다 없음  → null (익명)
        //   - 토큰만 무효 → 그대로 넘겨서 JwtDecoder 가 401-4 를 내게 한다
        if (apiKey.isBlank()) {
            return accessToken.isBlank() ? null : accessToken;
        }

        // apiKey 전략 : 서버에 영구 보관된 apiKey 로 회원을 찾아 accessToken 을 새로 발급한다.
        Member member = memberService
                .findByApiKey(apiKey)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("401-3", "API 키가 유효하지 않습니다.", null)
                ));

        String newAccessToken = memberService.genAccessToken(member);

        rq.setCookie("accessToken", newAccessToken);
        rq.setHeader("accessToken", newAccessToken);

        return newAccessToken;
    }
}
