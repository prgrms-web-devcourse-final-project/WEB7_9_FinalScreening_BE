package com.back.matchduo.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserBlockListResponse {
    private Long userId;
    private String nickname;
    private String profileImage;
    private LocalDateTime blockedAt;
}
