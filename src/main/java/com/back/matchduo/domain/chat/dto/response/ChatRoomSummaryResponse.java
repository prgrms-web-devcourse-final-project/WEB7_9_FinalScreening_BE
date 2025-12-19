package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.chat.entity.MessageType;
import com.back.matchduo.domain.user.entity.User;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 요약 DTO
 * - 채팅방 목록 1건에 필요한 데이터
 * - unreadCount: 안 읽은 메시지 수
 * - lastActivityAt: 목록 정렬/커서 페이징 기준
 */
public record ChatRoomSummaryResponse(
        Long chatRoomId,
        Long postId,
        OtherUserResponse otherUser,
        LastMessageResponse lastMessage,
        int unreadCount,
        String queueType,
        String memo,
        boolean isActive,
        LocalDateTime lastActivityAt
) {

    public record OtherUserResponse(
            Long userId,
            String nickname,
            String profileImage
    ) {
        public static OtherUserResponse of(User user) {
            return new OtherUserResponse(
                    user.getId(),
                    user.getNickname(),
                    user.getProfileImage()
            );
        }
    }

    public record LastMessageResponse(
            Long chatMessageId,
            Long senderId,
            String content,
            MessageType messageType,
            LocalDateTime createdAt
    ) {
        public static LastMessageResponse of(ChatMessage message) {
            if (message == null) return null;
            return new LastMessageResponse(
                    message.getId(),
                    message.getSender().getId(),
                    message.getContent(),
                    message.getMessageType(),
                    message.getCreatedAt()
            );
        }
    }

    public static ChatRoomSummaryResponse of(
            ChatRoom room,
            Long userId,
            ChatMessage lastMessage,
            int unreadCount
    ) {
        boolean isSender = room.isSender(userId);
        User otherUser = isSender ? room.getReceiver() : room.getSender();

        return new ChatRoomSummaryResponse(
                room.getId(),
                room.getPost().getId(),
                OtherUserResponse.of(otherUser),
                LastMessageResponse.of(lastMessage),
                unreadCount,
                room.getPost().getQueueType().name(),
                room.getPost().getMemo(),
                room.isActive(),
                lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt()
        );
    }

    public boolean hasUnread() {
        return unreadCount > 0;
    }
}

