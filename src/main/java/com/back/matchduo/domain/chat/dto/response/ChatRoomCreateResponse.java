package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.ChatRoom;

/**
 * 채팅방 생성 응답
 *  - chatRoomId: 생성된 채팅방 ID (멱등 처리 시 기존 방 ID일 수 있음)
 *  - otherUserId: 현재 로그인 유저 기준 상대방 유저 ID
 */
public record ChatRoomCreateResponse(
        Long chatRoomId,
        Long postId,
        Long otherUserId

) {
    public static ChatRoomCreateResponse of(ChatRoom chatRoom, Long currentUserId) {
        boolean isSender = chatRoom.getSender().getId().equals(currentUserId);
        Long otherUserId = isSender
                ? chatRoom.getReceiver().getId()
                : chatRoom.getSender().getId();

        return new ChatRoomCreateResponse(
                chatRoom.getId(),
                chatRoom.getPost().getId(),
                otherUserId
        );
    }
}