package com.rest1.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 인증 실패(401) 응답을 우리 RsData 양식으로 내는 곳.
// 두 경로가 여기로 모인다.
//   1) 인증 정보가 아예 없는데 보호된 URL에 온 경우 (ExceptionTranslationFilter)
//   2) Bearer 토큰이 있는데 검증에 실패한 경우 (BearerTokenAuthenticationFilter)
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String resultCode = "401-1";
        String msg = "로그인 후 이용해주세요.";

        if (authException instanceof OAuth2AuthenticationException oauth2Exception) {
            OAuth2Error error = oauth2Exception.getError();

            if (error.getErrorCode().matches("\\d{3}-\\d+")) {
                // 우리가 직접 던진 에러(예: 401-3 API 키 오류)는 그 코드와 메시지를 그대로 쓴다.
                resultCode = error.getErrorCode();
                msg = error.getDescription();
            } else {
                // 시큐리티가 던진 에러(invalid_token 등)는 한 가지로 묶는다.
                resultCode = "401-4";
                msg = "유효하지 않은 액세스 토큰입니다.";
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("""
                {
                    "resultCode": "%s",
                    "msg": "%s"
                }
                """.formatted(resultCode, msg));
    }
}
