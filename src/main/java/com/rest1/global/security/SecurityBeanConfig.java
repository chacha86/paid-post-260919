package com.rest1.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityBeanConfig {

    @Value("${custom.jwt.secretPattern}")
    private String secretPattern;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 리소스 서버가 요구하는 단 하나의 부품 : "토큰 서명을 무엇으로 검증할 것인가"
    // 우리는 우리가 발급한 JWT를 우리 secret으로 검증한다.
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(secretPattern.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512) // Ut.jwt(jjwt)는 키가 64바이트 이상이면 HS512로 서명한다. 검증도 같은 알고리즘이어야 한다.
                .build();
    }

    // JWT 클레임 → 시큐리티 권한(GrantedAuthority) 변환 규칙
    // 기본값은 "scope" 클레임을 SCOPE_ 접두어로 바꾼다. 우리는 "roles" 클레임을 ROLE_ 접두어로 바꾼다.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
