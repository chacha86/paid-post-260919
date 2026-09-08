package com.rest1.global.security;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.global.exception.ServiceException;
import com.rest1.global.rq.Rq;
import com.rest1.global.rsData.RsData;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final MemberService memberService;
    private final Rq rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("CustomAuthenticationFilter called");

        try {
            authenticate(request, response, filterChain);
        } catch (ServiceException e) {

            RsData rsData = e.getRsData();
            response.setContentType("application/json");
            response.setStatus(rsData.getStatusCode());
            response.getWriter().write("""
                    {
                        "resultCode": "%s",
                        "msg": "%s"
                    }
                    """.formatted(rsData.getResultCode(), rsData.getMsg()));

        } catch (Exception e) {
            throw e;
        }

    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if(List.of("/api/v1/members/join", "/api/v1/members/login").contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey;
        String accessToken;

        String headerAuthorization = rq.getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer "))
                throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");

            String[] headerAuthorizationBits = headerAuthorization.split(" ", 3);

            apiKey = headerAuthorizationBits[1];
            accessToken = headerAuthorizationBits.length == 3 ? headerAuthorizationBits[2] : "";
        } else {
            apiKey = rq.getCookieValue("apiKey", "");
            accessToken = rq.getCookieValue("accessToken", "");
        }

        boolean isAdiKeyExists = !apiKey.isBlank();
        boolean isAccessTokenExists = !accessToken.isBlank();

        if(!isAdiKeyExists && !isAccessTokenExists) {
            filterChain.doFilter(request, response);
            return;
        }

        Member member = null;
        boolean isAccessTokenValid = false;
        if (isAccessTokenExists) {
            Map<String, Object> payload = memberService.payloadOrNull(accessToken);

            if (payload != null) {
                long id = (long) payload.get("id");
                String username = (String) payload.get("username");
                String nickname = (String) payload.get("nickname");

                member = new Member(id, username, nickname);
                isAccessTokenValid = true;
            }
        }

        if (member == null) {
            member = memberService
                    .findByApiKey(apiKey)
                    .orElseThrow(() -> new ServiceException("401-3", "API 키가 유효하지 않습니다."));
        }

        if (isAccessTokenExists && !isAccessTokenValid) {
            String newAccessToken = memberService.genAccessToken(member);
            rq.setCookie("accessToken", newAccessToken);
            rq.setHeader("accessToken", newAccessToken);
        }

        UserDetails user = new SecurityUser(
                member.getId(),
                member.getUsername(),
                "",
                member.getNickname(),
                member.getAuthorities()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );


        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);


        filterChain.doFilter(request, response);
    }
}
