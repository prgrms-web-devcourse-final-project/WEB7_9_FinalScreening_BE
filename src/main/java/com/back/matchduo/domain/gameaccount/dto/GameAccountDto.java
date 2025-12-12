package com.back.matchduo.domain.gameaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GameAccountDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "게임 타입은 필수입니다.")
        private String gameType;

        @NotBlank(message = "닉네임은 필수입니다.")
        private String gameNickname;

        private String gameTag;

        // 임시: user_id를 직접 받아서 처리 (나중에 인증 정보로 대체)
        @NotNull(message = "유저 ID는 필수입니다.")
        private Long userId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long gameAccountId;
        private String gameNickname;
        private String gameTag;
        private String gameType;
        private String puuid;
        private Long userId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeleteResponse {
        private String message;
        private Long gameAccountId;
    }
}

