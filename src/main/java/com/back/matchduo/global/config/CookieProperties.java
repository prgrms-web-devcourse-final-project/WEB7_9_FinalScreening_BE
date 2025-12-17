package com.back.matchduo.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.cookie")
public record CookieProperties(
        boolean secure,
        String sameSite,
        String path
) {
}
