package com.back.matchduo.domain.chat.dto.internal;

import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatRoom;

import java.util.List;

/**
 * 메시지 목록과 채팅방 정보를 함께 담는 내부 DTO
 * - Service 레이어에서 중복 조회를 방지하기 위해 사용
 * - API 요청/응답이 아닌 Service 내부 전달용
 */
public record ChatMessagesWithRoom(
        List<ChatMessage> messages,
        ChatRoom room
) {}
