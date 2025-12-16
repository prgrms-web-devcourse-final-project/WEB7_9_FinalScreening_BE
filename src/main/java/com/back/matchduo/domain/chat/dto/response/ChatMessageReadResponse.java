package com.back.matchduo.domain.chat.dto.response;

import java.time.LocalDateTime;

/**
 * 채팅방에서 특정 메시지까지 읽음 처리했을 때 반환하는 응답 DTO
 * - chatRoomId: 어떤 채팅방에 대한 읽음 처리인지
 * - lastReadMessage: 어디까지 읽었는지
 */
public record ChatMessageReadResponse(
        Long chatRoomId,
        Long lastReadMessageId,
        LocalDateTime readAt
) {
    public static ChatMessageReadResponse of(Long chatRoomId, Long lastReadMessageId, LocalDateTime readAt) {
        return new ChatMessageReadResponse(chatRoomId, lastReadMessageId, readAt);
    }
}
