package com.back.matchduo.global.security;

import com.back.matchduo.global.config.CookieProperties;
import com.back.matchduo.global.config.JwtProperties;
import com.back.matchduo.global.security.filter.JwtAuthenticationFilter;
import com.back.matchduo.global.security.handler.JsonAccessDeniedHandler;
import com.back.matchduo.global.security.handler.JsonAuthEntryPoint;
import com.back.matchduo.global.security.jwt.JwtProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CookieProperties.class})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception {

        http
                // CSRF 사용 안 함 (JWT + Cookie 기반)
                .csrf(csrf -> csrf.disable())

                // 기본 로그인 / Basic Auth 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 사용 안 함
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인증 / 인가 예외 처리 (JSON 통일)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new JsonAuthEntryPoint())
                        .accessDeniedHandler(new JsonAccessDeniedHandler())
                )

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // Auth API는 모두 허용
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()

                        // 모집글 조회(GET)만 공개
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/posts",
                                "/api/v1/posts/**"
                        ).permitAll()

                        // TODO: 공개 API는 여기 추가
                        // 예: 모집글 목록/상세, 게임모드 목록 등
                        // .requestMatchers("/api/v1/posts/**").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
