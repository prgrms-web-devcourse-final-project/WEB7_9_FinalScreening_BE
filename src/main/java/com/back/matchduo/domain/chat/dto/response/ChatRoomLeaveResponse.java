package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.ChatRoom;

/**
 * 채팅방 나가기
 * - isClosed: 채팅 불가 여부 (한 쪽이라도 나갔으면 true)
 * - isFullyClosed: 양쪽 모두 나갔는지 여부
 */
public record ChatRoomLeaveResponse(
        Long chatRoomId,
        boolean isClosed,
        boolean isFullyClosed
) {
    public static ChatRoomLeaveResponse of(ChatRoom chatRoom) {
        return new ChatRoomLeaveResponse(
                chatRoom.getId(),
                chatRoom.isClosed(),
                chatRoom.isFullyClosed()
        );
    }
}
