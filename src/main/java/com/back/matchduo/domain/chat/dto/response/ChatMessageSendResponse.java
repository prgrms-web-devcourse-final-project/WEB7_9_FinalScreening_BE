package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.MessageType;

import java.time.LocalDateTime;

/**
 * 메시지 전송 응답
 */
public record ChatMessageSendResponse(
        Long chatMessageId,
        Long chatRoomId,
        MessageType messageType,
        Long senderId,
        String content,
        LocalDateTime createdAt
) {
    public static ChatMessageSendResponse of(ChatMessage message) {
        return new ChatMessageSendResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getMessageType(),
                message.getSender().getId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
