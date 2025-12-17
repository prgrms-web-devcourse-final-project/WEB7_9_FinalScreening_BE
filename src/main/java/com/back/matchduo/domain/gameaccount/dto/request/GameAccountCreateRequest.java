package com.back.matchduo.domain.gameaccount.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameAccountCreateRequest {
    @NotBlank(message = "게임 타입은 필수입니다.")
    private String gameType;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String gameNickname;

    private String gameTag;
}

