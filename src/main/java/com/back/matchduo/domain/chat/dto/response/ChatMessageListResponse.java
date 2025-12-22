package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 메시지 목록 응답 (커서 페이징)
 */
public record ChatMessageListResponse(
        Long chatRoomId,
        ChatHeaderResponse header, // 헤더 영역(상대 정보/모집글 요약/모집 상태)
        List<ChatMessageItemResponse> messages, // 채팅 메시지 목록
        Long nextCursor,
        boolean hasNext
) {

    /** 헤더 영역 */
    public record ChatHeaderResponse(
            OtherUserResponse otherUser,
            PostSummaryResponse postSummary, // 채팅이 시작된 모집글 요약
            String postStatus // 모집글 상태
    ) {
    }

    /** 채팅 상대(1:1) 사용자 정보*/
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

    /** 모집글 요약 */
    public record PostSummaryResponse(
            Long postId,
            String gameModeName,
            String queueType,
            String memo
    ) {}

    /** 채팅 메시지 아이템 */
    public record ChatMessageItemResponse(
            Long chatMessageId,
            Long senderId,
            String content,
            String messageType,
            LocalDateTime createdAt
    ) {
        public static ChatMessageItemResponse of(ChatMessage message) {
            return new ChatMessageItemResponse(
                    message.getId(),
                    message.getSender().getId(),
                    message.getContent(),
                    message.getMessageType().name(),
                    message.getCreatedAt()
            );
        }
    }

    /** 팩토리 메서드 */
    public static ChatMessageListResponse of(
            ChatRoom room,
            Long userId,
            List<ChatMessage> messages,
            Long nextCursor,
            boolean hasNext
    ) {
        boolean isSender = room.isSender(userId);
        User otherUser = isSender ? room.getReceiver() : room.getSender();

        ChatHeaderResponse header = new ChatHeaderResponse(
                OtherUserResponse.of(otherUser),
                new PostSummaryResponse(
                        room.getPost().getId(),
                        room.getPost().getGameMode().getName(),
                        room.getPost().getQueueType().name(),
                        room.getPost().getMemo()
                ),
                room.getPost().getStatus().name()
        );

        List<ChatMessageItemResponse> messageItems = messages.stream()
                .map(ChatMessageItemResponse::of)
                .toList();

        return new ChatMessageListResponse(
                room.getId(),
                header,
                messageItems,
                nextCursor,
                hasNext
        );
    }
}