package com.back.matchduo.global.security.jwt;

import com.back.matchduo.global.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {

    private final JwtProperties props;
    private final SecretKey key; // 핵심 변경 포인트

    public JwtProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(
                props.secretPattern().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, props.accessExpireSeconds());
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, props.refreshExpireSeconds());
    }

    private String createToken(Long userId, long expireSeconds) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireSeconds);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검증 (예외 발생 시 invalid)
    public void validate(String token) {
        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }
}
