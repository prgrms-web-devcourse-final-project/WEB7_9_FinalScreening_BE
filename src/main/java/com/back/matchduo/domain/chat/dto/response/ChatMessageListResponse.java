package com.back.matchduo.domain.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 메시지 목록 응답 (커서 페이징)
 * - header: 채팅방 헤더 정보 (상대 정보, 모집글 요약)
 * - messages: 메시지 목록
 */
public record ChatMessageListResponse(
        Long chatRoomId,
        ChatHeaderResponse header, // 헤더 영역(상대 정보/모집글 요약/모집 상태)
        Long myLastReadMessageId,  // 내 기준 마지막 읽은 메시지 ID
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
            String communityNickname,
            String profileImage,
            String gameNicknameTag
    ) {}

    /** 모집글 요약 */
    public record PostSummaryResponse(
            Long postId,
            String gameMode,
            String memo
    ) {}

    /** 채팅 메시지 아이템 */
    public record ChatMessageItemResponse(
            Long chatMessageId,
            Long senderId,
            String content,
            String messageType,
            LocalDateTime createdAt
    ) {}
}