package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.ChatRoom;

import java.time.LocalDateTime;

/**
 * 채팅방 나가기
 * - isClosed: 채팅 가능 여부 (한 쪽이라도 나갔으면 true)
 * - isActive: soft delete 여부 (둘 다 나가면 false)
 */
public record ChatRoomLeaveResponse(
        Long chatRoomId,
        boolean isClosed,
        boolean isActive,
        LocalDateTime disabledAt
) {
    public static ChatRoomLeaveResponse of(ChatRoom chatRoom) {
        return new ChatRoomLeaveResponse(
              chatRoom.getId(),
              chatRoom.isClosed(),
              Boolean.TRUE.equals(chatRoom.getIsActive()),
              chatRoom.getDeletedAt()
        );
    }
}
