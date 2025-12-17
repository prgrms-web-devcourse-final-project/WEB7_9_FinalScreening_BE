package com.back.matchduo.domain.auth.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
