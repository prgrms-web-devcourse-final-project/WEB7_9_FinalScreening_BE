package com.back.matchduo.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.jwt")
public record JwtProperties(
        String secretPattern,
        long accessExpireSeconds,
        long refreshExpireSeconds
) {
}
