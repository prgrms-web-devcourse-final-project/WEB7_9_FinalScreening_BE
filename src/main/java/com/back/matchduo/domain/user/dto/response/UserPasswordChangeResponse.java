package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserPasswordChangeResponse(
        @Schema(description = "비밀번호")
        String password
) {
    public static UserPasswordChangeResponse from(User user){
        return new UserPasswordChangeResponse(
                user.getPassword()
        );
    }
}
