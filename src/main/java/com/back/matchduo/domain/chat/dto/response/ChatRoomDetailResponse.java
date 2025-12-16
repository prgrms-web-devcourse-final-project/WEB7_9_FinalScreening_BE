package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.ChatMemberRole;

import java.time.LocalDateTime;

/**
 * 채팅방 상세 정보 응답
 * - 메시지 목록은 별도 API에서 조회
 */
public record ChatRoomDetailResponse(
        Long chatRoomId,
        Long postId,
        boolean isActive,
        LocalDateTime createdAt,
        ChatMemberRole myRole,
        boolean otherLeft,
        boolean myLeft
) {}
