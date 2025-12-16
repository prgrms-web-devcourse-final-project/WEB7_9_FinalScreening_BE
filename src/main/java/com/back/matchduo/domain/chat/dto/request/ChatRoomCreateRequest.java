package com.back.matchduo.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 채팅방 생성 요청
 * - postId(모집글 ID)만 받아와서 "현재 로그인 유저"와 "모집글 작성자" 조합으로 1:1 채팅방 생성
 */
public record ChatRoomCreateRequest(
        @NotNull @Positive Long postId
) {
}
