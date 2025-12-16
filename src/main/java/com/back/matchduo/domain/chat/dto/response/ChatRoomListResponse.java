package com.back.matchduo.domain.chat.dto.response;

import java.util.List;

/**
 * 채팅방 목록 응답 (커서 페이징)
 */
public record ChatRoomListResponse(
        List<ChatRoomSummaryResponse> chatRooms,
        String nextCursor,
        boolean hasNext
) {}
