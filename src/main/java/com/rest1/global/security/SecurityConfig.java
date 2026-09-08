package com.rest1.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationFilter customAuthenticationFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/*/posts", "/api/*/posts/{id:\\d+}",
                                "/api/*/posts/{postId:\\d+}/comments", "/api/*/posts/{postId:\\d+}/comments/{commentId:\\d+}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/login", "/api/v1/members/join").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/members/logout").permitAll()
                        .requestMatchers("/api/*/adm/**").hasRole("ADMIN")
                        .requestMatchers("/api/*/**").authenticated()
                        .anyRequest().authenticated())
                .csrf((csrf) -> csrf.disable())
                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .authenticationEntryPoint((request, response, authenticationException) -> {
                                    response.setContentType("application/json");
                                    response.setStatus(401);
                                    response.getWriter().write(
                                            """
                                                        {
                                                            "resultCode": "401-1",
                                                            "msg": "로그인 후 이용해주세요."
                                                        }
                                                    """);
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                            response.setContentType("application/json");
                                            response.setStatus(403);
                                            response.getWriter().write(
                                                    """
                                                                {
                                                                    "resultCode": "403-1",
                                                                    "msg": "권한이 없습니다."
                                                                }
                                                            """);
                                        }
                                ));
        ;

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("https://cdpn.io", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}