package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.MessageType;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 요약 DTO
 * - 채팅방 목록 1건에 필요한 데이터
 * - unreadCount: 안 읽은 메시지 수
 * - lastActivityAt: 목록 정렬/커서 페이징 기준
 *
 */
public record ChatRoomSummaryResponse(
        Long chatRoomId,
        Long postId,
        OtherUserResponse otherUser,
        LastMessageResponse lastMessage,
        int unreadCount,
        String gameMode,
        String memo,
        boolean isActive,
        LocalDateTime lastActivityAt
) {

    public record OtherUserResponse(
            Long userId,
            String nickname,
            String profileImage
    ) {}

    public record LastMessageResponse(
            Long chatMessageId,
            Long senderId,
            String content,
            MessageType messageType,
            LocalDateTime createdAt
    ) {}

    public boolean hasUnread() {
        return unreadCount > 0;
    }

}

